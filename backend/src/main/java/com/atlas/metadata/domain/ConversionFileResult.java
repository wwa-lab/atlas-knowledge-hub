package com.atlas.metadata.domain;

import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.SourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Per-file converter result evidence. */
@Entity
@Table(name = "conversion_file_result", schema = "atlas")
public class ConversionFileResult {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "run_id", nullable = false, columnDefinition = "text")
  private String runId;

  @Column(name = "file_item_id", nullable = false, columnDefinition = "text")
  private String fileItemId;

  @Column(name = "source_path", nullable = false, columnDefinition = "text")
  private String sourcePath;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", nullable = false, columnDefinition = "text")
  private SourceType sourceType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private FileStatus status;

  @Column(name = "pdf_path", columnDefinition = "text")
  private String pdfPath;

  @Column(precision = 4, scale = 3)
  private BigDecimal confidence;

  @Column(name = "adapter_key", nullable = false, columnDefinition = "text")
  private String adapterKey;

  @Column(name = "safe_error", columnDefinition = "text")
  private String safeError;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected ConversionFileResult() {}

  /** Creates a converter result evidence row. */
  public static ConversionFileResult create(
      String id,
      String runId,
      String fileItemId,
      String sourcePath,
      SourceType sourceType,
      FileStatus status,
      String pdfPath,
      BigDecimal confidence,
      String adapterKey,
      String safeError,
      OffsetDateTime createdAt) {
    ConversionFileResult result = new ConversionFileResult();
    result.id = id;
    result.runId = runId;
    result.fileItemId = fileItemId;
    result.sourcePath = sourcePath;
    result.sourceType = sourceType;
    result.status = status;
    result.pdfPath = pdfPath;
    result.confidence = confidence;
    result.adapterKey = adapterKey;
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

  public String getFileItemId() {
    return fileItemId;
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

  public String getPdfPath() {
    return pdfPath;
  }

  public BigDecimal getConfidence() {
    return confidence;
  }

  public String getAdapterKey() {
    return adapterKey;
  }

  public String getSafeError() {
    return safeError;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
