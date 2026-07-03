package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.ModelAdapterStatus;
import com.atlas.metadata.enums.ModelOperation;
import com.atlas.metadata.enums.ModelType;
import java.util.List;
import java.util.Map;

/** Masked capability metadata for one adapter/model pair. */
public record ModelCapability(
    String adapterKey,
    String modelKey,
    String displayName,
    String providerFamily,
    ModelType modelType,
    List<ModelOperation> supportedOperations,
    boolean defaultModel,
    ModelAdapterStatus status,
    int contextLimit,
    Map<String, String> maskedConfigSummary) {}
