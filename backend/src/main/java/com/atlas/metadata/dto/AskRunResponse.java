package com.atlas.metadata.dto;

import com.atlas.metadata.enums.AskReviewPolicy;
import com.atlas.metadata.enums.AskRunStatus;
import com.atlas.metadata.enums.AnswerReviewStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/** Safe trusted ask run response with answer review state and source evidence. */
public record AskRunResponse(
    String runId,
    String sessionId,
    String sessionTitle,
    String spaceId,
    String question,
    AskRunStatus status,
    AskReviewPolicy reviewPolicy,
    String mode,
    String requestedBy,
    String answer,
    BigDecimal answerConfidence,
    AnswerReviewStatus answerReviewStatus,
    String answerReviewLabel,
    String answerReviewReason,
    String answerReviewedBy,
    OffsetDateTime answerReviewedAt,
    boolean answerReusable,
    String modelRunId,
    String safeMessage,
    List<AskEvidenceResponse> evidence,
    OffsetDateTime createdAt,
    OffsetDateTime completedAt) {}
