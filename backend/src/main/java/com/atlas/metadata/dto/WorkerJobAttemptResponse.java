package com.atlas.metadata.dto;

import com.atlas.metadata.enums.WorkerAttemptStatus;
import com.atlas.metadata.enums.WorkerSafeErrorCategory;
import java.time.OffsetDateTime;
import java.util.Map;

/** API response for a worker job attempt. */
public record WorkerJobAttemptResponse(
    String id,
    String workerJobId,
    int attemptNumber,
    WorkerAttemptStatus status,
    boolean retryable,
    String safeErrorCode,
    WorkerSafeErrorCategory safeErrorCategory,
    String safeErrorMessage,
    Map<String, String> sourceTrace,
    OffsetDateTime startedAt,
    OffsetDateTime completedAt) {}
