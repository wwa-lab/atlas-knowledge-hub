package com.atlas.metadata.domain;

import com.atlas.metadata.enums.AskReviewPolicy;
import com.atlas.metadata.enums.AskRunStatus;
import com.atlas.metadata.enums.AnswerReviewStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Traceable trusted ask run with generated answers held for review. */
@Entity
@Table(name = "ask_run", schema = "atlas")
public class AskRun {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "space_id", nullable = false, columnDefinition = "text")
  private String spaceId;

  @Column(name = "session_id", nullable = false, columnDefinition = "text")
  private String sessionId;

  @Column(nullable = false, columnDefinition = "text")
  private String question;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private AskRunStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "review_policy", nullable = false, columnDefinition = "text")
  private AskReviewPolicy reviewPolicy;

  @Column(nullable = false, columnDefinition = "text")
  private String mode;

  @Column(name = "requested_by", nullable = false, columnDefinition = "text")
  private String requestedBy;

  @Column(columnDefinition = "text")
  private String answer;

  @Column(name = "answer_confidence", precision = 4, scale = 3)
  private BigDecimal answerConfidence;

  @Enumerated(EnumType.STRING)
  @Column(name = "answer_review_status", nullable = false, columnDefinition = "text")
  private AnswerReviewStatus answerReviewStatus;

  @Column(name = "answer_review_reason", columnDefinition = "text")
  private String answerReviewReason;

  @Column(name = "answer_reviewed_by", columnDefinition = "text")
  private String answerReviewedBy;

  @Column(name = "answer_reviewed_at")
  private OffsetDateTime answerReviewedAt;

  @Column(name = "model_run_id", columnDefinition = "text")
  private String modelRunId;

  @Column(name = "safe_message", columnDefinition = "text")
  private String safeMessage;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "completed_at")
  private OffsetDateTime completedAt;

  protected AskRun() {}

  /** Creates a requested ask run while enforcing review defaults for generated output. */
  public static AskRun create(
      String id,
      String spaceId,
      String question,
      AskReviewPolicy reviewPolicy,
      String mode,
      String requestedBy,
      OffsetDateTime createdAt) {
    return create(id, spaceId, null, question, reviewPolicy, mode, requestedBy, createdAt);
  }

  /** Creates a requested ask run inside a Trusted Ask session. */
  public static AskRun create(
      String id,
      String spaceId,
      String sessionId,
      String question,
      AskReviewPolicy reviewPolicy,
      String mode,
      String requestedBy,
      OffsetDateTime createdAt) {
    AskRun run = new AskRun();
    run.id = id;
    run.spaceId = spaceId;
    run.sessionId = sessionId;
    run.question = question;
    run.status = AskRunStatus.REQUESTED;
    run.reviewPolicy = reviewPolicy == null ? AskReviewPolicy.APPROVED_ONLY : reviewPolicy;
    run.mode = mode;
    run.requestedBy = requestedBy;
    run.answerReviewStatus = AnswerReviewStatus.REVIEW_REQUIRED;
    run.createdAt = createdAt;
    return run;
  }

  public void markRetrieving() {
    requireActive();
    status = AskRunStatus.RETRIEVING;
  }

  public void markGenerating() {
    requireActive();
    status = AskRunStatus.GENERATING;
  }

  /** Completes the run with a terminal status and safe generated answer metadata. */
  public void complete(
      AskRunStatus terminalStatus,
      String answer,
      BigDecimal answerConfidence,
      String modelRunId,
      String safeMessage,
      OffsetDateTime completedAt) {
    if (!isTerminal(terminalStatus)) {
      throw new IllegalArgumentException("Ask run can only complete with a terminal status.");
    }
    requireActive();
    status = terminalStatus;
    this.answer = answer;
    this.answerConfidence = answerConfidence;
    this.modelRunId = modelRunId;
    this.safeMessage = safeMessage;
    this.completedAt = completedAt;
    this.answerReviewStatus = AnswerReviewStatus.REVIEW_REQUIRED;
    this.answerReviewReason = null;
    this.answerReviewedBy = null;
    this.answerReviewedAt = null;
  }

  /** Updates answer-level governance metadata without changing source evidence. */
  public void reviewAnswer(
      AnswerReviewStatus status, String reviewer, String reason, OffsetDateTime reviewedAt) {
    this.answerReviewStatus = status;
    this.answerReviewedBy = reviewer;
    this.answerReviewReason = reason;
    this.answerReviewedAt = reviewedAt;
  }

  private void requireActive() {
    if (isTerminal(status)) {
      throw new IllegalStateException("Terminal ask run cannot be changed.");
    }
  }

  private boolean isTerminal(AskRunStatus candidate) {
    return candidate == AskRunStatus.SUCCEEDED
        || candidate == AskRunStatus.NO_EVIDENCE
        || candidate == AskRunStatus.PARTIAL_FAILED
        || candidate == AskRunStatus.FAILED;
  }

  public String getId() {
    return id;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public String getSessionId() {
    return sessionId;
  }

  public String getQuestion() {
    return question;
  }

  public AskRunStatus getStatus() {
    return status;
  }

  public AskReviewPolicy getReviewPolicy() {
    return reviewPolicy;
  }

  public String getMode() {
    return mode;
  }

  public String getRequestedBy() {
    return requestedBy;
  }

  public String getAnswer() {
    return answer;
  }

  public BigDecimal getAnswerConfidence() {
    return answerConfidence;
  }

  public AnswerReviewStatus getAnswerReviewStatus() {
    return answerReviewStatus;
  }

  public String getAnswerReviewReason() {
    return answerReviewReason;
  }

  public String getAnswerReviewedBy() {
    return answerReviewedBy;
  }

  public OffsetDateTime getAnswerReviewedAt() {
    return answerReviewedAt;
  }

  public String getModelRunId() {
    return modelRunId;
  }

  public String getSafeMessage() {
    return safeMessage;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getCompletedAt() {
    return completedAt;
  }
}
