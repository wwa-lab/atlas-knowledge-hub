package com.atlas.metadata.dto;

import java.util.Map;

/** User-safe API error body. */
public record ErrorBody(
    String code, String message, Map<String, String> fields, long timestamp, String path) {}
