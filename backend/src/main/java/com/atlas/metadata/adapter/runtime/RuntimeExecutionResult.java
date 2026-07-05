package com.atlas.metadata.adapter.runtime;

/** Bounded runtime execution output captured inside adapter scope. */
public record RuntimeExecutionResult(int exitCode, String stdout, String stderr, boolean timedOut) {

  /** Returns stderr when present, otherwise stdout. */
  public String diagnosticOutput() {
    if (stderr != null && !stderr.isBlank()) {
      return stderr;
    }
    return stdout;
  }
}
