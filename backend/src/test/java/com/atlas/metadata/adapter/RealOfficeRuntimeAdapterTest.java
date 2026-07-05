package com.atlas.metadata.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.adapter.runtime.RuntimeAdapterConfiguration;
import com.atlas.metadata.adapter.runtime.RuntimeExecutionResult;
import com.atlas.metadata.adapter.runtime.RuntimeExecutor;
import com.atlas.metadata.enums.ConverterAdapterStatus;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ParserAdapterStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for configured office/parser runtime adapters using fake executors. */
class RealOfficeRuntimeAdapterTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final RuntimeAdapterConfiguration AVAILABLE_CONFIG =
      new RuntimeAdapterConfiguration(true, "configured-command", Duration.ofSeconds(120), 65_536);

  @Test
  void converterCapabilityMasksDisabledMissingAndConfiguredState() {
    assertThat(converter(new RuntimeAdapterConfiguration(false, "", Duration.ofSeconds(120), 65_536), ok("{}"))
            .capability()
            .status())
        .isEqualTo(ConverterAdapterStatus.DISABLED);
    assertThat(converter(new RuntimeAdapterConfiguration(true, "", Duration.ofSeconds(120), 65_536), ok("{}"))
            .capability()
            .status())
        .isEqualTo(ConverterAdapterStatus.MISCONFIGURED);

    var capability = converter(AVAILABLE_CONFIG, ok("{}")).capability();

    assertThat(capability.status()).isEqualTo(ConverterAdapterStatus.AVAILABLE);
    assertThat(capability.defaultAdapter()).isFalse();
    assertThat(capability.maskedConfigSummary())
        .containsEntry("command", "configured")
        .containsEntry("externalNetwork", "disabled");
    assertThat(capability.maskedConfigSummary().values())
        .allSatisfy(value -> assertThat(value).doesNotContain("/", "\\", "configured-command"));
  }

  @Test
  void converterMapsRuntimeJsonToSafeProductResult() {
    TrinityOfficeConverterAdapter adapter =
        converter(
            AVAILABLE_CONFIG,
            ok(
                """
                {
                  "safeMessage": "Runtime conversion completed.",
                  "files": [
                    {
                      "fileId": "doc",
                      "status": "PDF_CONVERTED",
                      "pdfPath": "generated/pdf/doc.pdf",
                      "confidence": 0.991
                    }
                  ]
                }
                """));

    ConverterResult result =
        adapter.convert(
            new ConverterRequest(
                "run",
                "batch",
                List.of(new ConverterRequest.ConverterFile("doc", "input/doc.docx", SourceType.docx, FileStatus.UPLOADED)),
                "generated/pdf"));

    assertThat(result.adapterKey()).isEqualTo("trinity-office");
    assertThat(result.safeMessage()).isEqualTo("Runtime conversion completed.");
    assertThat(result.files().getFirst().status()).isEqualTo(FileStatus.PDF_CONVERTED);
    assertThat(result.files().getFirst().pdfPath()).isEqualTo("generated/pdf/doc.pdf");
    assertThat(result.files().getFirst().confidence()).isEqualByComparingTo("0.991");
  }

  @Test
  void converterMapsTimeoutAndOversizedSecretOutputToSafeFailure() {
    TrinityOfficeConverterAdapter adapter =
        converter(
            AVAILABLE_CONFIG,
            invocation ->
                new RuntimeExecutionResult(
                    124,
                    "",
                    "token=secret " + "/private/runtime/".repeat(5) + "x".repeat(70_000),
                    true));

    ConverterResult result =
        adapter.convert(
            new ConverterRequest(
                "run",
                "batch",
                List.of(new ConverterRequest.ConverterFile("doc", "input/doc.docx", SourceType.docx, FileStatus.UPLOADED)),
                "generated/pdf"));

    assertThat(result.safeMessage()).contains("timed out");
    assertThat(result.safeMessage()).doesNotContain("secret", "/private/runtime");
    assertThat(result.safeMessage()).hasSizeLessThanOrEqualTo(65_536);
    assertThat(result.files().getFirst().status()).isEqualTo(FileStatus.PDF_CONVERT_FAILED);
    assertThat(result.files().getFirst().safeError()).doesNotContain("secret", "/private/runtime");
  }

  @Test
  void converterRejectsUnsafeRuntimePdfPathBeforeReturningResult() {
    TrinityOfficeConverterAdapter adapter =
        converter(
            AVAILABLE_CONFIG,
            ok(
                """
                {
                  "files": [
                    {
                      "fileId": "doc",
                      "status": "PDF_CONVERTED",
                      "pdfPath": "https://internal.example.local/doc.pdf",
                      "confidence": 0.991
                    }
                  ]
                }
                """));

    ConverterResult result =
        adapter.convert(
            new ConverterRequest(
                "run",
                "batch",
                List.of(new ConverterRequest.ConverterFile("doc", "input/doc.docx", SourceType.docx, FileStatus.UPLOADED)),
                "generated/pdf"));

    assertThat(result.files().getFirst().status()).isEqualTo(FileStatus.PDF_CONVERT_FAILED);
    assertThat(result.files().getFirst().pdfPath()).isNull();
    assertThat(result.files().getFirst().safeError()).contains("path rejected");
  }

  @Test
  void parserCapabilityMasksDisabledMissingAndConfiguredState() {
    assertThat(parser(new RuntimeAdapterConfiguration(false, "", Duration.ofSeconds(120), 65_536), ok("{}"))
            .capability()
            .status())
        .isEqualTo(ParserAdapterStatus.DISABLED);
    assertThat(parser(new RuntimeAdapterConfiguration(true, "", Duration.ofSeconds(120), 65_536), ok("{}"))
            .capability()
            .status())
        .isEqualTo(ParserAdapterStatus.MISCONFIGURED);

    var capability = parser(AVAILABLE_CONFIG, ok("{}")).capability();

    assertThat(capability.status()).isEqualTo(ParserAdapterStatus.AVAILABLE);
    assertThat(capability.defaultAdapter()).isFalse();
    assertThat(capability.maskedConfigSummary())
        .containsEntry("command", "configured")
        .containsEntry("externalNetwork", "disabled");
    assertThat(capability.maskedConfigSummary().values())
        .allSatisfy(value -> assertThat(value).doesNotContain("/", "\\", "configured-command"));
  }

  @Test
  void parserMapsRuntimeJsonToReviewRequiredProductResult() {
    DocumentNormalizeParserAdapter adapter =
        parser(
            AVAILABLE_CONFIG,
            ok(
                """
                {
                  "safeMessage": "Runtime parsing completed.",
                  "files": [
                    {
                      "fileId": "pdf",
                      "status": "MARKDOWN_GENERATED",
                      "markdownPath": "generated/markdown/doc.md",
                      "assetsPath": "generated/assets/doc",
                      "confidence": 0.910,
                      "chunks": [
                        {
                          "chunkId": "chunk-pdf-001",
                          "page": 1,
                          "section": "Overview",
                          "confidence": 0.910
                        }
                      ]
                    }
                  ]
                }
                """));

    ParserResult result =
        adapter.parse(
            new ParserRequest(
                "run",
                "batch",
                List.of(file("pdf")),
                "generated/markdown",
                "generated/assets",
                new BigDecimal("0.800")));

    assertThat(result.adapterKey()).isEqualTo("document-normalize");
    assertThat(result.safeMessage()).isEqualTo("Runtime parsing completed.");
    assertThat(result.files().getFirst().status()).isEqualTo(FileStatus.MARKDOWN_GENERATED);
    assertThat(result.files().getFirst().markdownPath()).isEqualTo("generated/markdown/doc.md");
    assertThat(result.files().getFirst().chunks().getFirst().reviewStatus())
        .isEqualTo(ReviewStatus.REVIEW_REQUIRED);
  }

  @Test
  void parserMapsLowConfidenceAndRejectsUnsafeRuntimePaths() {
    DocumentNormalizeParserAdapter adapter =
        parser(
            AVAILABLE_CONFIG,
            ok(
                """
                {
                  "files": [
                    {
                      "fileId": "pdf",
                      "status": "MARKDOWN_GENERATED",
                      "markdownPath": "../unsafe/doc.md",
                      "assetsPath": "generated/assets/doc",
                      "confidence": 0.700,
                      "chunks": []
                    }
                  ]
                }
                """));

    ParserResult result =
        adapter.parse(
            new ParserRequest(
                "run",
                "batch",
                List.of(file("pdf")),
                "generated/markdown",
                "generated/assets",
                new BigDecimal("0.800")));

    assertThat(result.files().getFirst().status()).isEqualTo(FileStatus.FAILED);
    assertThat(result.files().getFirst().markdownPath()).isNull();
    assertThat(result.files().getFirst().safeError()).contains("path rejected");
  }

  private static RuntimeExecutor ok(String stdout) {
    return invocation -> new RuntimeExecutionResult(0, stdout, "", false);
  }

  private static TrinityOfficeConverterAdapter converter(
      RuntimeAdapterConfiguration configuration, RuntimeExecutor executor) {
    return new TrinityOfficeConverterAdapter(configuration, executor, OBJECT_MAPPER);
  }

  private static DocumentNormalizeParserAdapter parser(
      RuntimeAdapterConfiguration configuration, RuntimeExecutor executor) {
    return new DocumentNormalizeParserAdapter(configuration, executor, OBJECT_MAPPER);
  }

  private static ParserRequest.ParserFile file(String id) {
    return new ParserRequest.ParserFile(
        id, "input/doc.pdf", SourceType.pdf, FileStatus.PDF_CONVERTED, "generated/pdf/doc.pdf");
  }
}
