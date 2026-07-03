package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.domain.VectorItemResult;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.VectorItemStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for vector run summary calculation. */
class VectorSummaryCalculatorTest {

  @Test
  void computesVectorItemStatusCounts() {
    VectorSummaryCalculator calculator = new VectorSummaryCalculator();

    var summary =
        calculator.compute(
            List.of(
                result("a", VectorItemStatus.INDEXED),
                result("b", VectorItemStatus.DELETED),
                result("c", VectorItemStatus.SKIPPED),
                result("d", VectorItemStatus.FAILED)));

    assertThat(summary.totalCount()).isEqualTo(4);
    assertThat(summary.indexedCount()).isEqualTo(1);
    assertThat(summary.deletedCount()).isEqualTo(1);
    assertThat(summary.skippedCount()).isEqualTo(1);
    assertThat(summary.failedCount()).isEqualTo(1);
  }

  private VectorItemResult result(String chunkId, VectorItemStatus status) {
    return VectorItemResult.create(
        "result-" + chunkId,
        "run",
        chunkId,
        "file",
        "BRD.pdf",
        1,
        "Overview",
        ReviewStatus.APPROVED,
        new BigDecimal("0.900"),
        null,
        status,
        null,
        null,
        OffsetDateTime.parse("2026-07-03T00:00:00Z"));
  }
}
