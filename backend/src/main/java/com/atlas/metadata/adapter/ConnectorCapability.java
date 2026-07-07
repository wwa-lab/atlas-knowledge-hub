package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.ConnectorConfigurationState;
import com.atlas.metadata.enums.ConnectorDefinitionStatus;
import com.atlas.metadata.enums.ConnectorReviewPolicy;

/** Safe connector capability metadata exposed through the registry. */
public record ConnectorCapability(
    String connectorKey,
    String name,
    String connectorType,
    ConnectorDefinitionStatus status,
    String version,
    String capabilitySummary,
    ConnectorConfigurationState configurationState,
    ConnectorReviewPolicy reviewPolicy) {}
