package com.atlas.metadata.adapter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Vector query adapter result. */
public record VectorQueryResult(String adapterKey, List<VectorMatch> matches, String safeMessage) {

  /** One scored vector match. */
  public record VectorMatch(
      String sourceChunkId, String vectorItemKey, BigDecimal score, Map<String, String> safeMetadata) {}
}
