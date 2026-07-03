package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.ParserAdapterStatus;
import com.atlas.metadata.enums.SourceType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Optional real document-normalize boundary, disabled until configured by a future deployment. */
public class DocumentNormalizeParserAdapter implements ParserAdapter {

  private static final String CONFIGURED = "configured";
  private static final String MISSING = "missing";

  private final boolean commandConfigured;

  /** Creates a wrapper boundary with status-only configuration. */
  public DocumentNormalizeParserAdapter(String commandStatus) {
    this.commandConfigured = CONFIGURED.equals(commandStatus);
  }

  @Override
  public ParserCapability capability() {
    ParserAdapterStatus status =
        commandConfigured ? ParserAdapterStatus.AVAILABLE : ParserAdapterStatus.MISCONFIGURED;
    return new ParserCapability(
        MockDocumentNormalizeParserAdapter.ADAPTER_KEY,
        "Document Normalize Parser",
        CONFIGURED,
        List.of(SourceType.pdf),
        List.of("markdown", "assets"),
        false,
        status,
        new BigDecimal("0.800"),
        Map.of("command", commandConfigured ? CONFIGURED : MISSING, "externalNetwork", "disabled"));
  }

  @Override
  public ParserResult parse(ParserRequest request) {
    throw new IllegalStateException("Real document-normalize execution is not enabled in this slice.");
  }
}
