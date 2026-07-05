package com.atlas.metadata.adapter.runtime;

import java.time.Duration;
import java.util.List;

/** Safe invocation descriptor for configured document runtimes. */
public record RuntimeInvocation(
    String command,
    List<String> arguments,
    String stdinJson,
    Duration timeout,
    int capturedOutputLimit) {

  /** Creates a normalized invocation descriptor. */
  public RuntimeInvocation {
    command = command == null ? "" : command.trim();
    arguments = arguments == null ? List.of() : List.copyOf(arguments);
    stdinJson = stdinJson == null ? "" : stdinJson;
    timeout = timeout == null ? RuntimeAdapterConfiguration.DEFAULT_TIMEOUT : timeout;
    capturedOutputLimit =
        capturedOutputLimit > 0
            ? capturedOutputLimit
            : RuntimeAdapterConfiguration.DEFAULT_CAPTURED_OUTPUT_LIMIT;
  }
}
