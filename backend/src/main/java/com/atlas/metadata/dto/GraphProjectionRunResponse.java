package com.atlas.metadata.dto;

import com.atlas.metadata.enums.GraphProjectionStatus;
import java.time.OffsetDateTime;
import java.util.List;

/** Projection run response with item outcomes. */
public record GraphProjectionRunResponse(
    String runId,
    String spaceId,
    String adapterId,
    String scope,
    GraphProjectionStatus status,
    String requestedBy,
    String safeMessage,
    GraphSummaryResponse summary,
    List<GraphProjectionItemResponse> items,
    OffsetDateTime startedAt,
    OffsetDateTime completedAt) {}
