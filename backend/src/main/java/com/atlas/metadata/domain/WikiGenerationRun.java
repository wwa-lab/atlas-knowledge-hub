package com.atlas.metadata.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Safe metadata record for future Wiki generation or refresh runs. */
@Entity
@Table(name = "wiki_generation_run", schema = "atlas")
public class WikiGenerationRun {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "space_id", nullable = false, columnDefinition = "text")
  private String spaceId;

  @Column(name = "page_id", columnDefinition = "text")
  private String pageId;

  @Column(nullable = false, columnDefinition = "text")
  private String status;

  @Column(nullable = false, columnDefinition = "text")
  private String mode;

  @Column(name = "source_mode", nullable = false, columnDefinition = "text")
  private String sourceMode;

  @Column(name = "refresh_policy", nullable = false, columnDefinition = "text")
  private String refreshPolicy;

  @Column(name = "requested_by", columnDefinition = "text")
  private String requestedBy;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "input_source_refs", nullable = false, columnDefinition = "jsonb")
  private List<WikiReference> inputSourceRefs;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "created_page_ids", columnDefinition = "text[]")
  private String[] createdPageIds;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "updated_page_ids", columnDefinition = "text[]")
  private String[] updatedPageIds;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "issue_ids", columnDefinition = "text[]")
  private String[] issueIds;

  @Column(name = "safe_summary", columnDefinition = "text")
  private String safeSummary;

  @Column(name = "safe_error", columnDefinition = "text")
  private String safeError;

  @Column(name = "eligible_chunk_count", nullable = false)
  private Integer eligibleChunkCount;

  @Column(name = "excluded_chunk_count", nullable = false)
  private Integer excludedChunkCount;

  @Column(name = "started_at")
  private OffsetDateTime startedAt;

  @Column(name = "finished_at")
  private OffsetDateTime finishedAt;

  protected WikiGenerationRun() {}

  /** Creates a completed deterministic Auto Wiki ingest run metadata record. */
  public static WikiGenerationRun ingest(
      String id,
      String spaceId,
      String status,
      String mode,
      String requestedBy,
      List<WikiReference> inputSourceRefs,
      String[] createdPageIds,
      String[] updatedPageIds,
      String[] issueIds,
      Integer eligibleChunkCount,
      Integer excludedChunkCount,
      String safeSummary,
      String safeError,
      OffsetDateTime startedAt,
      OffsetDateTime finishedAt) {
    WikiGenerationRun run = new WikiGenerationRun();
    run.id = id;
    run.spaceId = spaceId;
    run.pageId = firstPageId(createdPageIds, updatedPageIds);
    run.status = status;
    run.mode = mode == null || mode.isBlank() ? "deterministic" : mode;
    run.sourceMode = "AUTO_GENERATED";
    run.refreshPolicy = "ON_SOURCE_CHANGE";
    run.requestedBy = requestedBy;
    run.inputSourceRefs = inputSourceRefs == null ? List.of() : List.copyOf(inputSourceRefs);
    run.createdPageIds = createdPageIds == null ? new String[0] : createdPageIds.clone();
    run.updatedPageIds = updatedPageIds == null ? new String[0] : updatedPageIds.clone();
    run.issueIds = issueIds == null ? new String[0] : issueIds.clone();
    run.eligibleChunkCount = eligibleChunkCount == null ? 0 : eligibleChunkCount;
    run.excludedChunkCount = excludedChunkCount == null ? 0 : excludedChunkCount;
    run.safeSummary = safeSummary;
    run.safeError = safeError;
    run.startedAt = startedAt;
    run.finishedAt = finishedAt;
    return run;
  }

  /** Creates a completed deterministic Wiki maintenance run metadata record. */
  public static WikiGenerationRun maintenance(
      String id,
      String spaceId,
      String status,
      String mode,
      String requestedBy,
      String[] updatedPageIds,
      String[] issueIds,
      String safeSummary,
      String safeError,
      OffsetDateTime startedAt,
      OffsetDateTime finishedAt) {
    WikiGenerationRun run = new WikiGenerationRun();
    run.id = id;
    run.spaceId = spaceId;
    run.pageId = firstPageId(null, updatedPageIds);
    run.status = status;
    run.mode = mode == null || mode.isBlank() ? "linkify-lint" : mode;
    run.sourceMode = "AUTO_GENERATED";
    run.refreshPolicy = "ON_SOURCE_CHANGE";
    run.requestedBy = requestedBy;
    run.inputSourceRefs = List.of();
    run.createdPageIds = new String[0];
    run.updatedPageIds = updatedPageIds == null ? new String[0] : updatedPageIds.clone();
    run.issueIds = issueIds == null ? new String[0] : issueIds.clone();
    run.eligibleChunkCount = 0;
    run.excludedChunkCount = 0;
    run.safeSummary = safeSummary;
    run.safeError = safeError;
    run.startedAt = startedAt;
    run.finishedAt = finishedAt;
    return run;
  }

  public String getId() {
    return id;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public String getPageId() {
    return pageId;
  }

  public String getStatus() {
    return status;
  }

  public String getMode() {
    return mode == null || mode.isBlank() ? "deterministic" : mode;
  }

  public String getSourceMode() {
    return sourceMode;
  }

  public String getRefreshPolicy() {
    return refreshPolicy;
  }

  public String getRequestedBy() {
    return requestedBy;
  }

  public List<WikiReference> getInputSourceRefs() {
    return inputSourceRefs == null ? List.of() : List.copyOf(inputSourceRefs);
  }

  public String[] getCreatedPageIds() {
    return createdPageIds == null ? new String[0] : createdPageIds.clone();
  }

  public String[] getUpdatedPageIds() {
    return updatedPageIds == null ? new String[0] : updatedPageIds.clone();
  }

  public String[] getIssueIds() {
    return issueIds == null ? new String[0] : issueIds.clone();
  }

  public String getSafeSummary() {
    return safeSummary;
  }

  public String getSafeError() {
    return safeError;
  }

  public Integer getEligibleChunkCount() {
    return eligibleChunkCount == null ? 0 : eligibleChunkCount;
  }

  public Integer getExcludedChunkCount() {
    return excludedChunkCount == null ? 0 : excludedChunkCount;
  }

  public OffsetDateTime getStartedAt() {
    return startedAt;
  }

  public OffsetDateTime getFinishedAt() {
    return finishedAt;
  }

  private static String firstPageId(String[] createdPageIds, String[] updatedPageIds) {
    if (createdPageIds != null && createdPageIds.length > 0) {
      return createdPageIds[0];
    }
    if (updatedPageIds != null && updatedPageIds.length > 0) {
      return updatedPageIds[0];
    }
    return null;
  }
}
