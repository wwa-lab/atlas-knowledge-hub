package com.atlas.metadata.service;

import com.atlas.metadata.domain.VectorItemResult;
import com.atlas.metadata.dto.VectorRunSummaryResponse;
import com.atlas.metadata.enums.VectorItemStatus;
import java.util.List;

/** Computes vector run summary counts from persisted evidence. */
public class VectorSummaryCalculator {

  /** Derives summary counts from vector item results. */
  public VectorRunSummaryResponse compute(List<VectorItemResult> results) {
    return new VectorRunSummaryResponse(
        results.size(),
        count(results, VectorItemStatus.INDEXED),
        count(results, VectorItemStatus.DELETED),
        count(results, VectorItemStatus.SKIPPED),
        count(results, VectorItemStatus.FAILED));
  }

  private int count(List<VectorItemResult> results, VectorItemStatus status) {
    return (int) results.stream().filter(result -> result.getStatus() == status).count();
  }
}
