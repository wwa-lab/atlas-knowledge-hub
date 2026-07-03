package com.atlas.metadata.enums;

/** Lifecycle status for a model run. */
public enum ModelRunStatus {
  REQUESTED,
  RUNNING,
  SUCCEEDED,
  PARTIAL_FAILED,
  FAILED
}
