package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.SourceType;
import java.util.List;

/** Product-facing conversion request. */
public record ConverterRequest(
    String runId, String batchId, List<ConverterFile> files, String artifactRoot) {

  /** File metadata passed to a converter adapter. */
  public record ConverterFile(
      String fileId, String sourcePath, SourceType sourceType, FileStatus currentStatus) {}
}
