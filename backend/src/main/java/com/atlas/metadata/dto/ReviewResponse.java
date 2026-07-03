package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ReviewAction;
import java.time.OffsetDateTime;
import java.util.List;

/** Append-only review record API response. */
public record ReviewResponse(
    Long id,
    String targetType,
    String targetId,
    ReviewAction action,
    String reviewer,
    String comment,
    List<String> affectedChunks,
    OffsetDateTime createdAt) {}
