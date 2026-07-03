package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.domain.ParserFileResult;
import com.atlas.metadata.dto.ParserRunSummaryResponse;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.SourceType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for parser summary derivation. */
class ParserSummaryCalculatorTest {

  @Test
  void computesCountsFromResultRowsAndSkippedFiles() {
    ParserRunSummaryResponse summary =
        new ParserSummaryCalculator()
            .compute(
                List.of(
                    result("1", FileStatus.MARKDOWN_GENERATED),
                    result("2", FileStatus.LOW_CONFIDENCE),
                    result("3", FileStatus.OCR_REQUIRED),
                    result("4", FileStatus.FAILED),
                    result("5", FileStatus.UNSUPPORTED),
                    skipped("6"),
                    skipped("7")));

    assertThat(summary.total()).isEqualTo(7);
    assertThat(summary.markdownGenerated()).isEqualTo(1);
    assertThat(summary.lowConfidence()).isEqualTo(1);
    assertThat(summary.ocrRequired()).isEqualTo(1);
    assertThat(summary.failed()).isEqualTo(1);
    assertThat(summary.unsupported()).isEqualTo(1);
    assertThat(summary.skipped()).isEqualTo(2);
  }

  private ParserFileResult result(String id, FileStatus status) {
    return ParserFileResult.create(
        "parser-result-" + id,
        "run",
        "file-" + id,
        "Discovery/" + id + ".pdf",
        "generated/pdf/" + id + ".pdf",
        SourceType.pdf,
        status,
        status == FileStatus.MARKDOWN_GENERATED ? "generated/markdown/" + id + ".md" : null,
        status == FileStatus.MARKDOWN_GENERATED ? "generated/assets/" + id : null,
        BigDecimal.ONE,
        "document-normalize",
        1,
        false,
        null,
        OffsetDateTime.now());
  }

  private ParserFileResult skipped(String id) {
    return ParserFileResult.create(
        "result-" + id,
        "run",
        "file-" + id,
        "Discovery/" + id + ".pdf",
        "generated/pdf/" + id + ".pdf",
        SourceType.pdf,
        FileStatus.UPLOADED,
        null,
        null,
        BigDecimal.ZERO,
        "document-normalize",
        0,
        true,
        "Skipped: file is not eligible for parser run.",
        OffsetDateTime.now());
  }
}
