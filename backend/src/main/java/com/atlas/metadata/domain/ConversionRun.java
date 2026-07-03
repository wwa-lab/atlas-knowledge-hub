package com.atlas.metadata.domain;

import com.atlas.metadata.enums.ConversionRunStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Append-oriented conversion run evidence for one batch. */
@Entity
@Table(name = "conversion_run", schema = "atlas")
public class ConversionRun {

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
  private ConversionRunStatus status;

  @Column(name = "requested_by", columnDefinition = "text")
  private String requestedBy;

  @Column(name = "started_at", nullable = false)
  private OffsetDateTime startedAt;

  @Column(name = "completed_at")
  private OffsetDateTime completedAt;

  @Column(name = "safe_message", columnDefinition = "text")
  private String safeMessage;

  protected ConversionRun() {}

  /** Creates a requested conversion run. */
  public static ConversionRun create(
      String id,
      String batchId,
      String adapterKey,
      String adapterVersion,
      String requestedBy,
      OffsetDateTime startedAt) {
    ConversionRun run = new ConversionRun();
    run.id = id;
    run.batchId = batchId;
    run.adapterKey = adapterKey;
    run.adapterVersion = adapterVersion;
    run.status = ConversionRunStatus.REQUESTED;
    run.requestedBy = requestedBy;
    run.startedAt = startedAt;
    return run;
  }

  /** Marks the run as executing. */
  public void markRunning() {
    status = ConversionRunStatus.RUNNING;
  }

  /** Marks the run as terminal with a user-safe message. */
  public void complete(ConversionRunStatus terminalStatus, OffsetDateTime completedAt, String safeMessage) {
    if (terminalStatus == ConversionRunStatus.REQUESTED || terminalStatus == ConversionRunStatus.RUNNING) {
      throw new IllegalArgumentException("Conversion run must complete with a terminal status.");
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

  public ConversionRunStatus getStatus() {
    return status;
  }

  public String getRequestedBy() {
    return requestedBy;
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
