package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ModelAdapterStatus;
import com.atlas.metadata.enums.ModelOperation;
import com.atlas.metadata.enums.ModelType;
import java.util.List;
import java.util.Map;

/** Masked model adapter capability metadata. */
public record ModelCapabilityResponse(
    String adapterKey,
    String modelKey,
    String displayName,
    String providerFamily,
    ModelType modelType,
    List<ModelOperation> supportedOperations,
    boolean defaultModel,
    ModelAdapterStatus status,
    int contextLimit,
    List<SecretStatusResponse> secretStatuses,
    Map<String, String> maskedConfigSummary) {}
