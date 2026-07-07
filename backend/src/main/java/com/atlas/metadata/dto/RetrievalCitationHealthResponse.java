package com.atlas.metadata.dto;

import java.math.BigDecimal;

/** Safe citation health metrics for one retrieval run. */
public record RetrievalCitationHealthResponse(
    int citationCount,
    int healthyCitationCount,
    int uncitedEvidenceCount,
    BigDecimal healthRatio,
    boolean unhealthy) {}
