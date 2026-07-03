package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;
import java.util.List;

/** Product-facing parser result. */
public record ParserResult(String adapterKey, List<ParserFileResult> files, String safeMessage) {

  /** Per-file parse result produced by an adapter. */
  public record ParserFileResult(
      String fileId,
      FileStatus status,
      String markdownPath,
      String assetsPath,
      BigDecimal confidence,
      String safeError,
      List<ParserChunkResult> chunks) {}

  /** Source trace chunk result produced by a parser adapter. */
  public record ParserChunkResult(
      String chunkId,
      Integer page,
      String section,
      BigDecimal confidence,
      ReviewStatus reviewStatus) {}
}
