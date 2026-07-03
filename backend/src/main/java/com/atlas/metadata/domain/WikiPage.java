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

/** Deferred wiki page metadata table entity; no endpoint in this slice. */
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
}
