package com.atlas.metadata.domain;

/** Safe metadata reference for Wiki source, chunk, page, or graph identifiers. */
public record WikiReference(String type, String id, String label, String locator) {}
