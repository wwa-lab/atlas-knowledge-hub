package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.StorageLayer;

/** Product-facing storage put request using a content reference, never raw bytes in metadata. */
public record StoragePutRequest(
    String operationId,
    String batchId,
    String workspaceId,
    StorageLayer layer,
    String objectKey,
    String contentType,
    String contentRef,
    String fileId) {}
