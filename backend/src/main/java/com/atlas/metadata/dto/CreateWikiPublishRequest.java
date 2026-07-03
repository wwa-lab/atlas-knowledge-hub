package com.atlas.metadata.dto;

import jakarta.validation.constraints.NotBlank;

/** Request to publish an approved Markdown file to Wiki metadata. */
public record CreateWikiPublishRequest(@NotBlank String title, @NotBlank String owner) {}
