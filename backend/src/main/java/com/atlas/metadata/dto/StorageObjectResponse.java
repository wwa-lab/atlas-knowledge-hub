package com.atlas.metadata.dto;

import com.atlas.metadata.enums.StorageLayer;
import com.atlas.metadata.enums.StorageObjectStatus;

/** Stored-object descriptor response without raw object bytes or storage internals. */
public record StorageObjectResponse(
    StorageLayer layer,
    String objectKey,
    String contentType,
    Long sizeBytes,
    String checksum,
    String adapterKey,
    StorageObjectStatus status,
    String fileId,
    String safeError) {}
