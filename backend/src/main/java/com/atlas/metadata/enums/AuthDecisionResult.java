package com.atlas.metadata.enums;

/** Safe auth decision result values for later audit persistence. */
public enum AuthDecisionResult {
  ALLOWED,
  UNAUTHENTICATED,
  FORBIDDEN,
  SAFE_NOT_FOUND,
  CONFLICT
}
