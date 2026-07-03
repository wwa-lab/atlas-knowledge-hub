package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;

/** Source evidence reference for graph details. */
public record GraphEvidenceReferenceResponse(
    String sourceChunkId,
    String sourceFile,
    Integer page,
    String section,
    BigDecimal confidence,
    ReviewStatus reviewStatus) {}
