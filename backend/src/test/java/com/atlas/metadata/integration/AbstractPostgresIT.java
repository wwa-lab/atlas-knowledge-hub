package com.atlas.metadata.integration;

import java.util.UUID;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/** Shared Testcontainers PostgreSQL database for metadata API integration tests. */
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@Import(AbstractPostgresIT.MockAtlasAuthConfiguration.class)
abstract class AbstractPostgresIT {

  private static final String TEST_USER_HEADER = "X-Atlas-User";
  private static final String DEFAULT_TEST_USER = "mock-owner";

  private static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
          .withDatabaseName("atlas_metadata_test")
          .withUsername("atlas_test_user")
          .withPassword(UUID.randomUUID().toString());

  static {
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void datasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
  }

  @TestConfiguration
  static class MockAtlasAuthConfiguration {

    @Bean
    MockMvcBuilderCustomizer atlasDefaultAuthHeader() {
      return builder ->
          builder.defaultRequest(
              MockMvcRequestBuilders.get("/").header(TEST_USER_HEADER, DEFAULT_TEST_USER));
    }
  }
}
