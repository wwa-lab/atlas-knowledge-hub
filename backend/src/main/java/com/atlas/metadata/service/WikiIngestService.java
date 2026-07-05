package com.atlas.metadata.service;

import com.atlas.metadata.domain.Batch;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.domain.WikiGenerationRun;
import com.atlas.metadata.domain.WikiLogEntry;
import com.atlas.metadata.domain.WikiPage;
import com.atlas.metadata.domain.WikiPageIssue;
import com.atlas.metadata.domain.WikiReference;
import com.atlas.metadata.dto.CreateWikiIngestRunRequest;
import com.atlas.metadata.dto.WikiIngestRunResponse;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.exception.NotFoundException;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.repository.BatchRepository;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.SourceChunkRepository;
import com.atlas.metadata.repository.WikiGenerationRunRepository;
import com.atlas.metadata.repository.WikiLogEntryRepository;
import com.atlas.metadata.repository.WikiPageIssueRepository;
import com.atlas.metadata.repository.WikiPageRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for deterministic Auto Wiki ingest v0. */
@Service
public class WikiIngestService {

  private final SpaceService spaceService;
  private final BatchRepository batchRepository;
  private final FileItemRepository fileItemRepository;
  private final SourceChunkRepository sourceChunkRepository;
  private final WikiPageRepository wikiPageRepository;
  private final WikiGenerationRunRepository wikiGenerationRunRepository;
  private final WikiLogEntryRepository wikiLogEntryRepository;
  private final WikiPageIssueRepository wikiPageIssueRepository;
  private final LocalArtifactStorageService artifactStorage;
  private final Clock clock;

  /** Creates the service. */
  @Autowired
  public WikiIngestService(
      SpaceService spaceService,
      BatchRepository batchRepository,
      FileItemRepository fileItemRepository,
      SourceChunkRepository sourceChunkRepository,
      WikiPageRepository wikiPageRepository,
      WikiGenerationRunRepository wikiGenerationRunRepository,
      WikiLogEntryRepository wikiLogEntryRepository,
      WikiPageIssueRepository wikiPageIssueRepository,
      LocalArtifactStorageService artifactStorage) {
    this(
        spaceService,
        batchRepository,
        fileItemRepository,
        sourceChunkRepository,
        wikiPageRepository,
        wikiGenerationRunRepository,
        wikiLogEntryRepository,
        wikiPageIssueRepository,
        artifactStorage,
        Clock.systemUTC());
  }

  WikiIngestService(
      SpaceService spaceService,
      BatchRepository batchRepository,
      FileItemRepository fileItemRepository,
      SourceChunkRepository sourceChunkRepository,
      WikiPageRepository wikiPageRepository,
      WikiGenerationRunRepository wikiGenerationRunRepository,
      WikiLogEntryRepository wikiLogEntryRepository,
      WikiPageIssueRepository wikiPageIssueRepository,
      LocalArtifactStorageService artifactStorage,
      Clock clock) {
    this.spaceService = spaceService;
    this.batchRepository = batchRepository;
    this.fileItemRepository = fileItemRepository;
    this.sourceChunkRepository = sourceChunkRepository;
    this.wikiPageRepository = wikiPageRepository;
    this.wikiGenerationRunRepository = wikiGenerationRunRepository;
    this.wikiLogEntryRepository = wikiLogEntryRepository;
    this.wikiPageIssueRepository = wikiPageIssueRepository;
    this.artifactStorage = artifactStorage;
    this.clock = clock;
  }

