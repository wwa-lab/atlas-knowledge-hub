package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlas.metadata.domain.Batch;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.domain.WikiGenerationRun;
import com.atlas.metadata.domain.WikiPage;
import com.atlas.metadata.domain.WikiPageIssue;
import com.atlas.metadata.dto.CreateWikiIngestRunRequest;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceKind;
import com.atlas.metadata.enums.SourceType;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.repository.BatchRepository;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.SourceChunkRepository;
import com.atlas.metadata.repository.WikiGenerationRunRepository;
import com.atlas.metadata.repository.WikiLogEntryRepository;
import com.atlas.metadata.repository.WikiPageIssueRepository;
import com.atlas.metadata.repository.WikiPageRepository;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for deterministic Auto Wiki ingest v0. */
@ExtendWith(MockitoExtension.class)
class WikiIngestServiceTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-07-05T00:00:00Z"), ZoneOffset.UTC);

  @TempDir private Path tempDir;

  @Mock private SpaceService spaceService;
  @Mock private BatchRepository batchRepository;
  @Mock private FileItemRepository fileItemRepository;
  @Mock private SourceChunkRepository sourceChunkRepository;
  @Mock private WikiPageRepository wikiPageRepository;
  @Mock private WikiGenerationRunRepository wikiGenerationRunRepository;
  @Mock private WikiLogEntryRepository wikiLogEntryRepository;
  @Mock private WikiPageIssueRepository wikiPageIssueRepository;

  private WikiIngestService service;

  @BeforeEach
  void setUp() {
    service =
        new WikiIngestService(
            spaceService,
            batchRepository,
            fileItemRepository,
            sourceChunkRepository,
            wikiPageRepository,
            wikiGenerationRunRepository,
            wikiLogEntryRepository,
            wikiPageIssueRepository,
            new LocalArtifactStorageService(tempDir),
            CLOCK);
  }

  @Test
  void createsReviewRequiredCandidateOnlyFromApprovedTraceableChunks() throws Exception {
    FileItem approved = file("file-approved", FileStatus.MARKDOWN_GENERATED, ReviewStatus.APPROVED);
    FileItem needsFix = file("file-fix", FileStatus.MARKDOWN_GENERATED, ReviewStatus.NEED_FIX);
    FileItem ocr = file("file-ocr", FileStatus.OCR_REQUIRED, ReviewStatus.APPROVED);
    when(batchRepository.findBySpaceId("space")).thenReturn(List.of(batch()));
    when(fileItemRepository.findByBatchIdIn(List.of("batch"))).thenReturn(List.of(approved, needsFix, ocr));
    when(sourceChunkRepository.findByFileItemIdIn(List.of("file-approved", "file-fix", "file-ocr")))
        .thenReturn(
            List.of(
                chunk("chunk-approved", "file-approved", ReviewStatus.APPROVED, "0.920"),
                chunk("chunk-fix", "file-fix", ReviewStatus.APPROVED, "0.910"),
                chunk("chunk-ocr", "file-ocr", ReviewStatus.APPROVED, "0.900")));
    when(wikiPageRepository.findBySpaceIdAndSlug("space", "file-approved")).thenReturn(Optional.empty());
    when(wikiPageRepository.save(any(WikiPage.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(wikiGenerationRunRepository.save(any(WikiGenerationRun.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response =
        service.startIngestRun(
            "space", new CreateWikiIngestRunRequest("deterministic", null, "knowledge-manager", false));

    assertThat(response.status()).isEqualTo("SUCCEEDED");
    assertThat(response.mode()).isEqualTo("deterministic");
    assertThat(response.createdPageIds()).containsExactly("wiki-auto-file-approved");
    assertThat(response.updatedPageIds()).isEmpty();
    assertThat(response.eligibleChunkCount()).isEqualTo(1);
    assertThat(response.excludedChunkCount()).isEqualTo(2);
    assertThat(response.safeSummary()).contains("review-required Wiki candidate");

    ArgumentCaptor<WikiPage> savedPage = ArgumentCaptor.forClass(WikiPage.class);
    verify(wikiPageRepository).save(savedPage.capture());
    assertThat(savedPage.getValue().getPageType()).isEqualTo("TOPIC");
    assertThat(savedPage.getValue().getReviewStatus()).isEqualTo(ReviewStatus.REVIEW_REQUIRED);
    assertThat(savedPage.getValue().getSourceMode()).isEqualTo("AUTO_GENERATED");
    assertThat(savedPage.getValue().getRefreshPolicy()).isEqualTo("ON_SOURCE_CHANGE");
    assertThat(savedPage.getValue().getConfidence()).isEqualByComparingTo("0.920");

    String markdown = Files.readString(tempDir.resolve(savedPage.getValue().getMarkdownPath()));
    assertThat(markdown).contains("Review status: REVIEW_REQUIRED");
    assertThat(markdown).contains("SOURCE_CHUNK chunk-approved");
    assertThat(markdown).doesNotContain("password").doesNotContain(System.getProperty("user.home"));
  }

  @Test
  void repeatedRunMergesGeneratedCandidateBySlug() {
    FileItem approved = file("file-approved", FileStatus.MARKDOWN_GENERATED, ReviewStatus.APPROVED);
    WikiPage existing =
        WikiPage.generatedCandidate(
            "wiki-auto-file-approved",
            "space",
            "file approved",
            "file-approved",
            "generated/wiki/space/file-approved.md",
            new String[] {"file-approved"},
            List.of(chunk("chunk-approved", "file-approved", ReviewStatus.APPROVED, "0.920")),
            new BigDecimal("0.920"),
            "knowledge-manager",
            OffsetDateTime.now(CLOCK));
    when(batchRepository.findBySpaceId("space")).thenReturn(List.of(batch()));
    when(fileItemRepository.findByBatchIdIn(List.of("batch"))).thenReturn(List.of(approved));
    when(sourceChunkRepository.findByFileItemIdIn(List.of("file-approved")))
        .thenReturn(List.of(chunk("chunk-approved", "file-approved", ReviewStatus.APPROVED, "0.910")));
    when(wikiPageRepository.findBySpaceIdAndSlug("space", "file-approved")).thenReturn(Optional.of(existing));
    when(wikiPageRepository.save(any(WikiPage.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(wikiGenerationRunRepository.save(any(WikiGenerationRun.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response =
        service.startIngestRun(
            "space", new CreateWikiIngestRunRequest(null, List.of("file-approved"), "knowledge-manager", false));

    assertThat(response.createdPageIds()).isEmpty();
    assertThat(response.updatedPageIds()).containsExactly("wiki-auto-file-approved");
    assertThat(existing.getVersion()).isEqualTo(2);
    assertThat(existing.getReviewStatus()).isEqualTo(ReviewStatus.REVIEW_REQUIRED);
  }

  @Test
  void trustedPublishedSlugCollisionRecordsSafeIssueWithoutOverwritingPage() {
    FileItem approved = file("file-approved", FileStatus.MARKDOWN_GENERATED, ReviewStatus.APPROVED);
    WikiPage trusted =
        WikiPage.publish(
            "wiki-trusted",
            "space",
            "file approved",
            "generated/wiki/file-approved.md",
            new String[] {"file-trusted"},
            new BigDecimal("0.970"),
            "sme-team",
            OffsetDateTime.now(CLOCK));
    when(batchRepository.findBySpaceId("space")).thenReturn(List.of(batch()));
    when(fileItemRepository.findByBatchIdIn(List.of("batch"))).thenReturn(List.of(approved));
    when(sourceChunkRepository.findByFileItemIdIn(List.of("file-approved")))
        .thenReturn(List.of(chunk("chunk-approved", "file-approved", ReviewStatus.APPROVED, "0.910")));
    when(wikiPageRepository.findBySpaceIdAndSlug("space", "file-approved")).thenReturn(Optional.of(trusted));
    when(wikiPageIssueRepository.save(any(WikiPageIssue.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(wikiGenerationRunRepository.save(any(WikiGenerationRun.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var response =
        service.startIngestRun(
            "space", new CreateWikiIngestRunRequest("deterministic", null, "knowledge-manager", false));

    assertThat(response.status()).isEqualTo("PARTIAL_FAILED");
    assertThat(response.issueIds()).hasSize(1);
    assertThat(trusted.getReviewStatus()).isEqualTo(ReviewStatus.PUBLISHED);
    assertThat(trusted.getSourceMode()).isEqualTo("PUBLISHED_FILE");
    verify(wikiPageRepository, never()).save(any(WikiPage.class));
  }

  @Test
  void modelAssistedModeIsRejectedInV0() {
    assertThatThrownBy(
            () ->
                service.startIngestRun(
                    "space",
                    new CreateWikiIngestRunRequest("model-assisted", null, "knowledge-manager", false)))
        .isInstanceOf(RequestValidationException.class)
        .hasMessageContaining("Invalid request");
  }

  private Batch batch() {
    return Batch.create(
        "batch",
        "space",
        "Wiki Ingest Test",
        SourceKind.folder,
        "delivery-lead",
        OffsetDateTime.now(CLOCK));
  }

  private FileItem file(String id, FileStatus status, ReviewStatus reviewStatus) {
    FileItem item =
        FileItem.create(
            id,
            "batch",
            "Discovery/" + id + ".pdf",
            SourceType.pdf,
            status,
            new BigDecimal("0.930"),
            ReviewStatus.REVIEW_REQUIRED,
            OffsetDateTime.now(CLOCK));
    item.setArtifacts(null, "generated/markdown/" + id + ".md", null, null);
    item.applyReviewStatus(reviewStatus);
    return item;
  }

  private SourceChunk chunk(String id, String fileId, ReviewStatus reviewStatus, String confidence) {
    return SourceChunk.create(
        id, fileId, fileId + ".pdf", 1, "Overview", new BigDecimal(confidence), reviewStatus);
  }
}
