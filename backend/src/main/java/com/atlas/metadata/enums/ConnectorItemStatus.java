package com.atlas.metadata.enums;

/** Per-item lifecycle status for connector sync output. */
public enum ConnectorItemStatus {
  DISCOVERED,
  FETCHED,
  OUTPUT_CREATED,
  REVIEW_REQUIRED,
  FAILED
}
