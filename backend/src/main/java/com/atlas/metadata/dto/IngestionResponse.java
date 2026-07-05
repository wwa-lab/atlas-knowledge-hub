package com.atlas.metadata.dto;

import java.util.List;

/** Response for the real upload ingestion closed loop. */
public record IngestionResponse(
    BatchResponse batch,
    List<FileItemResponse> files,
    List<SourceChunkResponse> chunks,
    ParserRunResponse parserRun,
    String message) {}
