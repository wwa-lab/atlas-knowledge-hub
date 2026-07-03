package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ModelOperation;
import java.util.List;

/** Request to execute a safe model run through the adapter contract. */
public record CreateModelRunRequest(
    String adapterKey,
    String modelKey,
    ModelOperation operationType,
    String purpose,
    String requestedBy,
    String mode,
    String inputReference,
    String safeMockInput,
    List<ModelSourceReferenceRequest> sourceReferences) {}
