package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.dto.BatchMetricsResponse;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for derived batch metrics. */
class MetricsCalculatorTest {

  @Test
  void computesMetricsFromFileItemsOnly() {
    List<FileItem> items =
        List.of(
            item("1", FileStatus.PDF_CONVERTED, ReviewStatus.REVIEW_REQUIRED),
            item("2", FileStatus.MARKDOWN_GENERATED, ReviewStatus.REVIEW_REQUIRED),
            item("3", FileStatus.PDF_CONVERT_FAILED, ReviewStatus.REVIEW_REQUIRED),
            item("4", FileStatus.FAILED, ReviewStatus.REVIEW_REQUIRED),
            item("5", FileStatus.UNSUPPORTED, ReviewStatus.REVIEW_REQUIRED),
            item("6", FileStatus.APPROVED, ReviewStatus.APPROVED));

    BatchMetricsResponse metrics = new MetricsCalculator().compute(items);

    assertThat(metrics.total()).isEqualTo(6);
    assertThat(metrics.pdfConverted()).isEqualTo(1);
    assertThat(metrics.markdownGenerated()).isEqualTo(1);
    assertThat(metrics.reviewRequired()).isEqualTo(5);
    assertThat(metrics.failed()).isEqualTo(2);
    assertThat(metrics.unsupported()).isEqualTo(1);
  }

  private FileItem item(String id, FileStatus status, ReviewStatus reviewStatus) {
    return FileItem.create(
        id,
        "batch",
        "Discovery/" + id + ".docx",
        SourceType.docx,
        status,
        BigDecimal.ONE,
        reviewStatus,
        OffsetDateTime.now());
  }
}
