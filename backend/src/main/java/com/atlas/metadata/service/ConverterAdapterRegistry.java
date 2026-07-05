package com.atlas.metadata.service;

import com.atlas.metadata.adapter.ConverterAdapter;
import com.atlas.metadata.adapter.ConverterCapability;
import com.atlas.metadata.exception.RequestValidationException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Registry for product-facing converter adapters. */
@Component
public class ConverterAdapterRegistry {

  private final List<ConverterAdapter> adapters;

  /** Creates the registry from configured converter adapters. */
  public ConverterAdapterRegistry(List<ConverterAdapter> adapters) {
    this.adapters = List.copyOf(adapters);
  }

  /** Lists all converter capabilities with default adapter first. */
  public List<ConverterCapability> capabilities() {
    return adapters.stream()
        .map(ConverterAdapter::capability)
        .sorted(Comparator.comparing(ConverterCapability::defaultAdapter).reversed())
        .toList();
  }

  /** Resolves a requested adapter key or the configured default. */
  public ConverterAdapter resolve(String adapterKey) {
    return resolve(adapterKey, null);
  }

  /** Resolves a requested adapter key and mode without falling through between mock and configured adapters. */
  public ConverterAdapter resolve(String adapterKey, String mode) {
    String effectiveAdapterKey =
        adapterKey == null || adapterKey.isBlank() ? defaultAdapterKey() : adapterKey;
    List<ConverterAdapter> candidates =
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
        .sorted(Comparator.comparing((ConverterAdapter adapter) -> adapter.capability().defaultAdapter()).reversed())
        .findFirst()
        .orElseThrow(this::unknownAdapter);
  }

  private RequestValidationException unknownAdapter() {
    return new RequestValidationException(
        Map.of("adapterKey", "must reference a configured converter adapter"));
  }

  private String defaultAdapterKey() {
    return adapters.stream()
        .map(ConverterAdapter::capability)
        .filter(ConverterCapability::defaultAdapter)
        .map(ConverterCapability::adapterKey)
        .findFirst()
        .orElse("");
  }
}
