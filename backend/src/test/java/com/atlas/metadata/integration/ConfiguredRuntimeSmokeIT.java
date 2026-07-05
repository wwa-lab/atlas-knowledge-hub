package com.atlas.metadata.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.adapter.runtime.LocalRuntimeExecutor;
import com.atlas.metadata.adapter.runtime.RuntimeAdapterConfiguration;
import com.atlas.metadata.adapter.runtime.RuntimeInvocation;
import com.atlas.metadata.adapter.runtime.RuntimeOutputSanitizer;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/** Optional runtime smoke checks that self-skip unless approved local runtime config is present. */
class ConfiguredRuntimeSmokeIT {

  private static final String SMOKE_ENABLED = "ATLAS_RUNTIME_SMOKE_ENABLED";
  private static final int SMOKE_CAPTURE_LIMIT = 4096;

  @Test
  void trinityOfficeSmokeSelfSkipsWithoutApprovedRuntimeConfig() {
    smoke(
        "ATLAS_TRINITY_OFFICE_COMMAND",
        "ATLAS_TRINITY_OFFICE_SMOKE_ARGS",
        "trinity-office smoke command");
  }

  @Test
  void documentNormalizeSmokeSelfSkipsWithoutApprovedRuntimeConfig() {
    smoke(
        "ATLAS_DOCUMENT_NORMALIZE_COMMAND",
        "ATLAS_DOCUMENT_NORMALIZE_SMOKE_ARGS",
        "document-normalize smoke command");
  }

  private void smoke(String commandEnv, String argsEnv, String label) {
    Assumptions.assumeTrue("true".equalsIgnoreCase(System.getenv(SMOKE_ENABLED)), "runtime smoke disabled");
    String command = System.getenv(commandEnv);
    Assumptions.assumeTrue(command != null && !command.isBlank(), label + " missing");

    var executor = new LocalRuntimeExecutor();
    var result =
        executor.execute(
            new RuntimeInvocation(
                command,
                args(System.getenv(argsEnv)),
                "",
                Duration.ofSeconds(15),
                SMOKE_CAPTURE_LIMIT));

    String safeDiagnostic = RuntimeOutputSanitizer.sanitize(result.diagnosticOutput(), SMOKE_CAPTURE_LIMIT);
    assertThat(result.timedOut()).isFalse();
    assertThat(result.exitCode()).isZero();
    assertThat(safeDiagnostic == null ? "" : safeDiagnostic).doesNotContain(command);
  }

  private List<String> args(String raw) {
    if (raw == null || raw.isBlank()) {
      return List.of("--version");
    }
    return Arrays.stream(raw.trim().split("\\s+")).filter(value -> !value.isBlank()).toList();
  }
}
