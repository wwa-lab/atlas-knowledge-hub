package com.atlas.metadata.enums;

/** Lifecycle status for a trusted ask run. */
public enum AskRunStatus {
  REQUESTED,
  RETRIEVING,
  GENERATING,
  SUCCEEDED,
  NO_EVIDENCE,
  PARTIAL_FAILED,
  FAILED
}
