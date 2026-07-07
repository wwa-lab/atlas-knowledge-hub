package com.atlas.metadata.enums;

/** Local worker job lifecycle states. */
public enum WorkerJobStatus {
  QUEUED,
  RUNNING,
  WAITING_RETRY,
  SUCCEEDED,
  DEAD_LETTERED,
  ACKNOWLEDGED
}
