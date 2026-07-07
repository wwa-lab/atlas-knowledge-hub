package com.atlas.metadata.enums;

/** Lifecycle status for a connector sync run. */
public enum ConnectorRunStatus {
  QUEUED,
  RUNNING,
  COMPLETED,
  FAILED,
  REVIEW_REQUIRED
}
