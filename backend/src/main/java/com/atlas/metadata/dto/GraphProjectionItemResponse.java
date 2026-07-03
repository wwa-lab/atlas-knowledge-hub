package com.atlas.metadata.dto;

import com.atlas.metadata.enums.GraphProjectionItemStatus;

/** Per-item projection outcome response. */
public record GraphProjectionItemResponse(
    String id,
    String sourceType,
    String sourceId,
    String targetType,
    String targetId,
    GraphProjectionItemStatus status,
    String reasonCode) {}
