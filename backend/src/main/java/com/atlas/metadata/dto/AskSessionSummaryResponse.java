package com.atlas.metadata.dto;

import com.atlas.metadata.enums.AskRunStatus;
import com.atlas.metadata.enums.ReviewStatus;
import java.time.OffsetDateTime;

/** Recent Trusted Ask session summary for a Knowledge Space. */
public record AskSessionSummaryResponse(
    String sessionId,
    String spaceId,
    String title,
    String createdBy,
    int runCount,
    AskRunStatus latestStatus,
    ReviewStatus latestAnswerReviewStatus,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {}
