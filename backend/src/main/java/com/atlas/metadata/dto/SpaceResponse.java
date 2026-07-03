package com.atlas.metadata.dto;

import com.atlas.metadata.enums.IndexStrategy;
import com.atlas.metadata.enums.SpaceStatus;
import com.atlas.metadata.enums.SpaceType;
import java.time.OffsetDateTime;

/** Knowledge Space API response. */
public record SpaceResponse(
    String id,
    String name,
    String description,
    SpaceType type,
    IndexStrategy indexStrategy,
    String owner,
    SpaceStatus status,
    int documentCount,
    int wikiPageCount,
    int reviewCount,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {}
