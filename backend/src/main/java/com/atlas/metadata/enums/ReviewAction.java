package com.atlas.metadata.enums;

/** Reviewer action recorded in append-only review history. */
public enum ReviewAction {
  APPROVE,
  NEED_FIX,
  OCR_REQUIRED;

  /** Maps a review action to the target's resulting review status. */
  public ReviewStatus resultingStatus() {
    return switch (this) {
      case APPROVE -> ReviewStatus.APPROVED;
      case NEED_FIX -> ReviewStatus.NEED_FIX;
      case OCR_REQUIRED -> ReviewStatus.OCR_REQUIRED;
    };
  }
}
