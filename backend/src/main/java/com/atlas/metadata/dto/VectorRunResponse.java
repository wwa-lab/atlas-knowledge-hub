package com.atlas.metadata.dto;

import com.atlas.metadata.enums.VectorReviewPolicy;
import com.atlas.metadata.enums.VectorRunOperation;
import com.atlas.metadata.enums.VectorRunStatus;
import java.time.OffsetDateTime;
import java.util.List;

/** Vector adapter run response with per-item trace evidence. */
public record VectorRunResponse(
    String runId,
    String spaceId,
    String batchId,
    String adapterKey,
    String adapterVersion,
    VectorRunOperation operation,
    VectorRunStatus status,
    String mode,
    VectorReviewPolicy reviewPolicy,
    Integer dimension,
    String requestedBy,
    String safeMessage,
    VectorRunSummaryResponse summary,
    List<VectorItemResultResponse> results,
    OffsetDateTime startedAt,
    OffsetDateTime completedAt) {}
