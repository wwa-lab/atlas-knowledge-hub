package com.atlas.metadata.dto;

/** Response for backend-orchestrated downstream evidence refresh. */
public record DownstreamRefreshResponse(
    GraphProjectionRunResponse graphRun, VectorRunResponse vectorRun, String message) {}
