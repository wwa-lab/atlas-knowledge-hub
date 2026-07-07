package com.atlas.metadata.domain;

import com.atlas.metadata.enums.ManualUrlEligibilityStatus;
import com.atlas.metadata.enums.ManualUrlFetchIntent;
import com.atlas.metadata.enums.ManualUrlFetchPolicy;
import com.atlas.metadata.enums.ManualUrlIngestStatus;
import com.atlas.metadata.enums.ReviewStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Metadata-only manual URL source registered for a Knowledge Space. */
@Entity
@Table(name = "manual_url_source", schema = "atlas")
public class ManualUrlSource {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "space_id", nullable = false, columnDefinition = "text")
  private String spaceId;

  @Column(name = "url_hash", nullable = false, columnDefinition = "text")
  private String urlHash;

  @Column(name = "display_url", nullable = false, columnDefinition = "text")
  private String displayUrl;

  @Column(nullable = false, columnDefinition = "text")
  private String host;

  @Column(columnDefinition = "text")
  private String title;

  @Column(columnDefinition = "text")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "fetch_intent", nullable = false, columnDefinition = "text")
  private ManualUrlFetchIntent fetchIntent;

  @Enumerated(EnumType.STRING)
  @Column(name = "fetch_policy", nullable = false, columnDefinition = "text")
  private ManualUrlFetchPolicy fetchPolicy;

  @Enumerated(EnumType.STRING)
  @Column(name = "ingest_status", nullable = false, columnDefinition = "text")
  private ManualUrlIngestStatus ingestStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "review_status", nullable = false, columnDefinition = "text")
  private ReviewStatus reviewStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "eligibility_status", nullable = false, columnDefinition = "text")
  private ManualUrlEligibilityStatus eligibilityStatus;

  @Column(nullable = false, precision = 4, scale = 3)
  private BigDecimal confidence;

  @Column(name = "source_trace", nullable = false, columnDefinition = "text")
  private String sourceTrace;

  @Column(name = "batch_id", nullable = false, columnDefinition = "text")
  private String batchId;

  @Column(name = "file_item_id", nullable = false, columnDefinition = "text")
  private String fileItemId;

  @Column(name = "created_by", nullable = false, columnDefinition = "text")
  private String createdBy;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  protected ManualUrlSource() {}

  /** Creates a metadata-only manual URL source. */
  public static ManualUrlSource create(
      String id,
      String spaceId,
      String urlHash,
      String displayUrl,
      String host,
      String title,
      String description,
      ManualUrlFetchIntent fetchIntent,
      String sourceTrace,
      String batchId,
      String fileItemId,
      String createdBy,
      BigDecimal confidence,
      OffsetDateTime createdAt) {
    ManualUrlSource source = new ManualUrlSource();
    source.id = id;
    source.spaceId = spaceId;
    source.urlHash = urlHash;
    source.displayUrl = displayUrl;
    source.host = host;
    source.title = title;
    source.description = description;
    source.fetchIntent = fetchIntent;
    source.fetchPolicy = ManualUrlFetchPolicy.NO_FETCH_METADATA_ONLY;
    source.ingestStatus = ManualUrlIngestStatus.REVIEW_REQUIRED;
    source.reviewStatus = ReviewStatus.REVIEW_REQUIRED;
    source.eligibilityStatus = ManualUrlEligibilityStatus.REVIEW_REQUIRED_ONLY;
    source.confidence = confidence;
    source.sourceTrace = sourceTrace;
    source.batchId = batchId;
    source.fileItemId = fileItemId;
    source.createdBy = createdBy;
    source.createdAt = createdAt;
    source.updatedAt = createdAt;
    return source;
  }

  public String getId() {
    return id;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public String getUrlHash() {
    return urlHash;
  }

  public String getDisplayUrl() {
    return displayUrl;
  }

  public String getHost() {
    return host;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public ManualUrlFetchIntent getFetchIntent() {
    return fetchIntent;
  }

  public ManualUrlFetchPolicy getFetchPolicy() {
    return fetchPolicy;
  }

  public ManualUrlIngestStatus getIngestStatus() {
    return ingestStatus;
  }

  public ReviewStatus getReviewStatus() {
    return reviewStatus;
  }

  public ManualUrlEligibilityStatus getEligibilityStatus() {
    return eligibilityStatus;
  }

  public BigDecimal getConfidence() {
    return confidence;
  }

  public String getSourceTrace() {
    return sourceTrace;
  }

  public String getBatchId() {
    return batchId;
  }

  public String getFileItemId() {
    return fileItemId;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
}
