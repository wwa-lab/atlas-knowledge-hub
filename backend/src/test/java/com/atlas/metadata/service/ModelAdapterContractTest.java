package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.adapter.ConfiguredModelAdapter;
import com.atlas.metadata.adapter.MockModelAdapter;
import com.atlas.metadata.adapter.ModelRequest;
import com.atlas.metadata.adapter.ModelResult;
import com.atlas.metadata.enums.ModelAdapterStatus;
import com.atlas.metadata.enums.ModelOperation;
import com.atlas.metadata.enums.ModelOutputKind;
import com.atlas.metadata.enums.ModelSourceReferenceType;
import com.atlas.metadata.enums.ModelType;
import com.atlas.metadata.enums.ReviewStatus;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for model adapter contracts and mock outcomes. */
class ModelAdapterContractTest {

  private final MockModelAdapter adapter = new MockModelAdapter();

  @Test
  void capabilitiesAreMaskedDefaultedAndCoverSupportedModelTypes() {
    var capabilities = adapter.capabilities();

    assertThat(capabilities).hasSize(5);
    assertThat(capabilities)
        .extracting(capability -> capability.modelType())
        .containsExactlyInAnyOrder(
            ModelType.CHAT, ModelType.EMBEDDING, ModelType.RERANK, ModelType.VISION, ModelType.SPEECH);
    assertThat(capabilities)
        .filteredOn(capability -> capability.modelType() == ModelType.CHAT)
        .singleElement()
        .satisfies(
            capability -> {
              assertThat(capability.adapterKey()).isEqualTo("mock-model");
              assertThat(capability.modelKey()).isEqualTo("deepseek-flash");
              assertThat(capability.defaultModel()).isTrue();
              assertThat(capability.status()).isEqualTo(ModelAdapterStatus.AVAILABLE);
              assertThat(capability.supportedOperations()).containsExactly(ModelOperation.CHAT);
              assertThat(capability.maskedConfigSummary())
                  .containsEntry("externalNetwork", "disabled")
                  .containsEntry("credential", "mock");
            });
    assertThat(capabilities)
        .flatExtracting(capability -> capability.maskedConfigSummary().values())
        .allSatisfy(value -> assertThat(value).doesNotContain("/", "\\", "password", "token"));
  }

  @Test
  void mockAdapterReturnsReviewRequiredOutputsForEachOperation() {
    assertOutput(ModelOperation.CHAT, "deepseek-flash", ModelOutputKind.TEXT_SUMMARY);
    assertOutput(ModelOperation.EMBEDDING, "text-embedding-v4", ModelOutputKind.EMBEDDING_METADATA);
    assertOutput(ModelOperation.RERANK, "mock-rerank", ModelOutputKind.RERANK_SCORES);
    assertOutput(ModelOperation.VISION, "mock-vision", ModelOutputKind.VISION_SUMMARY);
    assertOutput(ModelOperation.SPEECH, "mock-speech", ModelOutputKind.SPEECH_SUMMARY);
  }

  @Test
  void configuredAdapterReportsMisconfiguredWithoutLeakingRawConfig() {
    var capability = new ConfiguredModelAdapter("raw-provider-endpoint").capabilities().getFirst();

    assertThat(capability.status()).isEqualTo(ModelAdapterStatus.MISCONFIGURED);
    assertThat(capability.maskedConfigSummary()).containsEntry("endpoint", "missing");
    assertThat(capability.maskedConfigSummary().values())
        .allSatisfy(value -> assertThat(value).doesNotContain("/", "\\", "raw-provider-endpoint"));
  }

  private void assertOutput(ModelOperation operation, String modelKey, ModelOutputKind kind) {
    ModelResult result = adapter.execute(request(operation, modelKey));

    assertThat(result.adapterKey()).isEqualTo("mock-model");
    assertThat(result.modelKey()).isEqualTo(modelKey);
    assertThat(result.outputs()).hasSize(1);
    assertThat(result.outputs().getFirst().kind()).isEqualTo(kind);
    assertThat(result.outputs().getFirst().reviewStatus()).isEqualTo(ReviewStatus.REVIEW_REQUIRED);
    assertThat(result.outputs().getFirst().safeSummary()).contains("Mock");
    assertThat(result.usage().promptUnits()).isGreaterThanOrEqualTo(0);
    assertThat(result.usage().completionUnits()).isGreaterThanOrEqualTo(0);
  }

  private ModelRequest request(ModelOperation operation, String modelKey) {
    return new ModelRequest(
        "run",
        operation,
        modelKey,
        "mock",
        "review-assist",
        "source-chunk:chunk-file-001-p12-b02",
        "Summarize the referenced chunk.",
        List.of(
            new ModelRequest.ModelSourceReference(
                ModelSourceReferenceType.SOURCE_CHUNK,
                "chunk-file-001-p12-b02",
                "BRD page 12",
                null,
                ReviewStatus.REVIEW_REQUIRED)));
  }
}
