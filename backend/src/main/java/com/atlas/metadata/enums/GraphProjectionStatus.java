package com.atlas.metadata.enums;

/** Lifecycle status for a knowledge graph projection run. */
public enum GraphProjectionStatus {
  REQUESTED,
  RUNNING,
  SUCCEEDED,
  PARTIAL_FAILED,
  FAILED
}
