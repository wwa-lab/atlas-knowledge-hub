package com.atlas.metadata.dto.mapping;

import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.dto.FileItemResponse;

/** Maps file item entities to DTOs. */
public final class FileItemMapper {

  private FileItemMapper() {}

  /** Converts a file item entity to its response DTO. */
  public static FileItemResponse toResponse(FileItem item) {
    return new FileItemResponse(
        item.getId(),
        item.getBatchId(),
        item.getSourcePath(),
        item.getSourceType(),
        item.getStatus(),
        item.getConfidence(),
        item.getReviewStatus(),
        item.getPdfPath(),
        item.getMarkdownPath(),
        item.getAssetsPath(),
        item.getErrorMessage());
  }
}
