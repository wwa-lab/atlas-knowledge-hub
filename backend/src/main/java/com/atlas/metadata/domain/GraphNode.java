package com.atlas.metadata.domain;

import com.atlas.metadata.enums.GraphNodeType;
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

/** Deferred graph node metadata table entity; no endpoint in this slice. */
@Entity
@Table(name = "graph_node", schema = "atlas")
public class GraphNode {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "space_id", nullable = false, columnDefinition = "text")
  private String spaceId;

  @Column(nullable = false, columnDefinition = "text")
  private String label;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private GraphNodeType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "review_status", nullable = false, columnDefinition = "text")
  private ReviewStatus reviewStatus;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "evidence_chunk_ids", columnDefinition = "text[]")
  private String[] evidenceChunkIds;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "evidence_wiki_page_ids", columnDefinition = "text[]")
  private String[] evidenceWikiPageIds;

  @Column(precision = 4, scale = 3)
  private BigDecimal confidence;

  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  protected GraphNode() {}

  /** Creates or refreshes a graph node projection. */
  public static GraphNode create(
      String id,
      String spaceId,
      String label,
      GraphNodeType type,
      ReviewStatus reviewStatus,
      String[] evidenceChunkIds,
      String[] evidenceWikiPageIds,
      BigDecimal confidence,
      OffsetDateTime updatedAt) {
    GraphNode node = new GraphNode();
    node.id = id;
    node.spaceId = spaceId;
    node.label = label;
    node.type = type;
    node.reviewStatus = reviewStatus;
    node.evidenceChunkIds = evidenceChunkIds;
    node.evidenceWikiPageIds = evidenceWikiPageIds;
    node.confidence = confidence;
    node.updatedAt = updatedAt;
    return node;
  }

  public String getId() {
    return id;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public String getLabel() {
    return label;
  }

  public GraphNodeType getType() {
    return type;
  }

  public ReviewStatus getReviewStatus() {
    return reviewStatus;
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

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
}
