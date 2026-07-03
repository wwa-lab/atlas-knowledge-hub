package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlas.metadata.domain.AskEvidence;
import com.atlas.metadata.domain.AskRun;
import com.atlas.metadata.domain.Batch;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.dto.CreateAskRequest;
import com.atlas.metadata.dto.ModelOutputResponse;
import com.atlas.metadata.dto.ModelRunResponse;
import com.atlas.metadata.dto.VectorQueryMatchResponse;
import com.atlas.metadata.dto.VectorQueryResponse;
import com.atlas.metadata.enums.AskReviewPolicy;
import com.atlas.metadata.enums.AskRunStatus;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ModelOperation;
import com.atlas.metadata.enums.ModelOutputKind;
import com.atlas.metadata.enums.ModelRunStatus;
import com.atlas.metadata.enums.ModelType;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceKind;
import com.atlas.metadata.enums.SourceType;
import com.atlas.metadata.enums.VectorReviewPolicy;
import com.atlas.metadata.repository.AskEvidenceRepository;
import com.atlas.metadata.repository.AskRunRepository;
import com.atlas.metadata.repository.BatchRepository;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.SpaceRepository;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** Unit tests for trusted ask orchestration through vector and model services. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AskServiceTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-07-03T00:00:00Z"), ZoneOffset.UTC);

  @Mock private SpaceRepository spaceRepository;
  @Mock private BatchRepository batchRepository;
  @Mock private FileItemRepository fileItemRepository;
  @Mock private VectorService vectorService;
  @Mock private ModelService modelService;
  @Mock private AskRunRepository askRunRepository;
  @Mock private AskEvidenceRepository askEvidenceRepository;

  private final List<AskEvidence> savedEvidence = new ArrayList<>();

  @BeforeEach
  void setUp() {
    when(spaceRepository.existsById("space")).thenReturn(true);
    when(batchRepository.findBySpaceId("space"))
        .thenReturn(
            List.of(
                Batch.create(
                    "batch",
                    "space",
                    "Trusted Ask Test",
                    SourceKind.folder,
                    "delivery-lead",
                    OffsetDateTime.now(CLOCK))));
    when(fileItemRepository.findByBatchIdIn(any()))
        .thenReturn(List.of(file("file-approved", SourceType.pdf), file("file-review", SourceType.docx)));
    when(askRunRepository.save(any(AskRun.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(askEvidenceRepository.saveAll(any()))
        .thenAnswer(
            invocation -> {
              List<AskEvidence> items = invocation.getArgument(0);
              savedEvidence.addAll(items);
              return items;
            });
    when(askEvidenceRepository.findByAskRunIdOrderByCreatedAtAsc(any())).thenReturn(savedEvidence);
    when(vectorService.query(any(), any()))
        .thenReturn(
            new VectorQueryResponse(
                "space",
                "mock-vector",
                "mock",
                VectorReviewPolicy.APPROVED_ONLY,
                5,
                "Query completed.",
                List.of(
                    match("chunk-approved", "file-approved", ReviewStatus.APPROVED),
                    match("chunk-review", "file-review", ReviewStatus.REVIEW_REQUIRED))));
    when(modelService.createRun(any())).thenReturn(modelRun(ModelRunStatus.SUCCEEDED, true));
  }

  @Test
  void createRunPersistsEvidenceAndReviewRequiredAnswer() {
    AskService service = service();

    var response =
        service.createRun(
            "space",
            new CreateAskRequest(
                "Which scope is approved?", "delivery-lead", AskReviewPolicy.APPROVED_ONLY, 5, "mock", null));

    assertThat(response.status()).isEqualTo(AskRunStatus.SUCCEEDED);
    assertThat(response.answer()).contains("Mock trusted ask answer");
    assertThat(response.answerReviewStatus()).isEqualTo(ReviewStatus.REVIEW_REQUIRED);
    assertThat(response.evidence()).extracting(item -> item.sourceChunkId()).containsExactly("chunk-approved");
    assertThat(response.modelRunId()).isEqualTo("model-run-001");
    ArgumentCaptor<com.atlas.metadata.dto.CreateModelRunRequest> modelRequest =
        ArgumentCaptor.forClass(com.atlas.metadata.dto.CreateModelRunRequest.class);
    verify(modelService).createRun(modelRequest.capture());
    assertThat(modelRequest.getValue().sourceReferences()).hasSize(1);
    assertThat(modelRequest.getValue().inputReference()).startsWith("ask-context:");
  }

  @Test
  void noEvidenceCompletesWithoutModelCall() {
    when(vectorService.query(any(), any()))
        .thenReturn(
            new VectorQueryResponse(
                "space", "mock-vector", "mock", VectorReviewPolicy.APPROVED_ONLY, 5, "No matches.", List.of()));
    AskService service = service();

    var response =
        service.createRun(
            "space",
            new CreateAskRequest(
                "Which scope is approved?", "delivery-lead", AskReviewPolicy.APPROVED_ONLY, 5, "mock", null));

    assertThat(response.status()).isEqualTo(AskRunStatus.NO_EVIDENCE);
    assertThat(response.answer()).contains("No approved evidence");
    assertThat(response.safeMessage()).contains("No eligible evidence");
    verify(modelService, never()).createRun(any());
  }

  @Test
  void includeReviewRequiredPolicyIsPassedToVectorQuery() {
    AskService service = service();

    var response =
        service.createRun(
            "space",
            new CreateAskRequest(
                "Include review evidence?", "delivery-lead", AskReviewPolicy.INCLUDE_REVIEW_REQUIRED, 3, "mock", null));

    ArgumentCaptor<com.atlas.metadata.dto.VectorQueryRequestDto> queryRequest =
        ArgumentCaptor.forClass(com.atlas.metadata.dto.VectorQueryRequestDto.class);
    verify(vectorService).query(any(), queryRequest.capture());
    assertThat(queryRequest.getValue().reviewPolicy()).isEqualTo(VectorReviewPolicy.INCLUDE_REVIEW_REQUIRED);
    assertThat(queryRequest.getValue().limit()).isEqualTo(3);
    assertThat(response.evidence()).extracting(item -> item.sourceChunkId()).containsExactly("chunk-approved", "chunk-review");
  }

  @Test
  void filteredNoEvidenceSkipsModelCall() {
    AskService service = service();

    var response =
        service.createRun(
            "space",
            new CreateAskRequest(
                "Only spreadsheet evidence?",
                "delivery-lead",
                AskReviewPolicy.APPROVED_ONLY,
                5,
                "mock",
                new com.atlas.metadata.dto.AskFiltersRequest(null, List.of(SourceType.xlsx))));

    assertThat(response.status()).isEqualTo(AskRunStatus.NO_EVIDENCE);
    verify(modelService, never()).createRun(any());
  }

  private AskService service() {
    return new AskService(
        spaceRepository,
        batchRepository,
        fileItemRepository,
        vectorService,
        modelService,
        askRunRepository,
        askEvidenceRepository,
        new AskSummaryCalculator(),
        CLOCK);
  }

  private FileItem file(String id, SourceType sourceType) {
    return FileItem.create(
        id,
        "batch",
        "Trusted/" + id + "." + sourceType.name(),
        sourceType,
        FileStatus.MARKDOWN_GENERATED,
        new BigDecimal("0.910"),
        ReviewStatus.REVIEW_REQUIRED,
        OffsetDateTime.now(CLOCK));
  }

  private VectorQueryMatchResponse match(String chunkId, String fileId, ReviewStatus reviewStatus) {
    return new VectorQueryMatchResponse(
        chunkId,
        fileId,
        "Trusted/BRD.pdf",
        12,
        "Scope",
        reviewStatus,
        new BigDecimal("0.920"),
        "vector/" + chunkId,
        new BigDecimal("0.880"),
        java.util.Map.of());
  }

  private ModelRunResponse modelRun(ModelRunStatus status, boolean output) {
    return new ModelRunResponse(
        "model-run-001",
        "mock-model",
        "deepseek-flash",
        ModelType.CHAT,
        ModelOperation.CHAT,
        status,
        "mock",
        "trusted-ask",
        "delivery-lead",
        "ask-context:ask-001",
        "safe",
        "Model completed.",
        new com.atlas.metadata.dto.ModelUsageResponse(1, 1, output ? 1 : 0, 0),
        output
            ? List.of(
                new ModelOutputResponse(
                    "output-001",
                    ModelOutputKind.TEXT_SUMMARY,
                    "generated/model/output.json",
                    "Mock trusted ask answer grounded in evidence.",
                    List.of(),
                    null,
                    null,
                    new BigDecimal("0.820"),
                    ReviewStatus.REVIEW_REQUIRED,
                    null))
            : List.of(),
        List.of(),
        OffsetDateTime.now(CLOCK),
        OffsetDateTime.now(CLOCK));
  }
}
