package com.atlas.metadata.dto;

/** Summary counts for graph views and projection runs. */
public record GraphSummaryResponse(int createdCount, int updatedCount, int skippedCount, int failedCount) {}
