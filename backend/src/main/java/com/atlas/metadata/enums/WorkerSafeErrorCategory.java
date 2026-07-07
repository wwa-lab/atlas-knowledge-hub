package com.atlas.metadata.enums;

/** Worker-domain safe error category shown to operators. */
public enum WorkerSafeErrorCategory {
  NONE,
  VALIDATION,
  CONNECTOR_UNAVAILABLE,
  UNSUPPORTED_SOURCE,
  SOURCE_UNREADABLE,
  RATE_LIMITED,
  SAFE_SYSTEM
}
