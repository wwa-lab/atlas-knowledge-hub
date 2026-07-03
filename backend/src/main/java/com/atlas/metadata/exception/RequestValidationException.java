package com.atlas.metadata.exception;

import java.util.Map;

/** Exception for service-level validation failures with field detail. */
public class RequestValidationException extends RuntimeException {

  private final Map<String, String> fields;

  /** Creates a validation exception with field messages. */
  public RequestValidationException(Map<String, String> fields) {
    super("Invalid request.");
    this.fields = fields;
  }

  public Map<String, String> getFields() {
    return fields;
  }
}
