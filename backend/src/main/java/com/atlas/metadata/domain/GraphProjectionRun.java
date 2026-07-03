package com.atlas.metadata.domain;

import com.atlas.metadata.enums.GraphProjectionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Projection run evidence for approved-only knowledge graph generation. */
@Entity
@Table(name = "graph_projection_run", schema = "atlas")
public class GraphProjectionRun {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "space_id", nullable = false, columnDefinition = "text")
  private String spaceId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private GraphProjectionStatus status;

  @Column(name = "adapter_id", nullable = false, columnDefinition = "text")
  private String adapterId;

  @Column(nullable = false, columnDefinition = "text")
  private String scope;

  @Column(name = "requested_by", columnDefinition = "text")
  private String requestedBy;

  @Column(name = "created_count", nullable = false)
  private int createdCount;

  @Column(name = "updated_count", nullable = false)
  private int updatedCount;

  @Column(name = "skipped_count", nullable = false)
  private int skippedCount;

  @Column(name = "failed_count", nullable = false)
  private int failedCount;

  @Column(name = "started_at", nullable = false)
  private OffsetDateTime startedAt;

  @Column(name = "completed_at")
  private OffsetDateTime completedAt;

  @Column(name = "safe_message", columnDefinition = "text")
  private String safeMessage;

  protected GraphProjectionRun() {}

  /** Creates a requested graph projection run. */
  public static GraphProjectionRun create(
      String id, String spaceId, String adapterId, String scope, String requestedBy, OffsetDateTime startedAt) {
    GraphProjectionRun run = new GraphProjectionRun();
    run.id = id;
    run.spaceId = spaceId;
    run.adapterId = adapterId;
    run.scope = scope;
    run.requestedBy = requestedBy;
    run.status = GraphProjectionStatus.REQUESTED;
    run.startedAt = startedAt;
    return run;
  }

  public void markRunning() {
    this.status = GraphProjectionStatus.RUNNING;
  }

  /** Completes the run with summary counts. */
  public void complete(
      GraphProjectionStatus status,
      int createdCount,
      int updatedCount,
      int skippedCount,
      int failedCount,
      OffsetDateTime completedAt,
      String safeMessage) {
    this.status = status;
    this.createdCount = createdCount;
    this.updatedCount = updatedCount;
    this.skippedCount = skippedCount;
    this.failedCount = failedCount;
    this.completedAt = completedAt;
    this.safeMessage = safeMessage;
  }

  public String getId() {
    return id;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public GraphProjectionStatus getStatus() {
    return status;
  }

  public String getAdapterId() {
    return adapterId;
  }

  public String getScope() {
    return scope;
  }

  public String getRequestedBy() {
    return requestedBy;
  }

  public int getCreatedCount() {
    return createdCount;
  }

  public int getUpdatedCount() {
    return updatedCount;
  }

  public int getSkippedCount() {
    return skippedCount;
  }

  public int getFailedCount() {
    return failedCount;
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
