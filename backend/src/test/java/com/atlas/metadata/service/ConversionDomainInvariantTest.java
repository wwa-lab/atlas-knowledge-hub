package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.enums.ConversionRunStatus;
import com.atlas.metadata.enums.ConverterAdapterStatus;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/** Unit checks for converter slice domain invariants. */
class ConversionDomainInvariantTest {

  @Test
  void conversionRunStatusSetMatchesSpec() {
    assertThat(Arrays.stream(ConversionRunStatus.values()).map(Enum::name))
        .containsExactly("REQUESTED", "RUNNING", "SUCCEEDED", "PARTIAL_FAILED", "FAILED");
  }

  @Test
  void adapterStatusSetMatchesSpec() {
    assertThat(Arrays.stream(ConverterAdapterStatus.values()).map(Enum::name))
        .containsExactly("AVAILABLE", "DISABLED", "MISCONFIGURED");
  }

  @Test
  void conversionUpdatePreservesReviewStatus() {
    FileItem item =
        FileItem.create(
            "file",
            "batch",
            "Discovery/BRD.docx",
            SourceType.docx,
            FileStatus.UPLOADED,
            BigDecimal.ZERO,
            ReviewStatus.REVIEW_REQUIRED,
            OffsetDateTime.now());

    item.applyConversionResult(
        FileStatus.PDF_CONVERTED, BigDecimal.ONE, "generated/pdf/BRD.pdf", null);

    assertThat(item.getStatus()).isEqualTo(FileStatus.PDF_CONVERTED);
    assertThat(item.getPdfPath()).isEqualTo("generated/pdf/BRD.pdf");
    assertThat(item.getReviewStatus()).isEqualTo(ReviewStatus.REVIEW_REQUIRED);
  }
}
