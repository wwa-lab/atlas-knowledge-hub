package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.atlas.metadata.adapter.ConfiguredModelAdapter;
import com.atlas.metadata.adapter.MockModelAdapter;
import com.atlas.metadata.enums.ModelOperation;
import com.atlas.metadata.exception.RequestValidationException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for model adapter registry resolution. */
class ModelAdapterRegistryTest {

  private final ModelAdapterRegistry registry =
      new ModelAdapterRegistry(List.of(new MockModelAdapter(), new ConfiguredModelAdapter((String) null)));

  private final ModelAdapterRegistry deepSeekRegistry =
      new ModelAdapterRegistry(
          List.of(
              new MockModelAdapter(),
              new ConfiguredModelAdapter(
                  Map.of(
                      "ATLAS_MODEL_PROVIDER",
                      "deepseek",
                      "ATLAS_MODEL_ENDPOINT",
                      "https://api.deepseek.com",
                      "ATLAS_MODEL_API_KEY",
                      "configured-test-value",
                      "ATLAS_MODEL_NAME",
                      "deepseek-test"),
                  request ->
                      new ConfiguredModelAdapter.ChatCompletionResponse("Provider answer.", 1, 1))));

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
    var configured = deepSeekRegistry.resolve("deepseek", null, ModelOperation.CHAT);

    assertThat(explicit.capability().modelKey()).isEqualTo("text-embedding-v4");
    assertThat(defaulted.capability().modelKey()).isEqualTo("deepseek-flash");
    assertThat(configured.capability().modelKey()).isEqualTo("deepseek-test");
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
