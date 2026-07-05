package com.atlas.metadata.adapter.runtime;

import java.time.Duration;

/** Server-side runtime adapter configuration with safe status helpers. */
public record RuntimeAdapterConfiguration(
    boolean enabled, String command, Duration timeout, int capturedOutputLimit) {

  public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(120);
  public static final int DEFAULT_CAPTURED_OUTPUT_LIMIT = 65_536;

  /** Creates normalized runtime configuration. */
  public RuntimeAdapterConfiguration {
    command = command == null ? "" : command.trim();
    timeout = timeout == null ? DEFAULT_TIMEOUT : timeout;
    capturedOutputLimit =
        capturedOutputLimit > 0 ? capturedOutputLimit : DEFAULT_CAPTURED_OUTPUT_LIMIT;
  }

  /** Returns default disabled runtime configuration. */
  public static RuntimeAdapterConfiguration disabled(String command) {
    return new RuntimeAdapterConfiguration(false, command, DEFAULT_TIMEOUT, DEFAULT_CAPTURED_OUTPUT_LIMIT);
  }

  /** Returns whether a command path is present. */
  public boolean commandConfigured() {
    return command != null && !command.isBlank();
  }

  /** Returns a masked command status safe for capability responses. */
  public String commandState() {
    if (!enabled) {
      return "disabled";
    }
    return commandConfigured() ? "configured" : "missing";
  }
}
