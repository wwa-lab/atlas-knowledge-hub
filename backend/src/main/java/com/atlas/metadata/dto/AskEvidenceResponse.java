package com.atlas.metadata.dto;

import com.atlas.metadata.enums.AskCitationStatus;
import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Traceable evidence snapshot returned by trusted ask. */
public record AskEvidenceResponse(
    String evidenceId,
    String citationId,
    String sourceChunkId,
    String fileItemId,
    String sourceFile,
    Integer page,
    String section,
    ReviewStatus reviewStatus,
    BigDecimal confidence,
    String vectorItemKey,
    BigDecimal score,
    String evidenceLabel,
    String sourceLocator,
    AskCitationStatus citationStatus,
    boolean reviewEligible,
    String excludedReason,
    OffsetDateTime createdAt) {}
