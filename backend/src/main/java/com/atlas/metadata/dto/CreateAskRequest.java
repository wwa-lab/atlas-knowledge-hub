package com.atlas.metadata.dto;

import com.atlas.metadata.enums.AskReviewPolicy;

/** Request to run trusted ask over traceable Atlas evidence. */
public record CreateAskRequest(
    String question,
    String requestedBy,
    AskReviewPolicy reviewPolicy,
    Integer limit,
    String mode,
    AskFiltersRequest filters) {}
