package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ConnectorConfigurationState;
import com.atlas.metadata.enums.ConnectorDefinitionStatus;
import com.atlas.metadata.enums.ConnectorReviewPolicy;

/** Safe connector definition response. */
public record ConnectorDefinitionResponse(
    String id,
    String connectorKey,
    String name,
    String connectorType,
    ConnectorDefinitionStatus status,
    String version,
    String capabilitySummary,
    ConnectorConfigurationState configurationState,
    ConnectorReviewPolicy reviewPolicy) {}
