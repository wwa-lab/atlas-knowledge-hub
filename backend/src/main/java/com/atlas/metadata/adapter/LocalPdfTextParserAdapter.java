package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ParserAdapterStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceType;
import com.atlas.metadata.service.LocalArtifactStorageService;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

/** Local PDFBox-backed text parser for the v1 upload closed loop. */
@Component
public class LocalPdfTextParserAdapter implements ParserAdapter {

  private static final String ADAPTER_KEY = "local-pdf-text";
  private static final BigDecimal TEXT_CONFIDENCE = new BigDecimal("0.950");
  private static final BigDecimal OCR_REQUIRED_CONFIDENCE = new BigDecimal("0.400");

  private final LocalArtifactStorageService artifactStorage;

  /** Creates a local PDF text parser adapter. */
  public LocalPdfTextParserAdapter(LocalArtifactStorageService artifactStorage) {
    this.artifactStorage = artifactStorage;
  }

  @Override
  public ParserCapability capability() {
    return new ParserCapability(
        ADAPTER_KEY,
        "Local PDF Text Parser",
        "pdfbox-2.0.30",
        List.of(SourceType.pdf),
        List.of("markdown", "source-chunks"),
        false,
        ParserAdapterStatus.AVAILABLE,
        new BigDecimal("0.700"),
        Map.of("engine", "pdfbox", "externalNetwork", "disabled", "credential", "not-required"));
  }

  @Override
  public ParserResult parse(ParserRequest request) {
    List<ParserResult.ParserFileResult> files =
        request.files() == null ? List.of() : request.files().stream().map(file -> parseFile(request, file)).toList();
    return new ParserResult(ADAPTER_KEY, files, "Local PDF text parsing completed.");
  }

  private ParserResult.ParserFileResult parseFile(ParserRequest request, ParserRequest.ParserFile file) {
    if (file.sourceType() != SourceType.pdf || file.pdfPath() == null || file.pdfPath().isBlank()) {
      return failed(file.fileId(), "Only stored PDF files are supported by local-pdf-text.");
    }
    try (PDDocument document = PDDocument.load(artifactStorage.resolve(file.pdfPath()).toFile())) {
      List<PageText> pages = extractPages(document);
      if (pages.isEmpty()) {
        return new ParserResult.ParserFileResult(
            file.fileId(), FileStatus.OCR_REQUIRED, null, null, OCR_REQUIRED_CONFIDENCE, "No embedded text found.", List.of());
      }
      String markdown = markdown(file, pages);
      String markdownPath = artifactStorage.writeGeneratedMarkdown(request.markdownRoot(), file.fileId(), markdown);
      return new ParserResult.ParserFileResult(
          file.fileId(),
          FileStatus.MARKDOWN_GENERATED,
          markdownPath,
          null,
          TEXT_CONFIDENCE,
          null,
          chunks(file, pages));
    } catch (IOException | RuntimeException ex) {
      return failed(file.fileId(), "PDF text extraction failed safely.");
    }
  }

  private List<PageText> extractPages(PDDocument document) throws IOException {
    List<PageText> pages = new ArrayList<>();
    int totalPages = document.getNumberOfPages();
    for (int page = 1; page <= totalPages; page++) {
      PDFTextStripper stripper = new PDFTextStripper();
      stripper.setStartPage(page);
      stripper.setEndPage(page);
      String text = normalizeWhitespace(stripper.getText(document));
      if (!text.isBlank()) {
        pages.add(new PageText(page, text));
      }
    }
    return pages;
  }

  private String markdown(ParserRequest.ParserFile file, List<PageText> pages) {
    StringBuilder markdown = new StringBuilder();
    markdown.append("# ").append(file.sourcePath()).append("\n\n");
    for (PageText page : pages) {
      markdown.append("## Page ").append(page.page()).append("\n\n");
      markdown.append(page.text()).append("\n\n");
    }
    return markdown.toString();
  }

  private List<ParserResult.ParserChunkResult> chunks(ParserRequest.ParserFile file, List<PageText> pages) {
    return pages.stream()
        .map(
            page ->
                new ParserResult.ParserChunkResult(
                    "chunk-" + file.fileId() + "-p" + page.page(),
                    page.page(),
                    excerpt(page.text()),
                    TEXT_CONFIDENCE,
                    ReviewStatus.REVIEW_REQUIRED))
        .toList();
  }

  private ParserResult.ParserFileResult failed(String fileId, String safeError) {
    return new ParserResult.ParserFileResult(
        fileId, FileStatus.FAILED, null, null, BigDecimal.ZERO, safeError, List.of());
  }

  private String excerpt(String text) {
    String normalized = normalizeWhitespace(text);
    return normalized.length() <= 180 ? normalized : normalized.substring(0, 180);
  }

  private String normalizeWhitespace(String value) {
    return value == null ? "" : value.replaceAll("\\s+", " ").trim();
  }

  private record PageText(int page, String text) {}
}
