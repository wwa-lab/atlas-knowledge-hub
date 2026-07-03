package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ModelOperation;
import com.atlas.metadata.enums.ModelRunStatus;
import com.atlas.metadata.enums.ModelType;
import java.time.OffsetDateTime;
import java.util.List;

/** Safe model run report and output evidence. */
public record ModelRunResponse(
    String runId,
    String adapterKey,
    String modelKey,
    ModelType modelType,
    ModelOperation operationType,
    ModelRunStatus status,
    String mode,
    String purpose,
    String requestedBy,
    String inputReference,
    String safeInputSummary,
    String safeMessage,
    ModelUsageResponse usage,
    List<ModelOutputResponse> outputs,
    List<ModelSourceReferenceResponse> sourceReferences,
    OffsetDateTime startedAt,
    OffsetDateTime completedAt) {}
