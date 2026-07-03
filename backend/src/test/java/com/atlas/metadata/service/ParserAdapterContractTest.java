package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.adapter.DocumentNormalizeParserAdapter;
import com.atlas.metadata.adapter.MockDocumentNormalizeParserAdapter;
import com.atlas.metadata.adapter.ParserRequest;
import com.atlas.metadata.adapter.ParserResult;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ParserAdapterStatus;
import com.atlas.metadata.enums.SourceType;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for parser adapter contracts and mock outcomes. */
class ParserAdapterContractTest {

  private final MockDocumentNormalizeParserAdapter adapter = new MockDocumentNormalizeParserAdapter();

  @Test
  void capabilityIsMaskedDefaultedAndThresholded() {
    var capability = adapter.capability();

    assertThat(capability.adapterKey()).isEqualTo("document-normalize");
    assertThat(capability.defaultAdapter()).isTrue();
    assertThat(capability.status()).isEqualTo(ParserAdapterStatus.AVAILABLE);
    assertThat(capability.inputTypes()).containsExactly(SourceType.pdf);
    assertThat(capability.outputTypes()).containsExactly("markdown", "assets");
    assertThat(capability.lowConfidenceThreshold()).isEqualByComparingTo("0.800");
    assertThat(capability.maskedConfigSummary()).containsEntry("externalNetwork", "disabled");
    assertThat(capability.maskedConfigSummary().values())
        .allSatisfy(value -> assertThat(value).doesNotContain("/", "\\", "password"));
  }

  @Test
  void mockAdapterMapsEachPdfOutcomeAndChunks() {
    ParserResult result =
        adapter.parse(
            new ParserRequest(
                "run",
                "batch",
                List.of(
                    file("success", "Discovery/BRD.pdf"),
                    file("low", "Discovery/low-confidence.pdf"),
                    file("ocr", "Discovery/needs-ocr.pdf"),
                    file("broken", "Discovery/broken.pdf")),
                "generated/markdown",
                "generated/assets",
                new BigDecimal("0.800")));

    assertThat(result.files())
        .extracting(ParserResult.ParserFileResult::status)
        .containsExactly(
            FileStatus.MARKDOWN_GENERATED,
            FileStatus.LOW_CONFIDENCE,
            FileStatus.OCR_REQUIRED,
            FileStatus.FAILED);
    assertThat(result.files().getFirst().markdownPath()).isEqualTo("generated/markdown/BRD.md");
    assertThat(result.files().getFirst().assetsPath()).isEqualTo("generated/assets/BRD");
    assertThat(result.files().getFirst().chunks()).hasSize(2);
    assertThat(result.files().get(1).confidence()).isLessThan(new BigDecimal("0.800"));
    assertThat(result.files().get(3).safeError()).isEqualTo("Mock parser failure.");
  }

  @Test
  void documentNormalizeWrapperReportsMisconfiguredWithoutLeakingRawConfig() {
    var capability = new DocumentNormalizeParserAdapter("raw-command-placeholder").capability();

    assertThat(capability.status()).isEqualTo(ParserAdapterStatus.MISCONFIGURED);
    assertThat(capability.maskedConfigSummary()).containsEntry("command", "missing");
    assertThat(capability.maskedConfigSummary().values())
        .allSatisfy(value -> assertThat(value).doesNotContain("/", "\\", "raw-command-placeholder"));
  }

  private ParserRequest.ParserFile file(String id, String pdfPath) {
    return new ParserRequest.ParserFile(
        id, pdfPath, SourceType.pdf, FileStatus.PDF_CONVERTED, pdfPath);
  }
}
