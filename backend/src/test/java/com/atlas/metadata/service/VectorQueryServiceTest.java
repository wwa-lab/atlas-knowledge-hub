package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.atlas.metadata.adapter.VectorAdapter;
import com.atlas.metadata.adapter.VectorCapability;
import com.atlas.metadata.adapter.VectorDeleteRequest;
import com.atlas.metadata.adapter.VectorDeleteResult;
import com.atlas.metadata.adapter.VectorIndexRequest;
import com.atlas.metadata.adapter.VectorIndexResult;
import com.atlas.metadata.adapter.VectorQueryRequest;
import com.atlas.metadata.adapter.VectorQueryResult;
import com.atlas.metadata.domain.Batch;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.dto.VectorQueryRequestDto;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceKind;
import com.atlas.metadata.enums.SourceType;
import com.atlas.metadata.enums.VectorAdapterStatus;
import com.atlas.metadata.enums.VectorReviewPolicy;
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
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** Unit tests for review-aware vector query evidence behavior. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VectorQueryServiceTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-07-03T00:00:00Z"), ZoneOffset.UTC);

  @Mock private SpaceRepository spaceRepository;
  @Mock private BatchRepository batchRepository;
  @Mock private FileItemRepository fileItemRepository;
  @Mock private SourceChunkRepository sourceChunkRepository;
  @Mock private VectorRunRepository vectorRunRepository;
  @Mock private VectorItemResultRepository vectorItemResultRepository;

  @BeforeEach
  void setUp() {
    when(spaceRepository.existsById("space")).thenReturn(true);
    when(batchRepository.findBySpaceId("space"))
        .thenReturn(
            List.of(
                Batch.create(
                    "batch",
                    "space",
                    "Vector Query Test",
                    SourceKind.folder,
                    "delivery-lead",
                    OffsetDateTime.now(CLOCK))));
    when(fileItemRepository.findByBatchIdIn(any())).thenReturn(List.of(file("file-001")));
    when(sourceChunkRepository.findAllById(any()))
        .thenReturn(
            List.of(
                chunk("chunk-review", ReviewStatus.REVIEW_REQUIRED),
                chunk("chunk-approved", ReviewStatus.APPROVED)));
  }

  @Test
  void queryDefaultsToApprovedEvidenceAndSortsByScoreThenChunkId() {
    VectorService service = service(new QueryOnlyAdapter());

    var response =
        service.query(
            "space",
            new VectorQueryRequestDto(
                "query-adapter",
                List.of(new BigDecimal("0.100"), new BigDecimal("0.200"), new BigDecimal("0.300")),
                null,
                5,
                null));

    assertThat(response.reviewPolicy()).isEqualTo(VectorReviewPolicy.APPROVED_ONLY);
    assertThat(response.matches()).singleElement().satisfies(match -> {
      assertThat(match.sourceChunkId()).isEqualTo("chunk-approved");
      assertThat(match.reviewStatus()).isEqualTo(ReviewStatus.APPROVED);
      assertThat(match.score()).isEqualByComparingTo("0.900");
      assertThat(match.safeMetadata().get("diagnostic")).doesNotContain("https://", "internal.example");
    });
  }

  @Test
  void queryCanExplicitlyIncludeReviewRequiredEvidence() {
    VectorService service = service(new QueryOnlyAdapter());

    var response =
        service.query(
            "space",
            new VectorQueryRequestDto(
                "query-adapter",
                null,
                "mock-token",
                5,
                VectorReviewPolicy.INCLUDE_REVIEW_REQUIRED));

    assertThat(response.matches()).extracting(match -> match.sourceChunkId())
        .containsExactly("chunk-approved", "chunk-review");
    assertThat(response.matches())
        .filteredOn(match -> match.sourceChunkId().equals("chunk-review"))
        .singleElement()
        .satisfies(match -> assertThat(match.reviewStatus()).isEqualTo(ReviewStatus.REVIEW_REQUIRED));
  }

  @Test
  void queryRejectsMissingAdapterScoreAsInvalidEvidence() {
    VectorService service =
        service(
            new QueryOnlyAdapter() {
              @Override
              public VectorQueryResult query(VectorQueryRequest request) {
                return new VectorQueryResult(
                    "query-adapter",
                    List.of(
                        new VectorQueryResult.VectorMatch(
                            "chunk-approved", "vector/chunk-approved", null, Map.of())),
                    "Invalid score.");
              }
            });

    assertThatThrownBy(
            () ->
                service.query(
                    "space",
                    new VectorQueryRequestDto("query-adapter", null, "mock-token", 5, null)))
        .isInstanceOf(RequestValidationException.class)
        .satisfies(
            error ->
                assertThat(((RequestValidationException) error).getFields())
                    .containsEntry("score", "must be present for query matches"));
  }

  private VectorService service(VectorAdapter adapter) {
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

  private static class QueryOnlyAdapter implements VectorAdapter {
    @Override
    public VectorCapability capability() {
      return new VectorCapability(
          "query-adapter",
          "Query Adapter",
          "test",
          List.of(3),
          List.of("QUERY"),
          true,
          VectorAdapterStatus.AVAILABLE,
          Map.of("endpoint", "not_configured", "credentials", "not_configured"));
    }

    @Override
    public VectorIndexResult index(VectorIndexRequest request) {
      throw new UnsupportedOperationException("not used");
    }

    @Override
    public VectorDeleteResult delete(VectorDeleteRequest request) {
      throw new UnsupportedOperationException("not used");
    }

    @Override
    public VectorQueryResult query(VectorQueryRequest request) {
      return new VectorQueryResult(
          "query-adapter",
          List.of(
              new VectorQueryResult.VectorMatch(
                  "chunk-review",
                  "vector/chunk-review",
                  new BigDecimal("0.900"),
                  Map.of("diagnostic", "endpoint=https://internal.example/vector")),
              new VectorQueryResult.VectorMatch(
                  "chunk-approved",
                  "vector/chunk-approved",
                  new BigDecimal("0.900"),
                  Map.of("diagnostic", "endpoint=https://internal.example/vector"))),
          "Query completed.");
    }
  }
}
