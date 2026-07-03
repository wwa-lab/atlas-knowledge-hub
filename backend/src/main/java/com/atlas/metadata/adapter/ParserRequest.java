package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.SourceType;
import java.math.BigDecimal;
import java.util.List;

/** Product-facing parse request. */
public record ParserRequest(
    String runId,
    String batchId,
    List<ParserFile> files,
    String markdownRoot,
    String assetsRoot,
    BigDecimal lowConfidenceThreshold) {

  /** File metadata passed to a parser adapter. */
  public record ParserFile(
      String fileId,
      String sourcePath,
      SourceType sourceType,
      FileStatus currentStatus,
      String pdfPath) {}
}
