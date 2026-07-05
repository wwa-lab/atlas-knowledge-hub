package com.atlas.metadata.dto;

/** Safe reference included in Wiki API responses. */
public record WikiReferenceResponse(String type, String id, String label, String locator) {}
