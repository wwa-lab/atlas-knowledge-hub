package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlas.metadata.adapter.MockVectorAdapter;
import com.atlas.metadata.domain.Batch;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.domain.VectorItemResult;
import com.atlas.metadata.dto.CreateVectorRunRequest;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceKind;
import com.atlas.metadata.enums.SourceType;
import com.atlas.metadata.enums.VectorReviewPolicy;
import com.atlas.metadata.enums.VectorRunOperation;
import com.atlas.metadata.enums.VectorRunStatus;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.repository.BatchRepository;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.SourceChunkRepository;
import com.atlas.metadata.repository.SpaceRepository;
import com.atlas.metadata.repository.VectorItemResultRepository;
import com.atlas.metadata.repository.VectorRunRepository;
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

/** Unit tests for vector service run validation and execution behavior. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VectorServiceTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-07-03T00:00:00Z"), ZoneOffset.UTC);

  @Mock private SpaceRepository spaceRepository;
  @Mock private BatchRepository batchRepository;
  @Mock private FileItemRepository fileItemRepository;
  @Mock private SourceChunkRepository sourceChunkRepository;
  @Mock private VectorRunRepository vectorRunRepository;
  @Mock private VectorItemResultRepository vectorItemResultRepository;

  private final List<VectorItemResult> savedResults = new ArrayList<>();

  @BeforeEach
  void setUp() {
    when(spaceRepository.existsById("space")).thenReturn(true);
    when(batchRepository.findBySpaceId("space"))
        .thenReturn(
            List.of(
                Batch.create(
                    "batch",
                    "space",
                    "Vector Service Test",
                    SourceKind.folder,
                    "delivery-lead",
                    OffsetDateTime.now(CLOCK))));
    when(fileItemRepository.findByBatchIdIn(any())).thenReturn(List.of(file("file-001")));
    when(vectorRunRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(vectorItemResultRepository.save(any(VectorItemResult.class)))
        .thenAnswer(
            invocation -> {
              VectorItemResult result = invocation.getArgument(0);
              savedResults.add(result);
              return result;
            });
    when(vectorItemResultRepository.findByRunIdOrderByCreatedAtAsc(any()))
        .thenAnswer(invocation -> savedResults);
  }

  @Test
  void indexRunPreservesTraceSkipsReviewRequiredAndDoesNotMutateSourceMetadata() {
    SourceChunk approved = chunk("chunk-approved", ReviewStatus.APPROVED);
    SourceChunk reviewRequired = chunk("chunk-review", ReviewStatus.REVIEW_REQUIRED);
    when(sourceChunkRepository.findByFileItemIdIn(any())).thenReturn(List.of(approved, reviewRequired));
    VectorService service = service(new MockVectorAdapter());

    var response =
        service.createRun(
            "space",
            new CreateVectorRunRequest(
                "mock-vector",
                VectorRunOperation.INDEX,
                null,
                null,
                null,
                VectorReviewPolicy.APPROVED_ONLY,
                3,
                List.of(
                    vector("chunk-approved", "0.100", "0.200", "0.300"),
                    vector("chunk-review", "0.400", "0.500", "0.600")),
                "delivery-lead",
                "mock"));

    assertThat(response.status()).isEqualTo(VectorRunStatus.PARTIAL_FAILED);
    assertThat(response.summary().totalCount()).isEqualTo(2);
    assertThat(response.summary().indexedCount()).isEqualTo(1);
    assertThat(response.summary().skippedCount()).isEqualTo(1);
    assertThat(response.results()).extracting(result -> result.sourceChunkId())
        .containsExactly("chunk-approved", "chunk-review");
    assertThat(response.results())
        .filteredOn(result -> result.sourceChunkId().equals("chunk-approved"))
        .singleElement()
        .satisfies(result -> {
          assertThat(result.vectorItemKey()).isEqualTo("space/chunk-approved");
          assertThat(result.reviewStatus()).isEqualTo(ReviewStatus.APPROVED);
          assertThat(result.confidence()).isEqualByComparingTo("0.930");
        });
    assertThat(response.results())
        .filteredOn(result -> result.sourceChunkId().equals("chunk-review"))
        .singleElement()
        .satisfies(result -> {
          assertThat(result.status().name()).isEqualTo("SKIPPED");
          assertThat(result.reviewStatus()).isEqualTo(ReviewStatus.REVIEW_REQUIRED);
        });
    verify(fileItemRepository, never()).save(any());
    verify(sourceChunkRepository, never()).save(any());
  }

  @Test
  void indexRunIncludesReviewRequiredWhenPolicyAllowsIt() {
    SourceChunk approved = chunk("chunk-approved", ReviewStatus.APPROVED);
    SourceChunk reviewRequired = chunk("chunk-review", ReviewStatus.REVIEW_REQUIRED);
    when(sourceChunkRepository.findByFileItemIdIn(any())).thenReturn(List.of(approved, reviewRequired));
    VectorService service = service(new MockVectorAdapter());

    var response =
        service.createRun(
            "space",
            new CreateVectorRunRequest(
                "mock-vector",
                VectorRunOperation.INDEX,
                null,
                null,
                null,
                VectorReviewPolicy.INCLUDE_REVIEW_REQUIRED,
                3,
                List.of(
                    vector("chunk-approved", "0.100", "0.200", "0.300"),
                    vector("chunk-review", "0.400", "0.500", "0.600")),
                "delivery-lead",
                "mock"));

    assertThat(response.status()).isEqualTo(VectorRunStatus.SUCCEEDED);
    assertThat(response.summary().indexedCount()).isEqualTo(2);
    assertThat(response.summary().skippedCount()).isZero();
    assertThat(response.results())
        .filteredOn(result -> result.sourceChunkId().equals("chunk-review"))
        .singleElement()
        .satisfies(result -> {
          assertThat(result.status().name()).isEqualTo("INDEXED");
          assertThat(result.reviewStatus()).isEqualTo(ReviewStatus.REVIEW_REQUIRED);
          assertThat(result.vectorItemKey()).isEqualTo("space/chunk-review");
        });
    verify(fileItemRepository, never()).save(any());
    verify(sourceChunkRepository, never()).save(any());
  }

  @Test
  void rejectsInvalidDimensionBeforePersistingRun() {
    when(sourceChunkRepository.findByFileItemIdIn(any()))
        .thenReturn(List.of(chunk("chunk-approved", ReviewStatus.APPROVED)));
    VectorService service = service(new MockVectorAdapter());

    assertThatThrownBy(
            () ->
                service.createRun(
                    "space",
                    new CreateVectorRunRequest(
                        "mock-vector",
                        VectorRunOperation.INDEX,
                        null,
                        null,
                        null,
                        VectorReviewPolicy.APPROVED_ONLY,
                        42,
                        null,
                        "delivery-lead",
                        "mock")))
        .isInstanceOf(RequestValidationException.class);

    verify(vectorRunRepository, never()).save(any());
    verify(vectorItemResultRepository, never()).save(any());
  }

  private VectorService service(MockVectorAdapter adapter) {
    return new VectorService(
        spaceRepository,
        batchRepository,
        fileItemRepository,
        sourceChunkRepository,
        vectorRunRepository,
        vectorItemResultRepository,
        new VectorAdapterRegistry(List.of(adapter)),
        new VectorSummaryCalculator(),
        CLOCK);
  }

  private FileItem file(String id) {
    return FileItem.create(
        id,
        "batch",
        "Vector/BRD.pdf",
        SourceType.pdf,
        FileStatus.PDF_CONVERTED,
        BigDecimal.ONE,
        ReviewStatus.REVIEW_REQUIRED,
        OffsetDateTime.now(CLOCK));
  }

  private SourceChunk chunk(String id, ReviewStatus reviewStatus) {
    return SourceChunk.create(
        id,
        "file-001",
        "Vector/BRD.pdf",
        1,
        "Overview",
        new BigDecimal("0.930"),
        reviewStatus);
  }

  private CreateVectorRunRequest.VectorInputItem vector(
      String sourceChunkId, String first, String second, String third) {
    return new CreateVectorRunRequest.VectorInputItem(
        sourceChunkId,
        List.of(new BigDecimal(first), new BigDecimal(second), new BigDecimal(third)));
  }
}
