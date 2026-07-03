package com.atlas.metadata.exception;

/** Exception for unknown resource ids. */
public class NotFoundException extends RuntimeException {

  /** Creates a user-safe not found exception. */
  public NotFoundException(String message) {
    super(message);
  }
}
