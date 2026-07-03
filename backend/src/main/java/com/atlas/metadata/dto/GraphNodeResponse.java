package com.atlas.metadata.dto;

import com.atlas.metadata.enums.GraphNodeType;
import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;

/** Safe graph node response. */
public record GraphNodeResponse(
    String id,
    String label,
    GraphNodeType type,
    ReviewStatus reviewStatus,
    BigDecimal confidence,
    int evidenceCount) {}
