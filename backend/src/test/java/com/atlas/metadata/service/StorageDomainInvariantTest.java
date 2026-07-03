package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceType;
import com.atlas.metadata.enums.StorageAdapterStatus;
import com.atlas.metadata.enums.StorageLayer;
import com.atlas.metadata.enums.StorageObjectStatus;
import com.atlas.metadata.enums.StorageOperationStatus;
import com.atlas.metadata.enums.StorageOperationType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/** Unit checks for storage slice domain invariants. */
class StorageDomainInvariantTest {

  @Test
  void enumSetsMatchStorageSpec() {
    assertThat(Arrays.stream(StorageLayer.values()).map(Enum::name))
        .containsExactly("raw", "pdf", "markdown", "assets", "reports", "wiki");
    assertThat(Arrays.stream(StorageOperationType.values()).map(Enum::name))
        .containsExactly("STORE", "DELETE", "LIST");
    assertThat(Arrays.stream(StorageOperationStatus.values()).map(Enum::name))
        .containsExactly("REQUESTED", "RUNNING", "SUCCEEDED", "PARTIAL_FAILED", "FAILED");
    assertThat(Arrays.stream(StorageObjectStatus.values()).map(Enum::name))
        .containsExactly("STORED", "DELETED", "MISSING", "FAILED");
    assertThat(Arrays.stream(StorageAdapterStatus.values()).map(Enum::name))
        .containsExactly("AVAILABLE", "DISABLED", "MISCONFIGURED");
  }

  @Test
  void artifactPointerUpdateCanPreserveStatusConfidenceAndReviewStatus() {
    FileItem item =
        FileItem.create(
            "file",
            "batch",
            "Discovery/BRD.docx",
            SourceType.docx,
            FileStatus.MARKDOWN_GENERATED,
            new BigDecimal("0.910"),
            ReviewStatus.REVIEW_REQUIRED,
            OffsetDateTime.now());
    item.setArtifacts("batch/BRD.pdf", "batch/old.md", "batch/assets", null);

    item.setArtifacts(item.getPdfPath(), "batch/new.md", item.getAssetsPath(), item.getErrorMessage());

    assertThat(item.getStatus()).isEqualTo(FileStatus.MARKDOWN_GENERATED);
    assertThat(item.getConfidence()).isEqualByComparingTo("0.910");
    assertThat(item.getReviewStatus()).isEqualTo(ReviewStatus.REVIEW_REQUIRED);
    assertThat(item.getPdfPath()).isEqualTo("batch/BRD.pdf");
    assertThat(item.getMarkdownPath()).isEqualTo("batch/new.md");
  }
}
