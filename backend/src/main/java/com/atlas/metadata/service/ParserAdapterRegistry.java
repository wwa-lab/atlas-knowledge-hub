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
    return adapters.stream()
        .filter(adapter -> matches(adapter, adapterKey))
        .findFirst()
        .orElseThrow(
            () ->
                new RequestValidationException(
                    Map.of("adapterKey", "must reference a configured parser adapter")));
  }

  private boolean matches(ParserAdapter adapter, String adapterKey) {
    ParserCapability capability = adapter.capability();
    if (adapterKey == null || adapterKey.isBlank()) {
      return capability.defaultAdapter();
    }
    return capability.adapterKey().equals(adapterKey);
  }
}
