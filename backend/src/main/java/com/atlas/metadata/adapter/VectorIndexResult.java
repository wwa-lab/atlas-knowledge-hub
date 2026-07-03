package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.VectorItemStatus;
import java.math.BigDecimal;
import java.util.List;

/** Vector index adapter result. */
public record VectorIndexResult(String adapterKey, List<VectorItemResult> items, String safeMessage) {

  /** Per-item index result from a vector adapter. */
  public record VectorItemResult(
      String sourceChunkId,
      String vectorItemKey,
      VectorItemStatus status,
      BigDecimal score,
      String safeError) {}
}
