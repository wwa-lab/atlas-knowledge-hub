package com.atlas.metadata.dto;

import com.atlas.metadata.enums.GraphEdgeType;
import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;

/** Safe graph edge response. */
public record GraphEdgeResponse(
    String id,
    String sourceNodeId,
    String targetNodeId,
    GraphEdgeType type,
    ReviewStatus reviewStatus,
    BigDecimal confidence,
    int evidenceCount) {}
