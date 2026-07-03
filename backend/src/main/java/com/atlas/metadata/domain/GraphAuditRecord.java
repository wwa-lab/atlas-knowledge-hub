package com.atlas.metadata.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Append-only graph governance audit record. */
@Entity
@Table(name = "graph_audit_record", schema = "atlas")
public class GraphAuditRecord {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "space_id", nullable = false, columnDefinition = "text")
  private String spaceId;

  @Column(nullable = false, columnDefinition = "text")
  private String actor;

  @Column(nullable = false, columnDefinition = "text")
  private String action;

  @Column(name = "target_type", nullable = false, columnDefinition = "text")
  private String targetType;

  @Column(name = "target_id", nullable = false, columnDefinition = "text")
  private String targetId;

  @Column(name = "safe_summary", columnDefinition = "text")
  private String safeSummary;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected GraphAuditRecord() {}

  /** Creates an append-only audit record. */
  public static GraphAuditRecord create(
      String id,
      String spaceId,
      String actor,
      String action,
      String targetType,
      String targetId,
      String safeSummary,
      OffsetDateTime createdAt) {
    GraphAuditRecord record = new GraphAuditRecord();
    record.id = id;
    record.spaceId = spaceId;
    record.actor = actor;
    record.action = action;
    record.targetType = targetType;
    record.targetId = targetId;
    record.safeSummary = safeSummary;
    record.createdAt = createdAt;
    return record;
  }

  public String getId() {
    return id;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public String getActor() {
    return actor;
  }

  public String getAction() {
    return action;
  }

  public String getTargetType() {
    return targetType;
  }

  public String getTargetId() {
    return targetId;
  }

  public String getSafeSummary() {
    return safeSummary;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
