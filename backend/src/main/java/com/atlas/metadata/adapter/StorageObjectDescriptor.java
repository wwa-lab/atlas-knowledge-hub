package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.StorageLayer;
import com.atlas.metadata.enums.StorageObjectStatus;

/** Safe object descriptor returned by a storage adapter. */
public record StorageObjectDescriptor(
    StorageLayer layer,
    String objectKey,
    String contentType,
    Long sizeBytes,
    String checksum,
    String adapterKey,
    StorageObjectStatus status,
    String fileId,
    String safeError) {}
