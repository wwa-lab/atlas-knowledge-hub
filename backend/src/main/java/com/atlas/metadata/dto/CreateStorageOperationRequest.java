package com.atlas.metadata.dto;

import com.atlas.metadata.enums.StorageLayer;
import com.atlas.metadata.enums.StorageOperationType;
import java.util.List;

/** Request to execute storage operations for batch artifacts. */
public record CreateStorageOperationRequest(
    String adapterKey, String requestedBy, String mode, List<StorageObjectOperationRequest> operations) {

  /** Single object operation request. */
  public record StorageObjectOperationRequest(
      StorageOperationType operationType,
      StorageLayer layer,
      String objectKey,
      String contentType,
      String fileId) {}
}
