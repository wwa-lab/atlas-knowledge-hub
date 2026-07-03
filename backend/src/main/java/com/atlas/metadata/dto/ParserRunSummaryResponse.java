package com.atlas.metadata.dto;

/** Derived parser run result counts. */
public record ParserRunSummaryResponse(
    int total,
    int markdownGenerated,
    int lowConfidence,
    int ocrRequired,
    int failed,
    int skipped,
    int unsupported) {}
