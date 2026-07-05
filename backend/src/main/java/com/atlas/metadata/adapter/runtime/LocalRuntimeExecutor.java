package com.atlas.metadata.adapter.runtime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.stereotype.Component;

/** Local process implementation of the adapter-internal runtime executor. */
@Component
public class LocalRuntimeExecutor implements RuntimeExecutor {

  @Override
  public RuntimeExecutionResult execute(RuntimeInvocation invocation) {
    if (invocation.command().isBlank()) {
      return new RuntimeExecutionResult(127, "", "Runtime command is missing.", false);
    }
    List<String> command = new ArrayList<>();
    command.add(invocation.command());
    command.addAll(invocation.arguments());
    ExecutorService capturePool = Executors.newFixedThreadPool(2);
    try {
      Process process = new ProcessBuilder(command).start();
      if (!invocation.stdinJson().isBlank()) {
        process.getOutputStream().write(invocation.stdinJson().getBytes(StandardCharsets.UTF_8));
      }
      process.getOutputStream().close();
      Future<String> stdout = capturePool.submit(() -> capture(process.getInputStream(), invocation.capturedOutputLimit()));
      Future<String> stderr = capturePool.submit(() -> capture(process.getErrorStream(), invocation.capturedOutputLimit()));
      Duration timeout = invocation.timeout();
      boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
      if (!finished) {
        process.destroyForcibly();
      }
      return new RuntimeExecutionResult(
          finished ? process.exitValue() : 124,
          captured(stdout),
          captured(stderr),
          !finished);
    } catch (IOException ex) {
      return new RuntimeExecutionResult(127, "", ex.getMessage(), false);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      return new RuntimeExecutionResult(124, "", "Runtime execution was interrupted.", true);
    } finally {
      capturePool.shutdownNow();
    }
  }

  private String captured(Future<String> future) {
    try {
      return future.get(1, TimeUnit.SECONDS);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      return "";
    } catch (ExecutionException | TimeoutException ex) {
      return "";
    }
  }

  private String capture(InputStream input, int limit) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream(Math.min(limit, 8192));
    byte[] buffer = new byte[4096];
    int read;
    int remaining = limit;
    while ((read = input.read(buffer)) != -1) {
      if (remaining > 0) {
        int accepted = Math.min(read, remaining);
        output.write(buffer, 0, accepted);
        remaining -= accepted;
      }
    }
    return output.toString(StandardCharsets.UTF_8);
  }
}
