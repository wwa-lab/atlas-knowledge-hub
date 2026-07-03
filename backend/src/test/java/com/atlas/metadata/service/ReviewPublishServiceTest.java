package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlas.metadata.domain.Batch;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.domain.WikiPage;
import com.atlas.metadata.dto.CreateWikiPublishRequest;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceKind;
import com.atlas.metadata.enums.SourceType;
import com.atlas.metadata.exception.ConflictException;
import com.atlas.metadata.repository.BatchRepository;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.SourceChunkRepository;
import com.atlas.metadata.repository.WikiPageRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for review queues, publish eligibility, and wiki metadata publishing. */
@ExtendWith(MockitoExtension.class)
class ReviewPublishServiceTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-07-03T00:00:00Z"), ZoneOffset.UTC);

  @Mock private SpaceService spaceService;
  @Mock private BatchRepository batchRepository;
  @Mock private FileItemRepository fileItemRepository;
  @Mock private SourceChunkRepository sourceChunkRepository;
  @Mock private WikiPageRepository wikiPageRepository;

  private ReviewPublishService service;

  @BeforeEach
  void setUp() {
    service =
        new ReviewPublishService(
            spaceService, batchRepository, fileItemRepository, sourceChunkRepository, wikiPageRepository, CLOCK);
  }

  @Test
  void reviewQueuesSeparateBlockedConditionsAndPublishReadyCandidates() {
    when(batchRepository.findBySpaceId("space")).thenReturn(List.of(batch()));
    FileItem ready = file("ready", FileStatus.MARKDOWN_GENERATED, ReviewStatus.APPROVED, "md/ready.md", "0.910");
    FileItem low =
        file("low", FileStatus.LOW_CONFIDENCE, ReviewStatus.REVIEW_REQUIRED, "md/low.md", "0.540");
    FileItem ocr = file("ocr", FileStatus.OCR_REQUIRED, ReviewStatus.OCR_REQUIRED, null, "0.410");
    FileItem failed = file("failed", FileStatus.PDF_CONVERT_FAILED, ReviewStatus.REVIEW_REQUIRED, null, "0");
    FileItem missingTrace =
        file("missing", FileStatus.MARKDOWN_GENERATED, ReviewStatus.APPROVED, "md/missing.md", "0.880");
    when(fileItemRepository.findByBatchIdIn(List.of("batch"))).thenReturn(List.of(ready, low, ocr, failed, missingTrace));
    when(sourceChunkRepository.findByFileItemIdIn(List.of("ready", "low", "ocr", "failed", "missing")))
        .thenReturn(List.of(chunk("ready"), chunk("low")));

    var queues = service.getReviewQueues("space");

    assertThat(queues.queues())
        .extracting(queue -> queue.type().name(), queue -> queue.count(), queue -> queue.publishBlocked())
        .contains(
            org.assertj.core.groups.Tuple.tuple("PARSER_FAILURE", 1L, true),
            org.assertj.core.groups.Tuple.tuple("OCR_REQUIRED", 1L, true),
            org.assertj.core.groups.Tuple.tuple("LOW_CONFIDENCE", 1L, true),
            org.assertj.core.groups.Tuple.tuple("MISSING_SOURCE_TRACE", 1L, true),
            org.assertj.core.groups.Tuple.tuple("READY_TO_PUBLISH", 1L, false));
    assertThat(queues.queues())
        .filteredOn(queue -> queue.type().name().equals("MISSING_SOURCE_TRACE"))
        .singleElement()
        .satisfies(
            queue -> {
              assertThat(queue.representativeItems()).hasSize(1);
              assertThat(queue.representativeItems().getFirst().fileId()).isEqualTo("missing");
              assertThat(queue.representativeItems().getFirst().hasSourceTrace()).isFalse();
            });
    assertThat(queues.queues())
        .filteredOn(queue -> queue.type().name().equals("READY_TO_PUBLISH"))
        .singleElement()
        .satisfies(
            queue -> assertThat(queue.representativeItems().getFirst().fileId()).isEqualTo("ready"));
  }

  @Test
  void publishCreatesPublishedWikiMetadataWithoutChangingFileStatus() {
    FileItem approved =
        file("file-003", FileStatus.MARKDOWN_GENERATED, ReviewStatus.APPROVED, "generated/md/page.md", "0.960");
    when(fileItemRepository.findById("file-003")).thenReturn(Optional.of(approved));
    when(batchRepository.findById("batch")).thenReturn(Optional.of(batch()));
    when(sourceChunkRepository.findByFileItemId("file-003")).thenReturn(List.of(chunk("file-003")));
    when(wikiPageRepository.findBySourceDocumentIdsContaining("file-003")).thenReturn(Optional.empty());
    when(wikiPageRepository.save(any(WikiPage.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var response = service.publishFile("file-003", new CreateWikiPublishRequest("Migration Boundary", "sme-team"));

    assertThat(response.id()).isEqualTo("wiki-file-003");
    assertThat(response.reviewStatus()).isEqualTo(ReviewStatus.PUBLISHED);
    assertThat(response.sourceDocumentIds()).containsExactly("file-003");
    assertThat(response.lastUpdated()).isEqualTo(OffsetDateTime.now(CLOCK));
    assertThat(approved.getReviewStatus()).isEqualTo(ReviewStatus.APPROVED);

    ArgumentCaptor<WikiPage> saved = ArgumentCaptor.forClass(WikiPage.class);
    verify(wikiPageRepository).save(saved.capture());
    assertThat(saved.getValue().getMarkdownPath()).isEqualTo("generated/md/page.md");
  }

  @Test
  void publishIsIdempotentForExistingPublishedWikiPage() {
    FileItem approved =
        file("file-003", FileStatus.MARKDOWN_GENERATED, ReviewStatus.APPROVED, "generated/md/page.md", "0.960");
    WikiPage existing =
        WikiPage.publish(
            "wiki-file-003",
            "space",
            "Existing",
            "generated/md/page.md",
            new String[] {"file-003"},
            new BigDecimal("0.960"),
            "sme-team",
            OffsetDateTime.now(CLOCK));
    when(fileItemRepository.findById("file-003")).thenReturn(Optional.of(approved));
    when(batchRepository.findById("batch")).thenReturn(Optional.of(batch()));
    when(sourceChunkRepository.findByFileItemId("file-003")).thenReturn(List.of(chunk("file-003")));
    when(wikiPageRepository.findBySourceDocumentIdsContaining("file-003")).thenReturn(Optional.of(existing));
    when(wikiPageRepository.save(any(WikiPage.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var response = service.publishFile("file-003", new CreateWikiPublishRequest("Migration Boundary", "sme-team"));

    assertThat(response.id()).isEqualTo("wiki-file-003");
    assertThat(response.title()).isEqualTo("Migration Boundary");
    assertThat(response.reviewStatus()).isEqualTo(ReviewStatus.PUBLISHED);
  }

  @Test
  void publishRejectsMissingTraceBeforeWikiMutation() {
    FileItem approved =
        file("file-003", FileStatus.MARKDOWN_GENERATED, ReviewStatus.APPROVED, "generated/md/page.md", "0.960");
    when(fileItemRepository.findById("file-003")).thenReturn(Optional.of(approved));
    when(batchRepository.findById("batch")).thenReturn(Optional.of(batch()));
    when(sourceChunkRepository.findByFileItemId("file-003")).thenReturn(List.of());

    assertThatThrownBy(
            () -> service.publishFile("file-003", new CreateWikiPublishRequest("Migration Boundary", "sme-team")))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("source trace");

    assertThat(approved.getReviewStatus()).isEqualTo(ReviewStatus.APPROVED);
  }

  private Batch batch() {
    return Batch.create(
        "batch",
        "space",
        "Review Publish Test",
        SourceKind.folder,
        "delivery-lead",
        OffsetDateTime.now(CLOCK));
  }

  private FileItem file(
      String id, FileStatus status, ReviewStatus reviewStatus, String markdownPath, String confidence) {
    FileItem item =
        FileItem.create(
            id,
            "batch",
            "Discovery/" + id + ".pdf",
            SourceType.pdf,
            status,
            confidence == null ? null : new BigDecimal(confidence),
            reviewStatus,
            OffsetDateTime.now(CLOCK));
    item.setArtifacts(null, markdownPath, null, null);
    if (reviewStatus != ReviewStatus.REVIEW_REQUIRED) {
      item.applyReviewStatus(reviewStatus);
    }
    return item;
  }

  private SourceChunk chunk(String fileId) {
    return SourceChunk.create(
        "chunk-" + fileId, fileId, fileId + ".pdf", 1, "Overview", new BigDecimal("0.900"), ReviewStatus.APPROVED);
  }
}
