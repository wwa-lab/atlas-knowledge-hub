package com.atlas.metadata.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceType;
import com.atlas.metadata.service.LocalArtifactStorageService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Unit tests for local PDF text extraction parser adapter. */
class LocalPdfTextParserAdapterTest {

  @TempDir private Path tempDir;

  @Test
  void parsesPdfIntoMarkdownAndReviewRequiredChunks() throws Exception {
    LocalArtifactStorageService artifacts = new LocalArtifactStorageService(tempDir);
    Path pdf = tempDir.resolve("uploads/space/session/source.pdf");
    Files.createDirectories(pdf.getParent());
    writePdf(pdf, "Atlas closes the loop with source trace evidence.");
    LocalPdfTextParserAdapter adapter = new LocalPdfTextParserAdapter(artifacts);

    ParserResult result =
        adapter.parse(
            new ParserRequest(
                "parse-run",
                "batch",
                List.of(
                    new ParserRequest.ParserFile(
                        "file-1",
                        "source.pdf",
                        SourceType.pdf,
                        FileStatus.PDF_CONVERTED,
                        "uploads/space/session/source.pdf")),
                "generated/markdown",
                "generated/assets",
                null));

    assertThat(result.adapterKey()).isEqualTo("local-pdf-text");
    assertThat(result.files()).singleElement().satisfies(file -> {
      assertThat(file.status()).isEqualTo(FileStatus.MARKDOWN_GENERATED);
      assertThat(file.markdownPath()).isEqualTo("generated/markdown/file-1.md");
      assertThat(file.confidence()).isEqualByComparingTo("0.950");
      assertThat(file.chunks()).singleElement().satisfies(chunk -> {
        assertThat(chunk.page()).isEqualTo(1);
        assertThat(chunk.section()).contains("Atlas closes the loop");
        assertThat(chunk.reviewStatus()).isEqualTo(ReviewStatus.REVIEW_REQUIRED);
      });
    });
    assertThat(Files.readString(tempDir.resolve("generated/markdown/file-1.md")))
        .contains("Atlas closes the loop");
  }

  private void writePdf(Path path, String text) throws Exception {
    try (PDDocument document = new PDDocument()) {
      PDPage page = new PDPage();
      document.addPage(page);
      try (PDPageContentStream content = new PDPageContentStream(document, page)) {
        content.beginText();
        content.setFont(PDType1Font.HELVETICA, 12);
        content.newLineAtOffset(72, 720);
        content.showText(text);
        content.endText();
      }
      document.save(path.toFile());
    }
  }
}
