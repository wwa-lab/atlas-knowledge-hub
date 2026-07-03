package com.atlas.metadata.domain;

import com.atlas.metadata.enums.ParserRunStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Append-oriented parser run evidence for one batch. */
@Entity
@Table(name = "parser_run", schema = "atlas")
public class ParserRun {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "batch_id", nullable = false, columnDefinition = "text")
  private String batchId;

  @Column(name = "adapter_key", nullable = false, columnDefinition = "text")
  private String adapterKey;

  @Column(name = "adapter_version", columnDefinition = "text")
  private String adapterVersion;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private ParserRunStatus status;

  @Column(name = "requested_by", columnDefinition = "text")
  private String requestedBy;

  @Column(nullable = false, columnDefinition = "text")
  private String mode;

  @Column(name = "low_confidence_threshold", nullable = false, precision = 4, scale = 3)
  private BigDecimal lowConfidenceThreshold;

  @Column(name = "skipped_count", nullable = false)
  private int skippedCount;

  @Column(name = "started_at", nullable = false)
  private OffsetDateTime startedAt;

  @Column(name = "completed_at")
  private OffsetDateTime completedAt;

  @Column(name = "safe_message", columnDefinition = "text")
  private String safeMessage;

  protected ParserRun() {}

  /** Creates a requested parser run. */
  public static ParserRun create(
      String id,
      String batchId,
      String adapterKey,
      String adapterVersion,
      String requestedBy,
      String mode,
      BigDecimal lowConfidenceThreshold,
      int skippedCount,
      OffsetDateTime startedAt) {
    ParserRun run = new ParserRun();
    run.id = id;
    run.batchId = batchId;
    run.adapterKey = adapterKey;
    run.adapterVersion = adapterVersion;
    run.status = ParserRunStatus.REQUESTED;
    run.requestedBy = requestedBy;
    run.mode = mode;
    run.lowConfidenceThreshold = lowConfidenceThreshold;
    run.skippedCount = skippedCount;
    run.startedAt = startedAt;
    return run;
  }

  /** Marks the run as executing. */
  public void markRunning() {
    status = ParserRunStatus.RUNNING;
  }

  /** Marks the run as terminal with a user-safe message. */
  public void complete(ParserRunStatus terminalStatus, OffsetDateTime completedAt, String safeMessage) {
    if (terminalStatus == ParserRunStatus.REQUESTED || terminalStatus == ParserRunStatus.RUNNING) {
      throw new IllegalArgumentException("Parser run must complete with a terminal status.");
    }
    this.status = terminalStatus;
    this.completedAt = completedAt;
    this.safeMessage = safeMessage;
  }

  public String getId() {
    return id;
  }

  public String getBatchId() {
    return batchId;
  }

  public String getAdapterKey() {
    return adapterKey;
  }

  public String getAdapterVersion() {
    return adapterVersion;
  }

  public ParserRunStatus getStatus() {
    return status;
  }

  public String getRequestedBy() {
    return requestedBy;
  }

  public String getMode() {
    return mode;
  }

  public BigDecimal getLowConfidenceThreshold() {
    return lowConfidenceThreshold;
  }

  public int getSkippedCount() {
    return skippedCount;
  }

  public OffsetDateTime getStartedAt() {
    return startedAt;
  }

  public OffsetDateTime getCompletedAt() {
    return completedAt;
  }

  public String getSafeMessage() {
    return safeMessage;
  }
}
