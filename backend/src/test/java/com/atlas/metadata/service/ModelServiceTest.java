package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlas.metadata.adapter.MockModelAdapter;
import com.atlas.metadata.adapter.ModelAdapter;
import com.atlas.metadata.adapter.ModelCapability;
import com.atlas.metadata.adapter.ModelRequest;
import com.atlas.metadata.adapter.ModelResult;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.ModelRun;
import com.atlas.metadata.domain.ModelRunOutput;
import com.atlas.metadata.domain.ModelRunSourceReference;
import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.dto.CreateModelRunRequest;
import com.atlas.metadata.dto.ModelSourceReferenceRequest;
import com.atlas.metadata.enums.ModelAdapterStatus;
import com.atlas.metadata.enums.ModelOperation;
import com.atlas.metadata.enums.ModelOutputKind;
import com.atlas.metadata.enums.ModelRunStatus;
import com.atlas.metadata.enums.ModelSourceReferenceType;
import com.atlas.metadata.enums.ModelType;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceType;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.ModelRunOutputRepository;
import com.atlas.metadata.repository.ModelRunRepository;
import com.atlas.metadata.repository.ModelRunSourceReferenceRepository;
import com.atlas.metadata.repository.SourceChunkRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** Unit tests for model service validation, persistence, and safe failures. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ModelServiceTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-07-03T00:00:00Z"), ZoneOffset.UTC);

  @Mock private ModelRunRepository modelRunRepository;
  @Mock private ModelRunOutputRepository modelRunOutputRepository;
  @Mock private ModelRunSourceReferenceRepository sourceReferenceRepository;
  @Mock private FileItemRepository fileItemRepository;
  @Mock private SourceChunkRepository sourceChunkRepository;

  private final List<ModelRunOutput> savedOutputs = new ArrayList<>();
  private final List<ModelRunSourceReference> savedReferences = new ArrayList<>();
  private final MockModelAdapter adapter = new MockModelAdapter();

  @BeforeEach
  void setUp() {
    when(modelRunRepository.save(any(ModelRun.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(modelRunOutputRepository.save(any(ModelRunOutput.class)))
        .thenAnswer(
            invocation -> {
              ModelRunOutput output = invocation.getArgument(0);
              savedOutputs.add(output);
              return output;
            });
    when(sourceReferenceRepository.save(any(ModelRunSourceReference.class)))
        .thenAnswer(
            invocation -> {
              ModelRunSourceReference reference = invocation.getArgument(0);
              savedReferences.add(reference);
              return reference;
            });
    when(modelRunOutputRepository.findByRunIdOrderByIdAsc(any())).thenAnswer(invocation -> savedOutputs);
    when(sourceReferenceRepository.findByRunIdOrderByIdAsc(any())).thenAnswer(invocation -> savedReferences);
    when(fileItemRepository.findById("file-001")).thenReturn(Optional.of(file("file-001")));
    when(sourceChunkRepository.findById("chunk-file-001-p12-b02"))
        .thenReturn(
            Optional.of(
                SourceChunk.create(
                    "chunk-file-001-p12-b02",
                    "file-001",
                    "Discovery/BRD.docx",
                    12,
                    "Scope",
                    new BigDecimal("0.820"),
                    ReviewStatus.REVIEW_REQUIRED)));
  }

  @Test
  void createRunPersistsReviewRequiredOutputAndSourceReferences() {
    ModelService service = service(adapter);

    var response =
        service.createRun(
            new CreateModelRunRequest(
                "mock-model",
                "deepseek-flash",
                ModelOperation.CHAT,
                "review-assist",
                "delivery-lead",
                "mock",
                "source-chunk:chunk-file-001-p12-b02",
                "Summarize the referenced chunk.",
                List.of(
                    new ModelSourceReferenceRequest(
                        ModelSourceReferenceType.SOURCE_CHUNK,
                        "chunk-file-001-p12-b02",
                        "BRD page 12"))));

    assertThat(response.status()).isEqualTo(ModelRunStatus.SUCCEEDED);
    assertThat(response.outputs()).hasSize(1);
    assertThat(response.outputs().getFirst().reviewStatus()).isEqualTo(ReviewStatus.REVIEW_REQUIRED);
    assertThat(response.sourceReferences()).hasSize(1);
    assertThat(response.sourceReferences().getFirst().confidence()).isEqualByComparingTo("0.820");
    assertThat(response.usage().outputCount()).isEqualTo(1);
    assertThat(savedOutputs.getFirst().getReviewStatus()).isEqualTo(ReviewStatus.REVIEW_REQUIRED);
  }

  @Test
  void embeddingRunPersistsMetadataOnly() {
    ModelService service = service(adapter);

    var response =
        service.createRun(
            new CreateModelRunRequest(
                "mock-model",
                "text-embedding-v4",
                ModelOperation.EMBEDDING,
                "index-preview",
                "delivery-lead",
                "mock",
                "file:file-001",
                "Create mock embedding metadata.",
                List.of(new ModelSourceReferenceRequest(ModelSourceReferenceType.FILE_ITEM, "file-001", "BRD"))));

    assertThat(response.outputs().getFirst().kind()).isEqualTo(ModelOutputKind.EMBEDDING_METADATA);
    assertThat(response.outputs().getFirst().embeddingDimension()).isEqualTo(1024);
    assertThat(response.outputs().getFirst().embeddingItemCount()).isEqualTo(1);
    assertThat(response.outputs().getFirst().safeSummary()).contains("metadata");
  }

  @Test
  void rejectsUnsafeInputBeforeAdapterExecution() {
    CapturingModelAdapter capturingAdapter = new CapturingModelAdapter(adapter);
    ModelService service = service(capturingAdapter);

    assertThatThrownBy(
            () ->
                service.createRun(
                    new CreateModelRunRequest(
                        "mock-model",
                        "deepseek-flash",
                        ModelOperation.CHAT,
                        "review-assist",
                        "delivery-lead",
                        "mock",
                        "https://provider.example/raw",
                        "pass" + "word=secret",
                        List.of())))
        .isInstanceOf(RequestValidationException.class);

    assertThat(capturingAdapter.called).isFalse();
    verify(modelRunRepository, never()).save(any(ModelRun.class));
  }

  @Test
  void adapterFaultMasksSecretsEndpointsHostsAndPaths() {
    ModelAdapter faultyAdapter =
        new CapturingModelAdapter(adapter) {
          @Override
          public ModelResult execute(ModelRequest request) {
            throw new IllegalStateException(
                "token=${MODEL_TEST_TOKEN} at https://provider.internal.local/v1 host model.corp.example /private/model/runtime");
          }
        };
    ModelService service = service(faultyAdapter);

    var response =
        service.createRun(
            new CreateModelRunRequest(
                "mock-model",
                "deepseek-flash",
                ModelOperation.CHAT,
                "review-assist",
                "delivery-lead",
                "mock",
                "source-chunk:chunk-file-001-p12-b02",
                "Summarize safely.",
                List.of()));

    assertThat(response.status()).isEqualTo(ModelRunStatus.FAILED);
    assertThat(response.safeMessage())
        .doesNotContain(
            "${MODEL_TEST_TOKEN}",
            "MODEL_TEST_TOKEN",
            "https://",
            "provider.internal.local",
            "model.corp.example",
            "/private/model/runtime")
        .contains("token", "[masked]", "[endpoint]", "[host]", "[path]");
    assertThat(savedOutputs).isEmpty();
  }

  @Test
  void unavailableAdapterCompletesFailedWithoutExecution() {
    ModelAdapter unavailableAdapter =
        new ModelAdapter() {
          @Override
          public List<ModelCapability> capabilities() {
            return List.of(
                new ModelCapability(
                    "configured-model",
                    "configured-chat",
                    "Configured Chat Model",
                    "configured provider",
                    ModelType.CHAT,
                    List.of(ModelOperation.CHAT),
                    false,
                    ModelAdapterStatus.MISCONFIGURED,
                    8192,
                    java.util.Map.of("credential", "missing", "endpoint", "missing")));
          }

          @Override
          public ModelResult execute(ModelRequest request) {
            throw new AssertionError("Unavailable adapters must fail before execution.");
          }
        };
    ModelService service = service(unavailableAdapter);

    var response =
        service.createRun(
            new CreateModelRunRequest(
                "configured-model",
                "configured-chat",
                ModelOperation.CHAT,
                "review-assist",
                "delivery-lead",
                "configured",
                "source-chunk:chunk-file-001-p12-b02",
                "Summarize safely.",
                List.of()));

    assertThat(response.status()).isEqualTo(ModelRunStatus.FAILED);
    assertThat(response.safeMessage()).isEqualTo("Model adapter is unavailable.");
    assertThat(response.outputs()).isEmpty();
    assertThat(savedOutputs).isEmpty();
  }

  @Test
  void partialOutputFailuresMapToPartialFailedSummary() {
    ModelAdapter partialAdapter =
        new CapturingModelAdapter(adapter) {
          @Override
          public ModelResult execute(ModelRequest request) {
            return new ModelResult(
                "mock-model",
                "deepseek-flash",
                "Partial mock result.",
                new ModelResult.ModelUsage(3, 5),
                List.of(
                    new ModelResult.ModelOutput(
                        "partial-ok",
                        ModelOutputKind.TEXT_SUMMARY,
                        "generated/model/partial-ok.json",
                        "Mock output",
                        List.of(),
                        null,
                        null,
                        new BigDecimal("0.820"),
                        ReviewStatus.REVIEW_REQUIRED,
                        null),
                    new ModelResult.ModelOutput(
                        "partial-error",
                        ModelOutputKind.ERROR,
                        "generated/model/partial-error.json",
                        "Mock failed output descriptor",
                        List.of(),
                        null,
                        null,
                        new BigDecimal("0.120"),
                        ReviewStatus.REVIEW_REQUIRED,
                        "Mock output failed safely.")));
          }
        };
    ModelService service = service(partialAdapter);

    var response =
        service.createRun(
            new CreateModelRunRequest(
                "mock-model",
                "deepseek-flash",
                ModelOperation.CHAT,
                "review-assist",
                "delivery-lead",
                "mock",
                "source-chunk:chunk-file-001-p12-b02",
                "Summarize safely.",
                List.of()));

    assertThat(response.status()).isEqualTo(ModelRunStatus.PARTIAL_FAILED);
    assertThat(response.usage().outputCount()).isEqualTo(2);
    assertThat(response.usage().failedOutputCount()).isEqualTo(1);
    assertThat(response.outputs()).extracting(output -> output.kind()).contains(ModelOutputKind.ERROR);
  }

  @Test
  void rejectsAdapterOutputWithApprovedReviewStatus() {
    ModelAdapter invalidAdapter =
        new CapturingModelAdapter(adapter) {
          @Override
          public ModelResult execute(ModelRequest request) {
            return new ModelResult(
                "mock-model",
                "deepseek-flash",
                "Done.",
                new ModelResult.ModelUsage(1, 1),
                List.of(
                    new ModelResult.ModelOutput(
                        "output",
                        ModelOutputKind.TEXT_SUMMARY,
                        "generated/model/output.json",
                        "Mock output",
                        List.of(),
                        null,
                        null,
                        new BigDecimal("0.820"),
                        ReviewStatus.APPROVED,
                        null)));
          }
        };
    ModelService service = service(invalidAdapter);

    assertThatThrownBy(
            () ->
                service.createRun(
                    new CreateModelRunRequest(
                        "mock-model",
                        "deepseek-flash",
                        ModelOperation.CHAT,
                        "review-assist",
                        "delivery-lead",
                        "mock",
                        "source-chunk:chunk-file-001-p12-b02",
                        "Summarize safely.",
                        List.of())))
        .isInstanceOf(RequestValidationException.class);
  }

  private ModelService service(ModelAdapter modelAdapter) {
    return new ModelService(
        modelRunRepository,
        modelRunOutputRepository,
        sourceReferenceRepository,
        fileItemRepository,
        sourceChunkRepository,
        new ModelAdapterRegistry(List.of(modelAdapter)),
        new ModelSummaryCalculator(),
        CLOCK);
  }

  private FileItem file(String id) {
    return FileItem.create(
        id,
        "batch",
        "Discovery/" + id + ".pdf",
        SourceType.pdf,
        com.atlas.metadata.enums.FileStatus.MARKDOWN_GENERATED,
        new BigDecimal("0.820"),
        ReviewStatus.REVIEW_REQUIRED,
        OffsetDateTime.now(CLOCK));
  }

  private static class CapturingModelAdapter implements ModelAdapter {

    private final ModelAdapter delegate;
    private boolean called;

    CapturingModelAdapter(ModelAdapter delegate) {
      this.delegate = delegate;
    }

    @Override
    public List<ModelCapability> capabilities() {
      return delegate.capabilities();
    }

    @Override
    public ModelResult execute(ModelRequest request) {
      called = true;
      return delegate.execute(request);
    }
  }
}
