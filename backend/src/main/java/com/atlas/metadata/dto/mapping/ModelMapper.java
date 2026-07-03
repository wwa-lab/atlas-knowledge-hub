package com.atlas.metadata.dto.mapping;

import com.atlas.metadata.adapter.ModelCapability;
import com.atlas.metadata.domain.ModelRun;
import com.atlas.metadata.domain.ModelRunOutput;
import com.atlas.metadata.domain.ModelRunSourceReference;
import com.atlas.metadata.dto.ModelCapabilityResponse;
import com.atlas.metadata.dto.ModelOutputResponse;
import com.atlas.metadata.dto.ModelRunResponse;
import com.atlas.metadata.dto.ModelSourceReferenceResponse;
import com.atlas.metadata.dto.ModelUsageResponse;
import java.util.Arrays;
import java.util.List;

/** Mapping helpers for model adapter API responses. */
public final class ModelMapper {

  private ModelMapper() {}

  public static ModelCapabilityResponse toResponse(ModelCapability capability) {
    return new ModelCapabilityResponse(
        capability.adapterKey(),
        capability.modelKey(),
        capability.displayName(),
        capability.providerFamily(),
        capability.modelType(),
        capability.supportedOperations(),
        capability.defaultModel(),
        capability.status(),
        capability.contextLimit(),
        capability.maskedConfigSummary());
  }

  public static ModelRunResponse toResponse(
      ModelRun run,
      ModelUsageResponse usage,
      List<ModelRunOutput> outputs,
      List<ModelRunSourceReference> sourceReferences) {
    return new ModelRunResponse(
        run.getId(),
        run.getAdapterKey(),
        run.getModelKey(),
        run.getModelType(),
        run.getOperation(),
        run.getStatus(),
        run.getMode(),
        run.getPurpose(),
        run.getRequestedBy(),
        run.getInputReference(),
        run.getSafeInputSummary(),
        run.getSafeMessage(),
        usage,
        outputs.stream().map(ModelMapper::toResponse).toList(),
        sourceReferences.stream().map(ModelMapper::toResponse).toList(),
        run.getStartedAt(),
        run.getCompletedAt());
  }

  public static ModelOutputResponse toResponse(ModelRunOutput output) {
    return new ModelOutputResponse(
        output.getId(),
        output.getKind(),
        output.getOutputReference(),
        output.getSafeSummary(),
        Arrays.asList(output.getRankedItemIds()),
        output.getEmbeddingDimension(),
        output.getEmbeddingItemCount(),
        output.getConfidence(),
        output.getReviewStatus(),
        output.getSafeError());
  }

  public static ModelSourceReferenceResponse toResponse(ModelRunSourceReference reference) {
    return new ModelSourceReferenceResponse(
        reference.getId(),
        reference.getRefType(),
        reference.getRefId(),
        reference.getLabel(),
        reference.getConfidence(),
        reference.getReviewStatus());
  }
}
