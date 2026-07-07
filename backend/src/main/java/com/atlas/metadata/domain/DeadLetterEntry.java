package com.atlas.metadata.domain;

import com.atlas.metadata.enums.DeadLetterStatus;
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

/** Terminal local worker failure record for operator inspection. */
@Entity
@Table(name = "dead_letter_entry", schema = "atlas")
public class DeadLetterEntry {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "worker_job_id", nullable = false, columnDefinition = "text")
  private String workerJobId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private DeadLetterStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "job_type", nullable = false, columnDefinition = "text")
  private WorkerJobType jobType;

  @Column(name = "subject_type", nullable = false, columnDefinition = "text")
  private String subjectType;

  @Column(name = "subject_id", nullable = false, columnDefinition = "text")
  private String subjectId;

  @Column(name = "attempt_summary", nullable = false, columnDefinition = "text")
  private String attemptSummary;

  @Column(name = "safe_error_code", nullable = false, columnDefinition = "text")
  private String safeErrorCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "safe_error_category", nullable = false, columnDefinition = "text")
  private WorkerSafeErrorCategory safeErrorCategory;

  @Column(name = "safe_error_message", nullable = false, columnDefinition = "text")
  private String safeErrorMessage;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "source_trace", nullable = false, columnDefinition = "jsonb")
  private Map<String, String> sourceTrace;

  @Column(name = "review_eligible", nullable = false)
  private boolean reviewEligible;

  @Column(name = "operator_action_by", columnDefinition = "text")
  private String operatorActionBy;

  @Column(name = "operator_action_at")
  private OffsetDateTime operatorActionAt;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected DeadLetterEntry() {}

  /** Creates an open dead-letter entry from a terminal job. */
  public static DeadLetterEntry createOpen(
      String id,
      WorkerJob job,
      String attemptSummary,
      String safeErrorCode,
      WorkerSafeErrorCategory safeErrorCategory,
      String safeErrorMessage,
      OffsetDateTime now) {
    DeadLetterEntry entry = new DeadLetterEntry();
    entry.id = id;
    entry.workerJobId = job.getId();
    entry.status = DeadLetterStatus.OPEN;
    entry.jobType = job.getJobType();
    entry.subjectType = job.getSubjectType();
    entry.subjectId = job.getSubjectId();
    entry.attemptSummary = attemptSummary;
    entry.safeErrorCode = safeErrorCode;
    entry.safeErrorCategory = safeErrorCategory;
    entry.safeErrorMessage = safeErrorMessage;
    entry.sourceTrace = Map.copyOf(job.getSourceTrace());
    entry.reviewEligible = job.isReviewEligible();
    entry.createdAt = now;
    return entry;
  }

  /** Marks the entry as locally retried. */
  public void markRetried(String operator, OffsetDateTime now) {
    status = DeadLetterStatus.RETRIED;
    operatorActionBy = operator;
    operatorActionAt = now;
  }

  /** Marks the entry as acknowledged. */
  public void acknowledge(String operator, OffsetDateTime now) {
    status = DeadLetterStatus.ACKNOWLEDGED;
    operatorActionBy = operator;
    operatorActionAt = now;
  }

  public String getId() {
    return id;
  }

  public String getWorkerJobId() {
    return workerJobId;
  }

  public DeadLetterStatus getStatus() {
    return status;
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

  public String getAttemptSummary() {
    return attemptSummary;
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

  public boolean isReviewEligible() {
    return reviewEligible;
  }

  public String getOperatorActionBy() {
    return operatorActionBy;
  }

  public OffsetDateTime getOperatorActionAt() {
    return operatorActionAt;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
