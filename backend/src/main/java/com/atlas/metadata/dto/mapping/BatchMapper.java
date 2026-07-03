package com.atlas.metadata.dto.mapping;

import com.atlas.metadata.domain.Batch;
import com.atlas.metadata.dto.BatchMetricsResponse;
import com.atlas.metadata.dto.BatchResponse;

/** Maps batch entities to DTOs. */
public final class BatchMapper {

  private BatchMapper() {}

  /** Converts a batch entity and derived metrics to its response DTO. */
  public static BatchResponse toResponse(Batch batch, BatchMetricsResponse metrics) {
    return new BatchResponse(
        batch.getId(),
        batch.getSpaceId(),
        batch.getName(),
        batch.getSourceKind(),
        batch.getOwner(),
        batch.getUploadedAt(),
        metrics);
  }
}
