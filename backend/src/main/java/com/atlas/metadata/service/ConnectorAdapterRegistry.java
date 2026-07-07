package com.atlas.metadata.service;

import com.atlas.metadata.adapter.ConnectorAdapter;
import com.atlas.metadata.adapter.ConnectorCapability;
import com.atlas.metadata.exception.RequestValidationException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Registry for product-facing connector adapters. */
@Component
public class ConnectorAdapterRegistry {

  private final List<ConnectorAdapter> adapters;

  public ConnectorAdapterRegistry(List<ConnectorAdapter> adapters) {
    this.adapters = List.copyOf(adapters);
  }

  /** Lists connector capabilities in stable key order. */
  public List<ConnectorCapability> capabilities() {
    return adapters.stream()
        .map(ConnectorAdapter::capability)
        .sorted(Comparator.comparing(ConnectorCapability::connectorKey))
        .toList();
  }

  /** Resolves a connector adapter by key. */
  public ConnectorAdapter resolve(String connectorKey) {
    if (connectorKey == null || connectorKey.isBlank()) {
      throw new RequestValidationException(Map.of("connectorKey", "must be provided"));
    }
    return adapters.stream()
        .filter(adapter -> adapter.capability().connectorKey().equals(connectorKey))
        .findFirst()
        .orElseThrow(
            () ->
                new RequestValidationException(
                    Map.of("connectorKey", "must reference a registered connector adapter")));
  }
}
