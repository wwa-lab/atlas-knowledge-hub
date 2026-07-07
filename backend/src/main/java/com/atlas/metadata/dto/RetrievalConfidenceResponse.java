package com.atlas.metadata.dto;

import java.math.BigDecimal;

/** Safe confidence metrics for one retrieval run. */
public record RetrievalConfidenceResponse(
    BigDecimal averageEvidenceConfidence, BigDecimal answerConfidence, String band) {}
