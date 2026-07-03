package com.atlas.metadata.dto;

import java.util.List;

/** One review or publish readiness queue bucket. */
public record ReviewQueueItemResponse(
    ReviewQueueType type,
    long count,
    boolean publishBlocked,
    List<ReviewQueueRepresentativeResponse> representativeItems) {

  /** Queue buckets used by Processing Center. */
  public enum ReviewQueueType {
    PARSER_FAILURE,
    OCR_REQUIRED,
    LOW_CONFIDENCE,
    MISSING_SOURCE_TRACE,
    LLM_GENERATED_REVIEW_REQUIRED,
    READY_TO_PUBLISH
  }
}
