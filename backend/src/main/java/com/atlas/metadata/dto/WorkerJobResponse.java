package com.atlas.metadata.dto;

import com.atlas.metadata.enums.WorkerJobStatus;
import com.atlas.metadata.enums.WorkerJobType;
import com.atlas.metadata.enums.WorkerSafeErrorCategory;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/** API response for a local worker job. */
public record WorkerJobResponse(
    String id,
    WorkerJobType jobType,
    String subjectType,
    String subjectId,
    WorkerJobStatus status,
    int attemptCount,
    int maxAttempts,
    Integer retryDelaySeconds,
    OffsetDateTime nextRetryAt,
    Map<String, String> sourceTrace,
    boolean reviewEligible,
    String safeErrorCode,
    WorkerSafeErrorCategory safeErrorCategory,
    String safeErrorMessage,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<WorkerJobAttemptResponse> attempts) {}
