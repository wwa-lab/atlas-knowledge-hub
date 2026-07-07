package com.atlas.metadata.adapter;

/** Product-facing connector adapter boundary. */
public interface ConnectorAdapter {

  /** Returns safe connector capability metadata. */
  ConnectorCapability capability();

  /** Executes a deterministic local connector sync. */
  ConnectorSyncResult sync(ConnectorSyncRequest request);
}
