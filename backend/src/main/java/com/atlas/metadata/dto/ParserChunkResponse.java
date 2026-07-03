package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;

/** Parser source chunk report response. */
public record ParserChunkResponse(
    String chunkId,
    String fileId,
    String sourceFile,
    Integer page,
    String section,
    BigDecimal confidence,
    ReviewStatus reviewStatus) {}
