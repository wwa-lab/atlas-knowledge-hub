package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.domain.AskEvidence;
import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for ask confidence summary calculation. */
class AskSummaryCalculatorTest {

  private final AskSummaryCalculator calculator = new AskSummaryCalculator();

  @Test
  void usesModelConfidenceWhenProvided() {
    assertThat(calculator.answerConfidence(new BigDecimal("0.8214"), List.of()))
        .isEqualByComparingTo("0.821");
  }

  @Test
  void fallsBackToAverageEvidenceScore() {
    assertThat(
            calculator.answerConfidence(
                null, List.of(evidence("ev-1", "0.900"), evidence("ev-2", "0.800"))))
        .isEqualByComparingTo("0.850");
  }

  private AskEvidence evidence(String id, String score) {
    return AskEvidence.create(
        id,
        "ask-001",
        "chunk-" + id,
        "file-001",
        "Source.pdf",
        1,
        "Scope",
        ReviewStatus.APPROVED,
        new BigDecimal("0.910"),
        "vector/" + id,
        new BigDecimal(score),
        OffsetDateTime.parse("2026-07-03T00:00:00Z"));
  }
}
