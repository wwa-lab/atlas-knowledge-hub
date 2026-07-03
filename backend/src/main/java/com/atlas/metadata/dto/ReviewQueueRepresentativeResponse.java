package com.atlas.metadata.dto;

import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;

/** Safe representative item metadata for a review queue bucket. */
public record ReviewQueueRepresentativeResponse(
    String fileId,
    FileStatus status,
    ReviewStatus reviewStatus,
    BigDecimal confidence,
    boolean hasSourceTrace) {}
