package com.atlas.metadata.enums;

/** User-safe audit event outcomes. */
public enum AuditResult {
  SUCCEEDED,
  DENIED,
  FAILED,
  CONFLICT,
  SKIPPED,
  SAFE_NOT_FOUND
}
