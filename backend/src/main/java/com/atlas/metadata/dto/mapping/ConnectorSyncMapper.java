package com.atlas.metadata.dto.mapping;

import com.atlas.metadata.domain.ConnectorDefinition;
import com.atlas.metadata.domain.ConnectorOutputArtifact;
import com.atlas.metadata.domain.ConnectorSyncItem;
import com.atlas.metadata.domain.ConnectorSyncJob;
import com.atlas.metadata.domain.ConnectorSyncRun;
import com.atlas.metadata.dto.ConnectorDefinitionResponse;
import com.atlas.metadata.dto.ConnectorOutputArtifactResponse;
import com.atlas.metadata.dto.ConnectorSyncItemResponse;
import com.atlas.metadata.dto.ConnectorSyncRunResponse;
import java.util.List;
import java.util.Map;

/** Maps connector sync domain objects to API DTOs. */
public final class ConnectorSyncMapper {

  private ConnectorSyncMapper() {}

  /** Converts connector definition metadata to a safe response. */
  public static ConnectorDefinitionResponse toDefinitionResponse(ConnectorDefinition definition) {
    return new ConnectorDefinitionResponse(
        definition.getId(),
        definition.getConnectorKey(),
        definition.getName(),
        definition.getConnectorType(),
        definition.getStatus(),
        definition.getVersion(),
        definition.getCapabilitySummary(),
        definition.getConfigurationState(),
        definition.getReviewPolicy());
  }

  /** Converts a run and its owning job/definition to a response. */
  public static ConnectorSyncRunResponse toRunResponse(
      ConnectorSyncRun run, ConnectorSyncJob job, ConnectorDefinition definition) {
    return new ConnectorSyncRunResponse(
        run.getId(),
        run.getJobId(),
        job.getSpaceId(),
        definition.getConnectorKey(),
        run.getStatus(),
        run.getItemCount(),
        run.getReviewRequiredCount(),
        run.getFailedCount(),
        run.getSafeMessage(),
        run.getStartedAt(),
        run.getCompletedAt());
  }

  /** Converts an item plus its output artifacts to a response. */
  public static ConnectorSyncItemResponse toItemResponse(
      ConnectorSyncItem item, Map<String, List<ConnectorOutputArtifact>> artifactsByItemId) {
    return new ConnectorSyncItemResponse(
        item.getId(),
        item.getRunId(),
        item.getExternalId(),
        item.getTitle(),
        item.getItemStatus(),
        item.getSourceReference(),
        item.getSourceTrace(),
        item.getProvenance(),
        item.getConfidence(),
        item.isReviewEligible(),
        item.getSafeErrorCategory(),
        item.getSafeErrorMessage(),
        artifactsByItemId.getOrDefault(item.getId(), List.of()).stream()
            .map(ConnectorSyncMapper::toArtifactResponse)
            .toList(),
        item.getDiscoveredAt());
  }

  private static ConnectorOutputArtifactResponse toArtifactResponse(
      ConnectorOutputArtifact artifact) {
    return new ConnectorOutputArtifactResponse(
        artifact.getId(),
        artifact.getArtifactType(),
        artifact.getReviewStatus(),
        artifact.getTitle(),
        artifact.getTargetPath());
  }
}
