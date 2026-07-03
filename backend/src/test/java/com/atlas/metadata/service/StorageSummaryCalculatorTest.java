package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.domain.StorageObject;
import com.atlas.metadata.enums.StorageLayer;
import com.atlas.metadata.enums.StorageObjectStatus;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for storage summary derivation. */
class StorageSummaryCalculatorTest {

  @Test
  void computesCountsAndStoredBytesFromDescriptorRows() {
    var summary =
        new StorageSummaryCalculator()
            .compute(
                List.of(
                    object("1", StorageObjectStatus.STORED, 100L),
                    object("2", StorageObjectStatus.DELETED, 20L),
                    object("3", StorageObjectStatus.MISSING, null),
                    object("4", StorageObjectStatus.FAILED, null)),
                1);

    assertThat(summary.total()).isEqualTo(5);
    assertThat(summary.stored()).isEqualTo(1);
    assertThat(summary.deleted()).isEqualTo(1);
    assertThat(summary.missing()).isEqualTo(1);
    assertThat(summary.failed()).isEqualTo(1);
    assertThat(summary.skipped()).isEqualTo(1);
    assertThat(summary.totalBytes()).isEqualTo(100);
  }

  private StorageObject object(String id, StorageObjectStatus status, Long sizeBytes) {
    return StorageObject.create(
        "storage-object-" + id,
        "operation",
        "batch",
        "file-" + id,
        StorageLayer.markdown,
        "batch/" + id + ".md",
        "text/markdown",
        sizeBytes,
        status == StorageObjectStatus.STORED ? "sha256:" + id : null,
        "mock-storage",
        status,
        status == StorageObjectStatus.FAILED ? "failed" : null,
        OffsetDateTime.now(),
        null);
  }
}
