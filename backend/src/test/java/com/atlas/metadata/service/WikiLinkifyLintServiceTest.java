package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.domain.WikiGenerationRun;
import com.atlas.metadata.domain.WikiPage;
import com.atlas.metadata.domain.WikiPageIssue;
import com.atlas.metadata.dto.CreateWikiLinkifyLintRunRequest;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.exception.RequestValidationException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** Unit tests for deterministic Wiki linkify/lint maintenance runs. */
@ExtendWith(MockitoExtension.class)
class WikiLinkifyLintServiceTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-07-05T01:00:00Z"), ZoneOffset.UTC);

  @TempDir private Path tempDir;

  @Mock private SpaceService spaceService;
  @Mock private WikiPageRepository wikiPageRepository;
  @Mock private WikiGenerationRunRepository wikiGenerationRunRepository;
  @Mock private WikiLogEntryRepository wikiLogEntryRepository;
  @Mock private WikiPageIssueRepository wikiPageIssueRepository;
  @Mock private FileItemRepository fileItemRepository;
  @Mock private SourceChunkRepository sourceChunkRepository;

  private LocalArtifactStorageService artifactStorage;
  private WikiLinkifyLintService service;

  @BeforeEach
  void setUp() {
    artifactStorage = new LocalArtifactStorageService(tempDir);
    service =
        new WikiLinkifyLintService(
            spaceService,
            wikiPageRepository,
            wikiGenerationRunRepository,
            wikiLogEntryRepository,
            wikiPageIssueRepository,
            fileItemRepository,
            sourceChunkRepository,
            artifactStorage,
            CLOCK);
  }

  @Test
  void linkifiesPagesAndRecordsSafeLintIssues() throws Exception {
    WikiPage source =
        page(
            "wiki-source",
            "Migration Scope",
            "migration-scope",
            "file-source",
            chunk("chunk-source", "file-source"));
    WikiPage target =
        page(
            "wiki-target",
            "Application Inventory",
            "application-inventory",
            "file-target",
            chunk("chunk-target", "file-target"));
    WikiPage staleThin =
        page(
            "wiki-stale",
            "Stale Note",
            "stale-note",
            "file-missing",
            chunk("chunk-missing", "file-missing"));

    write(source, "Application Inventory appears twice. Application Inventory again. [[missing-page]]");
    write(target, "Application Inventory has enough reviewed source text to avoid thin content.");
    write(staleThin, "Thin.");

    when(wikiPageRepository.findBySpaceIdOrderByTitleAsc("space"))
        .thenReturn(List.of(target, source, staleThin));
    when(wikiPageRepository.save(any(WikiPage.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(wikiPageIssueRepository.save(any(WikiPageIssue.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(wikiGenerationRunRepository.save(any(WikiGenerationRun.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(fileItemRepository.existsById("file-source")).thenReturn(true);
    when(fileItemRepository.existsById("file-target")).thenReturn(true);
    when(fileItemRepository.existsById("file-missing")).thenReturn(false);
    when(sourceChunkRepository.existsById("chunk-source")).thenReturn(true);
    when(sourceChunkRepository.existsById("chunk-target")).thenReturn(true);

    var response =
        service.startLinkifyLintRun(
            "space", new CreateWikiLinkifyLintRunRequest(null, "knowledge-manager", false, true, true));

    assertThat(response.status()).isEqualTo("PARTIAL_FAILED");
    assertThat(response.mode()).isEqualTo("linkify-lint");
    assertThat(response.scannedPageCount()).isEqualTo(3);
    assertThat(response.insertedLinkCount()).isEqualTo(1);
    assertThat(response.brokenLinkCount()).isEqualTo(1);
    assertThat(response.orphanPageCount()).isEqualTo(2);
    assertThat(response.sourceIssueCount()).isEqualTo(1);
    assertThat(response.thinContentCount()).isEqualTo(1);
    assertThat(response.updatedPageIds()).containsExactly("wiki-target", "wiki-source");
    assertThat(Files.readString(tempDir.resolve(source.getMarkdownPath())))
        .contains("[[application-inventory]] appears twice")
        .contains("Application Inventory again");
    assertThat(source.getOutLinks()).containsExactly("application-inventory", "missing-page");
    assertThat(target.getInLinks()).containsExactly("migration-scope");
    assertThat(staleThin.getReviewStatus()).isEqualTo(ReviewStatus.REVIEW_REQUIRED);

    ArgumentCaptor<WikiPageIssue> issues = ArgumentCaptor.forClass(WikiPageIssue.class);
    org.mockito.Mockito.verify(wikiPageIssueRepository, org.mockito.Mockito.atLeast(1)).save(issues.capture());
    assertThat(issues.getAllValues())
        .extracting(WikiPageIssue::getIssueType)
        .contains("BROKEN_LINK", "ORPHAN_PAGE", "STALE_SOURCE", "THIN_CONTENT");
    assertThat(response.safeSummary()).doesNotContain(System.getProperty("user.home")).doesNotContain("password");
  }

  @Test
  void ambiguousAliasesAreNotAutoLinkedAndRequireReview() throws Exception {
    WikiPage source =
        page("wiki-source", "Migration Scope", "migration-scope", "file-source", chunk("chunk-source", "file-source"));
    WikiPage alpha = page("wiki-alpha", "Alpha App", "alpha-app", "file-alpha", chunk("chunk-alpha", "file-alpha"));
    WikiPage beta = page("wiki-beta", "Beta App", "beta-app", "file-beta", chunk("chunk-beta", "file-beta"));
    ReflectionTestUtils.setField(alpha, "aliases", new String[] {"Shared System"});
    ReflectionTestUtils.setField(beta, "aliases", new String[] {"Shared System"});

    write(source, "Shared System appears in enough reviewed text and should not be guessed.");
    write(alpha, "Alpha App has enough reviewed source text.");
    write(beta, "Beta App has enough reviewed source text.");

    when(wikiPageRepository.findBySpaceIdOrderByTitleAsc("space"))
        .thenReturn(List.of(alpha, beta, source));
    when(wikiPageIssueRepository.save(any(WikiPageIssue.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(wikiGenerationRunRepository.save(any(WikiGenerationRun.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(fileItemRepository.existsById("file-source")).thenReturn(true);
    when(fileItemRepository.existsById("file-alpha")).thenReturn(true);
    when(fileItemRepository.existsById("file-beta")).thenReturn(true);
    when(sourceChunkRepository.existsById("chunk-source")).thenReturn(true);
    when(sourceChunkRepository.existsById("chunk-alpha")).thenReturn(true);
    when(sourceChunkRepository.existsById("chunk-beta")).thenReturn(true);

    var response =
        service.startLinkifyLintRun(
            "space", new CreateWikiLinkifyLintRunRequest(null, "knowledge-manager", false, true, true));

    assertThat(Files.readString(tempDir.resolve(source.getMarkdownPath()))).doesNotContain("[[alpha-app]]", "[[beta-app]]");
    assertThat(source.getOutLinks()).isEmpty();
    assertThat(response.issueIds()).anyMatch(issueId -> issueId.contains("review-required-ambiguous-alias-shared-system"));
    ArgumentCaptor<WikiPageIssue> issues = ArgumentCaptor.forClass(WikiPageIssue.class);
    org.mockito.Mockito.verify(wikiPageIssueRepository, org.mockito.Mockito.atLeast(1)).save(issues.capture());
    assertThat(issues.getAllValues())
        .anySatisfy(
            issue -> {
              assertThat(issue.getPageId()).isEqualTo("wiki-source");
              assertThat(issue.getIssueType()).isEqualTo("REVIEW_REQUIRED");
              assertThat(issue.getMessage()).contains("ambiguous");
            });
  }

  @Test
  void rejectsCrossSpacePageScope() {
    when(wikiPageRepository.findBySpaceIdOrderByTitleAsc("space"))
        .thenReturn(List.of(page("wiki-source", "Migration Scope", "migration-scope", "file-source")));

    assertThatThrownBy(
            () ->
                service.startLinkifyLintRun(
                    "space",
                    new CreateWikiLinkifyLintRunRequest(
                        List.of("wiki-source", "wiki-other-space"), "knowledge-manager", false, true, true)))
        .isInstanceOf(RequestValidationException.class)
        .hasMessageContaining("Invalid request");
  }

  private WikiPage page(String id, String title, String slug, String fileId, SourceChunk... chunks) {
    String markdownPath = "generated/wiki/space/" + slug + ".md";
    return WikiPage.generatedCandidate(
        id,
        "space",
        title,
        slug,
        markdownPath,
        new String[] {fileId},
        List.of(chunks),
        new BigDecimal("0.910"),
        "knowledge-manager",
        OffsetDateTime.now(CLOCK));
  }

  private WikiPage page(String id, String title, String slug, String fileId) {
    return page(id, title, slug, fileId, chunk("chunk-" + fileId, fileId));
  }

  private SourceChunk chunk(String id, String fileId) {
    return SourceChunk.create(id, fileId, fileId + ".pdf", 1, "Overview", new BigDecimal("0.910"), ReviewStatus.APPROVED);
  }

  private void write(WikiPage page, String markdown) throws Exception {
    Files.createDirectories(tempDir.resolve(page.getMarkdownPath()).getParent());
    Files.writeString(tempDir.resolve(page.getMarkdownPath()), markdown);
  }
}
