package com.atlas.metadata.dto;

import java.math.BigDecimal;

/** Safe evidence coverage metrics for one retrieval run. */
public record RetrievalEvidenceCoverageResponse(
    int evidenceCount, int citedEvidenceCount, BigDecimal coverageRatio, boolean missingEvidence) {}