  /** Starts one deterministic Auto Wiki ingest run. */
  @Transactional
  public WikiIngestRunResponse startIngestRun(String spaceId, CreateWikiIngestRunRequest request) {
    spaceService.findSpace(spaceId);
    IngestOptions options = options(request);
    OffsetDateTime startedAt = OffsetDateTime.now(clock);
    String runId = "wiki-ingest-run-" + UUID.randomUUID();
    List<WikiLogEntry> logs = new ArrayList<>();
    logs.add(
        logEntry(
            logId(runId, "started"),
            spaceId,
            null,
            runId,
            "RUN_STARTED",
            options.requestedBy(),
            "Started deterministic Auto Wiki ingest v0.",
            Map.of("mode", options.mode(), "dryRun", options.dryRun()),
            startedAt));

    Selection selection = selectChunks(spaceId, options.sourceFileIds());
    List<String> createdPageIds = new ArrayList<>();
    List<String> updatedPageIds = new ArrayList<>();
    List<String> issueIds = new ArrayList<>();

    for (Candidate candidate : buildCandidates(spaceId, selection.eligibleChunks(), selection.fileById())) {
      wikiPageRepository
          .findBySpaceIdAndSlug(spaceId, candidate.slug())
          .ifPresentOrElse(
              existing ->
                  mergeOrIssue(
                      runId,
                      options,
                      candidate,
                      existing,
                      updatedPageIds,
                      issueIds,
                      logs,
                      startedAt),
              () -> createCandidate(runId, options, candidate, createdPageIds, startedAt));
    }

    OffsetDateTime finishedAt = OffsetDateTime.now(clock);
    String status = issueIds.isEmpty() ? "SUCCEEDED" : "PARTIAL_FAILED";
    String summary =
        summary(status, options.dryRun(), createdPageIds.size(), updatedPageIds.size(), issueIds.size());
    WikiGenerationRun run =
        WikiGenerationRun.ingest(
            runId,
            spaceId,
            status,
            options.mode(),
            options.requestedBy(),
            inputRefs(selection.eligibleChunks()),
            createdPageIds.toArray(String[]::new),
            updatedPageIds.toArray(String[]::new),
            issueIds.toArray(String[]::new),
            selection.eligibleChunks().size(),
            selection.excludedChunkCount(),
            summary,
            null,
            startedAt,
            finishedAt);
    WikiGenerationRun savedRun = wikiGenerationRunRepository.save(run);
    logs.add(
        logEntry(
            logId(runId, "finished"),
            spaceId,
            firstPage(createdPageIds, updatedPageIds),
            runId,
            "RUN_FINISHED",
            options.requestedBy(),
            summary,
            Map.of(
                "createdPageCount",
                createdPageIds.size(),
                "updatedPageCount",
                updatedPageIds.size(),
                "issueCount",
                issueIds.size()),
            finishedAt));
    logs.forEach(wikiLogEntryRepository::save);
    return toIngestResponse(savedRun);
  }

  /** Reads one safe ingest run summary. */
  @Transactional(readOnly = true)
  public WikiIngestRunResponse getIngestRun(String spaceId, String runId) {
    spaceService.findSpace(spaceId);
    WikiGenerationRun run =
        wikiGenerationRunRepository
            .findById(runId)
            .orElseThrow(() -> new NotFoundException("Wiki generation run not found."));
    if (!spaceId.equals(run.getSpaceId())) {
      throw new NotFoundException("Wiki generation run not found.");
    }
    return toIngestResponse(run);
  }

  private void mergeOrIssue(
      String runId,
      IngestOptions options,
      Candidate candidate,
      WikiPage existing,
      List<String> updatedPageIds,
      List<String> issueIds,
      List<WikiLogEntry> logs,
      OffsetDateTime timestamp) {
    if (isTrustedPublished(existing)) {
      WikiPageIssue issue =
          WikiPageIssue.open(
              "wiki-issue-" + runId + "-" + candidate.slug(),
              existing.getSpaceId(),
              existing.getId(),
              "REVIEW_REQUIRED",
              "MEDIUM",
              candidate.evidenceRefs(),
              "Generated candidate slug collision with trusted published Wiki page.",
              timestamp);
      wikiPageIssueRepository.save(issue);
      issueIds.add(issue.getId());
      logs.add(
          logEntry(
              logId(runId, "issue-" + candidate.slug()),
              existing.getSpaceId(),
              existing.getId(),
              runId,
              "ISSUE_RECORDED",
              options.requestedBy(),
              "Recorded safe generated candidate slug collision.",
              Map.of("slug", candidate.slug(), "issueType", "REVIEW_REQUIRED"),
              timestamp));
      return;
    }
    if (!options.dryRun()) {
      String markdownPath = writeMarkdown(existing.getSpaceId(), candidate);
      existing.mergeGeneratedCandidate(
          candidate.title(),
          markdownPath,
          candidate.sourceDocumentIds(),
          candidate.chunks(),
          candidate.confidence(),
          options.requestedBy(),
          timestamp);
      wikiPageRepository.save(existing);
    }
    updatedPageIds.add(existing.getId());
  }

  private void createCandidate(
      String runId,
      IngestOptions options,
      Candidate candidate,
      List<String> createdPageIds,
      OffsetDateTime timestamp) {
    String pageId = "wiki-auto-" + candidate.slug();
    if (!options.dryRun()) {
      String markdownPath = writeMarkdown(candidate.spaceId(), candidate);
      WikiPage page =
          WikiPage.generatedCandidate(
              pageId,
              candidate.spaceId(),
              candidate.title(),
              candidate.slug(),
              markdownPath,
              candidate.sourceDocumentIds(),
              candidate.chunks(),
              candidate.confidence(),
              options.requestedBy(),
              timestamp);
      wikiPageRepository.save(page);
    }
    createdPageIds.add(pageId);
  }

