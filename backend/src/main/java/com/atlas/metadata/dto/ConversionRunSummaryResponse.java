package com.atlas.metadata.dto;

/** Derived conversion run result counts. */
public record ConversionRunSummaryResponse(
    int total,
    int pdfConverted,
    int pdfConvertFailed,
    int ocrRequired,
    int unsupported,
    int skipped) {}
