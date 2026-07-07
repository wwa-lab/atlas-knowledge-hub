package com.atlas.metadata.dto;

import com.atlas.metadata.enums.AskRunStatus;
import java.util.List;

/** Safe deterministic quality metrics for one Trusted Ask run. */
public record RetrievalRunQualityMetricsResponse(
    String runId,
    String spaceId,
    AskRunStatus status,
    RetrievalEvidenceCoverageResponse evidenceCoverage,
    RetrievalCitationHealthResponse citationHealth,
    RetrievalConfidenceResponse confidence,
    RetrievalReviewEligibilityResponse reviewEligibility,
    boolean noEvidenceRefusal,
    List<String> safeDiagnostics) {}
