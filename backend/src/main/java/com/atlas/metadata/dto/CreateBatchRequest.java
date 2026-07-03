package com.atlas.metadata.dto;

import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceKind;
import com.atlas.metadata.enums.SourceType;
import com.atlas.metadata.validation.RelativePath;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/** Request to create a metadata-only batch from pre-computed inventory. */
public record CreateBatchRequest(
    @NotBlank String name,
    @NotNull SourceKind sourceKind,
    String owner,
    @NotEmpty List<@Valid InventoryFileRequest> files) {

  /** File item metadata supplied by an inventory step outside this service. */
  public record InventoryFileRequest(
      @NotBlank @RelativePath String sourcePath,
      @NotNull SourceType sourceType,
      @NotNull FileStatus status,
      @NotNull @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal confidence,
      @NotNull ReviewStatus reviewStatus,
      @RelativePath String pdfPath,
      @RelativePath String markdownPath,
      @RelativePath String assetsPath,
      String errorMessage,
      List<@Valid SourceChunkRequest> chunks) {}

  /** Optional source trace chunk metadata for a file item. */
  public record SourceChunkRequest(
      @NotBlank String sourceFile,
      Integer page,
      String section,
      @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal confidence,
      ReviewStatus reviewStatus) {}
}
