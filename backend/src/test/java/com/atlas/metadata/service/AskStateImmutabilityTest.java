package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.atlas.metadata.domain.AskRun;
import com.atlas.metadata.domain.Batch;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.dto.CreateAskRequest;
import com.atlas.metadata.dto.ModelRunResponse;
import com.atlas.metadata.dto.VectorQueryMatchResponse;
import com.atlas.metadata.dto.VectorQueryResponse;
import com.atlas.metadata.enums.AskReviewPolicy;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ModelOperation;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Verifies trusted ask does not mutate source file or chunk review state. */
@ExtendWith(MockitoExtension.class)
class AskStateImmutabilityTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-07-03T00:00:00Z"), ZoneOffset.UTC);

  @Mock private SpaceRepository spaceRepository;
  @Mock private BatchRepository batchRepository;
  @Mock private FileItemRepository fileItemRepository;
  @Mock private VectorService vectorService;
  @Mock private ModelService modelService;
  @Mock private AskRunRepository askRunRepository;
  @Mock private AskEvidenceRepository askEvidenceRepository;

  @Test
  void askRunDoesNotPromoteSourceReviewState() {
    FileItem file =
        FileItem.create(
            "file-001",
            "batch",
            "Trusted/BRD.pdf",
            SourceType.pdf,
            FileStatus.MARKDOWN_GENERATED,
            new BigDecimal("0.910"),
            ReviewStatus.REVIEW_REQUIRED,
            OffsetDateTime.now(CLOCK));
    SourceChunk chunk =
        SourceChunk.create(
            "chunk-001",
            "file-001",
            "Trusted/BRD.pdf",
            12,
            "Scope",
            new BigDecimal("0.920"),
            ReviewStatus.REVIEW_REQUIRED);
    List<com.atlas.metadata.domain.AskEvidence> evidence = new ArrayList<>();

    when(spaceRepository.existsById("space")).thenReturn(true);
    when(batchRepository.findBySpaceId("space"))
        .thenReturn(List.of(Batch.create("batch", "space", "Ask", SourceKind.folder, "lead", OffsetDateTime.now(CLOCK))));
    when(fileItemRepository.findByBatchIdIn(any())).thenReturn(List.of(file));
    when(askRunRepository.save(any(AskRun.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(askEvidenceRepository.saveAll(any()))
        .thenAnswer(
            invocation -> {
              List<com.atlas.metadata.domain.AskEvidence> items = invocation.getArgument(0);
              evidence.addAll(items);
              return items;
            });
    when(askEvidenceRepository.findByAskRunIdOrderByCreatedAtAsc(any())).thenReturn(evidence);
    when(vectorService.query(any(), any()))
        .thenReturn(
            new VectorQueryResponse(
                "space",
                "mock-vector",
                "mock",
                VectorReviewPolicy.INCLUDE_REVIEW_REQUIRED,
                5,
                "Query completed.",
                List.of(
                    new VectorQueryMatchResponse(
                        chunk.getId(),
                        file.getId(),
                        chunk.getSourceFile(),
                        chunk.getPage(),
                        chunk.getSection(),
                        chunk.getReviewStatus(),
                        chunk.getConfidence(),
                        "vector/chunk-001",
                        new BigDecimal("0.880"),
                        java.util.Map.of()))));
    when(modelService.createRun(any()))
        .thenReturn(
            new ModelRunResponse(
                "model-run-001",
                "mock-model",
                "deepseek-flash",
                ModelType.CHAT,
                ModelOperation.CHAT,
                ModelRunStatus.SUCCEEDED,
                "mock",
                "trusted-ask",
                "lead",
                "ask-context:ask-001",
                "safe",
                "Model completed.",
                new com.atlas.metadata.dto.ModelUsageResponse(1, 1, 0, 0),
                List.of(),
                List.of(),
                OffsetDateTime.now(CLOCK),
                OffsetDateTime.now(CLOCK)));

    AskService service =
        new AskService(
            spaceRepository,
            batchRepository,
            fileItemRepository,
            vectorService,
            modelService,
            askRunRepository,
            askEvidenceRepository,
            new AskSummaryCalculator(),
            CLOCK);

    service.createRun(
        "space",
        new CreateAskRequest(
            "Can review-required evidence be used?",
            "lead",
            AskReviewPolicy.INCLUDE_REVIEW_REQUIRED,
            5,
            "mock",
            null));

    assertThat(file.getReviewStatus()).isEqualTo(ReviewStatus.REVIEW_REQUIRED);
    assertThat(chunk.getReviewStatus()).isEqualTo(ReviewStatus.REVIEW_REQUIRED);
  }
}
