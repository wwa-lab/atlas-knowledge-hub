package com.atlas.metadata.dto;

import com.atlas.metadata.enums.StorageAdapterStatus;
import com.atlas.metadata.enums.StorageLayer;
import java.util.List;
import java.util.Map;

/** Masked storage adapter capability response. */
public record StorageCapabilityResponse(
    String adapterKey,
    String displayName,
    String version,
    List<StorageLayer> supportedLayers,
    boolean defaultAdapter,
    StorageAdapterStatus status,
    List<SecretStatusResponse> secretStatuses,
    Map<String, String> maskedConfigSummary) {}
