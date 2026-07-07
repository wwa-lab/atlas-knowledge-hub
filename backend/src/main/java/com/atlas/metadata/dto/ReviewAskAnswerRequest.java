package com.atlas.metadata.dto;

import com.atlas.metadata.enums.AnswerReviewStatus;

/** Reviewer-safe request to update one Trusted Ask answer's governance state. */
public record ReviewAskAnswerRequest(AnswerReviewStatus status, String reviewer, String reason) {}
