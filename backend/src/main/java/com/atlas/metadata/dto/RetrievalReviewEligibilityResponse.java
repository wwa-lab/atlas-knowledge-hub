package com.atlas.metadata.dto;

import java.util.List;

/** Conservative review eligibility signal for one retrieval run. */
public record RetrievalReviewEligibilityResponse(
    boolean reviewEligible, String status, List<String> reasons) {}
