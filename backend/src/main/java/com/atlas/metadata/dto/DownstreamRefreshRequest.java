package com.atlas.metadata.dto;

/** Request to refresh graph and vector evidence from reviewed chunks. */
public record DownstreamRefreshRequest(String batchId, String fileItemId, String requestedBy) {}
