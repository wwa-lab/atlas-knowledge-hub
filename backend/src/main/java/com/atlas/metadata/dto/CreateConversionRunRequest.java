package com.atlas.metadata.dto;

import java.util.List;

/** Request to execute a converter adapter over batch file metadata. */
public record CreateConversionRunRequest(
    String adapterKey, List<String> fileIds, String requestedBy, String mode) {}
