package com.atlas.metadata.adapter.runtime;

/** Sanitizes bounded runtime diagnostics before they reach product-facing records. */
public final class RuntimeOutputSanitizer {

  private RuntimeOutputSanitizer() {}

  /** Masks secrets, paths, endpoints, hosts, and stack frames, then truncates to maxLength. */
  public static String sanitize(String value, int maxLength) {
    if (value == null || value.isBlank()) {
      return null;
    }
    int limit = maxLength > 0 ? maxLength : RuntimeAdapterConfiguration.DEFAULT_CAPTURED_OUTPUT_LIMIT;
    String safe =
        value
            .replaceAll("(?i)(password|token|api[_-]?key|bearer)\\s*[:=]\\s*\\S+", "$1=[masked]")
            .replaceAll("(?i)\\b[a-z][a-z0-9+.-]*://[^\\s]+", "[endpoint]")
            .replaceAll("[A-Za-z]:[/\\\\][^\\s]+", "[path]")
            .replaceAll("(/[^\\s]+){2,}", "[path]")
            .replaceAll("\\bat\\s+[\\w.$]+\\([^)]*\\)", "[stack]")
            .replaceAll(
                "(?i)\\b[a-z0-9][a-z0-9-]*(?:\\.[a-z0-9][a-z0-9-]*)+(?::\\d+)?\\b",
                "[host]");
    return safe.length() <= limit ? safe : safe.substring(0, limit);
  }
}
