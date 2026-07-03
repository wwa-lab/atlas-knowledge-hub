package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.atlas.metadata.adapter.MockVectorAdapter;
import com.atlas.metadata.adapter.PgVectorAdapter;
import com.atlas.metadata.adapter.VectorCapability;
import com.atlas.metadata.exception.RequestValidationException;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for vector adapter registry resolution. */
class VectorAdapterRegistryTest {

  @Test
  void listsDefaultCapabilityFirstAndResolvesDefaultOrExplicitAdapter() {
    VectorAdapterRegistry registry =
        new VectorAdapterRegistry(List.of(new PgVectorAdapter(), new MockVectorAdapter()));

    assertThat(registry.capabilities()).extracting(VectorCapability::adapterKey)
        .containsExactly("mock-vector", "pgvector");
    assertThat(registry.resolve(null).capability().adapterKey()).isEqualTo("mock-vector");
    assertThat(registry.resolve("pgvector").capability().adapterKey()).isEqualTo("pgvector");
  }

  @Test
  void rejectsUnknownAdapterKeyWithValidationError() {
    VectorAdapterRegistry registry = new VectorAdapterRegistry(List.of(new MockVectorAdapter()));

    assertThatThrownBy(() -> registry.resolve("unknown"))
        .isInstanceOf(RequestValidationException.class)
        .satisfies(
            error ->
                assertThat(((RequestValidationException) error).getFields())
                    .containsEntry("adapterKey", "must reference a configured vector adapter"));
  }
}
