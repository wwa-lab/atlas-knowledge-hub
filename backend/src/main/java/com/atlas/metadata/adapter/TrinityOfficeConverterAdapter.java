package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.ConverterAdapterStatus;
import com.atlas.metadata.enums.SourceType;
import java.util.List;
import java.util.Map;

/** Optional real trinity-office boundary, disabled until configured by a future deployment. */
public class TrinityOfficeConverterAdapter implements ConverterAdapter {

  private static final String CONFIGURED = "configured";
  private static final String MISSING = "missing";

  private final boolean commandConfigured;

  /** Creates a wrapper boundary with status-only configuration. */
  public TrinityOfficeConverterAdapter(String commandStatus) {
    this.commandConfigured = CONFIGURED.equals(commandStatus);
  }

  @Override
  public ConverterCapability capability() {
    ConverterAdapterStatus status =
        commandConfigured ? ConverterAdapterStatus.AVAILABLE : ConverterAdapterStatus.MISCONFIGURED;
    return new ConverterCapability(
        MockTrinityOfficeConverterAdapter.ADAPTER_KEY,
        "Trinity Office Converter",
        CONFIGURED,
        "pdf",
        List.of(SourceType.pptx, SourceType.docx, SourceType.xlsx, SourceType.pdf),
        false,
        status,
        Map.of("command", commandConfigured ? CONFIGURED : MISSING, "externalNetwork", "disabled"));
  }

  @Override
  public ConverterResult convert(ConverterRequest request) {
    throw new IllegalStateException("Real trinity-office execution is not enabled in this slice.");
  }
}
