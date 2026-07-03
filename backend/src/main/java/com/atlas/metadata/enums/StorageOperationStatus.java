package com.atlas.metadata.enums;

/** Storage operation lifecycle status. */
public enum StorageOperationStatus {
  REQUESTED,
  RUNNING,
  SUCCEEDED,
  PARTIAL_FAILED,
  FAILED
}
