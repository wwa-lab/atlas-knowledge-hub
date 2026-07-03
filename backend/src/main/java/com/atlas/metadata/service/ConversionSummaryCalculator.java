package com.atlas.metadata.service;

import com.atlas.metadata.domain.ConversionFileResult;
import com.atlas.metadata.dto.ConversionRunSummaryResponse;
import com.atlas.metadata.enums.FileStatus;
import java.util.List;

/** Computes conversion report counts from per-file evidence rows. */
public class ConversionSummaryCalculator {

  /** Computes report counts from conversion results. */
  public ConversionRunSummaryResponse compute(List<ConversionFileResult> results) {
    int pdfConverted = count(results, FileStatus.PDF_CONVERTED);
    int pdfConvertFailed = count(results, FileStatus.PDF_CONVERT_FAILED) + count(results, FileStatus.FAILED);
    int ocrRequired = count(results, FileStatus.OCR_REQUIRED);
    int unsupported = count(results, FileStatus.UNSUPPORTED);
    int classified = pdfConverted + pdfConvertFailed + ocrRequired + unsupported;
    return new ConversionRunSummaryResponse(
        results.size(), pdfConverted, pdfConvertFailed, ocrRequired, unsupported, results.size() - classified);
  }

  private int count(List<ConversionFileResult> results, FileStatus status) {
    return (int) results.stream().filter(result -> result.getStatus() == status).count();
  }
}
