package com.atlas.metadata.domain;

import com.atlas.metadata.enums.GraphEdgeType;
import com.atlas.metadata.enums.ReviewStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Deferred graph edge metadata table entity; no endpoint in this slice. */
@Entity
@Table(name = "graph_edge", schema = "atlas")
public class GraphEdge {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "space_id", nullable = false, columnDefinition = "text")
  private String spaceId;

  @Column(name = "source_node_id", nullable = false, columnDefinition = "text")
  private String sourceNodeId;

  @Column(name = "target_node_id", nullable = false, columnDefinition = "text")
  private String targetNodeId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private GraphEdgeType type;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "evidence_chunk_ids", columnDefinition = "text[]")
  private String[] evidenceChunkIds;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "evidence_wiki_page_ids", columnDefinition = "text[]")
  private String[] evidenceWikiPageIds;

  @Column(precision = 4, scale = 3)
  private BigDecimal confidence;

  @Enumerated(EnumType.STRING)
  @Column(name = "review_status", nullable = false, columnDefinition = "text")
  private ReviewStatus reviewStatus;

  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  protected GraphEdge() {}

  /** Creates or refreshes an evidence-backed graph edge. */
  public static GraphEdge create(
      String id,
      String spaceId,
      String sourceNodeId,
      String targetNodeId,
      GraphEdgeType type,
      String[] evidenceChunkIds,
      String[] evidenceWikiPageIds,
      BigDecimal confidence,
      ReviewStatus reviewStatus,
      OffsetDateTime updatedAt) {
    GraphEdge edge = new GraphEdge();
    edge.id = id;
    edge.spaceId = spaceId;
    edge.sourceNodeId = sourceNodeId;
    edge.targetNodeId = targetNodeId;
    edge.type = type;
    edge.evidenceChunkIds = evidenceChunkIds;
    edge.evidenceWikiPageIds = evidenceWikiPageIds;
    edge.confidence = confidence;
    edge.reviewStatus = reviewStatus;
    edge.updatedAt = updatedAt;
    return edge;
  }

  /** Updates review state from an explicit graph review action. */
  public void applyReviewStatus(ReviewStatus status, OffsetDateTime updatedAt) {
    this.reviewStatus = status;
    this.updatedAt = updatedAt;
  }

  public String getId() {
    return id;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public String getSourceNodeId() {
    return sourceNodeId;
  }

  public String getTargetNodeId() {
    return targetNodeId;
  }

  public GraphEdgeType getType() {
    return type;
  }

  public String[] getEvidenceChunkIds() {
    return evidenceChunkIds;
  }

  public String[] getEvidenceWikiPageIds() {
    return evidenceWikiPageIds;
  }

  public BigDecimal getConfidence() {
    return confidence;
  }

  public ReviewStatus getReviewStatus() {
    return reviewStatus;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
}
