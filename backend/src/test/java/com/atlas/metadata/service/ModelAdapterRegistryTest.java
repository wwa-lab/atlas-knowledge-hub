package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.atlas.metadata.adapter.ConfiguredModelAdapter;
import com.atlas.metadata.adapter.MockModelAdapter;
import com.atlas.metadata.enums.ModelOperation;
import com.atlas.metadata.exception.RequestValidationException;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for model adapter registry resolution. */
class ModelAdapterRegistryTest {

  private final ModelAdapterRegistry registry =
      new ModelAdapterRegistry(List.of(new MockModelAdapter(), new ConfiguredModelAdapter(null)));

  @Test
  void capabilitiesSortDefaultModelsFirst() {
    var capabilities = registry.capabilities();

    assertThat(capabilities.getFirst().defaultModel()).isTrue();
    assertThat(capabilities).anySatisfy(capability -> assertThat(capability.status().name()).isEqualTo("MISCONFIGURED"));
  }

  @Test
  void resolvesExplicitAndDefaultModelSelections() {
    var explicit = registry.resolve("mock-model", "text-embedding-v4", ModelOperation.EMBEDDING);
    var defaulted = registry.resolve(null, null, ModelOperation.CHAT);

    assertThat(explicit.capability().modelKey()).isEqualTo("text-embedding-v4");
    assertThat(defaulted.capability().modelKey()).isEqualTo("deepseek-flash");
  }

  @Test
  void rejectsUnknownAndIncompatibleSelections() {
    assertThatThrownBy(() -> registry.resolve("missing", null, ModelOperation.CHAT))
        .isInstanceOf(RequestValidationException.class);
    assertThatThrownBy(() -> registry.resolve("mock-model", "deepseek-flash", ModelOperation.EMBEDDING))
        .isInstanceOf(RequestValidationException.class)
        .satisfies(
            error ->
                assertThat(((RequestValidationException) error).getFields())
                    .containsEntry("modelKey", "must support the requested model operation"));
  }
}
