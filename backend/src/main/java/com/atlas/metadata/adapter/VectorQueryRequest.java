package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.VectorReviewPolicy;
import java.math.BigDecimal;
import java.util.List;

/** Product-facing vector query request. */
public record VectorQueryRequest(
    String spaceId,
    List<BigDecimal> queryVector,
    String mockQuery,
    int limit,
    VectorReviewPolicy reviewPolicy) {}
