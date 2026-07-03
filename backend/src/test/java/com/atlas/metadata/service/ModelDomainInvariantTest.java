package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.atlas.metadata.domain.ModelRun;
import com.atlas.metadata.enums.ModelOperation;
import com.atlas.metadata.enums.ModelRunStatus;
import com.atlas.metadata.enums.ModelType;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

/** Unit tests for model domain invariants. */
class ModelDomainInvariantTest {

  @Test
  void modelRunCannotCompleteWithActiveStatus() {
    ModelRun run =
        ModelRun.create(
            "run",
            "mock-model",
            "deepseek-flash",
            ModelType.CHAT,
            ModelOperation.CHAT,
            "mock",
            "review-assist",
            "delivery-lead",
            "source-chunk:chunk-file-001",
            "Safe summary",
            OffsetDateTime.parse("2026-07-03T00:00:00Z"));

    assertThatThrownBy(
            () ->
                run.complete(
                    ModelRunStatus.RUNNING,
                    0,
                    0,
                    OffsetDateTime.parse("2026-07-03T00:00:01Z"),
                    "Nope"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
