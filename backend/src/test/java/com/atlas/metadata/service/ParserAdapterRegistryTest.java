package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.atlas.metadata.adapter.MockDocumentNormalizeParserAdapter;
import com.atlas.metadata.adapter.ParserAdapter;
import com.atlas.metadata.adapter.ParserCapability;
import com.atlas.metadata.adapter.ParserRequest;
import com.atlas.metadata.adapter.ParserResult;
import com.atlas.metadata.enums.ParserAdapterStatus;
import com.atlas.metadata.enums.SourceType;
import com.atlas.metadata.exception.RequestValidationException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for parser adapter registry resolution. */
class ParserAdapterRegistryTest {

  @Test
  void listsDefaultCapabilityFirstAndResolvesDefaultOrExplicitAdapter() {
    ParserAdapter defaultAdapter = new MockDocumentNormalizeParserAdapter();
    ParserAdapter secondaryAdapter = adapter("secondary-parser", false);
    ParserAdapterRegistry registry = new ParserAdapterRegistry(List.of(secondaryAdapter, defaultAdapter));

    assertThat(registry.capabilities()).extracting(ParserCapability::adapterKey)
        .containsExactly("document-normalize", "secondary-parser");
    assertThat(registry.resolve(null).capability().adapterKey()).isEqualTo("document-normalize");
    assertThat(registry.resolve("secondary-parser").capability().adapterKey())
        .isEqualTo("secondary-parser");
  }

  @Test
  void rejectsUnknownAdapterKeyWithValidationError() {
    ParserAdapterRegistry registry =
        new ParserAdapterRegistry(List.of(new MockDocumentNormalizeParserAdapter()));

    assertThatThrownBy(() -> registry.resolve("unknown"))
        .isInstanceOf(RequestValidationException.class)
        .satisfies(
            error ->
                assertThat(((RequestValidationException) error).getFields())
                    .containsEntry("adapterKey", "must reference a configured parser adapter"));
  }

  private ParserAdapter adapter(String key, boolean defaultAdapter) {
    return new ParserAdapter() {
      @Override
      public ParserCapability capability() {
        return new ParserCapability(
            key,
            "Secondary Parser",
            "test",
            List.of(SourceType.pdf),
            List.of("markdown", "assets"),
            defaultAdapter,
            ParserAdapterStatus.AVAILABLE,
            new BigDecimal("0.800"),
            Map.of("command", "configured", "externalNetwork", "disabled"));
      }

      @Override
      public ParserResult parse(ParserRequest request) {
        return new ParserResult(key, List.of(), "No-op parser.");
      }
    };
  }
}
