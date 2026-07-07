package com.atlas.metadata.dto;

import jakarta.validation.constraints.NotBlank;

/** Request body for starting a connector sync v0 run. */
public record CreateConnectorSyncJobRequest(
    @NotBlank String connectorKey, String requestedBy, String sourceScope) {}
