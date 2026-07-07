package com.atlas.metadata.domain;

import com.atlas.metadata.enums.ConnectorRunStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Deterministic connector sync run for one sync job. */
@Entity
@Table(name = "connector_sync_run", schema = "atlas")
public class ConnectorSyncRun {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "job_id", nullable = false, columnDefinition = "text")
  private String jobId;

  @Column(name = "connector_definition_id", nullable = false, columnDefinition = "text")
  private String connectorDefinitionId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private ConnectorRunStatus status;

  @Column(name = "started_at", nullable = false)
  private OffsetDateTime startedAt;

  @Column(name = "completed_at")
  private OffsetDateTime completedAt;

  @Column(name = "item_count", nullable = false)
  private int itemCount;

  @Column(name = "review_required_count", nullable = false)
  private int reviewRequiredCount;

  @Column(name = "failed_count", nullable = false)
  private int failedCount;

  @Column(name = "safe_message", columnDefinition = "text")
  private String safeMessage;

  protected ConnectorSyncRun() {}

  /** Creates a queued sync run. */
  public static ConnectorSyncRun create(
      String id, String jobId, String connectorDefinitionId, OffsetDateTime startedAt) {
    ConnectorSyncRun run = new ConnectorSyncRun();
    run.id = id;
    run.jobId = jobId;
    run.connectorDefinitionId = connectorDefinitionId;
    run.status = ConnectorRunStatus.QUEUED;
    run.startedAt = startedAt;
    return run;
  }

  /** Marks the run as executing. */
  public void markRunning() {
    status = ConnectorRunStatus.RUNNING;
  }

  /** Completes the run with deterministic counts. */
  public void complete(
      ConnectorRunStatus terminalStatus,
      int itemCount,
      int reviewRequiredCount,
      int failedCount,
      OffsetDateTime completedAt,
      String safeMessage) {
    if (terminalStatus == ConnectorRunStatus.QUEUED || terminalStatus == ConnectorRunStatus.RUNNING) {
      throw new IllegalArgumentException("Connector run must complete with a terminal status.");
    }
    this.status = terminalStatus;
    this.itemCount = itemCount;
    this.reviewRequiredCount = reviewRequiredCount;
    this.failedCount = failedCount;
    this.completedAt = completedAt;
    this.safeMessage = safeMessage;
  }

  public String getId() {
    return id;
  }

  public String getJobId() {
    return jobId;
  }

  public String getConnectorDefinitionId() {
    return connectorDefinitionId;
  }

  public ConnectorRunStatus getStatus() {
    return status;
  }

  public OffsetDateTime getStartedAt() {
    return startedAt;
  }

  public OffsetDateTime getCompletedAt() {
    return completedAt;
  }

  public int getItemCount() {
    return itemCount;
  }

  public int getReviewRequiredCount() {
    return reviewRequiredCount;
  }

  public int getFailedCount() {
    return failedCount;
  }

  public String getSafeMessage() {
    return safeMessage;
  }
}
