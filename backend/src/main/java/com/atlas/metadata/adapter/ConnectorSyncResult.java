package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.ConnectorSafeErrorCategory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Adapter result for connector sync v0. */
public record ConnectorSyncResult(String connectorKey, String safeMessage, List<Item> items) {

  /** One source item discovered by a connector adapter. */
  public record Item(
      String externalId,
      String title,
      String sourceReference,
      Map<String, String> sourceTrace,
      Map<String, String> provenance,
      BigDecimal confidence,
      boolean reviewRequired,
      ConnectorSafeErrorCategory safeErrorCategory,
      String safeErrorMessage,
      String outputTitle,
      String targetPath) {}
}
