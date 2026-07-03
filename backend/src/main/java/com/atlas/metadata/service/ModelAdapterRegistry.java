package com.atlas.metadata.service;

import com.atlas.metadata.adapter.ModelAdapter;
import com.atlas.metadata.adapter.ModelCapability;
import com.atlas.metadata.enums.ModelOperation;
import com.atlas.metadata.exception.RequestValidationException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Registry for product-facing model adapters. */
@Component
public class ModelAdapterRegistry {

  private final List<ModelAdapter> adapters;

  /** Creates the registry from configured model adapters. */
  public ModelAdapterRegistry(List<ModelAdapter> adapters) {
    this.adapters = List.copyOf(adapters);
  }

  /** Lists all model capabilities with default models first. */
  public List<ModelCapability> capabilities() {
    return adapters.stream()
        .flatMap(adapter -> adapter.capabilities().stream())
        .sorted(
            Comparator.comparing(ModelCapability::defaultModel)
                .reversed()
                .thenComparing(ModelCapability::adapterKey)
                .thenComparing(ModelCapability::modelKey))
        .toList();
  }

  /** Resolves explicit adapter/model selection or a default model for the requested operation. */
  public ResolvedModelAdapter resolve(
      String adapterKey, String modelKey, ModelOperation operationType) {
    if (operationType == null) {
      throw new RequestValidationException(Map.of("operationType", "must be provided"));
    }
    List<ResolvedModelAdapter> matches =
        adapters.stream()
            .flatMap(
                adapter ->
                    adapter.capabilities().stream()
                        .map(capability -> new ResolvedModelAdapter(adapter, capability)))
            .filter(resolved -> matchesAdapter(resolved.capability(), adapterKey))
            .filter(resolved -> matchesModel(resolved.capability(), modelKey))
            .toList();
    if (matches.isEmpty()) {
      throw new RequestValidationException(selectorError(adapterKey, modelKey));
    }
    return matches.stream()
        .filter(resolved -> resolved.capability().supportedOperations().contains(operationType))
        .filter(resolved -> modelKey != null && !modelKey.isBlank() || resolved.capability().defaultModel())
        .findFirst()
        .orElseThrow(
            () ->
                new RequestValidationException(
                    Map.of("modelKey", "must support the requested model operation")));
  }

  private Map<String, String> selectorError(String adapterKey, String modelKey) {
    if (adapterKey != null && !adapterKey.isBlank()) {
      return Map.of("adapterKey", "must reference a configured model adapter");
    }
    if (modelKey != null && !modelKey.isBlank()) {
      return Map.of("modelKey", "must reference a configured model");
    }
    return Map.of("modelKey", "must have a default model for the requested operation");
  }

  private boolean matchesAdapter(ModelCapability capability, String adapterKey) {
    return adapterKey == null || adapterKey.isBlank() || capability.adapterKey().equals(adapterKey);
  }

  private boolean matchesModel(ModelCapability capability, String modelKey) {
    return modelKey == null || modelKey.isBlank() || capability.modelKey().equals(modelKey);
  }

  /** Resolved adapter and selected capability. */
  public record ResolvedModelAdapter(ModelAdapter adapter, ModelCapability capability) {}
}
