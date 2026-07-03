package com.atlas.metadata.dto.mapping;

import com.atlas.metadata.adapter.ParserCapability;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.ParserFileResult;
import com.atlas.metadata.domain.ParserRun;
import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.dto.ParserCapabilityResponse;
import com.atlas.metadata.dto.ParserChunkResponse;
import com.atlas.metadata.dto.ParserFileResultResponse;
import com.atlas.metadata.dto.ParserRunResponse;
import com.atlas.metadata.dto.ParserRunSummaryResponse;
import java.util.List;
import java.util.Map;

/** Maps parser adapter and persistence models to DTOs. */
public final class ParserMapper {

  private ParserMapper() {}

  /** Converts capability metadata to its response DTO. */
  public static ParserCapabilityResponse toResponse(ParserCapability capability) {
    return new ParserCapabilityResponse(
        capability.adapterKey(),
        capability.displayName(),
        capability.version(),
        capability.inputTypes(),
        capability.outputTypes(),
        capability.defaultAdapter(),
        capability.status(),
        capability.lowConfidenceThreshold(),
        capability.maskedConfigSummary());
  }

  /** Converts run evidence, per-file evidence, and chunks to a response DTO. */
  public static ParserRunResponse toResponse(
      ParserRun run,
      ParserRunSummaryResponse summary,
      List<ParserFileResult> results,
      Map<String, FileItem> filesById,
      List<SourceChunk> chunks) {
    return new ParserRunResponse(
        run.getId(),
        run.getBatchId(),
        run.getAdapterKey(),
        run.getStatus(),
        run.getSafeMessage(),
        summary,
        results.stream().map(result -> toFileResult(result, filesById.get(result.getFileItemId()))).toList(),
        chunks.stream().map(ParserMapper::toChunkResponse).toList(),
        run.getStartedAt(),
        run.getCompletedAt());
  }

  private static ParserFileResultResponse toFileResult(ParserFileResult result, FileItem fileItem) {
    return new ParserFileResultResponse(
        result.getFileItemId(),
        result.getSourcePath(),
        result.getPdfPath(),
        result.getStatus(),
        result.getMarkdownPath(),
        result.getAssetsPath(),
        result.getConfidence(),
        fileItem == null ? null : fileItem.getReviewStatus(),
        result.getChunkCount(),
        result.isSkipped(),
        result.getSafeError());
  }

  private static ParserChunkResponse toChunkResponse(SourceChunk chunk) {
    return new ParserChunkResponse(
        chunk.getId(),
        chunk.getFileItemId(),
        chunk.getSourceFile(),
        chunk.getPage(),
        chunk.getSection(),
        chunk.getConfidence(),
        chunk.getReviewStatus());
  }
}
