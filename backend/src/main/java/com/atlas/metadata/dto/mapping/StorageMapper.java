package com.atlas.metadata.dto.mapping;

import com.atlas.metadata.adapter.StorageCapability;
import com.atlas.metadata.domain.StorageObject;
import com.atlas.metadata.domain.StorageOperation;
import com.atlas.metadata.dto.StorageCapabilityResponse;
import com.atlas.metadata.dto.StorageObjectResponse;
import com.atlas.metadata.dto.StorageOperationResponse;
import com.atlas.metadata.dto.StorageOperationSummaryResponse;
import java.util.List;

/** Maps storage adapter and persistence models to DTOs. */
public final class StorageMapper {

  private StorageMapper() {}

  /** Converts capability metadata to its response DTO. */
  public static StorageCapabilityResponse toResponse(StorageCapability capability) {
    return new StorageCapabilityResponse(
        capability.adapterKey(),
        capability.displayName(),
        capability.version(),
        capability.supportedLayers(),
        capability.defaultAdapter(),
        capability.status(),
        SecretStatusMapper.fromSummary(
            capability.adapterKey(), "storage-adapter", capability.maskedConfigSummary(), "adapter"),
        capability.maskedConfigSummary());
  }

  /** Converts operation evidence and object descriptors to a response DTO. */
  public static StorageOperationResponse toResponse(
      StorageOperation operation,
      StorageOperationSummaryResponse summary,
      List<StorageObject> objects) {
    return new StorageOperationResponse(
        operation.getId(),
        operation.getBatchId(),
        operation.getAdapterKey(),
        operation.getOperationType(),
        operation.getStatus(),
        operation.getSafeMessage(),
        summary,
        objects.stream().map(StorageMapper::toObjectResponse).toList(),
        operation.getStartedAt(),
        operation.getCompletedAt());
  }

  /** Converts a stored-object entity to its response DTO. */
  public static StorageObjectResponse toObjectResponse(StorageObject object) {
    return new StorageObjectResponse(
        object.getLayer(),
        object.objectKey(),
        object.getContentType(),
        object.getSizeBytes(),
        object.getChecksum(),
        object.getAdapterKey(),
        object.getStatus(),
        object.getFileItemId(),
        object.getSafeError());
  }

  /** Converts persisted summary fields to a DTO. */
  public static StorageOperationSummaryResponse toSummary(StorageOperation operation) {
    return new StorageOperationSummaryResponse(
        operation.getTotalCount(),
        operation.getStoredCount(),
        operation.getDeletedCount(),
        operation.getMissingCount(),
        operation.getFailedCount(),
        operation.getSkippedCount(),
        operation.getTotalBytes());
  }
}
