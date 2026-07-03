package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.ConverterAdapterStatus;
import com.atlas.metadata.enums.SourceType;
import java.util.List;
import java.util.Map;

/** Masked converter adapter capability metadata. */
public record ConverterCapability(
    String adapterKey,
    String displayName,
    String version,
    String outputType,
    List<SourceType> supportedSourceTypes,
    boolean defaultAdapter,
    ConverterAdapterStatus status,
    Map<String, String> maskedConfigSummary) {}
