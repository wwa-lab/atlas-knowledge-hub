package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.domain.ConversionFileResult;
import com.atlas.metadata.dto.ConversionRunSummaryResponse;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.SourceType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for conversion summary derivation. */
class ConversionSummaryCalculatorTest {

  @Test
  void computesCountsFromResultRows() {
    ConversionRunSummaryResponse summary =
        new ConversionSummaryCalculator()
            .compute(
                List.of(
                    result("1", FileStatus.PDF_CONVERTED),
                    result("2", FileStatus.PDF_CONVERT_FAILED),
                    result("3", FileStatus.OCR_REQUIRED),
                    result("4", FileStatus.UNSUPPORTED)));

    assertThat(summary.total()).isEqualTo(4);
    assertThat(summary.pdfConverted()).isEqualTo(1);
    assertThat(summary.pdfConvertFailed()).isEqualTo(1);
    assertThat(summary.ocrRequired()).isEqualTo(1);
    assertThat(summary.unsupported()).isEqualTo(1);
    assertThat(summary.skipped()).isZero();
  }

  private ConversionFileResult result(String id, FileStatus status) {
    return ConversionFileResult.create(
        "result-" + id,
        "run",
        "file-" + id,
        "Discovery/" + id + ".docx",
        SourceType.docx,
        status,
        status == FileStatus.PDF_CONVERTED ? "generated/pdf/" + id + ".pdf" : null,
        BigDecimal.ONE,
        "trinity-office",
        null,
        OffsetDateTime.now());
  }
}
