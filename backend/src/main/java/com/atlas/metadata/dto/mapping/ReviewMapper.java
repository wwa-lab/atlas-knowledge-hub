package com.atlas.metadata.dto.mapping;

import com.atlas.metadata.domain.ReviewRecord;
import com.atlas.metadata.dto.ReviewResponse;
import java.util.Arrays;
import java.util.List;

/** Maps review record entities to DTOs. */
public final class ReviewMapper {

  private ReviewMapper() {}

  /** Converts an append-only review record to its response DTO. */
  public static ReviewResponse toResponse(ReviewRecord record) {
    String[] affected = record.getAffectedChunks();
    List<String> affectedChunks = affected == null ? List.of() : Arrays.asList(affected);
    return new ReviewResponse(
        record.getId(),
        record.getTargetType(),
        record.getTargetId(),
        record.getAction(),
        record.getReviewer(),
        record.getComment(),
        affectedChunks,
        record.getCreatedAt());
  }
}
