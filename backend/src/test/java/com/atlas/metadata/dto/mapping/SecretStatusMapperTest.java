package com.atlas.metadata.dto.mapping;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for status-only secret metadata mapping. */
class SecretStatusMapperTest {

  @Test
  void mapsSummaryToSecretStatusesWithoutRawValues() {
    var statuses =
        SecretStatusMapper.fromSummary(
            "runtime",
            "converter-runtime",
            Map.of(
                "command", "configured",
                "endpoint", "missing",
                "externalNetwork", "disabled"),
            "adapter");

    assertThat(statuses).hasSize(2);
    assertThat(statuses)
        .anySatisfy(
            status -> {
              assertThat(status.reference().key()).isEqualTo("command");
              assertThat(status.status()).isEqualTo("CONFIGURED");
              assertThat(status.maskedLabel()).isEqualTo("Configured");
              assertThat(status.removable()).isTrue();
            })
        .anySatisfy(
            status -> {
              assertThat(status.reference().key()).isEqualTo("endpoint");
              assertThat(status.status()).isEqualTo("MISSING");
              assertThat(status.removable()).isFalse();
            });
    assertThat(statuses.toString()).doesNotContain("externalNetwork", "configured-command");
  }
}
