package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ConversionRunStatus;
import java.time.OffsetDateTime;
import java.util.List;

/** Conversion run summary and per-file report. */
public record ConversionRunResponse(
    String runId,
    String batchId,
    String adapterKey,
    ConversionRunStatus status,
    String safeMessage,
    ConversionRunSummaryResponse summary,
    List<ConversionFileResultResponse> results,
    OffsetDateTime startedAt,
    OffsetDateTime completedAt) {}
