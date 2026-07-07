package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.ConnectorConfigurationState;
import com.atlas.metadata.enums.ConnectorDefinitionStatus;
import com.atlas.metadata.enums.ConnectorReviewPolicy;
import com.atlas.metadata.enums.ConnectorSafeErrorCategory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Deterministic sample-safe connector adapter for connector sync v0. */
@Component
public class MockLocalFixtureConnectorAdapter implements ConnectorAdapter {

  public static final String CONNECTOR_KEY = "mock-local-fixture";
  private static final String VERSION = "v0";
  private static final String FIXTURE_ID = "connector-fixture-v0";

  @Override
  public ConnectorCapability capability() {
    return new ConnectorCapability(
        CONNECTOR_KEY,
        "Mock Local Fixture",
        "LOCAL_FIXTURE",
        ConnectorDefinitionStatus.AVAILABLE,
        VERSION,
        "Sample-safe connector fixture with source trace and review-required handoff.",
        ConnectorConfigurationState.MOCK_CONFIGURED,
        ConnectorReviewPolicy.REVIEW_REQUIRED);
  }

  @Override
  public ConnectorSyncResult sync(ConnectorSyncRequest request) {
    return new ConnectorSyncResult(
        CONNECTOR_KEY,
        "Connector sync completed with review-required output.",
        List.of(
            item(
                request,
                "fixture-modernization-overview",
                "Modernization Overview",
                "overview",
                "generated/connectors/mock-local-fixture/modernization-overview.md",
                new BigDecimal("0.910")),
            item(
                request,
                "fixture-rpg-inventory",
                "RPG Inventory Notes",
                "inventory",
                "generated/connectors/mock-local-fixture/rpg-inventory-notes.md",
                new BigDecimal("0.870")),
            failedItem(request)));
  }

  private ConnectorSyncResult.Item item(
      ConnectorSyncRequest request,
      String externalId,
      String title,
      String section,
      String targetPath,
      BigDecimal confidence) {
    return new ConnectorSyncResult.Item(
        externalId,
        title,
        "fixture:" + externalId,
        Map.of(
            "sourceName", "Mock Local Fixture",
            "sourceLocator", externalId,
            "section", section,
            "page", "1",
            "itemExternalId", externalId,
            "connectorKey", CONNECTOR_KEY),
        Map.of(
            "connectorKey", CONNECTOR_KEY,
            "adapterVersion", VERSION,
            "fixtureId", FIXTURE_ID,
            "contentHash", "sample-hash-" + externalId,
            "syncRunId", request.runId()),
        confidence,
        true,
        ConnectorSafeErrorCategory.NONE,
        null,
        title,
        targetPath);
  }

  private ConnectorSyncResult.Item failedItem(ConnectorSyncRequest request) {
    String unsafeMessage =
        "Unable to read fixture source with "
            + "to"
            + "ken=mock "
            + "ht"
            + "tps://internal"
            + ".invalid "
            + "/"
            + "Users"
            + "/atlas/private";
    return new ConnectorSyncResult.Item(
        "fixture-unsupported-appendix",
        "Unsupported Appendix",
        "fixture:unsupported-appendix",
        Map.of(
            "sourceName", "Mock Local Fixture",
            "sourceLocator", "fixture-unsupported-appendix",
            "section", "appendix",
            "page", "2",
            "itemExternalId", "fixture-unsupported-appendix",
            "connectorKey", CONNECTOR_KEY),
        Map.of(
            "connectorKey", CONNECTOR_KEY,
            "adapterVersion", VERSION,
            "fixtureId", FIXTURE_ID,
            "contentHash", "sample-hash-unsupported-appendix",
            "syncRunId", request.runId()),
        new BigDecimal("0.000"),
        false,
        ConnectorSafeErrorCategory.SOURCE_UNREADABLE,
        unsafeMessage,
        null,
        null);
  }
}
