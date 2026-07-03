package com.atlas.metadata.dto;

import java.util.List;

/** Request to execute a parser adapter over PDF-converted batch file metadata. */
public record CreateParserRunRequest(
    String adapterKey, List<String> fileIds, String requestedBy, String mode) {}
