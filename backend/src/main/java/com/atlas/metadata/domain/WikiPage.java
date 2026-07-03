package com.atlas.metadata.domain;

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

/** Published Wiki page metadata preserving source trace, confidence, and review state. */
@Entity
@Table(name = "wiki_page", schema = "atlas")
public class WikiPage {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "space_id", nullable = false, columnDefinition = "text")
  private String spaceId;

  @Column(nullable = false, columnDefinition = "text")
  private String title;

  @Column(name = "markdown_path", columnDefinition = "text")
  private String markdownPath;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "source_document_ids", columnDefinition = "text[]")
  private String[] sourceDocumentIds;

  @Column(precision = 4, scale = 3)
  private BigDecimal confidence;

  @Enumerated(EnumType.STRING)
  @Column(name = "review_status", nullable = false, columnDefinition = "text")
  private ReviewStatus reviewStatus;

  @Column(columnDefinition = "text")
  private String owner;

  @Column(name = "last_updated")
  private OffsetDateTime lastUpdated;

  protected WikiPage() {}

  /** Creates published Wiki metadata from an approved Markdown file. */
  public static WikiPage publish(
      String id,
      String spaceId,
      String title,
      String markdownPath,
      String[] sourceDocumentIds,
      BigDecimal confidence,
      String owner,
      OffsetDateTime lastUpdated) {
    WikiPage page = new WikiPage();
    page.id = id;
    page.spaceId = spaceId;
    page.title = title;
    page.markdownPath = markdownPath;
    page.sourceDocumentIds = sourceDocumentIds == null ? new String[0] : sourceDocumentIds.clone();
    page.confidence = confidence;
    page.reviewStatus = ReviewStatus.PUBLISHED;
    page.owner = owner;
    page.lastUpdated = lastUpdated;
    return page;
  }

  /** Updates published metadata while preserving the stable page id. */
  public void republish(
      String title,
      String markdownPath,
      String[] sourceDocumentIds,
      BigDecimal confidence,
      String owner,
      OffsetDateTime lastUpdated) {
    this.title = title;
    this.markdownPath = markdownPath;
    this.sourceDocumentIds = sourceDocumentIds == null ? new String[0] : sourceDocumentIds.clone();
    this.confidence = confidence;
    this.reviewStatus = ReviewStatus.PUBLISHED;
    this.owner = owner;
    this.lastUpdated = lastUpdated;
  }

  public String getId() {
    return id;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public String getTitle() {
    return title;
  }

  public String getMarkdownPath() {
    return markdownPath;
  }

  public String[] getSourceDocumentIds() {
    return sourceDocumentIds == null ? new String[0] : sourceDocumentIds.clone();
  }

  public BigDecimal getConfidence() {
    return confidence;
  }

  public ReviewStatus getReviewStatus() {
    return reviewStatus;
  }

  public String getOwner() {
    return owner;
  }

  public OffsetDateTime getLastUpdated() {
    return lastUpdated;
  }
}
