package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Traceable evidence snapshot returned by trusted ask. */
public record AskEvidenceResponse(
    String evidenceId,
    String sourceChunkId,
    String fileItemId,
    String sourceFile,
    Integer page,
    String section,
    ReviewStatus reviewStatus,
    BigDecimal confidence,
    String vectorItemKey,
    BigDecimal score,
    OffsetDateTime createdAt) {}
