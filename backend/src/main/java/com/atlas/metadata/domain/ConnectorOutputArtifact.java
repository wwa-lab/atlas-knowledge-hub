package com.atlas.metadata.domain;

import com.atlas.metadata.enums.ConnectorOutputArtifactType;
import com.atlas.metadata.enums.ReviewStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Review-required metadata handoff artifact produced by connector sync. */
@Entity
@Table(name = "connector_output_artifact", schema = "atlas")
public class ConnectorOutputArtifact {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "item_id", nullable = false, columnDefinition = "text")
  private String itemId;

  @Enumerated(EnumType.STRING)
  @Column(name = "artifact_type", nullable = false, columnDefinition = "text")
  private ConnectorOutputArtifactType artifactType;

  @Enumerated(EnumType.STRING)
  @Column(name = "review_status", nullable = false, columnDefinition = "text")
  private ReviewStatus reviewStatus;

  @Column(nullable = false, columnDefinition = "text")
  private String title;

  @Column(name = "target_path", nullable = false, columnDefinition = "text")
  private String targetPath;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "source_trace", nullable = false, columnDefinition = "jsonb")
  private Map<String, String> sourceTrace;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private Map<String, String> provenance;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected ConnectorOutputArtifact() {}

  /** Creates a review-required output artifact. */
  public static ConnectorOutputArtifact reviewRequired(
      String id,
      String itemId,
      String title,
      String targetPath,
      Map<String, String> sourceTrace,
      Map<String, String> provenance,
      OffsetDateTime createdAt) {
    ConnectorOutputArtifact artifact = new ConnectorOutputArtifact();
    artifact.id = id;
    artifact.itemId = itemId;
    artifact.artifactType = ConnectorOutputArtifactType.MARKDOWN_CANDIDATE;
    artifact.reviewStatus = ReviewStatus.REVIEW_REQUIRED;
    artifact.title = title;
    artifact.targetPath = targetPath;
    artifact.sourceTrace = Map.copyOf(sourceTrace);
    artifact.provenance = Map.copyOf(provenance);
    artifact.createdAt = createdAt;
    return artifact;
  }

  public String getId() {
    return id;
  }

  public String getItemId() {
    return itemId;
  }

  public ConnectorOutputArtifactType getArtifactType() {
    return artifactType;
  }

  public ReviewStatus getReviewStatus() {
    return reviewStatus;
  }

  public String getTitle() {
    return title;
  }

  public String getTargetPath() {
    return targetPath;
  }

  public Map<String, String> getSourceTrace() {
    return sourceTrace;
  }

  public Map<String, String> getProvenance() {
    return provenance;
  }
}
