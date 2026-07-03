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

/** Per-file parser result evidence. */
@Entity
@Table(name = "parser_file_result", schema = "atlas")
public class ParserFileResult {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "run_id", nullable = false, columnDefinition = "text")
  private String runId;

  @Column(name = "file_item_id", nullable = false, columnDefinition = "text")
  private String fileItemId;

  @Column(name = "source_path", nullable = false, columnDefinition = "text")
  private String sourcePath;

  @Column(name = "pdf_path", columnDefinition = "text")
  private String pdfPath;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", nullable = false, columnDefinition = "text")
  private SourceType sourceType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private FileStatus status;

  @Column(name = "markdown_path", columnDefinition = "text")
  private String markdownPath;

  @Column(name = "assets_path", columnDefinition = "text")
  private String assetsPath;

  @Column(precision = 4, scale = 3)
  private BigDecimal confidence;

  @Column(name = "adapter_key", nullable = false, columnDefinition = "text")
  private String adapterKey;

  @Column(name = "chunk_count", nullable = false)
  private int chunkCount;

  @Column(nullable = false)
  private boolean skipped;

  @Column(name = "safe_error", columnDefinition = "text")
  private String safeError;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected ParserFileResult() {}

  /** Creates a parser result evidence row. */
  public static ParserFileResult create(
      String id,
      String runId,
      String fileItemId,
      String sourcePath,
      String pdfPath,
      SourceType sourceType,
      FileStatus status,
      String markdownPath,
      String assetsPath,
      BigDecimal confidence,
      String adapterKey,
      int chunkCount,
      boolean skipped,
      String safeError,
      OffsetDateTime createdAt) {
    ParserFileResult result = new ParserFileResult();
    result.id = id;
    result.runId = runId;
    result.fileItemId = fileItemId;
    result.sourcePath = sourcePath;
    result.pdfPath = pdfPath;
    result.sourceType = sourceType;
    result.status = status;
    result.markdownPath = markdownPath;
    result.assetsPath = assetsPath;
    result.confidence = confidence;
    result.adapterKey = adapterKey;
    result.chunkCount = chunkCount;
    result.skipped = skipped;
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

  public String getPdfPath() {
    return pdfPath;
  }

  public SourceType getSourceType() {
    return sourceType;
  }

  public FileStatus getStatus() {
    return status;
  }

  public String getMarkdownPath() {
    return markdownPath;
  }

  public String getAssetsPath() {
    return assetsPath;
  }

  public BigDecimal getConfidence() {
    return confidence;
  }

  public String getAdapterKey() {
    return adapterKey;
  }

  public int getChunkCount() {
    return chunkCount;
  }

  public boolean isSkipped() {
    return skipped;
  }

  public String getSafeError() {
    return safeError;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
