package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.VectorAdapterStatus;
import java.util.List;
import java.util.Map;

/** Masked vector adapter capability metadata. */
public record VectorCapability(
    String adapterKey,
    String displayName,
    String version,
    List<Integer> supportedDimensions,
    List<String> supportedOperations,
    boolean defaultAdapter,
    VectorAdapterStatus status,
    Map<String, String> maskedConfigSummary) {}
