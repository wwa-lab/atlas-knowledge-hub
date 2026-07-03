package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ReviewAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Request to append a review record against a file item. */
public record CreateReviewRequest(
    @NotNull ReviewAction action,
    @NotBlank String reviewer,
    String comment,
    List<String> affectedChunks) {}
