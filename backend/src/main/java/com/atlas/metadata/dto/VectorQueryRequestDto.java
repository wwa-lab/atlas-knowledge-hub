package com.atlas.metadata.dto;

import com.atlas.metadata.enums.VectorReviewPolicy;
import java.math.BigDecimal;
import java.util.List;

/** Request for immediate vector evidence query. */
public record VectorQueryRequestDto(
    String adapterKey,
    List<BigDecimal> queryVector,
    String mockQuery,
    Integer limit,
    VectorReviewPolicy reviewPolicy) {}
