package com.atlas.metadata.dto;

import java.util.Map;

/** User-safe API error body. */
public record ErrorBody(
    String code,
    String message,
    Map<String, String> fields,
    long timestamp,
    String path,
    String correlationId,
    Integer retryAfterSeconds) {

  /** Backwards-compatible constructor for existing non-correlated errors. */
  public ErrorBody(
      String code, String message, Map<String, String> fields, long timestamp, String path) {
    this(code, message, fields, timestamp, path, null, null);
  }
}
