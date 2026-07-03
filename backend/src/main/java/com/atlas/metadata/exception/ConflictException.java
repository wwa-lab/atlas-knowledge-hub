package com.atlas.metadata.exception;

/** Exception for duplicate or conflicting create requests. */
public class ConflictException extends RuntimeException {

  /** Creates a user-safe conflict exception. */
  public ConflictException(String message) {
    super(message);
  }
}
