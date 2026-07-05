package com.atlas.metadata.dto;

/** Minimal Wiki folder response. */
public record WikiFolderResponse(
    String id,
    String spaceId,
    String parentFolderId,
    String slug,
    String name,
    String description,
    Integer sortOrder) {}
