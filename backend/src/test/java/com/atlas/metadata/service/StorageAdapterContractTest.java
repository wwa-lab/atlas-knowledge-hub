package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.adapter.ConfiguredS3CompatibleStorageAdapter;
import com.atlas.metadata.adapter.MockInMemoryStorageAdapter;
import com.atlas.metadata.adapter.StorageListRequest;
import com.atlas.metadata.adapter.StorageObjectRef;
import com.atlas.metadata.adapter.StoragePutRequest;
import com.atlas.metadata.enums.StorageAdapterStatus;
import com.atlas.metadata.enums.StorageLayer;
import com.atlas.metadata.enums.StorageObjectStatus;
import org.junit.jupiter.api.Test;

/** Unit tests for storage adapter contracts and mock outcomes. */
class StorageAdapterContractTest {

  private final MockInMemoryStorageAdapter adapter = new MockInMemoryStorageAdapter();

  @Test
  void capabilityIsMaskedDefaultedAndLayerAware() {
    var capability = adapter.capability();

    assertThat(capability.adapterKey()).isEqualTo("mock-storage");
    assertThat(capability.defaultAdapter()).isTrue();
    assertThat(capability.status()).isEqualTo(StorageAdapterStatus.AVAILABLE);
    assertThat(capability.supportedLayers())
        .containsExactly(
            StorageLayer.raw,
            StorageLayer.pdf,
            StorageLayer.markdown,
            StorageLayer.assets,
            StorageLayer.reports,
            StorageLayer.wiki);
    assertThat(capability.maskedConfigSummary()).containsEntry("externalNetwork", "disabled");
    assertThat(capability.maskedConfigSummary().values())
        .allSatisfy(value -> assertThat(value).doesNotContain("/", "\\", "password", "token"));
  }

  @Test
  void mockAdapterStoresGetsListsExistsAndDeletesObjects() {
    var put =
        adapter.put(
            new StoragePutRequest(
                "operation",
                "batch",
                "space",
                StorageLayer.markdown,
                "batch/BRD.md",
                "text/markdown",
                "file:batch/BRD.md",
                "file-001"));

    assertThat(put.status()).isEqualTo(StorageObjectStatus.STORED);
    assertThat(put.checksum()).startsWith("sha256:");
    assertThat(put.sizeBytes()).isPositive();
    var ref =
        new StorageObjectRef(
            "operation", "batch", "space", StorageLayer.markdown, "batch/BRD.md", "file-001");
    assertThat(adapter.exists(ref)).isTrue();
    assertThat(adapter.get(ref).status()).isEqualTo(StorageObjectStatus.STORED);
    assertThat(adapter.list(new StorageListRequest("batch", "space", StorageLayer.markdown, 10, null)).objects())
        .singleElement()
        .satisfies(object -> assertThat(object.objectKey()).isEqualTo("batch/BRD.md"));
    assertThat(adapter.delete(ref).descriptor().status()).isEqualTo(StorageObjectStatus.DELETED);
    assertThat(adapter.exists(ref)).isFalse();
    assertThat(adapter.get(ref).status()).isEqualTo(StorageObjectStatus.MISSING);
  }

  @Test
  void configuredS3BoundaryReportsMisconfiguredWithoutLeakingRawConfig() {
    var capability = new ConfiguredS3CompatibleStorageAdapter().capability();

    assertThat(capability.adapterKey()).isEqualTo("s3-compatible");
    assertThat(capability.defaultAdapter()).isFalse();
    assertThat(capability.status()).isEqualTo(StorageAdapterStatus.MISCONFIGURED);
    assertThat(capability.maskedConfigSummary())
        .containsEntry("endpoint", "missing")
        .containsEntry("credentials", "missing")
        .containsEntry("externalNetwork", "disabled");
    assertThat(capability.maskedConfigSummary().values())
        .allSatisfy(value -> assertThat(value).doesNotContain("/", "\\", "http", "AK" + "IA"));
  }
}
