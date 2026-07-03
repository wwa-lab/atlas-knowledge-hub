package com.atlas.metadata.service;

import com.atlas.metadata.adapter.StorageAdapter;
import com.atlas.metadata.adapter.StorageCapability;
import com.atlas.metadata.exception.RequestValidationException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Registry for product-facing storage adapters. */
@Component
public class StorageAdapterRegistry {

  private final List<StorageAdapter> adapters;

  /** Creates the registry from configured storage adapters. */
  public StorageAdapterRegistry(List<StorageAdapter> adapters) {
    this.adapters = List.copyOf(adapters);
  }

  /** Lists all storage capabilities with default adapter first. */
  public List<StorageCapability> capabilities() {
    return adapters.stream()
        .map(StorageAdapter::capability)
        .sorted(Comparator.comparing(StorageCapability::defaultAdapter).reversed())
        .toList();
  }

  /** Resolves a requested adapter key or the configured default. */
  public StorageAdapter resolve(String adapterKey) {
    return adapters.stream()
        .filter(adapter -> matches(adapter, adapterKey))
        .findFirst()
        .orElseThrow(
            () ->
                new RequestValidationException(
                    Map.of("adapterKey", "must reference a configured storage adapter")));
  }

  private boolean matches(StorageAdapter adapter, String adapterKey) {
    StorageCapability capability = adapter.capability();
    if (adapterKey == null || adapterKey.isBlank()) {
      return capability.defaultAdapter();
    }
    return capability.adapterKey().equals(adapterKey);
  }
}
