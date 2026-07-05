package com.atlas.metadata.dto;

import java.util.List;

/** Request to start deterministic Auto Wiki ingest v0 for one Knowledge Space. */
public record CreateWikiIngestRunRequest(
    String mode, List<String> sourceFileIds, String requestedBy, Boolean dryRun) {}
