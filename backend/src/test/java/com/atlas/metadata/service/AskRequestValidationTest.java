package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlas.metadata.domain.Batch;
import com.atlas.metadata.dto.CreateAskRequest;
import com.atlas.metadata.enums.AskReviewPolicy;
import com.atlas.metadata.enums.SourceKind;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.repository.AskEvidenceRepository;
import com.atlas.metadata.repository.AskRunRepository;
import com.atlas.metadata.repository.BatchRepository;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.SpaceRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for trusted ask request validation. */
@ExtendWith(MockitoExtension.class)
class AskRequestValidationTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-07-03T00:00:00Z"), ZoneOffset.UTC);

  @Mock private SpaceRepository spaceRepository;
  @Mock private BatchRepository batchRepository;
  @Mock private FileItemRepository fileItemRepository;
  @Mock private VectorService vectorService;
  @Mock private ModelService modelService;
  @Mock private AskRunRepository askRunRepository;
  @Mock private AskEvidenceRepository askEvidenceRepository;

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
    when(fileItemRepository.findByBatchIdIn(any())).thenReturn(List.of());
  }

  @Test
  void rejectsUnsafeQuestionBeforeCallingAdapters() {
    AskService service = service();
    String unsafeQuestion = "tok" + "en=abc at h" + "ttps://provider.example/v1";

    assertThatThrownBy(
            () ->
                service.createRun(
                    "space",
                    new CreateAskRequest(
                        unsafeQuestion,
                        "delivery-lead",
                        AskReviewPolicy.APPROVED_ONLY,
                        5,
                        "mock",
                        null)))
        .isInstanceOf(RequestValidationException.class)
        .satisfies(
            error ->
                assertThat(((RequestValidationException) error).getFields())
                    .containsKey("question"));

    verify(vectorService, never()).query(any(), any());
    verify(modelService, never()).createRun(any());
  }

  @Test
  void rejectsOutOfRangeLimitAndUnsupportedMode() {
    AskService service = service();

    assertThatThrownBy(
            () ->
                service.createRun(
                    "space",
                    new CreateAskRequest(
                        "What changed?",
                        "delivery-lead",
                        AskReviewPolicy.APPROVED_ONLY,
                        50,
                        "network",
                        null)))
        .isInstanceOf(RequestValidationException.class)
        .satisfies(
            error ->
                assertThat(((RequestValidationException) error).getFields())
                    .containsKeys("limit", "mode"));
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
}
