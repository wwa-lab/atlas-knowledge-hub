package com.atlas.metadata.dto;

import java.util.List;
import java.util.Map;

/** Masked runtime model configuration state. */
public record ModelConfigurationResponse(
    String adapterKey,
    String provider,
    String modelKey,
    String credentialStatus,
    String endpointStatus,
    String mode,
    List<SecretStatusResponse> secretStatuses,
    Map<String, String> maskedConfigSummary) {}
