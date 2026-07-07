package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.domain.AskEvidence;
import com.atlas.metadata.domain.AskRun;
import com.atlas.metadata.dto.RetrievalRunQualityMetricsResponse;
import com.atlas.metadata.enums.AskCitationStatus;
import com.atlas.metadata.enums.AskReviewPolicy;
import com.atlas.metadata.enums.AskRunStatus;
import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for safe retrieval quality metric calculation. */
class RetrievalQualityMetricsCalculatorTest {

  private final RetrievalQualityMetricsCalculator calculator = new RetrievalQualityMetricsCalculator();
  private final OffsetDateTime now = OffsetDateTime.parse("2026-07-07T00:00:00Z");

  @Test
  void computesDeterministicHealthyRunMetrics() {
    AskRun run = succeededRun("ask-1", new BigDecimal("0.880"));
    List<AskEvidence> evidence =
        List.of(
            evidence("ev-1", ReviewStatus.APPROVED, new BigDecimal("0.930"), AskCitationStatus.ELIGIBLE, true),
            evidence("ev-2", ReviewStatus.APPROVED, new BigDecimal("0.890"), AskCitationStatus.ELIGIBLE, true));

    RetrievalRunQualityMetricsResponse first = calculator.compute(run, evidence);
    RetrievalRunQualityMetricsResponse second = calculator.compute(run, evidence);

    assertThat(first).isEqualTo(second);
    assertThat(first.evidenceCoverage().evidenceCount()).isEqualTo(2);
    assertThat(first.evidenceCoverage().coverageRatio()).isEqualByComparingTo("1.000");
    assertThat(first.citationHealth().unhealthy()).isFalse();
    assertThat(first.confidence().averageEvidenceConfidence()).isEqualByComparingTo("0.910");
    assertThat(first.confidence().band()).isEqualTo("HIGH");
    assertThat(first.reviewEligibility().reviewEligible()).isTrue();
    assertThat(first.safeDiagnostics()).isEmpty();
  }

  @Test
  void noEvidenceRunIsARefusalAndNeverReviewEligible() {
    AskRun run = terminalRun("ask-empty", AskRunStatus.NO_EVIDENCE, null);

    RetrievalRunQualityMetricsResponse metrics = calculator.compute(run, List.of());

    assertThat(metrics.noEvidenceRefusal()).isTrue();
    assertThat(metrics.evidenceCoverage().missingEvidence()).isTrue();
    assertThat(metrics.reviewEligibility().reviewEligible()).isFalse();
    assertThat(metrics.reviewEligibility().reasons()).contains("NO_EVIDENCE", "LOW_CONFIDENCE");
  }

  @Test
  void reviewRequiredEvidenceAndLowConfidenceBlockEligibility() {
    AskRun run = succeededRun("ask-review", new BigDecimal("0.690"));

    RetrievalRunQualityMetricsResponse metrics =
        calculator.compute(
            run,
            List.of(
                evidence(
                    "ev-review",
                    ReviewStatus.REVIEW_REQUIRED,
                    new BigDecimal("0.650"),
                    AskCitationStatus.REVIEW_REQUIRED,
                    false)));

    assertThat(metrics.confidence().band()).isEqualTo("LOW");
    assertThat(metrics.citationHealth().unhealthy()).isTrue();
    assertThat(metrics.reviewEligibility().reviewEligible()).isFalse();
    assertThat(metrics.reviewEligibility().reasons())
        .contains("REVIEW_REQUIRED_EVIDENCE", "LOW_CONFIDENCE", "UNHEALTHY_CITATIONS");
  }

  @Test
  void summarizesSpaceMetricsWithoutRawContent() {
    AskRun healthy = succeededRun("ask-healthy", new BigDecimal("0.900"));
    AskRun noEvidence = terminalRun("ask-no-evidence", AskRunStatus.NO_EVIDENCE, null);
    AskRun failed = terminalRun("ask-failed", AskRunStatus.FAILED, null);

    var summary =
        calculator.summarize(
            "space",
            List.of(
                calculator.compute(
                    healthy,
                    List.of(
                        evidence(
                            "ev-healthy",
                            ReviewStatus.APPROVED,
                            new BigDecimal("0.900"),
                            AskCitationStatus.ELIGIBLE,
                            true))),
                calculator.compute(noEvidence, List.of()),
                calculator.compute(failed, List.of())),
            now);

    assertThat(summary.totalRuns()).isEqualTo(3);
    assertThat(summary.succeededRuns()).isEqualTo(1);
    assertThat(summary.noEvidenceRefusals()).isEqualTo(1);
    assertThat(summary.failedRuns()).isEqualTo(1);
    assertThat(summary.reviewEligibleRuns()).isEqualTo(1);
    assertThat(summary.generatedAt()).isEqualTo(now);
  }

  private AskRun succeededRun(String id, BigDecimal confidence) {
    return terminalRun(id, AskRunStatus.SUCCEEDED, confidence);
  }

  private AskRun terminalRun(String id, AskRunStatus status, BigDecimal confidence) {
    AskRun run =
        AskRun.create(id, "space", "session", "Safe sample question?", AskReviewPolicy.APPROVED_ONLY, "mock", "tester", now);
    run.complete(status, status == AskRunStatus.NO_EVIDENCE ? "No approved evidence was found." : "Safe answer.", confidence, null, null, now);
    return run;
  }

  private AskEvidence evidence(
      String id,
      ReviewStatus reviewStatus,
      BigDecimal confidence,
      AskCitationStatus citationStatus,
      boolean reviewEligible) {
    return AskEvidence.create(
        id,
        "ask-1",
        "chunk-" + id,
        "file-" + id,
        "sample.md",
        1,
        "Overview",
        reviewStatus,
        confidence,
        "vector-" + id,
        new BigDecimal("0.900"),
        "C" + id,
        "Evidence " + id,
        "sample.md#overview",
        citationStatus,
        reviewEligible,
        reviewEligible ? null : citationStatus.name(),
        now);
  }
}
