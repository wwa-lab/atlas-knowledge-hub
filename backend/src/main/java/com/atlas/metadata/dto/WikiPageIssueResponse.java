package com.atlas.metadata.dto;

import java.time.OffsetDateTime;
import java.util.List;

/** Safe Wiki page issue response for future lint and freshness workflows. */
public record WikiPageIssueResponse(
    String id,
    String spaceId,
    String pageId,
    String issueType,
    String severity,
    String status,
    List<WikiReferenceResponse> evidenceRefs,
    String message,
    OffsetDateTime createdAt,
    OffsetDateTime resolvedAt) {}
