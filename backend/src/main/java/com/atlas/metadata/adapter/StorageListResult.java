package com.atlas.metadata.adapter;

import java.util.List;

/** Bounded storage adapter listing result. */
public record StorageListResult(List<StorageObjectDescriptor> objects, String nextPageToken) {}
