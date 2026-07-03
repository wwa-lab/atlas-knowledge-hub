package com.atlas.metadata.adapter;

/** Product-facing storage adapter contract. */
public interface StorageAdapter {

  /** Returns masked capability metadata for this adapter. */
  StorageCapability capability();

  /** Stores an object from a content reference and returns a safe descriptor. */
  StorageObjectDescriptor put(StoragePutRequest request);

  /** Gets a safe object descriptor or a MISSING descriptor. */
  StorageObjectDescriptor get(StorageObjectRef ref);

  /** Returns whether the object currently exists behind this adapter. */
  boolean exists(StorageObjectRef ref);

  /** Lists objects inside the requested namespace and layer. */
  StorageListResult list(StorageListRequest request);

  /** Deletes an object and returns a safe descriptor outcome. */
  StorageDeleteResult delete(StorageObjectRef ref);
}
