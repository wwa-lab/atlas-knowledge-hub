package com.atlas.metadata.domain;

import com.atlas.metadata.enums.WorkerJobStatus;
import com.atlas.metadata.enums.WorkerJobType;
import com.atlas.metadata.enums.WorkerSafeErrorCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Local worker job reliability record. */
@Entity
@Table(name = "worker_job", schema = "atlas")
public class WorkerJob {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Enumerated(EnumType.STRING)
  @Column(name = "job_type", nullable = false, columnDefinition = "text")
  private WorkerJobType jobType;

  @Column(name = "subject_type", nullable = false, columnDefinition = "text")
  private String subjectType;

  @Column(name = "subject_id", nullable = false, columnDefinition = "text")
  private String subjectId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private WorkerJobStatus status;

  @Column(name = "attempt_count", nullable = false)
  private int attemptCount;

  @Column(name = "max_attempts", nullable = false)
  private int maxAttempts;

  @Column(name = "retry_delay_seconds")
  private Integer retryDelaySeconds;

  @Column(name = "next_retry_at")
  private OffsetDateTime nextRetryAt;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "source_trace", nullable = false, columnDefinition = "jsonb")
  private Map<String, String> sourceTrace;

  @Column(name = "review_eligible", nullable = false)
  private boolean reviewEligible;

  @Column(name = "safe_error_code", columnDefinition = "text")
  private String safeErrorCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "safe_error_category", columnDefinition = "text")
  private WorkerSafeErrorCategory safeErrorCategory;

  @Column(name = "safe_error_message", columnDefinition = "text")
  private String safeErrorMessage;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  protected WorkerJob() {}

  /** Creates a queued local worker job. */
  public static WorkerJob create(
      String id,
      WorkerJobType jobType,
      String subjectType,
      String subjectId,
      Map<String, String> sourceTrace,
      boolean reviewEligible,
      OffsetDateTime now) {
    WorkerJob job = new WorkerJob();
    job.id = id;
    job.jobType = jobType;
    job.subjectType = subjectType;
    job.subjectId = subjectId;
    job.status = WorkerJobStatus.QUEUED;
    job.attemptCount = 0;
    job.maxAttempts = 3;
    job.sourceTrace = Map.copyOf(sourceTrace);
    job.reviewEligible = reviewEligible;
    job.createdAt = now;
    job.updatedAt = now;
    return job;
  }

  /** Marks the job as waiting for a deterministic local retry. */
  public void markWaitingRetry(
      int attemptCount,
      int retryDelaySeconds,
      OffsetDateTime nextRetryAt,
      String safeErrorCode,
      WorkerSafeErrorCategory safeErrorCategory,
      String safeErrorMessage) {
    this.status = WorkerJobStatus.WAITING_RETRY;
    this.attemptCount = attemptCount;
    this.retryDelaySeconds = retryDelaySeconds;
    this.nextRetryAt = nextRetryAt;
    recordSafeError(safeErrorCode, safeErrorCategory, safeErrorMessage);
    this.updatedAt = nextRetryAt.minusSeconds(retryDelaySeconds);
  }

  /** Marks the job as terminally dead-lettered. */
  public void markDeadLettered(
      int attemptCount,
      String safeErrorCode,
      WorkerSafeErrorCategory safeErrorCategory,
      String safeErrorMessage,
      OffsetDateTime now) {
    this.status = WorkerJobStatus.DEAD_LETTERED;
    this.attemptCount = attemptCount;
    this.retryDelaySeconds = null;
    this.nextRetryAt = null;
    recordSafeError(safeErrorCode, safeErrorCategory, safeErrorMessage);
    this.updatedAt = now;
  }

  /** Marks the job as locally acknowledged by an operator. */
  public void acknowledge(OffsetDateTime now) {
    this.status = WorkerJobStatus.ACKNOWLEDGED;
    this.updatedAt = now;
  }

  private void recordSafeError(
      String safeErrorCode, WorkerSafeErrorCategory safeErrorCategory, String safeErrorMessage) {
    this.safeErrorCode = safeErrorCode;
    this.safeErrorCategory = safeErrorCategory;
    this.safeErrorMessage = safeErrorMessage;
  }

  public String getId() {
    return id;
  }

  public WorkerJobType getJobType() {
    return jobType;
  }

  public String getSubjectType() {
    return subjectType;
  }

  public String getSubjectId() {
    return subjectId;
  }

  public WorkerJobStatus getStatus() {
    return status;
  }

  public int getAttemptCount() {
    return attemptCount;
  }

  public int getMaxAttempts() {
    return maxAttempts;
  }

  public Integer getRetryDelaySeconds() {
    return retryDelaySeconds;
  }

  public OffsetDateTime getNextRetryAt() {
    return nextRetryAt;
  }

  public Map<String, String> getSourceTrace() {
    return sourceTrace;
  }

  public boolean isReviewEligible() {
    return reviewEligible;
  }

  public String getSafeErrorCode() {
    return safeErrorCode;
  }

  public WorkerSafeErrorCategory getSafeErrorCategory() {
    return safeErrorCategory;
  }

  public String getSafeErrorMessage() {
    return safeErrorMessage;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
}
