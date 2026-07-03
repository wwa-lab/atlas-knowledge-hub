package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ParserAdapterStatus;
import com.atlas.metadata.enums.SourceType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Public capability metadata for a parser adapter. */
public record ParserCapabilityResponse(
    String adapterKey,
    String displayName,
    String version,
    List<SourceType> inputTypes,
    List<String> outputTypes,
    boolean defaultAdapter,
    ParserAdapterStatus status,
    BigDecimal lowConfidenceThreshold,
    Map<String, String> maskedConfigSummary) {}
