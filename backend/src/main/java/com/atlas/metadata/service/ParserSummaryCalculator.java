package com.atlas.metadata.service;

import com.atlas.metadata.domain.ParserFileResult;
import com.atlas.metadata.dto.ParserRunSummaryResponse;
import com.atlas.metadata.enums.FileStatus;
import java.util.List;

/** Computes parser run summary counts from persisted evidence. */
public class ParserSummaryCalculator {

  /** Derives summary counts from parser file results and skipped/ineligible report rows. */
  public ParserRunSummaryResponse compute(List<ParserFileResult> results) {
    int markdownGenerated = count(results, FileStatus.MARKDOWN_GENERATED);
    int lowConfidence = count(results, FileStatus.LOW_CONFIDENCE);
    int ocrRequired = count(results, FileStatus.OCR_REQUIRED);
    int failed = count(results, FileStatus.FAILED);
    int unsupported = count(results, FileStatus.UNSUPPORTED);
    int skipped = (int) results.stream().filter(ParserFileResult::isSkipped).count();
    return new ParserRunSummaryResponse(
        results.size(),
        markdownGenerated,
        lowConfidence,
        ocrRequired,
        failed,
        skipped,
        unsupported);
  }

  private int count(List<ParserFileResult> results, FileStatus status) {
    return (int)
        results.stream()
            .filter(result -> !result.isSkipped())
            .filter(result -> result.getStatus() == status)
            .count();
  }
}
