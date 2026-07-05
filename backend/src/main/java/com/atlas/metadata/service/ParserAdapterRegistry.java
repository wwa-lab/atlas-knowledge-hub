package com.atlas.metadata.service;

import com.atlas.metadata.adapter.ParserAdapter;
import com.atlas.metadata.adapter.ParserCapability;
import com.atlas.metadata.exception.RequestValidationException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Registry for product-facing parser adapters. */
@Component
public class ParserAdapterRegistry {

  private final List<ParserAdapter> adapters;

  /** Creates the registry from configured parser adapters. */
  public ParserAdapterRegistry(List<ParserAdapter> adapters) {
    this.adapters = List.copyOf(adapters);
  }

  /** Lists all parser capabilities with default adapter first. */
  public List<ParserCapability> capabilities() {
    return adapters.stream()
        .map(ParserAdapter::capability)
        .sorted(Comparator.comparing(ParserCapability::defaultAdapter).reversed())
        .toList();
  }

  /** Resolves a requested adapter key or the configured default. */
  public ParserAdapter resolve(String adapterKey) {
    return resolve(adapterKey, null);
  }

  /** Resolves a requested adapter key and mode without falling through between mock and configured adapters. */
  public ParserAdapter resolve(String adapterKey, String mode) {
    String effectiveAdapterKey =
        adapterKey == null || adapterKey.isBlank() ? defaultAdapterKey() : adapterKey;
    List<ParserAdapter> candidates =
        adapters.stream()
            .filter(adapter -> adapter.capability().adapterKey().equals(effectiveAdapterKey))
            .toList();
    if ("configured".equals(mode)) {
      return candidates.stream()
          .filter(adapter -> !adapter.capability().defaultAdapter())
          .findFirst()
          .orElseThrow(this::unknownAdapter);
    }
    if (adapterKey == null || adapterKey.isBlank()) {
      return candidates.stream()
          .filter(adapter -> adapter.capability().defaultAdapter())
          .findFirst()
          .orElseThrow(this::unknownAdapter);
    }
    return candidates.stream()
        .sorted(Comparator.comparing((ParserAdapter adapter) -> adapter.capability().defaultAdapter()).reversed())
        .findFirst()
        .orElseThrow(this::unknownAdapter);
  }

  private RequestValidationException unknownAdapter() {
    return new RequestValidationException(
        Map.of("adapterKey", "must reference a configured parser adapter"));
  }

  private String defaultAdapterKey() {
    return adapters.stream()
        .map(ParserAdapter::capability)
        .filter(ParserCapability::defaultAdapter)
        .map(ParserCapability::adapterKey)
        .findFirst()
        .orElse("");
  }
}
