package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.atlas.metadata.adapter.ConfiguredS3CompatibleStorageAdapter;
import com.atlas.metadata.adapter.MockInMemoryStorageAdapter;
import com.atlas.metadata.exception.RequestValidationException;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for storage adapter registry resolution. */
class StorageAdapterRegistryTest {

  @Test
  void resolvesExplicitAndDefaultStorageAdapters() {
    StorageAdapterRegistry registry =
        new StorageAdapterRegistry(
            List.of(new ConfiguredS3CompatibleStorageAdapter(), new MockInMemoryStorageAdapter()));

    assertThat(registry.resolve(null).capability().adapterKey()).isEqualTo("mock-storage");
    assertThat(registry.resolve("s3-compatible").capability().adapterKey()).isEqualTo("s3-compatible");
    assertThat(registry.capabilities())
        .extracting(capability -> capability.adapterKey())
        .containsExactly("mock-storage", "s3-compatible");
  }

  @Test
  void rejectsUnknownStorageAdapterKey() {
    StorageAdapterRegistry registry = new StorageAdapterRegistry(List.of(new MockInMemoryStorageAdapter()));

    assertThatThrownBy(() -> registry.resolve("unknown-storage"))
        .isInstanceOf(RequestValidationException.class)
        .satisfies(
            error ->
                assertThat(((RequestValidationException) error).getFields())
                    .containsEntry("adapterKey", "must reference a configured storage adapter"));
  }
}
