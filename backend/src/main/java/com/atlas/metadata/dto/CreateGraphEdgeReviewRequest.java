package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ReviewAction;
import jakarta.validation.constraints.NotNull;

/** Request to append a graph edge review action. */
public record CreateGraphEdgeReviewRequest(@NotNull ReviewAction action, String comment) {}
