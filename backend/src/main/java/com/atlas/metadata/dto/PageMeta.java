package com.atlas.metadata.dto;

/** Pagination metadata for list endpoints. */
public record PageMeta(int page, int size, long total) {}
