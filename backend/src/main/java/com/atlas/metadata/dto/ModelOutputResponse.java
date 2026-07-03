package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ModelOutputKind;
import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;
import java.util.List;

/** Safe model output descriptor. */
public record ModelOutputResponse(
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
