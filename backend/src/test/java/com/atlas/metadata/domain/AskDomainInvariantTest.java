package com.atlas.metadata.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.atlas.metadata.enums.AskReviewPolicy;
import com.atlas.metadata.enums.AskRunStatus;
import com.atlas.metadata.enums.ReviewStatus;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

/** Domain invariant tests for trusted ask run lifecycle and review defaults. */
class AskDomainInvariantTest {

  @Test
  void askRunGeneratedAnswerIsAlwaysReviewRequired() {
    AskRun run =
        AskRun.create(
            "ask-001",
            "space-001",
            "What changed?",
            AskReviewPolicy.APPROVED_ONLY,
            "mock",
            "delivery-lead",
            OffsetDateTime.parse("2026-07-03T00:00:00Z"));

    run.markRetrieving();
    run.markGenerating();
    run.complete(
        AskRunStatus.SUCCEEDED,
        "Mock answer.",
        null,
        "model-run-001",
        "Completed.",
        OffsetDateTime.parse("2026-07-03T00:01:00Z"));

    assertThat(run.getStatus()).isEqualTo(AskRunStatus.SUCCEEDED);
    assertThat(run.getAnswerReviewStatus()).isEqualTo(ReviewStatus.REVIEW_REQUIRED);
  }

  @Test
  void terminalAskRunCannotBeChangedOrCompletedWithActiveStatus() {
    AskRun run =
        AskRun.create(
            "ask-001",
            "space-001",
            "What changed?",
            AskReviewPolicy.APPROVED_ONLY,
            "mock",
            "delivery-lead",
            OffsetDateTime.parse("2026-07-03T00:00:00Z"));

    assertThatThrownBy(
            () ->
                run.complete(
                    AskRunStatus.GENERATING,
                    null,
                    null,
                    null,
                    null,
                    OffsetDateTime.parse("2026-07-03T00:01:00Z")))
        .isInstanceOf(IllegalArgumentException.class);

    run.complete(
        AskRunStatus.NO_EVIDENCE,
        null,
        null,
        null,
        "No evidence.",
        OffsetDateTime.parse("2026-07-03T00:01:00Z"));

    assertThatThrownBy(run::markGenerating).isInstanceOf(IllegalStateException.class);
  }
}
