package com.atlas.metadata.dto;

import jakarta.validation.constraints.Size;

/** Request to save backend-held runtime model configuration. */
public record SaveModelConfigurationRequest(
    @Size(max = 64) String provider,
    @Size(max = 512) String endpoint,
    @Size(max = 128) String modelName,
    @Size(max = 4096) String apiKey) {}
