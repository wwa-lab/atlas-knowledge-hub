package com.atlas.metadata.dto.mapping;

import com.atlas.metadata.adapter.VectorCapability;
import com.atlas.metadata.adapter.VectorQueryResult;
import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.domain.VectorItemResult;
import com.atlas.metadata.domain.VectorRun;
import com.atlas.metadata.dto.VectorCapabilityResponse;
import com.atlas.metadata.dto.VectorItemResultResponse;
import com.atlas.metadata.dto.VectorQueryMatchResponse;
import com.atlas.metadata.dto.VectorQueryResponse;
import com.atlas.metadata.dto.VectorRunResponse;
import com.atlas.metadata.dto.VectorRunSummaryResponse;
import com.atlas.metadata.enums.VectorReviewPolicy;
import java.util.List;
import java.util.Map;

/** Maps vector adapter and persistence models to DTOs. */
public final class VectorMapper {

  private VectorMapper() {}

  /** Converts vector capability metadata to response DTO. */
  public static VectorCapabilityResponse toResponse(VectorCapability capability) {
    return new VectorCapabilityResponse(
        capability.adapterKey(),
        capability.displayName(),
        capability.version(),
        capability.supportedDimensions(),
        capability.supportedOperations(),
        capability.defaultAdapter(),
        capability.status(),
        capability.maskedConfigSummary());
  }

  /** Converts vector run and per-item evidence to response DTO. */
  public static VectorRunResponse toResponse(
      VectorRun run, VectorRunSummaryResponse summary, List<VectorItemResult> results) {
    return new VectorRunResponse(
        run.getId(),
        run.getSpaceId(),
        run.getBatchId(),
        run.getAdapterKey(),
        run.getAdapterVersion(),
        run.getOperation(),
        run.getStatus(),
        run.getMode(),
        run.getReviewPolicy(),
        run.getDimension(),
        run.getRequestedBy(),
        run.getSafeMessage(),
        summary,
        results.stream().map(VectorMapper::toItemResponse).toList(),
        run.getStartedAt(),
        run.getCompletedAt());
  }

  /** Converts immediate query matches to response DTO. */
  public static VectorQueryResponse toQueryResponse(
      String spaceId,
      String adapterKey,
      String mode,
      VectorReviewPolicy reviewPolicy,
      int limit,
      String safeMessage,
      List<VectorQueryResult.VectorMatch> matches,
      Map<String, SourceChunk> chunksById) {
    return new VectorQueryResponse(
        spaceId,
        adapterKey,
        mode,
        reviewPolicy,
        limit,
        safeMessage,
        matches.stream()
            .filter(match -> chunksById.containsKey(match.sourceChunkId()))
            .map(match -> toMatchResponse(match, chunksById.get(match.sourceChunkId())))
            .toList());
  }

  private static VectorItemResultResponse toItemResponse(VectorItemResult result) {
    return new VectorItemResultResponse(
        result.getSourceChunkId(),
        result.getFileItemId(),
        result.getSourceFile(),
        result.getPage(),
        result.getSection(),
        result.getReviewStatus(),
        result.getConfidence(),
        result.getVectorItemKey(),
        result.getStatus(),
        result.getScore(),
        result.getSafeError(),
        result.getCreatedAt());
  }

  private static VectorQueryMatchResponse toMatchResponse(
      VectorQueryResult.VectorMatch match, SourceChunk chunk) {
    return new VectorQueryMatchResponse(
        chunk.getId(),
        chunk.getFileItemId(),
        chunk.getSourceFile(),
        chunk.getPage(),
        chunk.getSection(),
        chunk.getReviewStatus(),
        chunk.getConfidence(),
        match.vectorItemKey(),
        match.score(),
        match.safeMetadata());
  }
}
