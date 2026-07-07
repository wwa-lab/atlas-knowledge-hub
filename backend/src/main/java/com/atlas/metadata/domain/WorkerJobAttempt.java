package com.atlas.metadata.domain;

import com.atlas.metadata.enums.WorkerAttemptStatus;
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

/** Append-only local worker attempt record. */
@Entity
@Table(name = "worker_job_attempt", schema = "atlas")
public class WorkerJobAttempt {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "worker_job_id", nullable = false, columnDefinition = "text")
  private String workerJobId;

  @Column(name = "attempt_number", nullable = false)
  private int attemptNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private WorkerAttemptStatus status;

  @Column(nullable = false)
  private boolean retryable;

  @Column(name = "safe_error_code", columnDefinition = "text")
  private String safeErrorCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "safe_error_category", columnDefinition = "text")
  private WorkerSafeErrorCategory safeErrorCategory;

  @Column(name = "safe_error_message", columnDefinition = "text")
  private String safeErrorMessage;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "source_trace", nullable = false, columnDefinition = "jsonb")
  private Map<String, String> sourceTrace;

  @Column(name = "started_at", nullable = false)
  private OffsetDateTime startedAt;

  @Column(name = "completed_at")
  private OffsetDateTime completedAt;

  protected WorkerJobAttempt() {}

  /** Creates a failed attempt record. */
  public static WorkerJobAttempt failed(
      String id,
      WorkerJob job,
      int attemptNumber,
      boolean retryable,
      String safeErrorCode,
      WorkerSafeErrorCategory safeErrorCategory,
      String safeErrorMessage,
      Map<String, String> sourceTrace,
      OffsetDateTime now,
      boolean terminal) {
    WorkerJobAttempt attempt = new WorkerJobAttempt();
    attempt.id = id;
    attempt.workerJobId = job.getId();
    attempt.attemptNumber = attemptNumber;
    attempt.status = terminal ? WorkerAttemptStatus.FAILED_TERMINAL : WorkerAttemptStatus.FAILED_RETRYABLE;
    attempt.retryable = retryable;
    attempt.safeErrorCode = safeErrorCode;
    attempt.safeErrorCategory = safeErrorCategory;
    attempt.safeErrorMessage = safeErrorMessage;
    attempt.sourceTrace = Map.copyOf(sourceTrace);
    attempt.startedAt = now;
    attempt.completedAt = now.plusSeconds(1);
    return attempt;
  }

  public String getId() {
    return id;
  }

  public String getWorkerJobId() {
    return workerJobId;
  }

  public int getAttemptNumber() {
    return attemptNumber;
  }

  public WorkerAttemptStatus getStatus() {
    return status;
  }

  public boolean isRetryable() {
    return retryable;
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

  public Map<String, String> getSourceTrace() {
    return sourceTrace;
  }

  public OffsetDateTime getStartedAt() {
    return startedAt;
  }

  public OffsetDateTime getCompletedAt() {
    return completedAt;
  }
}
