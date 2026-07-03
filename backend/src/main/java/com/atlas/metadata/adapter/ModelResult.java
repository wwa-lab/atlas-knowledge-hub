package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.ModelOutputKind;
import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;
import java.util.List;

/** Safe model operation result with descriptors instead of raw provider payloads. */
public record ModelResult(
    String adapterKey,
    String modelKey,
    String safeMessage,
    ModelUsage usage,
    List<ModelOutput> outputs) {

  /** Token-like mock usage counts. */
  public record ModelUsage(int promptUnits, int completionUnits) {}

  /** Safe output descriptor; embedding vectors and raw prompts are intentionally absent. */
  public record ModelOutput(
      String outputId,
      ModelOutputKind kind,
      String outputReference,
      String safeSummary,
      List<String> rankedItemIds,
      Integer embeddingDimension,
      Integer embeddingItemCount,
      BigDecimal confidence,
      ReviewStatus reviewStatus,
      String safeError) {}
}
