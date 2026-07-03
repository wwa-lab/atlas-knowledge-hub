package com.atlas.metadata.dto;

/** Derived storage operation outcome counts. */
public record StorageOperationSummaryResponse(
    int total, int stored, int deleted, int missing, int failed, int skipped, long totalBytes) {}
