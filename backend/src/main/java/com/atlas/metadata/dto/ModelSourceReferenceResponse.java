package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ModelSourceReferenceType;
import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;

/** Source trace reference returned with a model run. */
public record ModelSourceReferenceResponse(
    String id,
    ModelSourceReferenceType refType,
    String refId,
    String label,
    BigDecimal confidence,
    ReviewStatus reviewStatus) {}
