package com.atlas.metadata.domain;

import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.VectorItemStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Per-chunk vector index/deindex evidence. */
@Entity
@Table(name = "vector_item_result", schema = "atlas")
public class VectorItemResult {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "run_id", nullable = false, columnDefinition = "text")
  private String runId;

  @Column(name = "source_chunk_id", nullable = false, columnDefinition = "text")
  private String sourceChunkId;

  @Column(name = "file_item_id", nullable = false, columnDefinition = "text")
  private String fileItemId;

  @Column(name = "source_file", nullable = false, columnDefinition = "text")
  private String sourceFile;

  @Column(name = "page")
  private Integer page;

  @Column(columnDefinition = "text")
  private String section;

  @Enumerated(EnumType.STRING)
  @Column(name = "review_status", nullable = false, columnDefinition = "text")
  private ReviewStatus reviewStatus;

  @Column(precision = 4, scale = 3)
  private BigDecimal confidence;

  @Column(name = "vector_item_key", columnDefinition = "text")
  private String vectorItemKey;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private VectorItemStatus status;

  @Column(precision = 4, scale = 3)
  private BigDecimal score;

  @Column(name = "safe_error", columnDefinition = "text")
  private String safeError;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected VectorItemResult() {}

  /** Creates a vector item result evidence row. */
  public static VectorItemResult create(
      String id,
      String runId,
      String sourceChunkId,
      String fileItemId,
      String sourceFile,
      Integer page,
      String section,
      ReviewStatus reviewStatus,
      BigDecimal confidence,
      String vectorItemKey,
      VectorItemStatus status,
      BigDecimal score,
      String safeError,
      OffsetDateTime createdAt) {
    VectorItemResult result = new VectorItemResult();
    result.id = id;
    result.runId = runId;
    result.sourceChunkId = sourceChunkId;
    result.fileItemId = fileItemId;
    result.sourceFile = sourceFile;
    result.page = page;
    result.section = section;
    result.reviewStatus = reviewStatus;
    result.confidence = confidence;
    result.vectorItemKey = vectorItemKey;
    result.status = status;
    result.score = score;
    result.safeError = safeError;
    result.createdAt = createdAt;
    return result;
  }

  public String getId() {
    return id;
  }

  public String getRunId() {
    return runId;
  }

  public String getSourceChunkId() {
    return sourceChunkId;
  }

  public String getFileItemId() {
    return fileItemId;
  }

  public String getSourceFile() {
    return sourceFile;
  }

  public Integer getPage() {
    return page;
  }

  public String getSection() {
    return section;
  }

  public ReviewStatus getReviewStatus() {
    return reviewStatus;
  }

  public BigDecimal getConfidence() {
    return confidence;
  }

  public String getVectorItemKey() {
    return vectorItemKey;
  }

  public VectorItemStatus getStatus() {
    return status;
  }

  public BigDecimal getScore() {
    return score;
  }

  public String getSafeError() {
    return safeError;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
