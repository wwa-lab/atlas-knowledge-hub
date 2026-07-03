package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.VectorItemStatus;
import java.util.List;

/** Vector delete adapter result. */
public record VectorDeleteResult(String adapterKey, List<VectorDeletedItem> items, String safeMessage) {

  /** Per-item delete result from a vector adapter. */
  public record VectorDeletedItem(
      String sourceChunkId, String vectorItemKey, VectorItemStatus status, String safeError) {}
}