  private Selection selectChunks(String spaceId, List<String> sourceFileIds) {
    List<String> batchIds = batchRepository.findBySpaceId(spaceId).stream().map(Batch::getId).toList();
    List<FileItem> files = batchIds.isEmpty() ? List.of() : fileItemRepository.findByBatchIdIn(batchIds);
    Map<String, FileItem> fileById =
        files.stream().collect(Collectors.toUnmodifiableMap(FileItem::getId, Function.identity()));
    validateSourceFileFilter(sourceFileIds, fileById.keySet());
    List<String> selectedFileIds =
        sourceFileIds.isEmpty()
            ? files.stream().map(FileItem::getId).toList()
            : sourceFileIds;
    List<SourceChunk> chunks =
        selectedFileIds.isEmpty() ? List.of() : sourceChunkRepository.findByFileItemIdIn(selectedFileIds);
    List<SourceChunk> eligible =
        chunks.stream()
            .filter(chunk -> isEligible(fileById.get(chunk.getFileItemId()), chunk))
            .sorted(Comparator.comparing(SourceChunk::getFileItemId).thenComparing(SourceChunk::getId))
            .toList();
    return new Selection(fileById, eligible, chunks.size() - eligible.size());
  }

  private List<Candidate> buildCandidates(
      String spaceId, List<SourceChunk> chunks, Map<String, FileItem> fileById) {
    Map<String, List<SourceChunk>> byFile = new LinkedHashMap<>();
    for (SourceChunk chunk : chunks) {
      byFile.computeIfAbsent(chunk.getFileItemId(), ignored -> new ArrayList<>()).add(chunk);
    }
    return byFile.entrySet().stream()
        .map(entry -> candidate(spaceId, fileById.get(entry.getKey()), entry.getValue()))
        .toList();
  }

  private Candidate candidate(String spaceId, FileItem file, List<SourceChunk> chunks) {
    String title = titleFrom(file);
    String slug = slugFrom(title);
    BigDecimal confidence =
        chunks.stream()
            .map(SourceChunk::getConfidence)
            .min(BigDecimal::compareTo)
            .orElse(file.getConfidence());
    String[] sourceDocumentIds = new String[] {file.getId()};
    List<WikiReference> evidenceRefs = inputRefs(chunks);
    return new Candidate(spaceId, title, slug, sourceDocumentIds, List.copyOf(chunks), confidence, evidenceRefs);
  }

  private boolean isEligible(FileItem file, SourceChunk chunk) {
    return file != null
        && file.getReviewStatus() == ReviewStatus.APPROVED
        && chunk.getReviewStatus() == ReviewStatus.APPROVED
        && file.getStatus() != FileStatus.OCR_REQUIRED
        && file.getStatus() != FileStatus.PDF_CONVERT_FAILED
        && file.getStatus() != FileStatus.FAILED
        && file.getStatus() != FileStatus.UNSUPPORTED
        && chunk.getSourceFile() != null
        && !chunk.getSourceFile().isBlank()
        && (chunk.getPage() != null || (chunk.getSection() != null && !chunk.getSection().isBlank()))
        && chunk.getConfidence() != null;
  }

  private boolean isTrustedPublished(WikiPage page) {
    return page.getReviewStatus() == ReviewStatus.PUBLISHED || "PUBLISHED_FILE".equals(page.getSourceMode());
  }

  private String writeMarkdown(String spaceId, Candidate candidate) {
    return artifactStorage.writeGeneratedMarkdown(
        "generated/wiki/" + spaceId, candidate.slug(), markdown(candidate));
  }

  private String markdown(Candidate candidate) {
    String chunkLines =
        candidate.chunks().stream()
            .map(chunk -> "- SOURCE_CHUNK " + chunk.getId() + " (" + locator(chunk) + ")")
            .collect(Collectors.joining("\n"));
    return "# "
        + candidate.title()
        + "\n\n"
        + "Generated by Atlas deterministic wiki-ingest-v0.\n\n"
        + "Review status: REVIEW_REQUIRED\n"
        + "Source mode: AUTO_GENERATED\n"
        + "Refresh policy: ON_SOURCE_CHANGE\n\n"
        + "## Safe Summary\n\n"
        + "Deterministic candidate generated from approved source chunk references.\n\n"
        + "## Source References\n\n"
        + "- FILE "
        + candidate.sourceDocumentIds()[0]
        + "\n\n"
        + "## Chunk References\n\n"
        + chunkLines
        + "\n";
  }

  private List<WikiReference> inputRefs(List<SourceChunk> chunks) {
    return chunks.stream()
        .map(chunk -> new WikiReference("SOURCE_CHUNK", chunk.getId(), "source chunk", locator(chunk)))
        .toList();
  }

