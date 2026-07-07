package com.atlas.metadata.dto;

import java.time.OffsetDateTime;
import java.util.List;

/** Trusted Ask session detail with ordered answer history. */
public record AskSessionDetailResponse(
    String sessionId,
    String spaceId,
    String title,
    String createdBy,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<AskRunResponse> runs) {}
