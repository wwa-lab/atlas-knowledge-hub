package com.atlas.metadata.dto;

/** Vector run count summary. */
public record VectorRunSummaryResponse(
    int totalCount, int indexedCount, int deletedCount, int skippedCount, int failedCount) {}
