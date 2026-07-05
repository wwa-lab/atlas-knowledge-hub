package com.atlas.metadata.adapter;

import java.util.Map;

/** Supplies effective model configuration to adapter implementations. */
public interface ModelConfigurationProvider {

  /** Returns environment-shaped configuration values for adapter execution. */
  Map<String, String> effectiveEnvironment();
}
