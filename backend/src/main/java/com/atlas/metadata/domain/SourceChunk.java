package com.atlas.metadata.domain;

import com.atlas.metadata.enums.ReviewStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** Source trace chunk linked to a file item. */
@Entity
@Table(name = "source_chunk", schema = "atlas")
public class SourceChunk {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "file_item_id", nullable = false, columnDefinition = "text")
  private String fileItemId;

  @Column(name = "source_file", nullable = false, columnDefinition = "text")
  private String sourceFile;

  @Column(name = "page")
  private Integer page;

  @Column(columnDefinition = "text")
  private String section;

  @Column(precision = 4, scale = 3)
  private BigDecimal confidence;

  @Enumerated(EnumType.STRING)
  @Column(name = "review_status", nullable = false, columnDefinition = "text")
  private ReviewStatus reviewStatus;

  protected SourceChunk() {}

  /** Creates a chunk trace record. */
  public static SourceChunk create(
      String id,
      String fileItemId,
      String sourceFile,
      Integer page,
      String section,
      BigDecimal confidence,
      ReviewStatus reviewStatus) {
    SourceChunk chunk = new SourceChunk();
    chunk.id = id;
    chunk.fileItemId = fileItemId;
    chunk.sourceFile = sourceFile;
    chunk.page = page;
    chunk.section = section;
    chunk.confidence = confidence;
    chunk.reviewStatus = reviewStatus == null ? ReviewStatus.REVIEW_REQUIRED : reviewStatus;
    return chunk;
  }

  public String getId() {
    return id;
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

  public BigDecimal getConfidence() {
    return confidence;
  }

  public ReviewStatus getReviewStatus() {
    return reviewStatus;
  }

  /** Updates the chunk review status from an explicit review action. */
  public void applyReviewStatus(ReviewStatus status) {
    if (status == ReviewStatus.PUBLISHED) {
      throw new IllegalArgumentException("PUBLISHED is not set by the metadata review endpoint.");
    }
    this.reviewStatus = status;
  }
}
