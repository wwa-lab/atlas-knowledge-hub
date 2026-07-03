package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.StorageLayer;

/** Bounded list request inside a batch/workspace storage namespace. */
public record StorageListRequest(
    String batchId,
    String workspaceId,
    StorageLayer layer,
    int pageSize,
    String pageToken) {}
