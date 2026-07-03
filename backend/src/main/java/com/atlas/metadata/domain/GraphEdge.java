package com.atlas.metadata.domain;

import com.atlas.metadata.enums.GraphEdgeType;
import com.atlas.metadata.enums.ReviewStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

  @Enumerated(EnumType.STRING)
  @Column(name = "review_status", nullable = false, columnDefinition = "text")
  private ReviewStatus reviewStatus;

  protected GraphEdge() {}
}
