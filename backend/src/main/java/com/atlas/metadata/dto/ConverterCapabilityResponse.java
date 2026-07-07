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
    List<SecretStatusResponse> secretStatuses,
    Map<String, String> maskedConfigSummary) {}
