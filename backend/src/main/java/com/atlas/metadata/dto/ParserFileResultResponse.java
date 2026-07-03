package com.atlas.metadata.dto;

import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;

/** Per-file parser report response. */
public record ParserFileResultResponse(
    String fileId,
    String sourcePath,
    String pdfPath,
    FileStatus status,
    String markdownPath,
    String assetsPath,
    BigDecimal confidence,
    ReviewStatus reviewStatus,
    int chunkCount,
    boolean skipped,
    String safeError) {}
