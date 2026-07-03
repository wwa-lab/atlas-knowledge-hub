package com.atlas.metadata.dto.mapping;

import com.atlas.metadata.domain.Space;
import com.atlas.metadata.dto.SpaceResponse;

/** Maps Knowledge Space entities to DTOs. */
public final class SpaceMapper {

  private SpaceMapper() {}

  /** Converts a space entity to its response DTO. */
  public static SpaceResponse toResponse(Space space) {
    return new SpaceResponse(
        space.getId(),
        space.getName(),
        space.getDescription(),
        space.getType(),
        space.getIndexStrategy(),
        space.getOwner(),
        space.getStatus(),
        space.getDocumentCount(),
        space.getWikiPageCount(),
        space.getReviewCount(),
        space.getCreatedAt(),
        space.getUpdatedAt());
  }
}
