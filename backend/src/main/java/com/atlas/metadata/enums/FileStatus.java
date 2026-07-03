package com.atlas.metadata.enums;

import java.util.Set;

/** Persisted file lifecycle status. */
public enum FileStatus {
  NEW,
  UPLOADED,
  PDF_CONVERTED,
  PDF_CONVERT_FAILED,
  MARKDOWN_GENERATED,
  OCR_REQUIRED,
  LOW_CONFIDENCE,
  REVIEW_REQUIRED,
  APPROVED,
  PUBLISHED,
  FAILED,
  UNSUPPORTED;

  private static final Set<FileStatus> GENERATED_REVIEW_REQUIRED_STATUSES =
      Set.of(MARKDOWN_GENERATED, LOW_CONFIDENCE, OCR_REQUIRED, REVIEW_REQUIRED);

  /** Returns true when this status represents generated or review-gated content. */
  public boolean requiresReviewByDefault() {
    return GENERATED_REVIEW_REQUIRED_STATUSES.contains(this);
  }
}
