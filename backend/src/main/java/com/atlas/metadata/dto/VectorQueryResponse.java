package com.atlas.metadata.dto;

import com.atlas.metadata.enums.VectorReviewPolicy;
import java.util.List;

/** Immediate vector query evidence response. */
public record VectorQueryResponse(
    String spaceId,
    String adapterKey,
    String mode,
    VectorReviewPolicy reviewPolicy,
    int limit,
    String safeMessage,
    List<VectorQueryMatchResponse> matches) {}
