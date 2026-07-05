package com.atlas.metadata.dto.mapping;

import com.atlas.metadata.domain.WikiFolder;
import com.atlas.metadata.domain.WikiGenerationRun;
import com.atlas.metadata.domain.WikiLogEntry;
import com.atlas.metadata.domain.WikiPage;
import com.atlas.metadata.domain.WikiPageIssue;
import com.atlas.metadata.domain.WikiReference;
import com.atlas.metadata.dto.WikiFolderResponse;
import com.atlas.metadata.dto.WikiGenerationRunResponse;
import com.atlas.metadata.dto.WikiLogEntryResponse;
import com.atlas.metadata.dto.WikiPageResponse;
import com.atlas.metadata.dto.WikiPageIssueResponse;
import com.atlas.metadata.dto.WikiReferenceResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Maps Wiki page metadata entities to API responses. */
public final class WikiPageMapper {

  private WikiPageMapper() {}

  /** Converts one Wiki page entity to its response DTO. */
  public static WikiPageResponse toResponse(WikiPage page) {
    String[] sourceIds = page.getSourceDocumentIds();
    List<String> sourceDocumentIds = sourceIds.length == 0 ? List.of() : Arrays.asList(sourceIds);
    return new WikiPageResponse(
        page.getId(),
        page.getSpaceId(),
        page.getFolderId(),
        page.getTitle(),
        page.getSlug(),
        page.getPageType(),
        page.getMarkdownPath(),
        sourceDocumentIds,
        strings(page.getAliases()),
        references(page.getSourceRefs()),
        references(page.getChunkRefs()),
        strings(page.getInLinks()),
        strings(page.getOutLinks()),
        page.getVersion(),
        page.getSourceMode(),
        page.getRefreshPolicy(),
        page.getConfidence(),
        page.getReviewStatus(),
        page.getOwner(),
        page.getLastUpdated());
  }

  /** Converts one Wiki folder entity to its response DTO. */
  public static WikiFolderResponse toResponse(WikiFolder folder) {
    return new WikiFolderResponse(
        folder.getId(),
        folder.getSpaceId(),
        folder.getParentFolderId(),
        folder.getSlug(),
        folder.getName(),
        folder.getDescription(),
        folder.getSortOrder());
  }

  /** Converts one Wiki generation run entity to its response DTO. */
  public static WikiGenerationRunResponse toResponse(WikiGenerationRun run) {
    return new WikiGenerationRunResponse(
        run.getId(),
        run.getSpaceId(),
        run.getPageId(),
        run.getStatus(),
        run.getMode(),
        run.getSourceMode(),
        run.getRefreshPolicy(),
        run.getRequestedBy(),
        references(run.getInputSourceRefs()),
        strings(run.getCreatedPageIds()),
        strings(run.getUpdatedPageIds()),
        strings(run.getIssueIds()),
        run.getSafeSummary(),
        run.getSafeError(),
        run.getStartedAt(),
        run.getFinishedAt());
  }

  /** Converts one Wiki log entity to its response DTO. */
  public static WikiLogEntryResponse toResponse(WikiLogEntry log) {
    return new WikiLogEntryResponse(
        log.getId(),
        log.getSpaceId(),
        log.getPageId(),
        log.getRunId(),
        log.getEventType(),
        log.getActor(),
        log.getMessage(),
        Map.copyOf(log.getMetadata()),
        log.getCreatedAt());
  }

  /** Converts one Wiki page issue entity to its response DTO. */
  public static WikiPageIssueResponse toResponse(WikiPageIssue issue) {
    return new WikiPageIssueResponse(
        issue.getId(),
        issue.getSpaceId(),
        issue.getPageId(),
        issue.getIssueType(),
        issue.getSeverity(),
        issue.getStatus(),
        references(issue.getEvidenceRefs()),
        issue.getMessage(),
        issue.getCreatedAt(),
        issue.getResolvedAt());
  }

  private static List<WikiReferenceResponse> references(List<WikiReference> references) {
    if (references == null || references.isEmpty()) {
      return List.of();
    }
    return references.stream()
        .map(ref -> new WikiReferenceResponse(ref.type(), ref.id(), ref.label(), ref.locator()))
        .toList();
  }

  private static List<String> strings(String[] values) {
    return values == null || values.length == 0 ? List.of() : Arrays.asList(values);
  }
}
