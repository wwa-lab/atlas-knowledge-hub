package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ConnectorOutputArtifactType;
import com.atlas.metadata.enums.ReviewStatus;

/** Review-required connector output artifact response. */
public record ConnectorOutputArtifactResponse(
    String id,
    ConnectorOutputArtifactType artifactType,
    ReviewStatus reviewStatus,
    String title,
    String targetPath) {}
