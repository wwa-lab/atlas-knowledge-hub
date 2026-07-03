package com.atlas.metadata.domain;

import com.atlas.metadata.enums.ModelOutputKind;
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

/** Safe model output descriptor; never stores vectors, raw prompts, or provider payloads. */
@Entity
@Table(name = "model_run_output", schema = "atlas")
public class ModelRunOutput {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "run_id", nullable = false, columnDefinition = "text")
  private String runId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private ModelOutputKind kind;

  @Column(name = "output_reference", columnDefinition = "text")
  private String outputReference;

  @Column(name = "safe_summary", columnDefinition = "text")
  private String safeSummary;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "ranked_item_ids", columnDefinition = "text[]")
  private String[] rankedItemIds;

  @Column(name = "embedding_dimension")
  private Integer embeddingDimension;

  @Column(name = "embedding_item_count")
  private Integer embeddingItemCount;

  @Column(precision = 4, scale = 3)
  private BigDecimal confidence;

  @Enumerated(EnumType.STRING)
  @Column(name = "review_status", nullable = false, columnDefinition = "text")
  private ReviewStatus reviewStatus;

  @Column(name = "safe_error", columnDefinition = "text")
  private String safeError;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected ModelRunOutput() {}

  /** Creates a safe output descriptor. */
  public static ModelRunOutput create(
      String id,
      String runId,
      ModelOutputKind kind,
      String outputReference,
      String safeSummary,
      String[] rankedItemIds,
      Integer embeddingDimension,
      Integer embeddingItemCount,
      BigDecimal confidence,
      ReviewStatus reviewStatus,
      String safeError,
      OffsetDateTime createdAt) {
    ModelRunOutput output = new ModelRunOutput();
    output.id = id;
    output.runId = runId;
    output.kind = kind;
    output.outputReference = outputReference;
    output.safeSummary = safeSummary;
    output.rankedItemIds = rankedItemIds == null ? new String[0] : rankedItemIds.clone();
    output.embeddingDimension = embeddingDimension;
    output.embeddingItemCount = embeddingItemCount;
    output.confidence = confidence;
    output.reviewStatus = reviewStatus == null ? ReviewStatus.REVIEW_REQUIRED : reviewStatus;
    output.safeError = safeError;
    output.createdAt = createdAt;
    return output;
  }

  public String getId() {
    return id;
  }

  public String getRunId() {
    return runId;
  }

  public ModelOutputKind getKind() {
    return kind;
  }

  public String getOutputReference() {
    return outputReference;
  }

  public String getSafeSummary() {
    return safeSummary;
  }

  public String[] getRankedItemIds() {
    return rankedItemIds == null ? new String[0] : rankedItemIds.clone();
  }

  public Integer getEmbeddingDimension() {
    return embeddingDimension;
  }

  public Integer getEmbeddingItemCount() {
    return embeddingItemCount;
  }

  public BigDecimal getConfidence() {
    return confidence;
  }

  public ReviewStatus getReviewStatus() {
    return reviewStatus;
  }

  public String getSafeError() {
    return safeError;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
