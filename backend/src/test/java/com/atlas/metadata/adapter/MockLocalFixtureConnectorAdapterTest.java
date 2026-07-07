package com.atlas.metadata.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.enums.ConnectorReviewPolicy;
import com.atlas.metadata.enums.ConnectorSafeErrorCategory;
import org.junit.jupiter.api.Test;

/** Unit tests for the mock/local fixture connector adapter boundary. */
class MockLocalFixtureConnectorAdapterTest {

  private final MockLocalFixtureConnectorAdapter adapter = new MockLocalFixtureConnectorAdapter();

  @Test
  void exposesOnlySafeCapabilityMetadata() {
    ConnectorCapability capability = adapter.capability();

    assertThat(capability.connectorKey()).isEqualTo("mock-local-fixture");
    assertThat(capability.reviewPolicy()).isEqualTo(ConnectorReviewPolicy.REVIEW_REQUIRED);
    assertThat(capability.capabilitySummary()).contains("Sample-safe");
  }

  @Test
  void producesDeterministicFixtureItemsWithTraceAndProvenance() {
    ConnectorSyncResult first =
        adapter.sync(
            new ConnectorSyncRequest(
                "connector-run-test", "ibm-i-modernization", "mock-local-fixture", "sample-fixture"));
    ConnectorSyncResult second =
        adapter.sync(
            new ConnectorSyncRequest(
                "connector-run-test", "ibm-i-modernization", "mock-local-fixture", "sample-fixture"));

    assertThat(first).isEqualTo(second);
    assertThat(first.items()).hasSize(3);
    assertThat(first.items().get(0).sourceTrace()).containsEntry("connectorKey", "mock-local-fixture");
    assertThat(first.items().get(0).provenance()).containsEntry("syncRunId", "connector-run-test");
    assertThat(first.items().get(0).reviewRequired()).isTrue();
    assertThat(first.items().get(2).safeErrorCategory())
        .isEqualTo(ConnectorSafeErrorCategory.SOURCE_UNREADABLE);
  }
}
