package com.atlas.metadata.dto;

import java.time.OffsetDateTime;
import java.util.List;

/** Safe Wiki generation or refresh run metadata response. */
public record WikiGenerationRunResponse(
    String id,
    String spaceId,
    String pageId,
    String status,
    String sourceMode,
    String refreshPolicy,
    String requestedBy,
    List<WikiReferenceResponse> inputSourceRefs,
    List<String> createdPageIds,
    List<String> updatedPageIds,
    List<String> issueIds,
    String safeSummary,
    String safeError,
    OffsetDateTime startedAt,
    OffsetDateTime finishedAt) {}
