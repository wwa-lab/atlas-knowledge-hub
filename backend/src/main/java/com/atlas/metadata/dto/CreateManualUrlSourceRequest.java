package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ManualUrlFetchIntent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request to register a metadata-only manual URL source. */
public record CreateManualUrlSourceRequest(
    @NotBlank String url,
    @Size(max = 120) String title,
    @Size(max = 500) String description,
    ManualUrlFetchIntent fetchIntent,
    String createdBy) {}
