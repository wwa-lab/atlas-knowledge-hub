package com.atlas.metadata.dto.mapping;

import com.atlas.metadata.adapter.ConverterCapability;
import com.atlas.metadata.domain.ConversionFileResult;
import com.atlas.metadata.domain.ConversionRun;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.dto.ConversionFileResultResponse;
import com.atlas.metadata.dto.ConversionRunResponse;
import com.atlas.metadata.dto.ConversionRunSummaryResponse;
import com.atlas.metadata.dto.ConverterCapabilityResponse;
import java.util.List;
import java.util.Map;

/** Maps converter adapter and persistence models to DTOs. */
public final class ConverterMapper {

  private ConverterMapper() {}

  /** Converts capability metadata to its response DTO. */
  public static ConverterCapabilityResponse toResponse(ConverterCapability capability) {
    return new ConverterCapabilityResponse(
        capability.adapterKey(),
        capability.displayName(),
        capability.version(),
        capability.outputType(),
        capability.supportedSourceTypes(),
        capability.defaultAdapter(),
        capability.status(),
        capability.maskedConfigSummary());
  }

  /** Converts run evidence and per-file evidence to a response DTO. */
  public static ConversionRunResponse toResponse(
      ConversionRun run,
      ConversionRunSummaryResponse summary,
      List<ConversionFileResult> results,
      Map<String, FileItem> filesById) {
    return new ConversionRunResponse(
        run.getId(),
        run.getBatchId(),
        run.getAdapterKey(),
        run.getStatus(),
        run.getSafeMessage(),
        summary,
        results.stream().map(result -> toFileResult(result, filesById.get(result.getFileItemId()))).toList(),
        run.getStartedAt(),
        run.getCompletedAt());
  }

  private static ConversionFileResultResponse toFileResult(
      ConversionFileResult result, FileItem fileItem) {
    return new ConversionFileResultResponse(
        result.getFileItemId(),
        result.getSourcePath(),
        result.getSourceType(),
        result.getStatus(),
        result.getPdfPath(),
        result.getConfidence(),
        fileItem == null ? null : fileItem.getReviewStatus(),
        result.getSafeError());
  }
}
