package com.atlas.metadata.dto;

import com.atlas.metadata.enums.AskReviewPolicy;

/** Request to run trusted ask over traceable Atlas evidence. */
public record CreateAskRequest(
    String question,
    String requestedBy,
    AskReviewPolicy reviewPolicy,
    Integer limit,
    String mode,
    String sessionId,
    String sessionTitle,
    AskFiltersRequest filters) {

  /** Backward-compatible request constructor for older Ask clients and tests. */
  public CreateAskRequest(
      String question,
      String requestedBy,
      AskReviewPolicy reviewPolicy,
      Integer limit,
      String mode,
      AskFiltersRequest filters) {
    this(question, requestedBy, reviewPolicy, limit, mode, null, null, filters);
  }
}