  private String locator(SourceChunk chunk) {
    if (chunk.getPage() != null) {
      return "page " + chunk.getPage();
    }
    return chunk.getSection() == null || chunk.getSection().isBlank() ? "section n/a" : chunk.getSection();
  }

  private void validateSourceFileFilter(List<String> sourceFileIds, Set<String> spaceFileIds) {
    List<String> invalid =
        sourceFileIds.stream().filter(fileId -> !spaceFileIds.contains(fileId)).toList();
    if (!invalid.isEmpty()) {
      throw new RequestValidationException(Map.of("sourceFileIds", "must belong to the selected space"));
    }
  }

  private IngestOptions options(CreateWikiIngestRunRequest request) {
    String mode = request == null || request.mode() == null || request.mode().isBlank() ? "deterministic" : request.mode();
    if (!"deterministic".equals(mode)) {
      throw new RequestValidationException(Map.of("mode", "model-assisted is out of scope for wiki-ingest-v0"));
    }
    List<String> sourceFileIds =
        request == null || request.sourceFileIds() == null
            ? List.of()
            : request.sourceFileIds().stream().filter(id -> id != null && !id.isBlank()).distinct().toList();
    String requestedBy = request == null ? null : request.requestedBy();
    boolean dryRun = request != null && Boolean.TRUE.equals(request.dryRun());
    return new IngestOptions(mode, sourceFileIds, safeActor(requestedBy), dryRun);
  }

  private String safeActor(String value) {
    String safe =
        (value == null ? "" : value.trim())
            .replace('\\', '-')
            .replace('/', '-')
            .replaceAll("[^A-Za-z0-9 ._@-]", "-");
    if (safe.isBlank()) {
      return "system";
    }
    return safe.length() > 80 ? safe.substring(0, 80) : safe;
  }

  private String titleFrom(FileItem file) {
    String path = file.getSourcePath() == null || file.getSourcePath().isBlank() ? file.getId() : file.getSourcePath();
    String leaf = path.replace('\\', '/');
    int slash = leaf.lastIndexOf('/');
    if (slash >= 0) {
      leaf = leaf.substring(slash + 1);
    }
    int dot = leaf.lastIndexOf('.');
    if (dot > 0) {
      leaf = leaf.substring(0, dot);
    }
    String title = leaf.replaceAll("[_-]+", " ").trim();
    return title.isBlank() ? file.getId() : title;
  }

  private String slugFrom(String title) {
    String slug =
        title
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-|-$", "");
    return slug.isBlank() ? "wiki-candidate" : slug;
  }

  private String summary(
      String status, boolean dryRun, int createdCount, int updatedCount, int issueCount) {
    String action = dryRun ? "Previewed" : "Created or updated";
    return action
        + " "
        + (createdCount + updatedCount)
        + " review-required Wiki candidate(s); "
        + issueCount
        + " safe issue(s); status "
        + status
        + ".";
  }

  private WikiLogEntry logEntry(
      String id,
      String spaceId,
      String pageId,
      String runId,
      String eventType,
      String actor,
      String message,
      Map<String, Object> metadata,
      OffsetDateTime createdAt) {
    return WikiLogEntry.create(id, spaceId, pageId, runId, eventType, actor, message, metadata, createdAt);
  }

  private String logId(String runId, String suffix) {
    return "wiki-log-" + runId + "-" + suffix;
  }

  private String firstPage(List<String> createdPageIds, List<String> updatedPageIds) {
    if (!createdPageIds.isEmpty()) {
      return createdPageIds.getFirst();
    }
    return updatedPageIds.isEmpty() ? null : updatedPageIds.getFirst();
  }

  private WikiIngestRunResponse toIngestResponse(WikiGenerationRun run) {
    return new WikiIngestRunResponse(
        run.getId(),
        run.getSpaceId(),
        run.getStatus(),
        run.getMode(),
        List.of(run.getCreatedPageIds()),
        List.of(run.getUpdatedPageIds()),
        List.of(run.getIssueIds()),
        run.getEligibleChunkCount(),
        run.getExcludedChunkCount(),
        run.getSafeSummary(),
        run.getSafeError(),
        run.getStartedAt(),
        run.getFinishedAt());
  }

  private record IngestOptions(
      String mode, List<String> sourceFileIds, String requestedBy, boolean dryRun) {}

  private record Selection(
      Map<String, FileItem> fileById, List<SourceChunk> eligibleChunks, int excludedChunkCount) {}

  private record Candidate(
      String spaceId,
      String title,
      String slug,
      String[] sourceDocumentIds,
      List<SourceChunk> chunks,
      BigDecimal confidence,
      List<WikiReference> evidenceRefs) {}
}
