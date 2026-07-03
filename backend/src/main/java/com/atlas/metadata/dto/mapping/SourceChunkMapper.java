package com.atlas.metadata.dto.mapping;

import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.dto.SourceChunkResponse;

/** Maps source chunk entities to DTOs. */
public final class SourceChunkMapper {

  private SourceChunkMapper() {}

  /** Converts a source chunk entity to its response DTO. */
  public static SourceChunkResponse toResponse(SourceChunk chunk) {
    return new SourceChunkResponse(
        chunk.getId(),
        chunk.getFileItemId(),
        chunk.getSourceFile(),
        chunk.getPage(),
        chunk.getSection(),
        chunk.getConfidence(),
        chunk.getReviewStatus());
  }
}
