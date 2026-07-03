package com.atlas.metadata.dto;

import com.atlas.metadata.enums.VectorReviewPolicy;
import com.atlas.metadata.enums.VectorRunOperation;
import java.math.BigDecimal;
import java.util.List;

/** Request to execute a vector adapter index or deindex run. */
public record CreateVectorRunRequest(
    String adapterKey,
    VectorRunOperation operation,
    String batchId,
    List<String> fileItemIds,
    List<String> sourceChunkIds,
    VectorReviewPolicy reviewPolicy,
    Integer dimension,
    List<VectorInputItem> items,
    String requestedBy,
    String mode) {

  /** Optional mock-only vector input for a source chunk. */
  public record VectorInputItem(String sourceChunkId, List<BigDecimal> vector) {}
}
