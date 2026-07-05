package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.atlas.metadata.adapter.ConverterAdapter;
import com.atlas.metadata.adapter.ConverterCapability;
import com.atlas.metadata.adapter.ConverterRequest;
import com.atlas.metadata.adapter.ConverterResult;
import com.atlas.metadata.adapter.MockTrinityOfficeConverterAdapter;
import com.atlas.metadata.adapter.TrinityOfficeConverterAdapter;
import com.atlas.metadata.adapter.runtime.RuntimeAdapterConfiguration;
import com.atlas.metadata.adapter.runtime.RuntimeExecutionResult;
import com.atlas.metadata.enums.ConverterAdapterStatus;
import com.atlas.metadata.enums.SourceType;
import com.atlas.metadata.exception.RequestValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for converter adapter registry resolution. */
class ConverterAdapterRegistryTest {

  @Test
  void listsDefaultFirstAndResolvesMockOrConfiguredModeDeterministically() {
    ConverterAdapter defaultAdapter = new MockTrinityOfficeConverterAdapter();
    ConverterAdapter configuredAdapter =
        new TrinityOfficeConverterAdapter(
            new RuntimeAdapterConfiguration(true, "configured-command", Duration.ofSeconds(120), 65_536),
            invocation -> new RuntimeExecutionResult(0, "{\"files\":[]}", "", false),
            new ObjectMapper());
    ConverterAdapter secondaryAdapter = adapter("secondary-converter", false);
    ConverterAdapterRegistry registry =
        new ConverterAdapterRegistry(List.of(configuredAdapter, secondaryAdapter, defaultAdapter));

    assertThat(registry.capabilities()).extracting(ConverterCapability::adapterKey)
        .containsExactly("trinity-office", "trinity-office", "secondary-converter");
    assertThat(registry.resolve(null).capability().defaultAdapter()).isTrue();
    assertThat(registry.resolve("trinity-office", "mock").capability().defaultAdapter()).isTrue();
    assertThat(registry.resolve("trinity-office", "configured").capability().defaultAdapter()).isFalse();
  }

  @Test
  void rejectsUnknownOrMissingConfiguredAdapterKeyWithValidationError() {
    ConverterAdapterRegistry registry =
        new ConverterAdapterRegistry(List.of(new MockTrinityOfficeConverterAdapter()));

    assertThatThrownBy(() -> registry.resolve("unknown"))
        .isInstanceOf(RequestValidationException.class)
        .satisfies(
            error ->
                assertThat(((RequestValidationException) error).getFields())
                    .containsEntry("adapterKey", "must reference a configured converter adapter"));
    assertThatThrownBy(() -> registry.resolve("trinity-office", "configured"))
        .isInstanceOf(RequestValidationException.class)
        .satisfies(
            error ->
                assertThat(((RequestValidationException) error).getFields())
                    .containsEntry("adapterKey", "must reference a configured converter adapter"));
  }

  private ConverterAdapter adapter(String key, boolean defaultAdapter) {
    return new ConverterAdapter() {
      @Override
      public ConverterCapability capability() {
        return new ConverterCapability(
            key,
            "Secondary Converter",
            "test",
            "pdf",
            List.of(SourceType.docx),
            defaultAdapter,
            ConverterAdapterStatus.AVAILABLE,
            Map.of("command", "configured", "externalNetwork", "disabled"));
      }

      @Override
      public ConverterResult convert(ConverterRequest request) {
        return new ConverterResult(key, List.of(), "No-op converter.");
      }
    };
  }
}
