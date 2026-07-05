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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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

  @Column(name = "folder_id", columnDefinition = "text")
  private String folderId;

  @Column(nullable = false, columnDefinition = "text")
  private String title;

  @Column(nullable = false, columnDefinition = "text")
  private String slug;

  @Column(name = "page_type", nullable = false, columnDefinition = "text")
  private String pageType;

  @Column(name = "markdown_path", columnDefinition = "text")
  private String markdownPath;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "source_document_ids", columnDefinition = "text[]")
  private String[] sourceDocumentIds;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(columnDefinition = "text[]")
  private String[] aliases;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "source_refs", nullable = false, columnDefinition = "jsonb")
  private List<WikiReference> sourceRefs;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "chunk_refs", nullable = false, columnDefinition = "jsonb")
  private List<WikiReference> chunkRefs;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "in_links", columnDefinition = "text[]")
  private String[] inLinks;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "out_links", columnDefinition = "text[]")
  private String[] outLinks;

  @Column(nullable = false)
  private Integer version;

  @Column(name = "source_mode", nullable = false, columnDefinition = "text")
  private String sourceMode;

  @Column(name = "refresh_policy", nullable = false, columnDefinition = "text")
  private String refreshPolicy;

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
    return publish(
        id,
        spaceId,
        title,
        markdownPath,
        sourceDocumentIds,
        List.of(),
        confidence,
        owner,
        lastUpdated);
  }

  /** Creates published Wiki metadata from an approved Markdown file with source chunks. */
  public static WikiPage publish(
      String id,
      String spaceId,
      String title,
      String markdownPath,
      String[] sourceDocumentIds,
      List<SourceChunk> sourceChunks,
      BigDecimal confidence,
      String owner,
      OffsetDateTime lastUpdated) {
    WikiPage page = new WikiPage();
    page.id = id;
    page.spaceId = spaceId;
    page.folderId = null;
    page.title = title;
    page.slug = slugFrom(markdownPath, title, id);
    page.pageType = "SOURCE_SUMMARY";
    page.markdownPath = markdownPath;
    page.sourceDocumentIds = sourceDocumentIds == null ? new String[0] : sourceDocumentIds.clone();
    page.aliases = new String[0];
    page.sourceRefs = sourceReferences(page.sourceDocumentIds, markdownPath);
    page.chunkRefs = chunkReferences(sourceChunks);
    page.inLinks = new String[0];
    page.outLinks = new String[0];
    page.version = 1;
    page.sourceMode = "PUBLISHED_FILE";
    page.refreshPolicy = "MANUAL";
    page.confidence = confidence;
    page.reviewStatus = ReviewStatus.PUBLISHED;
    page.owner = owner;
    page.lastUpdated = lastUpdated;
    return page;
  }

  /** Creates a deterministic Auto Wiki candidate that must be reviewed before trust use. */
  public static WikiPage generatedCandidate(
      String id,
      String spaceId,
      String title,
      String slug,
      String markdownPath,
      String[] sourceDocumentIds,
      List<SourceChunk> sourceChunks,
      BigDecimal confidence,
      String owner,
      OffsetDateTime lastUpdated) {
    WikiPage page = new WikiPage();
    page.id = id;
    page.spaceId = spaceId;
    page.folderId = null;
    page.title = title;
    page.slug = slugFrom(null, slug, id);
    page.pageType = "TOPIC";
    page.markdownPath = markdownPath;
    page.sourceDocumentIds = sourceDocumentIds == null ? new String[0] : sourceDocumentIds.clone();
    page.aliases = new String[0];
    page.sourceRefs = sourceReferences(page.sourceDocumentIds, markdownPath);
    page.chunkRefs = chunkReferences(sourceChunks);
    page.inLinks = new String[0];
    page.outLinks = new String[0];
    page.version = 1;
    page.sourceMode = "AUTO_GENERATED";
    page.refreshPolicy = "ON_SOURCE_CHANGE";
    page.confidence = confidence;
    page.reviewStatus = ReviewStatus.REVIEW_REQUIRED;
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
    republish(title, markdownPath, sourceDocumentIds, List.of(), confidence, owner, lastUpdated);
  }

  /** Updates published metadata and refreshes safe source references. */
  public void republish(
      String title,
      String markdownPath,
      String[] sourceDocumentIds,
      List<SourceChunk> sourceChunks,
      BigDecimal confidence,
      String owner,
      OffsetDateTime lastUpdated) {
    this.title = title;
    this.slug = slug == null || slug.isBlank() ? slugFrom(markdownPath, title, id) : slug;
    this.pageType = pageType == null || pageType.isBlank() ? "SOURCE_SUMMARY" : pageType;
    this.markdownPath = markdownPath;
    this.sourceDocumentIds = sourceDocumentIds == null ? new String[0] : sourceDocumentIds.clone();
    this.aliases = aliases == null ? new String[0] : aliases.clone();
    this.sourceRefs = sourceReferences(this.sourceDocumentIds, markdownPath);
    this.chunkRefs = chunkReferences(sourceChunks);
    this.inLinks = inLinks == null ? new String[0] : inLinks.clone();
    this.outLinks = outLinks == null ? new String[0] : outLinks.clone();
    this.version = version == null || version < 1 ? 1 : version;
    this.sourceMode = sourceMode == null || sourceMode.isBlank() ? "PUBLISHED_FILE" : sourceMode;
    this.refreshPolicy = refreshPolicy == null || refreshPolicy.isBlank() ? "MANUAL" : refreshPolicy;
    this.confidence = confidence;
    this.reviewStatus = ReviewStatus.PUBLISHED;
    this.owner = owner;
    this.lastUpdated = lastUpdated;
  }

  /** Merges a deterministic Auto Wiki candidate while preserving the stable page id and slug. */
  public void mergeGeneratedCandidate(
      String title,
      String markdownPath,
      String[] sourceDocumentIds,
      List<SourceChunk> sourceChunks,
      BigDecimal confidence,
      String owner,
      OffsetDateTime lastUpdated) {
    this.title = title;
    this.pageType = "TOPIC";
    this.markdownPath = markdownPath;
    this.sourceDocumentIds = sourceDocumentIds == null ? new String[0] : sourceDocumentIds.clone();
    this.aliases = aliases == null ? new String[0] : aliases.clone();
    this.sourceRefs = sourceReferences(this.sourceDocumentIds, markdownPath);
    this.chunkRefs = chunkReferences(sourceChunks);
    this.inLinks = inLinks == null ? new String[0] : inLinks.clone();
    this.outLinks = outLinks == null ? new String[0] : outLinks.clone();
    this.version = version == null || version < 1 ? 1 : version + 1;
    this.sourceMode = "AUTO_GENERATED";
    this.refreshPolicy = "ON_SOURCE_CHANGE";
    this.confidence = confidence;
    this.reviewStatus = ReviewStatus.REVIEW_REQUIRED;
    this.owner = owner;
    this.lastUpdated = lastUpdated;
  }

  /** Applies deterministic Wiki link metadata while preserving trust and source evidence fields. */
  public void applyLinkMetadata(
      String[] inLinks, String[] outLinks, boolean artifactChanged, OffsetDateTime lastUpdated) {
    String[] nextInLinks = inLinks == null ? new String[0] : inLinks.clone();
    String[] nextOutLinks = outLinks == null ? new String[0] : outLinks.clone();
    boolean changed =
        artifactChanged
            || !Arrays.equals(this.inLinks == null ? new String[0] : this.inLinks, nextInLinks)
            || !Arrays.equals(this.outLinks == null ? new String[0] : this.outLinks, nextOutLinks);
    this.inLinks = nextInLinks;
    this.outLinks = nextOutLinks;
    if (changed) {
      this.version = version == null || version < 1 ? 1 : version + 1;
      this.lastUpdated = lastUpdated;
    }
  }

  public String getId() {
    return id;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public String getFolderId() {
    return folderId;
  }

  public String getTitle() {
    return title;
  }

  public String getSlug() {
    return slug;
  }

  public String getPageType() {
    return pageType;
  }

  public String getMarkdownPath() {
    return markdownPath;
  }

  public String[] getSourceDocumentIds() {
    return sourceDocumentIds == null ? new String[0] : sourceDocumentIds.clone();
  }

  public String[] getAliases() {
    return aliases == null ? new String[0] : aliases.clone();
  }

  public List<WikiReference> getSourceRefs() {
    return sourceRefs == null ? List.of() : List.copyOf(sourceRefs);
  }

  public List<WikiReference> getChunkRefs() {
    return chunkRefs == null ? List.of() : List.copyOf(chunkRefs);
  }

  public String[] getInLinks() {
    return inLinks == null ? new String[0] : inLinks.clone();
  }

  public String[] getOutLinks() {
    return outLinks == null ? new String[0] : outLinks.clone();
  }

  public Integer getVersion() {
    return version == null ? 1 : version;
  }

  public String getSourceMode() {
    return sourceMode == null ? "PUBLISHED_FILE" : sourceMode;
  }

  public String getRefreshPolicy() {
    return refreshPolicy == null ? "MANUAL" : refreshPolicy;
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

  private static List<WikiReference> sourceReferences(String[] sourceDocumentIds, String markdownPath) {
    if (sourceDocumentIds == null || sourceDocumentIds.length == 0) {
      return List.of();
    }
    return Arrays.stream(sourceDocumentIds)
        .filter(id -> id != null && !id.isBlank())
        .map(id -> new WikiReference("FILE", id, id, markdownPath))
        .toList();
  }

  private static List<WikiReference> chunkReferences(List<SourceChunk> sourceChunks) {
    if (sourceChunks == null || sourceChunks.isEmpty()) {
      return List.of();
    }
    return sourceChunks.stream()
        .map(
            chunk ->
                new WikiReference(
                    "SOURCE_CHUNK",
                    chunk.getId(),
                    "source chunk",
                    chunk.getPage() == null ? chunk.getSection() : "page " + chunk.getPage()))
        .toList();
  }

  private static String slugFrom(String markdownPath, String title, String id) {
    String candidate = firstPresent(fileStem(markdownPath), title, id);
    String slug =
        candidate
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-|-$", "");
    return slug.isBlank() ? "wiki-page" : slug;
  }

  private static String fileStem(String path) {
    if (path == null || path.isBlank()) {
      return "";
    }
    String name = path.replace('\\', '/');
    int slash = name.lastIndexOf('/');
    if (slash >= 0) {
      name = name.substring(slash + 1);
    }
    int dot = name.lastIndexOf('.');
    return dot > 0 ? name.substring(0, dot) : name;
  }

  private static String firstPresent(String first, String second, String third) {
    if (first != null && !first.isBlank()) {
      return first;
    }
    if (second != null && !second.isBlank()) {
      return second;
    }
    return third == null ? "" : third;
  }
}
