package com.atlas.metadata.exception;

/** Stable user-safe API error codes. */
public final class SafeErrorCodes {

  public static final String AUTHENTICATION_REQUIRED = "AUTHENTICATION_REQUIRED";
  public static final String PERMISSION_DENIED = "PERMISSION_DENIED";
  public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
  public static final String NOT_FOUND = "NOT_FOUND";
  public static final String CONFLICT = "CONFLICT";
  public static final String RATE_LIMITED = "RATE_LIMITED";
  public static final String SAFE_SYSTEM_ERROR = "SAFE_SYSTEM_ERROR";

  private SafeErrorCodes() {}
}
