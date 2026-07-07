package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ConnectorItemStatus;
import com.atlas.metadata.enums.ConnectorSafeErrorCategory;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/** Per-item connector sync response with trace and provenance. */
public record ConnectorSyncItemResponse(
    String id,
    String runId,
    String externalId,
    String title,
    ConnectorItemStatus itemStatus,
    String sourceReference,
    Map<String, String> sourceTrace,
    Map<String, String> provenance,
    BigDecimal confidence,
    boolean reviewEligible,
    ConnectorSafeErrorCategory safeErrorCategory,
    String safeErrorMessage,
    List<ConnectorOutputArtifactResponse> outputArtifacts,
    OffsetDateTime discoveredAt) {}
