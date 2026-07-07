package com.atlas.metadata.dto;

/** Status-only secret/configuration metadata safe for API and UI responses. */
public record SecretStatusResponse(
    SecretReferenceResponse reference,
    String status,
    String source,
    String maskedLabel,
    boolean replaceable,
    boolean removable) {}
