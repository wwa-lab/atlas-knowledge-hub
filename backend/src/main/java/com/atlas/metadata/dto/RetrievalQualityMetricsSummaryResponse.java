package com.atlas.metadata.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Safe aggregate retrieval quality metrics for a Knowledge Space. */
public record RetrievalQualityMetricsSummaryResponse(
    String spaceId,
    int totalRuns,
    int succeededRuns,
    int noEvidenceRefusals,
    int failedRuns,
    int reviewEligibleRuns,
    int lowConfidenceRuns,
    BigDecimal averageEvidenceCoverage,
    BigDecimal averageCitationHealth,
    OffsetDateTime generatedAt) {}
