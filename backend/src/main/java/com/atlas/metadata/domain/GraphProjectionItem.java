package com.atlas.metadata.domain;

import com.atlas.metadata.enums.GraphProjectionItemStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Per-source projection outcome for graph traceability. */
@Entity
@Table(name = "graph_projection_item", schema = "atlas")
public class GraphProjectionItem {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "run_id", nullable = false, columnDefinition = "text")
  private String runId;

  @Column(name = "source_type", nullable = false, columnDefinition = "text")
  private String sourceType;

  @Column(name = "source_id", nullable = false, columnDefinition = "text")
  private String sourceId;

  @Column(name = "target_type", nullable = false, columnDefinition = "text")
  private String targetType;

  @Column(name = "target_id", columnDefinition = "text")
  private String targetId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private GraphProjectionItemStatus status;

  @Column(name = "reason_code", columnDefinition = "text")
  private String reasonCode;

  protected GraphProjectionItem() {}

  /** Creates a projection item outcome. */
  public static GraphProjectionItem create(
      String id,
      String runId,
      String sourceType,
      String sourceId,
      String targetType,
      String targetId,
      GraphProjectionItemStatus status,
      String reasonCode) {
    GraphProjectionItem item = new GraphProjectionItem();
    item.id = id;
    item.runId = runId;
    item.sourceType = sourceType;
    item.sourceId = sourceId;
    item.targetType = targetType;
    item.targetId = targetId;
    item.status = status;
    item.reasonCode = reasonCode;
    return item;
  }

  public String getId() {
    return id;
  }

  public String getRunId() {
    return runId;
  }

  public String getSourceType() {
    return sourceType;
  }

  public String getSourceId() {
    return sourceId;
  }

  public String getTargetType() {
    return targetType;
  }

  public String getTargetId() {
    return targetId;
  }

  public GraphProjectionItemStatus getStatus() {
    return status;
  }

  public String getReasonCode() {
    return reasonCode;
  }
}
