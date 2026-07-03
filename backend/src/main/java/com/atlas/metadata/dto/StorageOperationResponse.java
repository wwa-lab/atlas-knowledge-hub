package com.atlas.metadata.dto;

import com.atlas.metadata.enums.StorageOperationStatus;
import com.atlas.metadata.enums.StorageOperationType;
import java.time.OffsetDateTime;
import java.util.List;

/** Storage operation summary and per-object descriptors. */
public record StorageOperationResponse(
    String operationId,
    String batchId,
    String adapterKey,
    StorageOperationType operationType,
    StorageOperationStatus status,
    String safeMessage,
    StorageOperationSummaryResponse summary,
    List<StorageObjectResponse> objects,
    OffsetDateTime startedAt,
    OffsetDateTime completedAt) {}
