package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.VectorItemStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Per-source-chunk vector run evidence. */
public record VectorItemResultResponse(
    String sourceChunkId,
    String fileItemId,
    String sourceFile,
    Integer page,
    String section,
    ReviewStatus reviewStatus,
    BigDecimal confidence,
    String vectorItemKey,
    VectorItemStatus status,
    BigDecimal score,
    String safeError,
    OffsetDateTime createdAt) {}
