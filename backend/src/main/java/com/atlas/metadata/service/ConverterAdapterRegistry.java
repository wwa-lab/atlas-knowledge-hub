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
    return adapters.stream()
        .filter(adapter -> matches(adapter, adapterKey))
        .findFirst()
        .orElseThrow(
            () ->
                new RequestValidationException(
                    Map.of("adapterKey", "must reference a configured converter adapter")));
  }

  private boolean matches(ConverterAdapter adapter, String adapterKey) {
    ConverterCapability capability = adapter.capability();
    if (adapterKey == null || adapterKey.isBlank()) {
      return capability.defaultAdapter();
    }
    return capability.adapterKey().equals(adapterKey);
  }
}
