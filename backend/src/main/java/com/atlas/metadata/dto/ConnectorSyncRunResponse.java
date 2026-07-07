package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ConnectorRunStatus;
import java.time.OffsetDateTime;

/** Connector sync run response. */
public record ConnectorSyncRunResponse(
    String runId,
    String jobId,
    String spaceId,
    String connectorKey,
    ConnectorRunStatus status,
    int itemCount,
    int reviewRequiredCount,
    int failedCount,
    String safeMessage,
    OffsetDateTime startedAt,
    OffsetDateTime completedAt) {}
