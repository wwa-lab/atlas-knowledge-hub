package com.atlas.metadata.dto;

import java.time.OffsetDateTime;
import java.util.List;

/** Safe response for one deterministic Wiki linkify/lint run. */
public record WikiLinkifyLintRunResponse(
    String runId,
    String spaceId,
    String status,
    String mode,
    int scannedPageCount,
    List<String> updatedPageIds,
    List<String> issueIds,
    int insertedLinkCount,
    int brokenLinkCount,
    int orphanPageCount,
    int sourceIssueCount,
    int thinContentCount,
    String safeSummary,
    String safeError,
    OffsetDateTime startedAt,
    OffsetDateTime finishedAt) {}
