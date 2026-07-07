package com.atlas.metadata.dto;

/** Logical secret/configuration reference that never contains secret material. */
public record SecretReferenceResponse(
    String provider, String scope, String key, String displayName) {}
