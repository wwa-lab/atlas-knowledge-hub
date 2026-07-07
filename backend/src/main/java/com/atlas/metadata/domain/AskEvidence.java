package com.atlas.metadata.domain;

import com.atlas.metadata.enums.AskCitationStatus;
import com.atlas.metadata.enums.ReviewStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Evidence snapshot used by one trusted ask run. */
@Entity
@Table(name = "ask_evidence", schema = "atlas")
public class AskEvidence {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "ask_run_id", nullable = false, columnDefinition = "text")
  private String askRunId;

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

  @Column(precision = 4, scale = 3)
  private BigDecimal score;

  @Column(name = "citation_id", nullable = false, columnDefinition = "text")
  private String citationId;

  @Column(name = "evidence_label", nullable = false, columnDefinition = "text")
  private String evidenceLabel;

  @Column(name = "source_locator", nullable = false, columnDefinition = "text")
  private String sourceLocator;

  @Enumerated(EnumType.STRING)
  @Column(name = "citation_status", nullable = false, columnDefinition = "text")
  private AskCitationStatus citationStatus;

  @Column(name = "review_eligible", nullable = false)
  private boolean reviewEligible;

  @Column(name = "excluded_reason", columnDefinition = "text")
  private String excludedReason;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected AskEvidence() {}

  /** Creates an immutable evidence snapshot for one ask run. */
  public static AskEvidence create(
      String id,
      String askRunId,
      String sourceChunkId,
      String fileItemId,
      String sourceFile,
      Integer page,
      String section,
      ReviewStatus reviewStatus,
      BigDecimal confidence,
      String vectorItemKey,
      BigDecimal score,
      OffsetDateTime createdAt) {
    return create(
        id,
        askRunId,
        sourceChunkId,
        fileItemId,
        sourceFile,
        page,
        section,
        reviewStatus,
        confidence,
        vectorItemKey,
        score,
        id,
        defaultEvidenceLabel(sourceFile, page),
        defaultSourceLocator(page, section, sourceChunkId),
        AskCitationStatus.ELIGIBLE,
        true,
        null,
        createdAt);
  }

  /** Creates an immutable evidence snapshot with safe citation eligibility metadata. */
  public static AskEvidence create(
      String id,
      String askRunId,
      String sourceChunkId,
      String fileItemId,
      String sourceFile,
      Integer page,
      String section,
      ReviewStatus reviewStatus,
      BigDecimal confidence,
      String vectorItemKey,
      BigDecimal score,
      String citationId,
      String evidenceLabel,
      String sourceLocator,
      AskCitationStatus citationStatus,
      boolean reviewEligible,
      String excludedReason,
      OffsetDateTime createdAt) {
    AskEvidence evidence = new AskEvidence();
    evidence.id = id;
    evidence.askRunId = askRunId;
    evidence.sourceChunkId = sourceChunkId;
    evidence.fileItemId = fileItemId;
    evidence.sourceFile = sourceFile;
    evidence.page = page;
    evidence.section = section;
    evidence.reviewStatus = reviewStatus;
    evidence.confidence = confidence;
    evidence.vectorItemKey = vectorItemKey;
    evidence.score = score;
    evidence.citationId = citationId == null ? id : citationId;
    evidence.evidenceLabel = evidenceLabel;
    evidence.sourceLocator = sourceLocator;
    evidence.citationStatus = citationStatus;
    evidence.reviewEligible = reviewEligible;
    evidence.excludedReason = excludedReason;
    evidence.createdAt = createdAt;
    return evidence;
  }

  private static String defaultEvidenceLabel(String sourceFile, Integer page) {
    String pageLabel = page == null ? "" : " page " + page;
    return sourceFile + pageLabel;
  }

  private static String defaultSourceLocator(Integer page, String section, String sourceChunkId) {
    String pageLabel = page == null ? "page n/a" : "page " + page;
    String sectionLabel = section == null || section.isBlank() ? "section n/a" : section;
    return pageLabel + " / " + sectionLabel + " / chunk " + sourceChunkId;
  }

  public String getId() {
    return id;
  }

  public String getAskRunId() {
    return askRunId;
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

  public BigDecimal getScore() {
    return score;
  }

  public String getCitationId() {
    return citationId;
  }

  public String getEvidenceLabel() {
    return evidenceLabel;
  }

  public String getSourceLocator() {
    return sourceLocator;
  }

  public AskCitationStatus getCitationStatus() {
    return citationStatus;
  }

  public boolean isReviewEligible() {
    return reviewEligible;
  }

  public String getExcludedReason() {
    return excludedReason;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
