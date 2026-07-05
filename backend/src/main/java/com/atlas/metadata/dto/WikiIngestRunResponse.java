package com.atlas.metadata.dto;

import java.time.OffsetDateTime;
import java.util.List;

/** Safe Auto Wiki ingest v0 run response. */
public record WikiIngestRunResponse(
    String runId,
    String spaceId,
    String status,
    String mode,
    List<String> createdPageIds,
    List<String> updatedPageIds,
    List<String> issueIds,
    Integer eligibleChunkCount,
    Integer excludedChunkCount,
    String safeSummary,
    String safeError,
    OffsetDateTime startedAt,
    OffsetDateTime finishedAt) {}
