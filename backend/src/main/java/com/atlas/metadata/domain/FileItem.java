package com.atlas.metadata.domain;

import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** File item metadata with trace, confidence, and review state. */
@Entity
@Table(name = "file_item", schema = "atlas")
public class FileItem {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "batch_id", nullable = false, columnDefinition = "text")
  private String batchId;

  @Column(name = "source_path", nullable = false, columnDefinition = "text")
  private String sourcePath;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", nullable = false, columnDefinition = "text")
  private SourceType sourceType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private FileStatus status;

  @Column(precision = 4, scale = 3)
  private BigDecimal confidence;

  @Enumerated(EnumType.STRING)
  @Column(name = "review_status", nullable = false, columnDefinition = "text")
  private ReviewStatus reviewStatus;

  @Column(name = "pdf_path", columnDefinition = "text")
  private String pdfPath;

  @Column(name = "markdown_path", columnDefinition = "text")
  private String markdownPath;

  @Column(name = "assets_path", columnDefinition = "text")
  private String assetsPath;

  @Column(name = "error_message", columnDefinition = "text")
  private String errorMessage;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected FileItem() {}

  /** Creates a file item while enforcing generated-content review defaults. */
  public static FileItem create(
      String id,
      String batchId,
      String sourcePath,
      SourceType sourceType,
      FileStatus status,
      BigDecimal confidence,
      ReviewStatus reviewStatus,
      OffsetDateTime createdAt) {
    FileItem item = new FileItem();
    item.id = id;
    item.batchId = batchId;
    item.sourcePath = sourcePath;
    item.sourceType = sourceType;
    item.status = status;
    item.confidence = confidence;
    item.reviewStatus = status.requiresReviewByDefault() ? ReviewStatus.REVIEW_REQUIRED : reviewStatus;
    item.createdAt = createdAt;
    return item;
  }

  /** Updates artifact path metadata without touching source bytes. */
  public void setArtifacts(String pdfPath, String markdownPath, String assetsPath, String errorMessage) {
    this.pdfPath = pdfPath;
    this.markdownPath = markdownPath;
    this.assetsPath = assetsPath;
    this.errorMessage = errorMessage;
  }

  /** Applies a converter result while preserving review status. */
  public void applyConversionResult(
      FileStatus status, BigDecimal confidence, String pdfPath, String errorMessage) {
    this.status = status;
    this.confidence = confidence;
    this.pdfPath = pdfPath;
    this.errorMessage = errorMessage;
  }

  /** Applies a parser result while preserving PDF trace and review status. */
  public void applyParserResult(
      FileStatus status,
      BigDecimal confidence,
      String markdownPath,
      String assetsPath,
      String errorMessage) {
    this.status = status;
    this.confidence = confidence;
    this.markdownPath = markdownPath;
    this.assetsPath = assetsPath;
    this.errorMessage = errorMessage;
  }

  /** Updates the target review status from an explicit review action. */
  public void applyReviewStatus(ReviewStatus status) {
    if (status == ReviewStatus.PUBLISHED) {
      throw new IllegalArgumentException("PUBLISHED is not set by the metadata review endpoint.");
    }
    this.reviewStatus = status;
  }

  public String getId() {
    return id;
  }

  public String getBatchId() {
    return batchId;
  }

  public String getSourcePath() {
    return sourcePath;
  }

  public SourceType getSourceType() {
    return sourceType;
  }

  public FileStatus getStatus() {
    return status;
  }

  public BigDecimal getConfidence() {
    return confidence;
  }

  public ReviewStatus getReviewStatus() {
    return reviewStatus;
  }

  public String getPdfPath() {
    return pdfPath;
  }

  public String getMarkdownPath() {
    return markdownPath;
  }

  public String getAssetsPath() {
    return assetsPath;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
