package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ParserAdapterStatus;
import com.atlas.metadata.enums.ParserRunStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/** Unit checks for parser slice domain invariants. */
class ParserDomainInvariantTest {

  @Test
  void parserRunStatusSetMatchesSpec() {
    assertThat(Arrays.stream(ParserRunStatus.values()).map(Enum::name))
        .containsExactly("REQUESTED", "RUNNING", "SUCCEEDED", "PARTIAL_FAILED", "FAILED");
  }

  @Test
  void adapterStatusSetMatchesSpec() {
    assertThat(Arrays.stream(ParserAdapterStatus.values()).map(Enum::name))
        .containsExactly("AVAILABLE", "DISABLED", "MISCONFIGURED");
  }

  @Test
  void parserUpdatePreservesReviewStatusAndWritesMarkdownArtifacts() {
    FileItem item =
        FileItem.create(
            "file",
            "batch",
            "Discovery/BRD.pdf",
            SourceType.pdf,
            FileStatus.PDF_CONVERTED,
            BigDecimal.ONE,
            ReviewStatus.REVIEW_REQUIRED,
            OffsetDateTime.now());
    item.setArtifacts("generated/pdf/BRD.pdf", null, null, null);

    item.applyParserResult(
        FileStatus.MARKDOWN_GENERATED,
        new BigDecimal("0.910"),
        "generated/markdown/BRD.md",
        "generated/assets/BRD",
        null);

    assertThat(item.getStatus()).isEqualTo(FileStatus.MARKDOWN_GENERATED);
    assertThat(item.getPdfPath()).isEqualTo("generated/pdf/BRD.pdf");
    assertThat(item.getMarkdownPath()).isEqualTo("generated/markdown/BRD.md");
    assertThat(item.getAssetsPath()).isEqualTo("generated/assets/BRD");
    assertThat(item.getReviewStatus()).isEqualTo(ReviewStatus.REVIEW_REQUIRED);
  }

  @Test
  void sourceChunksDefaultToReviewRequired() {
    SourceChunk chunk =
        SourceChunk.create(
            "chunk",
            "file",
            "Discovery/BRD.pdf",
            1,
            "Overview",
            new BigDecimal("0.910"),
            null);

    assertThat(chunk.getReviewStatus()).isEqualTo(ReviewStatus.REVIEW_REQUIRED);
  }
}
