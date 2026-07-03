package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ModelSourceReferenceType;

/** Source trace reference included in a model run request. */
public record ModelSourceReferenceRequest(
    ModelSourceReferenceType refType, String refId, String label) {}
