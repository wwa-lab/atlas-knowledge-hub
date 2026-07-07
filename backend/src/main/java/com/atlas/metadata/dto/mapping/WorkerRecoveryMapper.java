package com.atlas.metadata.dto.mapping;

import com.atlas.metadata.domain.DeadLetterEntry;
import com.atlas.metadata.domain.WorkerJob;
import com.atlas.metadata.domain.WorkerJobAttempt;
import com.atlas.metadata.dto.DeadLetterEntryResponse;
import com.atlas.metadata.dto.WorkerJobAttemptResponse;
import com.atlas.metadata.dto.WorkerJobResponse;
import java.util.List;

/** Maps worker retry/dead-letter domain records to API DTOs. */
public final class WorkerRecoveryMapper {

  private WorkerRecoveryMapper() {}

  /** Converts a worker job to an API response. */
  public static WorkerJobResponse toJobResponse(
      WorkerJob job, List<WorkerJobAttempt> attempts) {
    return new WorkerJobResponse(
        job.getId(),
        job.getJobType(),
        job.getSubjectType(),
        job.getSubjectId(),
        job.getStatus(),
        job.getAttemptCount(),
        job.getMaxAttempts(),
        job.getRetryDelaySeconds(),
        job.getNextRetryAt(),
        job.getSourceTrace(),
        job.isReviewEligible(),
        job.getSafeErrorCode(),
        job.getSafeErrorCategory(),
        job.getSafeErrorMessage(),
        job.getCreatedAt(),
        job.getUpdatedAt(),
        attempts.stream().map(WorkerRecoveryMapper::toAttemptResponse).toList());
  }

  /** Converts a dead-letter entry to an API response. */
  public static DeadLetterEntryResponse toDeadLetterResponse(
      DeadLetterEntry entry, WorkerJob job, List<WorkerJobAttempt> attempts) {
    return new DeadLetterEntryResponse(
        entry.getId(),
        entry.getWorkerJobId(),
        entry.getStatus(),
        entry.getJobType(),
        entry.getSubjectType(),
        entry.getSubjectId(),
        entry.getAttemptSummary(),
        entry.getSafeErrorCode(),
        entry.getSafeErrorCategory(),
        entry.getSafeErrorMessage(),
        entry.getSourceTrace(),
        entry.isReviewEligible(),
        entry.getOperatorActionBy(),
        entry.getOperatorActionAt(),
        entry.getCreatedAt(),
        toJobResponse(job, attempts),
        attempts.stream().map(WorkerRecoveryMapper::toAttemptResponse).toList());
  }

  private static WorkerJobAttemptResponse toAttemptResponse(WorkerJobAttempt attempt) {
    return new WorkerJobAttemptResponse(
        attempt.getId(),
        attempt.getWorkerJobId(),
        attempt.getAttemptNumber(),
        attempt.getStatus(),
        attempt.isRetryable(),
        attempt.getSafeErrorCode(),
        attempt.getSafeErrorCategory(),
        attempt.getSafeErrorMessage(),
        attempt.getSourceTrace(),
        attempt.getStartedAt(),
        attempt.getCompletedAt());
  }
}
