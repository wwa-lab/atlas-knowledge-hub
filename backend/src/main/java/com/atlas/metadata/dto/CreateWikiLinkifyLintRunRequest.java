package com.atlas.metadata.dto;

import java.util.List;

/** Request to run deterministic Wiki linkify/lint for one Knowledge Space. */
public record CreateWikiLinkifyLintRunRequest(
    List<String> pageIds,
    String requestedBy,
    Boolean dryRun,
    Boolean linkify,
    Boolean lint) {}
