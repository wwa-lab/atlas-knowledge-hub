package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.ParserAdapterStatus;
import com.atlas.metadata.enums.SourceType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Masked parser adapter capability metadata. */
public record ParserCapability(
    String adapterKey,
    String displayName,
    String version,
    List<SourceType> inputTypes,
    List<String> outputTypes,
    boolean defaultAdapter,
    ParserAdapterStatus status,
    BigDecimal lowConfidenceThreshold,
    Map<String, String> maskedConfigSummary) {}
