package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.adapter.MockVectorAdapter;
import com.atlas.metadata.adapter.VectorDeleteRequest;
import com.atlas.metadata.adapter.VectorIndexRequest;
import com.atlas.metadata.adapter.VectorQueryRequest;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.VectorItemStatus;
import com.atlas.metadata.enums.VectorReviewPolicy;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for the mock vector adapter contract. */
class VectorAdapterContractTest {

  @Test
  void mockAdapterExposesMaskedCapabilityAndSupportsIndexQueryDelete() {
    MockVectorAdapter adapter = new MockVectorAdapter();

    assertThat(adapter.capability().adapterKey()).isEqualTo("mock-vector");
    assertThat(adapter.capability().supportedOperations()).containsExactly("INDEX", "DEINDEX", "QUERY");
    assertThat(adapter.capability().maskedConfigSummary())
        .containsEntry("endpoint", "not_configured")
        .containsEntry("credentials", "not_configured");

    var index =
        adapter.index(
            new VectorIndexRequest(
                "run-001",
                "space",
                "batch",
                "mock",
                VectorReviewPolicy.APPROVED_ONLY,
                3,
                List.of(
                    new VectorIndexRequest.VectorIndexItem(
                        "chunk-001",
                        "file-001",
                        "BRD.pdf",
                        1,
                        "Overview",
                        ReviewStatus.APPROVED,
                        new BigDecimal("0.930"),
                        List.of(new BigDecimal("0.100"), new BigDecimal("0.200"), new BigDecimal("0.300"))))));

    assertThat(index.adapterKey()).isEqualTo("mock-vector");
    assertThat(index.items()).singleElement().satisfies(item -> {
      assertThat(item.status()).isEqualTo(VectorItemStatus.INDEXED);
      assertThat(item.vectorItemKey()).isEqualTo("space/chunk-001");
    });

    var query =
        adapter.query(
            new VectorQueryRequest(
                "space",
                List.of(new BigDecimal("0.100"), new BigDecimal("0.200"), new BigDecimal("0.300")),
                null,
                5,
                VectorReviewPolicy.APPROVED_ONLY));

    assertThat(query.matches()).singleElement().satisfies(match -> {
      assertThat(match.sourceChunkId()).isEqualTo("chunk-001");
      assertThat(match.score()).isEqualByComparingTo("1.000");
      assertThat(match.safeMetadata()).containsEntry("vectorItemKey", "space/chunk-001");
    });

    var deleted =
        adapter.delete(new VectorDeleteRequest("run-002", "space", "batch", List.of("chunk-001")));

    assertThat(deleted.items()).singleElement().satisfies(item -> {
      assertThat(item.status()).isEqualTo(VectorItemStatus.DELETED);
      assertThat(item.vectorItemKey()).isEqualTo("space/chunk-001");
    });
  }
}
