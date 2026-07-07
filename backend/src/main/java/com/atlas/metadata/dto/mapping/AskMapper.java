package com.atlas.metadata.dto.mapping;

import com.atlas.metadata.domain.AskEvidence;
import com.atlas.metadata.domain.AskRun;
import com.atlas.metadata.domain.AskSession;
import com.atlas.metadata.dto.AskEvidenceResponse;
import com.atlas.metadata.dto.AskRunResponse;
import com.atlas.metadata.enums.AnswerReviewStatus;
import java.util.List;

/** Maps trusted ask domain records into safe API DTOs. */
public final class AskMapper {

  private AskMapper() {}

  public static AskRunResponse toResponse(AskRun run, List<AskEvidence> evidence) {
    return toResponse(run, null, evidence);
  }

  public static AskRunResponse toResponse(AskRun run, AskSession session, List<AskEvidence> evidence) {
    List<AskEvidenceResponse> evidenceResponses = evidence.stream().map(AskMapper::toEvidenceResponse).toList();
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
        answerReviewLabel(run.getAnswerReviewStatus()),
        run.getAnswerReviewReason(),
        run.getAnswerReviewedBy(),
        run.getAnswerReviewedAt(),
        answerReusable(run, evidence),
        run.getModelRunId(),
        run.getSafeMessage(),
        evidenceResponses,
        run.getCreatedAt(),
        run.getCompletedAt());
  }

  private static String answerReviewLabel(AnswerReviewStatus status) {
    return switch (status) {
      case APPROVED -> "Approved answer";
      case REJECTED -> "Rejected answer";
      case NEEDS_REVISION -> "Needs revision";
      case REVIEW_REQUIRED -> "Review required";
    };
  }

  private static boolean answerReusable(AskRun run, List<AskEvidence> evidence) {
    return run.getAnswerReviewStatus() == AnswerReviewStatus.APPROVED
        && run.getAnswer() != null
        && !run.getAnswer().isBlank()
        && evidence.stream().anyMatch(AskEvidence::isReviewEligible);
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
