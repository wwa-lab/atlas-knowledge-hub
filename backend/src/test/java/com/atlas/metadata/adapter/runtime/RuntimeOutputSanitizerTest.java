package com.atlas.metadata.adapter.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Unit tests for bounded runtime diagnostic sanitization. */
class RuntimeOutputSanitizerTest {

  @Test
  void masksSecretsPathsEndpointsHostsAndStackFrames() {
    String raw =
        """
        token=runtime-secret
        password=hunter2
        api_key=abc123
        failed at /private/runtime/input.docx
        endpoint https://internal.example.local:8443/convert
        host parser.internal.local
        at com.vendor.Parser.run(Parser.java:42)
        """;

    String sanitized = RuntimeOutputSanitizer.sanitize(raw, 65_536);

    assertThat(sanitized)
        .doesNotContain("runtime-secret")
        .doesNotContain("hunter2")
        .doesNotContain("abc123")
        .doesNotContain("/private/runtime")
        .doesNotContain("internal.example.local")
        .doesNotContain("parser.internal.local")
        .doesNotContain("Parser.java:42");
    assertThat(sanitized)
        .contains("token=[masked]")
        .contains("password=[masked]")
        .contains("api_key=[masked]")
        .contains("[path]")
        .contains("[endpoint]")
        .contains("[host]")
        .contains("[stack]");
  }

  @Test
  void truncatesCapturedOutputAtLimit() {
    String sanitized = RuntimeOutputSanitizer.sanitize("x".repeat(80), 16);

    assertThat(sanitized).hasSize(16);
  }
}
