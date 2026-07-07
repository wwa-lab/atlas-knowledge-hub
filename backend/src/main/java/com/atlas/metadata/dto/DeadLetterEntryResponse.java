package com.atlas.metadata.dto;

import com.atlas.metadata.enums.DeadLetterStatus;
import com.atlas.metadata.enums.WorkerJobType;
import com.atlas.metadata.enums.WorkerSafeErrorCategory;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/** API response for a dead-letter entry and associated job detail. */
public record DeadLetterEntryResponse(
    String id,
    String workerJobId,
    DeadLetterStatus status,
    WorkerJobType jobType,
    String subjectType,
    String subjectId,
    String attemptSummary,
    String safeErrorCode,
    WorkerSafeErrorCategory safeErrorCategory,
    String safeErrorMessage,
    Map<String, String> sourceTrace,
    boolean reviewEligible,
    String operatorActionBy,
    OffsetDateTime operatorActionAt,
    OffsetDateTime createdAt,
    WorkerJobResponse job,
    List<WorkerJobAttemptResponse> attempts) {}
