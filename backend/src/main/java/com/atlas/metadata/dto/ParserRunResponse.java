package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ParserRunStatus;
import java.time.OffsetDateTime;
import java.util.List;

/** Parser run summary, per-file report, and source chunks. */
public record ParserRunResponse(
    String runId,
    String batchId,
    String adapterKey,
    ParserRunStatus status,
    String safeMessage,
    ParserRunSummaryResponse summary,
    List<ParserFileResultResponse> results,
    List<ParserChunkResponse> chunks,
    OffsetDateTime startedAt,
    OffsetDateTime completedAt) {}
