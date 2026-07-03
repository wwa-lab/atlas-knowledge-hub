package com.atlas.metadata.dto;

import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceType;
import java.math.BigDecimal;

/** Per-file conversion report response. */
public record ConversionFileResultResponse(
    String fileId,
    String sourcePath,
    SourceType sourceType,
    FileStatus status,
    String pdfPath,
    BigDecimal confidence,
    ReviewStatus reviewStatus,
    String safeError) {}
