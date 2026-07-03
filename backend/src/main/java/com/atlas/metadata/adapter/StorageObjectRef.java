package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.StorageLayer;

/** Reference to one object inside a batch/workspace storage namespace. */
public record StorageObjectRef(
    String operationId,
    String batchId,
    String workspaceId,
    StorageLayer layer,
    String objectKey,
    String fileId) {}
