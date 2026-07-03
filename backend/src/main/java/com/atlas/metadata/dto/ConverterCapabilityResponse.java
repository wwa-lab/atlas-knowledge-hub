package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ConverterAdapterStatus;
import com.atlas.metadata.enums.SourceType;
import java.util.List;
import java.util.Map;

/** Public capability metadata for a converter adapter. */
public record ConverterCapabilityResponse(
    String adapterKey,
    String displayName,
    String version,
    String outputType,
    List<SourceType> supportedSourceTypes,
    boolean defaultAdapter,
    ConverterAdapterStatus status,
    Map<String, String> maskedConfigSummary) {}
