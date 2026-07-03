package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.VectorReviewPolicy;
import java.math.BigDecimal;
import java.util.List;

/** Product-facing vector index request. */
public record VectorIndexRequest(
    String runId,
    String spaceId,
    String batchId,
    String mode,
    VectorReviewPolicy reviewPolicy,
    Integer dimension,
    List<VectorIndexItem> items) {

  /** One traceable item to index. */
  public record VectorIndexItem(
      String sourceChunkId,
      String fileItemId,
      String sourceFile,
      Integer page,
      String section,
      ReviewStatus reviewStatus,
      BigDecimal confidence,
      List<BigDecimal> vector) {}
}
