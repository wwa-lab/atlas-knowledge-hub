package com.atlas.metadata.adapter;

import java.util.List;

/** Product-facing model adapter contract; implementations hide provider/runtime details. */
public interface ModelAdapter {

  /** Returns masked capability metadata for each model exposed by the adapter. */
  List<ModelCapability> capabilities();

  /** Executes a safe model operation and returns descriptors only. */
  ModelResult execute(ModelRequest request);
}
