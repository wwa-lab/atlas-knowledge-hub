package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ParserAdapterStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Deterministic mock parser for CI and local contract tests. */
@Component
public class MockDocumentNormalizeParserAdapter implements ParserAdapter {

  public static final String ADAPTER_KEY = "document-normalize";
  private static final BigDecimal LOW_CONFIDENCE_THRESHOLD = new BigDecimal("0.800");

  @Override
  public ParserCapability capability() {
    return new ParserCapability(
        ADAPTER_KEY,
        "Document Normalize Parser",
        "configured",
        List.of(SourceType.pdf),
        List.of("markdown", "assets"),
        true,
        ParserAdapterStatus.AVAILABLE,
        LOW_CONFIDENCE_THRESHOLD,
        Map.of(
            "command", "configured",
            "workingDirectory", "configured",
            "externalNetwork", "disabled"));
  }

  @Override
  public ParserResult parse(ParserRequest request) {
    List<ParserResult.ParserFileResult> results = request.files().stream().map(this::parseFile).toList();
    return new ParserResult(ADAPTER_KEY, results, "Mock parsing completed with review-required output.");
  }

  private ParserResult.ParserFileResult parseFile(ParserRequest.ParserFile file) {
    String discriminator = (file.sourcePath() + " " + file.pdfPath()).toLowerCase();
    if (discriminator.contains("ocr")) {
      return new ParserResult.ParserFileResult(
          file.fileId(), FileStatus.OCR_REQUIRED, null, null, BigDecimal.ZERO, "OCR is required.", List.of());
    }
    if (discriminator.contains("broken") || discriminator.contains("fail")) {
      return new ParserResult.ParserFileResult(
          file.fileId(), FileStatus.FAILED, null, null, BigDecimal.ZERO, "Mock parser failure.", List.of());
    }
    BigDecimal confidence = discriminator.contains("low") ? new BigDecimal("0.700") : new BigDecimal("0.910");
    FileStatus status =
        confidence.compareTo(LOW_CONFIDENCE_THRESHOLD) < 0
            ? FileStatus.LOW_CONFIDENCE
            : FileStatus.MARKDOWN_GENERATED;
    return new ParserResult.ParserFileResult(
        file.fileId(),
        status,
        markdownPath(file.pdfPath()),
        assetsPath(file.pdfPath()),
        confidence,
        null,
        chunks(file, confidence));
  }

  private List<ParserResult.ParserChunkResult> chunks(ParserRequest.ParserFile file, BigDecimal confidence) {
    return List.of(
        new ParserResult.ParserChunkResult(
            "chunk-" + file.fileId() + "-001", 1, "Overview", confidence, ReviewStatus.REVIEW_REQUIRED),
        new ParserResult.ParserChunkResult(
            "chunk-" + file.fileId() + "-002", 2, "Details", confidence, ReviewStatus.REVIEW_REQUIRED));
  }

  private String markdownPath(String pdfPath) {
    return "generated/markdown/" + baseName(pdfPath) + ".md";
  }

  private String assetsPath(String pdfPath) {
    return "generated/assets/" + baseName(pdfPath);
  }

  private String baseName(String path) {
    String fileName = path.substring(path.lastIndexOf('/') + 1);
    int extension = fileName.lastIndexOf('.');
    return extension > 0 ? fileName.substring(0, extension) : fileName;
  }
}
