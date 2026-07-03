package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.adapter.ConverterRequest;
import com.atlas.metadata.adapter.ConverterResult;
import com.atlas.metadata.adapter.MockTrinityOfficeConverterAdapter;
import com.atlas.metadata.adapter.TrinityOfficeConverterAdapter;
import com.atlas.metadata.enums.ConverterAdapterStatus;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.SourceType;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for converter adapter contracts and mock outcomes. */
class ConverterAdapterContractTest {

  private final MockTrinityOfficeConverterAdapter adapter = new MockTrinityOfficeConverterAdapter();

  @Test
  void capabilityIsMaskedAndDefaulted() {
    var capability = adapter.capability();

    assertThat(capability.adapterKey()).isEqualTo("trinity-office");
    assertThat(capability.defaultAdapter()).isTrue();
    assertThat(capability.status()).isEqualTo(ConverterAdapterStatus.AVAILABLE);
    assertThat(capability.supportedSourceTypes())
        .containsExactly(SourceType.pptx, SourceType.docx, SourceType.xlsx, SourceType.pdf);
    assertThat(capability.maskedConfigSummary()).containsEntry("externalNetwork", "disabled");
    assertThat(capability.maskedConfigSummary().values())
        .allSatisfy(value -> assertThat(value).doesNotContain("/", "\\", "password"));
  }

  @Test
  void mockAdapterMapsEachSourceOutcome() {
    ConverterResult result =
        adapter.convert(
            new ConverterRequest(
                "run",
                "batch",
                List.of(
                    file("doc", "Discovery/BRD.docx", SourceType.docx),
                    file("pdf", "Discovery/Current-State.pdf", SourceType.pdf),
                    file("image", "Discovery/screen.png", SourceType.image),
                    file("unsupported", "Discovery/notes.txt", SourceType.unsupported),
                    file("broken", "Discovery/broken.pptx", SourceType.pptx)),
                "generated/pdf"));

    assertThat(result.files())
        .extracting(ConverterResult.ConverterFileResult::status)
        .containsExactly(
            FileStatus.PDF_CONVERTED,
            FileStatus.PDF_CONVERTED,
            FileStatus.OCR_REQUIRED,
            FileStatus.UNSUPPORTED,
            FileStatus.PDF_CONVERT_FAILED);
    assertThat(result.files().getFirst().pdfPath()).isEqualTo("generated/pdf/BRD.pdf");
    assertThat(result.files().get(4).safeError()).isEqualTo("Mock conversion failure.");
  }

  @Test
  void trinityWrapperReportsMisconfiguredWithoutLeakingRawConfig() {
    var capability = new TrinityOfficeConverterAdapter("raw-command-placeholder").capability();

    assertThat(capability.status()).isEqualTo(ConverterAdapterStatus.MISCONFIGURED);
    assertThat(capability.maskedConfigSummary()).containsEntry("command", "missing");
    assertThat(capability.maskedConfigSummary().values())
        .allSatisfy(value -> assertThat(value).doesNotContain("/", "\\", "raw-command-placeholder"));
  }

  private ConverterRequest.ConverterFile file(String id, String sourcePath, SourceType sourceType) {
    return new ConverterRequest.ConverterFile(id, sourcePath, sourceType, FileStatus.UPLOADED);
  }
}
