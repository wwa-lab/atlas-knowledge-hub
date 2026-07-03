package com.atlas.metadata.dto;

/** Request to create a graph projection run. */
public record CreateGraphProjectionRunRequest(String scope, String adapterId, Boolean dryRun) {}
