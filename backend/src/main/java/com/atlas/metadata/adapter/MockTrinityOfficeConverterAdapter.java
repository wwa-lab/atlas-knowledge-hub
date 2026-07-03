package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.ConverterAdapterStatus;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.SourceType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Deterministic mock converter for CI and local contract tests. */
@Component
public class MockTrinityOfficeConverterAdapter implements ConverterAdapter {

  public static final String ADAPTER_KEY = "trinity-office";

  @Override
  public ConverterCapability capability() {
    return new ConverterCapability(
        ADAPTER_KEY,
        "Trinity Office Converter",
        "configured",
        "pdf",
        List.of(SourceType.pptx, SourceType.docx, SourceType.xlsx, SourceType.pdf),
        true,
        ConverterAdapterStatus.AVAILABLE,
        Map.of(
            "command", "configured",
            "workingDirectory", "configured",
            "externalNetwork", "disabled"));
  }

  @Override
  public ConverterResult convert(ConverterRequest request) {
    List<ConverterResult.ConverterFileResult> results =
        request.files().stream().map(this::convertFile).toList();
    return new ConverterResult(ADAPTER_KEY, results, "Mock conversion completed.");
  }

  private ConverterResult.ConverterFileResult convertFile(ConverterRequest.ConverterFile file) {
    return switch (file.sourceType()) {
      case pptx, docx, xlsx -> officeResult(file);
      case pdf ->
          new ConverterResult.ConverterFileResult(
              file.fileId(), FileStatus.PDF_CONVERTED, pdfPath(file.sourcePath()), BigDecimal.ONE, null);
      case image ->
          new ConverterResult.ConverterFileResult(
              file.fileId(), FileStatus.OCR_REQUIRED, null, BigDecimal.ZERO, "OCR is required.");
      case unsupported ->
          new ConverterResult.ConverterFileResult(
              file.fileId(), FileStatus.UNSUPPORTED, null, BigDecimal.ZERO, "Unsupported source type.");
    };
  }

  private ConverterResult.ConverterFileResult officeResult(ConverterRequest.ConverterFile file) {
    if (file.sourcePath().contains("broken") || file.sourcePath().contains("fail")) {
      return new ConverterResult.ConverterFileResult(
          file.fileId(), FileStatus.PDF_CONVERT_FAILED, null, BigDecimal.ZERO, "Mock conversion failure.");
    }
    return new ConverterResult.ConverterFileResult(
        file.fileId(), FileStatus.PDF_CONVERTED, pdfPath(file.sourcePath()), BigDecimal.ONE, null);
  }

  private String pdfPath(String sourcePath) {
    String fileName = sourcePath.substring(sourcePath.lastIndexOf('/') + 1);
    int extension = fileName.lastIndexOf('.');
    String base = extension > 0 ? fileName.substring(0, extension) : fileName;
    return "generated/pdf/" + base + ".pdf";
  }
}
