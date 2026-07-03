package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;

/** Source chunk trace API response. */
public record SourceChunkResponse(
    String id,
    String fileItemId,
    String sourceFile,
    Integer page,
    String section,
    BigDecimal confidence,
    ReviewStatus reviewStatus) {}
