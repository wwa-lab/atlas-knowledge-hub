package com.atlas.metadata.dto;

/** Usage and output count summary for a model run. */
public record ModelUsageResponse(
    int promptUnits, int completionUnits, int outputCount, int failedOutputCount) {}
