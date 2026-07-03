package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.ModelAdapterStatus;
import com.atlas.metadata.enums.ModelOperation;
import com.atlas.metadata.enums.ModelType;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Configured provider boundary placeholder; real provider execution is intentionally deferred. */
@Component
public class ConfiguredModelAdapter implements ModelAdapter {

  /** Creates the configured adapter placeholder. */
  public ConfiguredModelAdapter() {
    this(null);
  }

  /** Visible for contract tests that verify raw configuration never leaks. */
  public ConfiguredModelAdapter(String configuredEndpoint) {}

  @Override
  public List<ModelCapability> capabilities() {
    return List.of(
        new ModelCapability(
            "configured-model",
            "configured-chat",
            "Configured Chat Model",
            "configured provider",
            ModelType.CHAT,
            List.of(ModelOperation.CHAT),
            false,
            ModelAdapterStatus.MISCONFIGURED,
            8192,
            Map.of("credential", "missing", "endpoint", "missing", "externalNetwork", "disabled")));
  }

  @Override
  public ModelResult execute(ModelRequest request) {
    throw new IllegalStateException("Configured model adapter is not enabled for this slice.");
  }
}
