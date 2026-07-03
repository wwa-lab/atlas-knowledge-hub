package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;
import java.util.Map;

/** One traceable vector evidence match. */
public record VectorQueryMatchResponse(
    String sourceChunkId,
    String fileItemId,
    String sourceFile,
    Integer page,
    String section,
    ReviewStatus reviewStatus,
    BigDecimal confidence,
    String vectorItemKey,
    BigDecimal score,
    Map<String, String> safeMetadata) {}
