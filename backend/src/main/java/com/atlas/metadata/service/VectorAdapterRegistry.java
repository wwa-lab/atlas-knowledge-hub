package com.atlas.metadata.service;

import com.atlas.metadata.adapter.VectorAdapter;
import com.atlas.metadata.adapter.VectorCapability;
import com.atlas.metadata.exception.RequestValidationException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Registry for product-facing vector adapters. */
@Component
public class VectorAdapterRegistry {

  private final List<VectorAdapter> adapters;

  /** Creates the registry from configured vector adapters. */
  public VectorAdapterRegistry(List<VectorAdapter> adapters) {
    this.adapters = List.copyOf(adapters);
  }

  /** Lists all vector capabilities with the default adapter first. */
  public List<VectorCapability> capabilities() {
    return adapters.stream()
        .map(VectorAdapter::capability)
        .sorted(Comparator.comparing(VectorCapability::defaultAdapter).reversed())
        .toList();
  }

  /** Resolves a requested adapter key or the configured default. */
  public VectorAdapter resolve(String adapterKey) {
    return adapters.stream()
        .filter(adapter -> matches(adapter, adapterKey))
        .findFirst()
        .orElseThrow(
            () ->
                new RequestValidationException(
                    Map.of("adapterKey", "must reference a configured vector adapter")));
  }

  private boolean matches(VectorAdapter adapter, String adapterKey) {
    VectorCapability capability = adapter.capability();
    if (adapterKey == null || adapterKey.isBlank()) {
      return capability.defaultAdapter();
    }
    return capability.adapterKey().equals(adapterKey);
  }
}
