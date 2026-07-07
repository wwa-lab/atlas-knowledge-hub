package com.atlas.metadata.domain;

import com.atlas.metadata.enums.ConnectorRunStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** User-visible connector sync request for one Knowledge Space. */
@Entity
@Table(name = "connector_sync_job", schema = "atlas")
public class ConnectorSyncJob {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "space_id", nullable = false, columnDefinition = "text")
  private String spaceId;

  @Column(name = "connector_definition_id", nullable = false, columnDefinition = "text")
  private String connectorDefinitionId;

  @Column(name = "requested_by", nullable = false, columnDefinition = "text")
  private String requestedBy;

  @Column(name = "requested_at", nullable = false)
  private OffsetDateTime requestedAt;

  @Column(name = "source_scope", nullable = false, columnDefinition = "text")
  private String sourceScope;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private ConnectorRunStatus status;

  @Column(name = "safe_message", columnDefinition = "text")
  private String safeMessage;

  protected ConnectorSyncJob() {}

  /** Creates a queued connector sync job. */
  public static ConnectorSyncJob create(
      String id,
      String spaceId,
      String connectorDefinitionId,
      String requestedBy,
      String sourceScope,
      OffsetDateTime requestedAt) {
    ConnectorSyncJob job = new ConnectorSyncJob();
    job.id = id;
    job.spaceId = spaceId;
    job.connectorDefinitionId = connectorDefinitionId;
    job.requestedBy = requestedBy;
    job.sourceScope = sourceScope;
    job.requestedAt = requestedAt;
    job.status = ConnectorRunStatus.QUEUED;
    return job;
  }

  /** Mirrors the latest run terminal status on the job. */
  public void complete(ConnectorRunStatus status, String safeMessage) {
    this.status = status;
    this.safeMessage = safeMessage;
  }

  public String getId() {
    return id;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public String getConnectorDefinitionId() {
    return connectorDefinitionId;
  }

  public String getRequestedBy() {
    return requestedBy;
  }

  public OffsetDateTime getRequestedAt() {
    return requestedAt;
  }

  public String getSourceScope() {
    return sourceScope;
  }

  public ConnectorRunStatus getStatus() {
    return status;
  }
}
