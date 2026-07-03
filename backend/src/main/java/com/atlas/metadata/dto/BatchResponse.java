package com.atlas.metadata.dto;

import com.atlas.metadata.enums.SourceKind;
import java.time.OffsetDateTime;

/** Batch API response with derived metrics. */
public record BatchResponse(
    String id,
    String spaceId,
    String name,
    SourceKind sourceKind,
    String owner,
    OffsetDateTime uploadedAt,
    BatchMetricsResponse metrics) {}
