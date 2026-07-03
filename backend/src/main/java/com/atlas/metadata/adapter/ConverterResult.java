package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.FileStatus;
import java.math.BigDecimal;
import java.util.List;

/** Product-facing conversion result. */
public record ConverterResult(String adapterKey, List<ConverterFileResult> files, String safeMessage) {

  /** Per-file conversion result produced by an adapter. */
  public record ConverterFileResult(
      String fileId,
      FileStatus status,
      String pdfPath,
      BigDecimal confidence,
      String safeError) {}
}
