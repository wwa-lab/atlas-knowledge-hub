package com.atlas.metadata.dto;

import java.time.OffsetDateTime;
import java.util.Map;

/** Safe Wiki lifecycle log response. */
public record WikiLogEntryResponse(
    String id,
    String spaceId,
    String pageId,
    String runId,
    String eventType,
    String actor,
    String message,
    Map<String, Object> metadata,
    OffsetDateTime createdAt) {}
