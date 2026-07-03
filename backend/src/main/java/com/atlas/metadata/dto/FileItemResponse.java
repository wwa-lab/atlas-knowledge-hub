package com.atlas.metadata.dto;

import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceType;
import java.math.BigDecimal;

/** File item API response preserving trace, confidence, and review status. */
public record FileItemResponse(
    String id,
    String batchId,
    String sourcePath,
    SourceType sourceType,
    FileStatus status,
    BigDecimal confidence,
    ReviewStatus reviewStatus,
    String pdfPath,
    String markdownPath,
    String assetsPath,
    String errorMessage) {}
