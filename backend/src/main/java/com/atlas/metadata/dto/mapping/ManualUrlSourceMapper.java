package com.atlas.metadata.dto.mapping;

import com.atlas.metadata.domain.ManualUrlSource;
import com.atlas.metadata.dto.ManualUrlSourceResponse;

/** Maps manual URL source entities to safe DTOs. */
public final class ManualUrlSourceMapper {

  private ManualUrlSourceMapper() {}

  /** Converts an entity to a response DTO. */
  public static ManualUrlSourceResponse toResponse(ManualUrlSource source) {
    return new ManualUrlSourceResponse(
        source.getId(),
        source.getSpaceId(),
        source.getDisplayUrl(),
        source.getHost(),
        source.getTitle(),
        source.getDescription(),
        source.getFetchIntent(),
        source.getFetchPolicy(),
        source.getIngestStatus(),
        source.getReviewStatus(),
        source.getEligibilityStatus(),
        source.getConfidence(),
        source.getSourceTrace(),
        source.getBatchId(),
        source.getFileItemId(),
        source.getCreatedBy(),
        source.getCreatedAt(),
        source.getUpdatedAt());
  }
}
