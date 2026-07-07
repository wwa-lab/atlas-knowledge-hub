package com.atlas.metadata.enums;

/** Worker attempt lifecycle states. */
public enum WorkerAttemptStatus {
  RUNNING,
  FAILED_RETRYABLE,
  FAILED_TERMINAL,
  SUCCEEDED
}
