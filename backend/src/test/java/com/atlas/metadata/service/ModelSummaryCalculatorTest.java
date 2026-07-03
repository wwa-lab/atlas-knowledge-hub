package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.domain.ModelRunOutput;
import com.atlas.metadata.dto.ModelUsageResponse;
import com.atlas.metadata.enums.ModelOutputKind;
import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for model run usage summaries. */
class ModelSummaryCalculatorTest {

  @Test
  void computesOutputAndFailureCountsFromEvidence() {
    ModelSummaryCalculator calculator = new ModelSummaryCalculator();

    ModelUsageResponse summary =
        calculator.compute(
            12,
            34,
            List.of(
                output("ok", ModelOutputKind.TEXT_SUMMARY),
                output("failed", ModelOutputKind.ERROR)));

    assertThat(summary.promptUnits()).isEqualTo(12);
    assertThat(summary.completionUnits()).isEqualTo(34);
    assertThat(summary.outputCount()).isEqualTo(2);
    assertThat(summary.failedOutputCount()).isEqualTo(1);
  }

  private ModelRunOutput output(String id, ModelOutputKind kind) {
    return ModelRunOutput.create(
        id,
        "run",
        kind,
        "generated/model/" + id + ".json",
        "Mock summary",
        new String[0],
        null,
        null,
        new BigDecimal("0.820"),
        ReviewStatus.REVIEW_REQUIRED,
        kind == ModelOutputKind.ERROR ? "Mock failure" : null,
        OffsetDateTime.parse("2026-07-03T00:00:00Z"));
  }
}
