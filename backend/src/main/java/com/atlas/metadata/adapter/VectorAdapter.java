package com.atlas.metadata.adapter;

/** Product-facing vector adapter contract. */
public interface VectorAdapter {

  /** Returns masked capability metadata for this adapter. */
  VectorCapability capability();

  /** Indexes traceable vector items. */
  VectorIndexResult index(VectorIndexRequest request);

  /** Deletes traceable vector items from the adapter index. */
  VectorDeleteResult delete(VectorDeleteRequest request);

  /** Queries traceable vector evidence. */
  VectorQueryResult query(VectorQueryRequest request);
}
