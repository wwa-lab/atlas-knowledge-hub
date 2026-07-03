package com.atlas.metadata.integration;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** Shared PostgreSQL database for metadata API integration tests. */
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
abstract class AbstractPostgresIT {

  private static final JdbcConfig POSTGRES = PostgresRuntime.start();

  @DynamicPropertySource
  static void datasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::url);
    registry.add("spring.datasource.username", POSTGRES::username);
    registry.add("spring.datasource.password", POSTGRES::dbSecret);
  }

  private record JdbcConfig(String url, String username, String dbSecret) {}

  private static final class PostgresRuntime {

    private static final String DB_NAME = "atlas_metadata_test";
    private static final String USERNAME = "atlas_test_user";

    private PostgresRuntime() {}

    static JdbcConfig start() {
      String externalUrl = System.getenv("ATLAS_TEST_DB_URL");
      if (externalUrl != null && !externalUrl.isBlank()) {
        return new JdbcConfig(
            externalUrl,
            System.getenv().getOrDefault("ATLAS_TEST_DB_USERNAME", USERNAME),
            System.getenv().getOrDefault("ATLAS_TEST_DB_PASSWORD", ""));
      }

      String dbSecret = UUID.randomUUID().toString();
      String name = "atlas-metadata-api-it-" + UUID.randomUUID();
      run(
          "docker",
          "run",
          "-d",
          "--rm",
          "--name",
          name,
          "-e",
          "POSTGRES_DB=" + DB_NAME,
          "-e",
          "POSTGRES_USER=" + USERNAME,
          "-e",
          "POSTGRES_PASSWORD=" + dbSecret,
          "-p",
          "127.0.0.1::5432",
          "postgres:16-alpine");
      Runtime.getRuntime().addShutdownHook(new Thread(() -> runQuietly("docker", "rm", "-f", name)));

      String port = mappedPort(name);
      waitUntilReady(name);
      String jdbcScheme = "jdbc";
      return new JdbcConfig(
          jdbcScheme + ":postgresql://127.0.0.1:" + port + "/" + DB_NAME, USERNAME, dbSecret);
    }

    private static String mappedPort(String containerName) {
      long deadline = System.nanoTime() + Duration.ofSeconds(30).toNanos();
      while (System.nanoTime() < deadline) {
        String output = runQuietly("docker", "port", containerName, "5432/tcp").trim();
        int index = output.lastIndexOf(':');
        if (index >= 0 && index < output.length() - 1) {
          return output.substring(index + 1);
        }
        sleep();
      }
      throw new IllegalStateException("PostgreSQL test container port was not published.");
    }

    private static void waitUntilReady(String containerName) {
      long deadline = System.nanoTime() + Duration.ofSeconds(60).toNanos();
      while (System.nanoTime() < deadline) {
        if (runStatus("docker", "exec", containerName, "pg_isready", "-U", USERNAME, "-d", DB_NAME)
            == 0) {
          return;
        }
        sleep();
      }
      throw new IllegalStateException("PostgreSQL test container did not become ready.");
    }

    private static String run(String... command) {
      ProcessResult result = execute(command);
      if (result.exitCode() != 0) {
        throw new IllegalStateException("Docker command failed: " + result.output());
      }
      return result.output();
    }

    private static String runQuietly(String... command) {
      return execute(command).output();
    }

    private static int runStatus(String... command) {
      return execute(command).exitCode();
    }

    private static ProcessResult execute(String... command) {
      try {
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes());
        return new ProcessResult(process.waitFor(), output);
      } catch (IOException ex) {
        throw new IllegalStateException("Docker CLI is required for PostgreSQL integration tests.", ex);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Interrupted while starting PostgreSQL integration tests.", ex);
      }
    }

    private static void sleep() {
      try {
        Thread.sleep(500);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Interrupted while waiting for PostgreSQL.", ex);
      }
    }
  }

  private record ProcessResult(int exitCode, String output) {}
  }
