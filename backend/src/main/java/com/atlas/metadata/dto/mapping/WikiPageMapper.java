package com.atlas.metadata.dto.mapping;

import com.atlas.metadata.domain.WikiPage;
import com.atlas.metadata.dto.WikiPageResponse;
import java.util.Arrays;
import java.util.List;

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
        page.getTitle(),
        page.getMarkdownPath(),
        sourceDocumentIds,
        page.getConfidence(),
        page.getReviewStatus(),
        page.getOwner(),
        page.getLastUpdated());
  }
}
