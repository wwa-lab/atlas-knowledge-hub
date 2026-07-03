package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.ModelAdapterStatus;
import com.atlas.metadata.enums.ModelOperation;
import com.atlas.metadata.enums.ModelOutputKind;
import com.atlas.metadata.enums.ModelType;
import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Deterministic mock model adapter used for contract verification. */
@Component
public class MockModelAdapter implements ModelAdapter {

  private static final String ADAPTER_KEY = "mock-model";

  @Override
  public List<ModelCapability> capabilities() {
    return List.of(
        capability("deepseek-flash", "DeepSeek Flash", ModelType.CHAT, ModelOperation.CHAT, true, 8192),
        capability(
            "text-embedding-v4",
            "Text Embedding V4",
            ModelType.EMBEDDING,
            ModelOperation.EMBEDDING,
            true,
            8192),
        capability("mock-rerank", "Mock Rerank", ModelType.RERANK, ModelOperation.RERANK, true, 4096),
        capability("mock-vision", "Mock Vision", ModelType.VISION, ModelOperation.VISION, true, 4096),
        capability("mock-speech", "Mock Speech", ModelType.SPEECH, ModelOperation.SPEECH, true, 4096));
  }

  @Override
  public ModelResult execute(ModelRequest request) {
    return switch (request.operationType()) {
      case CHAT -> result(
          request,
          ModelOutputKind.TEXT_SUMMARY,
          "generated/model/" + request.runId() + "-chat.json",
          "Mock chat summary for the referenced Atlas evidence.",
          List.of(),
          null,
          null);
      case EMBEDDING -> result(
          request,
          ModelOutputKind.EMBEDDING_METADATA,
          "generated/model/" + request.runId() + "-embedding.json",
          "Mock embedding metadata descriptor; vector values are intentionally omitted.",
          List.of(),
          1024,
          Math.max(1, request.sourceReferences() == null ? 0 : request.sourceReferences().size()));
      case RERANK -> result(
          request,
          ModelOutputKind.RERANK_SCORES,
          "generated/model/" + request.runId() + "-rerank.json",
          "Mock rerank scores for safe Atlas item references.",
          List.of("chunk-file-001-p12-b02", "chunk-file-005-p03-b01"),
          null,
          null);
      case VISION -> result(
          request,
          ModelOutputKind.VISION_SUMMARY,
          "generated/model/" + request.runId() + "-vision.json",
          "Mock vision summary for referenced image evidence.",
          List.of(),
          null,
          null);
      case SPEECH -> result(
          request,
          ModelOutputKind.SPEECH_SUMMARY,
          "generated/model/" + request.runId() + "-speech.json",
          "Mock speech transcript summary for referenced audio evidence.",
          List.of(),
          null,
          null);
    };
  }

  private ModelCapability capability(
      String modelKey,
      String displayName,
      ModelType modelType,
      ModelOperation operation,
      boolean defaultModel,
      int contextLimit) {
    return new ModelCapability(
        ADAPTER_KEY,
        modelKey,
        displayName,
        "built-in mock",
        modelType,
        List.of(operation),
        defaultModel,
        ModelAdapterStatus.AVAILABLE,
        contextLimit,
        Map.of("credential", "mock", "endpoint", "not_configured", "externalNetwork", "disabled"));
  }

  private ModelResult result(
      ModelRequest request,
      ModelOutputKind kind,
      String outputReference,
      String safeSummary,
      List<String> rankedItemIds,
      Integer embeddingDimension,
      Integer embeddingItemCount) {
    return new ModelResult(
        ADAPTER_KEY,
        request.modelKey(),
        "Mock model operation completed.",
        new ModelResult.ModelUsage(promptUnits(request), 8),
        List.of(
            new ModelResult.ModelOutput(
                request.runId() + "-output-001",
                kind,
                outputReference,
                safeSummary,
                rankedItemIds,
                embeddingDimension,
                embeddingItemCount,
                new BigDecimal("0.820"),
                ReviewStatus.REVIEW_REQUIRED,
                null)));
  }

  private int promptUnits(ModelRequest request) {
    int inputLength = request.safeMockInput() == null ? 0 : request.safeMockInput().length();
    int referenceCount = request.sourceReferences() == null ? 0 : request.sourceReferences().size();
    return Math.max(1, inputLength / 8 + referenceCount);
  }
}
