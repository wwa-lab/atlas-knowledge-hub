package com.atlas.metadata.dto;

/** Derived batch metrics computed from file items. */
public record BatchMetricsResponse(
    long total,
    long pdfConverted,
    long markdownGenerated,
    long reviewRequired,
    long failed,
    long unsupported) {}
