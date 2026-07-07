package com.atlas.metadata.service;

import com.atlas.metadata.domain.AskEvidence;
import com.atlas.metadata.domain.AskRun;
import com.atlas.metadata.dto.RetrievalCitationHealthResponse;
import com.atlas.metadata.dto.RetrievalConfidenceResponse;
import com.atlas.metadata.dto.RetrievalEvidenceCoverageResponse;
import com.atlas.metadata.dto.RetrievalQualityMetricsSummaryResponse;
import com.atlas.metadata.dto.RetrievalReviewEligibilityResponse;
import com.atlas.metadata.dto.RetrievalRunQualityMetricsResponse;
import com.atlas.metadata.enums.AskCitationStatus;
import com.atlas.metadata.enums.AskRunStatus;
import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Deterministic safe quality metric calculations over stored Ask metadata. */
public class RetrievalQualityMetricsCalculator {

  private static final BigDecimal MEDIUM_CONFIDENCE = new BigDecimal("0.700");
  private static final BigDecimal HIGH_CONFIDENCE = new BigDecimal("0.850");
  private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);

  /** Computes safe metrics for one Ask run. */
  public RetrievalRunQualityMetricsResponse compute(AskRun run, List<AskEvidence> evidence) {
    List<AskEvidence> safeEvidence = evidence == null ? List.of() : List.copyOf(evidence);
    RetrievalEvidenceCoverageResponse coverage = coverage(safeEvidence);
    RetrievalCitationHealthResponse citationHealth = citationHealth(safeEvidence);
    RetrievalConfidenceResponse confidence = confidence(run, safeEvidence);
    RetrievalReviewEligibilityResponse eligibility =
        eligibility(run, safeEvidence, citationHealth, confidence);
    List<String> diagnostics = new ArrayList<>(eligibility.reasons());
    if (run.getStatus() == AskRunStatus.NO_EVIDENCE && !diagnostics.contains("NO_EVIDENCE")) {
      diagnostics.add("NO_EVIDENCE");
    }
    return new RetrievalRunQualityMetricsResponse(
        run.getId(),
        run.getSpaceId(),
        run.getStatus(),
        coverage,
        citationHealth,
        confidence,
        eligibility,
        run.getStatus() == AskRunStatus.NO_EVIDENCE,
        List.copyOf(diagnostics));
  }

  /** Aggregates already-computed run metrics into a safe space summary. */
  public RetrievalQualityMetricsSummaryResponse summarize(
      String spaceId, List<RetrievalRunQualityMetricsResponse> metrics, OffsetDateTime generatedAt) {
    List<RetrievalRunQualityMetricsResponse> safeMetrics =
        metrics == null ? List.of() : List.copyOf(metrics);
    int totalRuns = safeMetrics.size();
    int succeededRuns = countStatus(safeMetrics, AskRunStatus.SUCCEEDED);
    int noEvidenceRefusals = countStatus(safeMetrics, AskRunStatus.NO_EVIDENCE);
    int failedRuns =
        countStatus(safeMetrics, AskRunStatus.FAILED)
            + countStatus(safeMetrics, AskRunStatus.PARTIAL_FAILED);
    int reviewEligibleRuns =
        (int) safeMetrics.stream().filter(item -> item.reviewEligibility().reviewEligible()).count();
    int lowConfidenceRuns =
        (int) safeMetrics.stream().filter(item -> "LOW".equals(item.confidence().band())).count();
    return new RetrievalQualityMetricsSummaryResponse(
        spaceId,
        totalRuns,
        succeededRuns,
        noEvidenceRefusals,
        failedRuns,
        reviewEligibleRuns,
        lowConfidenceRuns,
        average(safeMetrics.stream().map(item -> item.evidenceCoverage().coverageRatio()).toList()),
        average(safeMetrics.stream().map(item -> item.citationHealth().healthRatio()).toList()),
        generatedAt);
  }

  private RetrievalEvidenceCoverageResponse coverage(List<AskEvidence> evidence) {
    int evidenceCount = evidence.size();
    int citedEvidenceCount = (int) evidence.stream().filter(this::hasSafeSourceTrace).count();
    return new RetrievalEvidenceCoverageResponse(
        evidenceCount, citedEvidenceCount, ratio(citedEvidenceCount, evidenceCount), evidenceCount == 0);
  }

  private RetrievalCitationHealthResponse citationHealth(List<AskEvidence> evidence) {
    int evidenceCount = evidence.size();
    int healthyCitationCount = (int) evidence.stream().filter(this::isHealthyCitation).count();
    int uncitedEvidenceCount = Math.max(0, evidenceCount - healthyCitationCount);
    return new RetrievalCitationHealthResponse(
        evidenceCount,
        healthyCitationCount,
        uncitedEvidenceCount,
        ratio(healthyCitationCount, evidenceCount),
        evidenceCount == 0 || uncitedEvidenceCount > 0);
  }

  private RetrievalConfidenceResponse confidence(AskRun run, List<AskEvidence> evidence) {
    BigDecimal averageEvidenceConfidence =
        average(evidence.stream().map(AskEvidence::getConfidence).toList());
    BigDecimal answerConfidence = scale(run.getAnswerConfidence());
    return new RetrievalConfidenceResponse(
        averageEvidenceConfidence, answerConfidence, confidenceBand(averageEvidenceConfidence, answerConfidence));
  }

  private RetrievalReviewEligibilityResponse eligibility(
      AskRun run,
      List<AskEvidence> evidence,
      RetrievalCitationHealthResponse citationHealth,
      RetrievalConfidenceResponse confidence) {
    List<String> reasons = new ArrayList<>();
    if (run.getStatus() == AskRunStatus.NO_EVIDENCE || evidence.isEmpty()) {
      reasons.add("NO_EVIDENCE");
    }
    if (run.getStatus() == AskRunStatus.FAILED || run.getStatus() == AskRunStatus.PARTIAL_FAILED) {
      reasons.add("FAILED_RUN");
    }
    if (run.getStatus() != AskRunStatus.SUCCEEDED
        && run.getStatus() != AskRunStatus.NO_EVIDENCE
        && run.getStatus() != AskRunStatus.FAILED
        && run.getStatus() != AskRunStatus.PARTIAL_FAILED) {
      reasons.add("RUN_NOT_COMPLETE");
    }
    if (citationHealth.unhealthy()) {
      reasons.add("UNHEALTHY_CITATIONS");
    }
    if (evidence.stream().anyMatch(item -> item.getReviewStatus() != ReviewStatus.APPROVED)) {
      reasons.add("REVIEW_REQUIRED_EVIDENCE");
    }
    if (evidence.stream().anyMatch(item -> !hasSafeSourceTrace(item))) {
      reasons.add("MISSING_SOURCE_TRACE");
    }
    if ("LOW".equals(confidence.band()) || "UNKNOWN".equals(confidence.band())) {
      reasons.add("LOW_CONFIDENCE");
    }
    boolean reviewEligible = reasons.isEmpty();
    String status = reviewEligible ? "ELIGIBLE" : evidence.isEmpty() ? "NOT_ELIGIBLE" : "NEEDS_REVIEW";
    return new RetrievalReviewEligibilityResponse(reviewEligible, status, List.copyOf(reasons));
  }

  private boolean isHealthyCitation(AskEvidence evidence) {
    return hasSafeSourceTrace(evidence)
        && evidence.getCitationStatus() == AskCitationStatus.ELIGIBLE
        && evidence.isReviewEligible();
  }

  private boolean hasSafeSourceTrace(AskEvidence evidence) {
    return notBlank(evidence.getSourceChunkId())
        && (notBlank(evidence.getSourceFile()) || evidence.getPage() != null || notBlank(evidence.getSection()));
  }

  private String confidenceBand(BigDecimal evidenceConfidence, BigDecimal answerConfidence) {
    List<BigDecimal> known =
        Arrays.asList(evidenceConfidence, answerConfidence).stream().filter(value -> value != null).toList();
    if (known.isEmpty()) {
      return "UNKNOWN";
    }
    if (known.stream().anyMatch(value -> value.compareTo(MEDIUM_CONFIDENCE) < 0)) {
      return "LOW";
    }
    if (known.stream().allMatch(value -> value.compareTo(HIGH_CONFIDENCE) >= 0)) {
      return "HIGH";
    }
    return "MEDIUM";
  }

  private int countStatus(List<RetrievalRunQualityMetricsResponse> metrics, AskRunStatus status) {
    return (int) metrics.stream().filter(item -> item.status() == status).count();
  }

  private BigDecimal average(List<BigDecimal> values) {
    List<BigDecimal> known = values.stream().filter(value -> value != null).toList();
    if (known.isEmpty()) {
      return null;
    }
    BigDecimal sum = known.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    return sum.divide(BigDecimal.valueOf(known.size()), 3, RoundingMode.HALF_UP);
  }

  private BigDecimal ratio(int numerator, int denominator) {
    if (denominator <= 0) {
      return ZERO;
    }
    return BigDecimal.valueOf(numerator).divide(BigDecimal.valueOf(denominator), 3, RoundingMode.HALF_UP);
  }

  private BigDecimal scale(BigDecimal value) {
    return value == null ? null : value.setScale(3, RoundingMode.HALF_UP);
  }

  private boolean notBlank(String value) {
    return value != null && !value.isBlank();
  }
}
