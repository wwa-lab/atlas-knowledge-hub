package com.atlas.metadata.dto.mapping;

import com.atlas.metadata.domain.AskEvidence;
import com.atlas.metadata.domain.AskRun;
import com.atlas.metadata.domain.AskSession;
import com.atlas.metadata.dto.AskEvidenceResponse;
import com.atlas.metadata.dto.AskRunResponse;
import java.util.List;

/** Maps trusted ask domain records into safe API DTOs. */
public final class AskMapper {

  private AskMapper() {}

  public static AskRunResponse toResponse(AskRun run, List<AskEvidence> evidence) {
    return toResponse(run, null, evidence);
  }

  public static AskRunResponse toResponse(AskRun run, AskSession session, List<AskEvidence> evidence) {
    return new AskRunResponse(
        run.getId(),
        run.getSessionId(),
        session == null ? null : session.getTitle(),
        run.getSpaceId(),
        run.getQuestion(),
        run.getStatus(),
        run.getReviewPolicy(),
        run.getMode(),
        run.getRequestedBy(),
        run.getAnswer(),
        run.getAnswerConfidence(),
        run.getAnswerReviewStatus(),
        run.getModelRunId(),
        run.getSafeMessage(),
        evidence.stream().map(AskMapper::toEvidenceResponse).toList(),
        run.getCreatedAt(),
        run.getCompletedAt());
  }

  private static AskEvidenceResponse toEvidenceResponse(AskEvidence evidence) {
    return new AskEvidenceResponse(
        evidence.getId(),
        evidence.getCitationId(),
        evidence.getSourceChunkId(),
        evidence.getFileItemId(),
        evidence.getSourceFile(),
        evidence.getPage(),
        evidence.getSection(),
        evidence.getReviewStatus(),
        evidence.getConfidence(),
        evidence.getVectorItemKey(),
        evidence.getScore(),
        evidence.getEvidenceLabel(),
        evidence.getSourceLocator(),
        evidence.getCitationStatus(),
        evidence.isReviewEligible(),
        evidence.getExcludedReason(),
        evidence.getCreatedAt());
  }
}
