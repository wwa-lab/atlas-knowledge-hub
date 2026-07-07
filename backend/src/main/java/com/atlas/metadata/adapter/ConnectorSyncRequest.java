package com.atlas.metadata.adapter;

/** Adapter request for local connector sync execution. */
public record ConnectorSyncRequest(
    String runId, String spaceId, String connectorKey, String sourceScope) {}
