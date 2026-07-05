package com.atlas.metadata.service;

import com.atlas.metadata.domain.WikiGenerationRun;
import com.atlas.metadata.domain.WikiLogEntry;
import com.atlas.metadata.domain.WikiPage;
import com.atlas.metadata.domain.WikiPageIssue;
import com.atlas.metadata.domain.WikiReference;
import com.atlas.metadata.dto.CreateWikiLinkifyLintRunRequest;
import com.atlas.metadata.dto.WikiLinkifyLintRunResponse;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.SourceChunkRepository;
import com.atlas.metadata.repository.WikiGenerationRunRepository;
import com.atlas.metadata.repository.WikiLogEntryRepository;
import com.atlas.metadata.repository.WikiPageIssueRepository;
import com.atlas.metadata.repository.WikiPageRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

/** Application service for deterministic Wiki linkify/lint maintenance runs. */
@Service
public class WikiLinkifyLintService {

  private static final int THIN_CONTENT_MIN_CHARS = 40;

  private final SpaceService spaceService;
  private final WikiPageRepository wikiPageRepository;
  private final WikiGenerationRunRepository wikiGenerationRunRepository;
  private final WikiLogEntryRepository wikiLogEntryRepository;
  private final WikiPageIssueRepository wikiPageIssueRepository;
  private final FileItemRepository fileItemRepository;
  private final SourceChunkRepository sourceChunkRepository;
  private final LocalArtifactStorageService artifactStorage;
  private final Clock clock;

  /** Creates the service. */
  @Autowired
  public WikiLinkifyLintService(
      SpaceService spaceService,
      WikiPageRepository wikiPageRepository,
      WikiGenerationRunRepository wikiGenerationRunRepository,
      WikiLogEntryRepository wikiLogEntryRepository,
      WikiPageIssueRepository wikiPageIssueRepository,
      FileItemRepository fileItemRepository,
      SourceChunkRepository sourceChunkRepository,
      LocalArtifactStorageService artifactStorage) {
    this(
        spaceService,
        wikiPageRepository,
        wikiGenerationRunRepository,
        wikiLogEntryRepository,
        wikiPageIssueRepository,
        fileItemRepository,
        sourceChunkRepository,
        artifactStorage,
        Clock.systemUTC());
  }

  WikiLinkifyLintService(
      SpaceService spaceService,
      WikiPageRepository wikiPageRepository,
      WikiGenerationRunRepository wikiGenerationRunRepository,
      WikiLogEntryRepository wikiLogEntryRepository,
      WikiPageIssueRepository wikiPageIssueRepository,
      FileItemRepository fileItemRepository,
      SourceChunkRepository sourceChunkRepository,
      LocalArtifactStorageService artifactStorage,
      Clock clock) {
    this.spaceService = spaceService;
    this.wikiPageRepository = wikiPageRepository;
    this.wikiGenerationRunRepository = wikiGenerationRunRepository;
    this.wikiLogEntryRepository = wikiLogEntryRepository;
    this.wikiPageIssueRepository = wikiPageIssueRepository;
    this.fileItemRepository = fileItemRepository;
    this.sourceChunkRepository = sourceChunkRepository;
    this.artifactStorage = artifactStorage;
    this.clock = clock;
  }

  /** Starts one deterministic Wiki linkify/lint run. */
  @Transactional
  public WikiLinkifyLintRunResponse startLinkifyLintRun(
      String spaceId, CreateWikiLinkifyLintRunRequest request) {
    spaceService.findSpace(spaceId);
    Options options = options(request);
    OffsetDateTime startedAt = OffsetDateTime.now(clock);
    String runId = "wiki-linkify-lint-run-" + UUID.randomUUID();
    List<WikiPage> allPages = wikiPageRepository.findBySpaceIdOrderByTitleAsc(spaceId);
    List<WikiPage> scopedPages = scopedPages(allPages, options.pageIds());
    Map<String, WikiPage> pageBySlug =
        allPages.stream()
            .collect(Collectors.toMap(WikiPage::getSlug, Function.identity(), (first, ignored) -> first));
    TargetIndex targetIndex = targetIndex(allPages);
    RunAccumulator accumulator = new RunAccumulator();
    accumulator.options = options;

    Map<String, List<String>> outLinksBySlug = new LinkedHashMap<>();
    for (WikiPage page : scopedPages) {
      String markdown = readMarkdown(page, runId, options, accumulator, startedAt);
      if (markdown == null) {
        outLinksBySlug.put(page.getSlug(), List.of(page.getOutLinks()));
        continue;
      }
      WikiMarkdownLinkifier.Result linkResult =
          options.linkify()
              ? WikiMarkdownLinkifier.linkify(markdown, page.getSlug(), targetIndex.targets())
              : new WikiMarkdownLinkifier.Result(markdown, List.of(), List.of(page.getOutLinks()));
      recordAmbiguousAliases(page, markdown, targetIndex.ambiguousTerms(), options, accumulator, runId, startedAt);
      accumulator.insertedLinkCount += linkResult.insertedSlugs().size();
      outLinksBySlug.put(page.getSlug(), linkResult.allOutboundSlugs());
      boolean artifactChanged = !markdown.equals(linkResult.markdown());
      if (artifactChanged && !options.dryRun()) {
        artifactStorage.writeText(page.getMarkdownPath(), linkResult.markdown());
      }
      accumulator.artifactChangedBySlug.put(page.getSlug(), artifactChanged);
      accumulator.markdownBySlug.put(page.getSlug(), linkResult.markdown());
    }

    Map<String, List<String>> incomingBySlug = incomingLinks(outLinksBySlug, pageBySlug.keySet());
    for (WikiPage page : scopedPages) {
      List<String> outLinks = sortedUnique(outLinksBySlug.getOrDefault(page.getSlug(), List.of(page.getOutLinks())));
      List<String> inLinks = sortedUnique(incomingBySlug.getOrDefault(page.getSlug(), List.of()));
      boolean artifactChanged = accumulator.artifactChangedBySlug.getOrDefault(page.getSlug(), false);
      boolean metadataChanged =
          !sortedUnique(List.of(page.getInLinks())).equals(inLinks)
              || !sortedUnique(List.of(page.getOutLinks())).equals(outLinks);
      if (artifactChanged || metadataChanged) {
        accumulator.updatedPageIds.add(page.getId());
      }
      if ((artifactChanged || metadataChanged) && !options.dryRun()) {
        page.applyLinkMetadata(
            inLinks.toArray(String[]::new),
            outLinks.toArray(String[]::new),
            artifactChanged,
            startedAt);
        wikiPageRepository.save(page);
      }
    }

    if (options.lint()) {
      lint(scopedPages, outLinksBySlug, incomingBySlug, pageBySlug, accumulator, runId, startedAt);
    }

    OffsetDateTime finishedAt = OffsetDateTime.now(clock);
    String status = accumulator.issueIds.isEmpty() ? "SUCCEEDED" : "PARTIAL_FAILED";
    String safeSummary =
        "Scanned "
            + scopedPages.size()
            + " Wiki pages, inserted "
            + accumulator.insertedLinkCount
            + " links, and recorded "
            + accumulator.issueIds.size()
            + " issue(s).";
    WikiGenerationRun run =
        WikiGenerationRun.maintenance(
            runId,
            spaceId,
            status,
            "linkify-lint",
            options.requestedBy(),
            accumulator.updatedPageIds.toArray(String[]::new),
            accumulator.issueIds.toArray(String[]::new),
            safeSummary,
            null,
            startedAt,
            finishedAt);
    WikiGenerationRun savedRun = wikiGenerationRunRepository.save(run);
    if (!options.dryRun()) {
      wikiLogEntryRepository.save(
          logEntry(
              "wiki-log-" + runId + "-started",
              spaceId,
              null,
              runId,
              "RUN_STARTED",
              options.requestedBy(),
              "Started deterministic Wiki linkify/lint run.",
              Map.of("mode", "linkify-lint"),
              startedAt));
      wikiLogEntryRepository.save(
          logEntry(
              "wiki-log-" + runId + "-finished",
              spaceId,
              firstPageId(accumulator.updatedPageIds),
              runId,
              "RUN_FINISHED",
              options.requestedBy(),
              safeSummary,
              Map.of(
                  "updatedPageCount",
                  accumulator.updatedPageIds.size(),
                  "issueCount",
                  accumulator.issueIds.size(),
                  "insertedLinkCount",
                  accumulator.insertedLinkCount),
              finishedAt));
    }
    return response(savedRun, scopedPages.size(), accumulator);
  }

  private String readMarkdown(
      WikiPage page,
      String runId,
      Options options,
      RunAccumulator accumulator,
      OffsetDateTime timestamp) {
    try {
      return artifactStorage.readText(page.getMarkdownPath());
    } catch (RuntimeException ex) {
      recordIssue(
          page,
          "REVIEW_REQUIRED",
          "MEDIUM",
          "Markdown artifact could not be read safely.",
          "artifact",
          options,
          accumulator,
          runId,
          timestamp);
      return null;
    }
  }

  private void lint(
      List<WikiPage> pages,
      Map<String, List<String>> outLinksBySlug,
      Map<String, List<String>> incomingBySlug,
      Map<String, WikiPage> pageBySlug,
      RunAccumulator accumulator,
      String runId,
      OffsetDateTime timestamp) {
    for (WikiPage page : pages) {
      for (String outLink : outLinksBySlug.getOrDefault(page.getSlug(), List.of())) {
        if (!pageBySlug.containsKey(outLink)) {
          accumulator.brokenLinkCount++;
          recordIssue(
              page,
              "BROKEN_LINK",
              "MEDIUM",
              "Wiki link target does not exist in this Knowledge Space.",
              outLink,
              accumulator.options,
              accumulator,
              runId,
              timestamp);
        }
      }
      if (!isOrphanExempt(page) && incomingBySlug.getOrDefault(page.getSlug(), List.of()).isEmpty()) {
        accumulator.orphanPageCount++;
        recordIssue(
            page,
            "ORPHAN_PAGE",
            "LOW",
            "Wiki page has no incoming links.",
            "orphan",
            accumulator.options,
            accumulator,
            runId,
            timestamp);
      }
      if (page.getSourceRefs().isEmpty() && page.getChunkRefs().isEmpty()) {
        accumulator.sourceIssueCount++;
        recordIssue(
            page,
            "MISSING_SOURCE_REF",
            "HIGH",
            "Wiki page has no source or chunk references.",
            "missing-source",
            accumulator.options,
            accumulator,
            runId,
            timestamp);
      } else if (hasStaleSource(page)) {
        accumulator.sourceIssueCount++;
        recordIssue(
            page,
            "STALE_SOURCE",
            "HIGH",
            "Wiki page references source metadata that no longer resolves.",
            "stale-source",
            accumulator.options,
            accumulator,
            runId,
            timestamp);
      }
      String markdown = accumulator.markdownBySlug.get(page.getSlug());
      if (markdown != null && visibleText(markdown).length() < THIN_CONTENT_MIN_CHARS) {
        accumulator.thinContentCount++;
        recordIssue(
            page,
            "THIN_CONTENT",
            "LOW",
            "Wiki page content is too thin for trusted use.",
            "thin-content",
            accumulator.options,
            accumulator,
            runId,
            timestamp);
      }
    }
  }

  private void recordIssue(
      WikiPage page,
      String issueType,
      String severity,
      String message,
      String token,
      Options options,
      RunAccumulator accumulator,
      String runId,
      OffsetDateTime timestamp) {
    String issueId = issueId(page, issueType, token);
    accumulator.issueIds.add(issueId);
    if (!options.dryRun()) {
      wikiPageIssueRepository.save(
          WikiPageIssue.open(
              issueId,
              page.getSpaceId(),
              page.getId(),
              issueType,
              severity,
              List.of(new WikiReference("WIKI_PAGE", page.getId(), page.getSlug(), null)),
              message,
              timestamp));
      wikiLogEntryRepository.save(
          logEntry(
              "wiki-log-" + runId + "-issue-" + issueId,
              page.getSpaceId(),
              page.getId(),
              runId,
              "ISSUE_RECORDED",
              options.requestedBy(),
              "Recorded safe Wiki lint issue.",
              Map.of("issueType", issueType),
              timestamp));
    }
  }

  private List<WikiPage> scopedPages(List<WikiPage> allPages, List<String> pageIds) {
    if (pageIds.isEmpty()) {
      return allPages;
    }
    Set<String> availableIds = allPages.stream().map(WikiPage::getId).collect(Collectors.toSet());
    List<String> invalid = pageIds.stream().filter(pageId -> !availableIds.contains(pageId)).toList();
    if (!invalid.isEmpty()) {
      throw new RequestValidationException(Map.of("pageIds", "must belong to the selected space"));
    }
    Set<String> requested = new LinkedHashSet<>(pageIds);
    return allPages.stream().filter(page -> requested.contains(page.getId())).toList();
  }

  private TargetIndex targetIndex(List<WikiPage> pages) {
    Map<String, Set<String>> slugsByTerm = new LinkedHashMap<>();
    for (WikiPage page : pages) {
      for (String term : candidateTerms(page)) {
        String normalized = normalizeTerm(term);
        if (!normalized.isBlank()) {
          slugsByTerm.computeIfAbsent(normalized, ignored -> new LinkedHashSet<>()).add(page.getSlug());
        }
      }
    }
    Set<String> ambiguousTerms =
        slugsByTerm.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .map(Map.Entry::getKey)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    List<WikiMarkdownLinkifier.Target> targets =
        pages.stream()
            .sorted(Comparator.comparing(WikiPage::getSlug))
            .map(
                page ->
                    new WikiMarkdownLinkifier.Target(
                        page.getSlug(),
                        candidateTerms(page).stream()
                            .filter(term -> !ambiguousTerms.contains(normalizeTerm(term)))
                            .toList()))
            .toList();
    return new TargetIndex(targets, ambiguousTerms);
  }

  private List<String> candidateTerms(WikiPage page) {
    List<String> terms = new ArrayList<>();
    terms.add(page.getSlug().replace('-', ' '));
    terms.addAll(List.of(page.getAliases()));
    return terms;
  }

  private void recordAmbiguousAliases(
      WikiPage page,
      String markdown,
      Set<String> ambiguousTerms,
      Options options,
      RunAccumulator accumulator,
      String runId,
      OffsetDateTime timestamp) {
    String normalizedBody = normalizeTerm(visibleText(markdown));
    for (String term : ambiguousTerms) {
      if (containsTerm(normalizedBody, term)) {
        recordIssue(
            page,
            "REVIEW_REQUIRED",
            "MEDIUM",
            "Wiki alias is ambiguous and was not auto-linked.",
            "ambiguous-alias-" + term,
            options,
            accumulator,
            runId,
            timestamp);
      }
    }
  }

  private Map<String, List<String>> incomingLinks(
      Map<String, List<String>> outLinksBySlug, Set<String> targetSlugs) {
    Map<String, Set<String>> incoming = new LinkedHashMap<>();
    for (Map.Entry<String, List<String>> entry : outLinksBySlug.entrySet()) {
      for (String targetSlug : entry.getValue()) {
        if (targetSlugs.contains(targetSlug)) {
          incoming.computeIfAbsent(targetSlug, ignored -> new LinkedHashSet<>()).add(entry.getKey());
        }
      }
    }
    return incoming.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> sortedUnique(entry.getValue()), (a, b) -> a, LinkedHashMap::new));
  }

  private boolean hasStaleSource(WikiPage page) {
    for (WikiReference ref : page.getSourceRefs()) {
      if ("FILE".equals(ref.type()) && !fileItemRepository.existsById(ref.id())) {
        return true;
      }
    }
    for (WikiReference ref : page.getChunkRefs()) {
      if ("SOURCE_CHUNK".equals(ref.type()) && !sourceChunkRepository.existsById(ref.id())) {
        return true;
      }
    }
    return false;
  }

  private boolean isOrphanExempt(WikiPage page) {
    return "INDEX".equals(page.getPageType()) || "index".equals(page.getSlug()) || "root".equals(page.getSlug());
  }

  private String visibleText(String markdown) {
    return markdown
        .replaceAll("(?s)```.*?```", " ")
        .replaceAll("`[^`]*`", " ")
        .replaceAll("!?\\[[^\\]]*]\\([^)]*\\)", " ")
        .replaceAll("\\[\\[[^\\]]+]]", " ")
        .replaceAll("[#>*_`\\-]+", " ")
        .replaceAll("\\s+", " ")
        .trim();
  }

  private Options options(CreateWikiLinkifyLintRunRequest request) {
    List<String> pageIds =
        request == null || request.pageIds() == null
            ? List.of()
            : request.pageIds().stream().filter(id -> id != null && !id.isBlank()).distinct().toList();
    boolean linkify = request == null || request.linkify() == null || Boolean.TRUE.equals(request.linkify());
    boolean lint = request == null || request.lint() == null || Boolean.TRUE.equals(request.lint());
    if (!linkify && !lint) {
      throw new RequestValidationException(Map.of("linkify", "linkify or lint must be enabled"));
    }
    String requestedBy = request == null ? null : request.requestedBy();
    boolean dryRun = request != null && Boolean.TRUE.equals(request.dryRun());
    Options options = new Options(pageIds, safeActor(requestedBy), dryRun, linkify, lint);
    return options;
  }

  private WikiLinkifyLintRunResponse response(
      WikiGenerationRun run, int scannedPageCount, RunAccumulator accumulator) {
    return new WikiLinkifyLintRunResponse(
        run.getId(),
        run.getSpaceId(),
        run.getStatus(),
        run.getMode(),
        scannedPageCount,
        List.of(run.getUpdatedPageIds()),
        List.of(run.getIssueIds()),
        accumulator.insertedLinkCount,
        accumulator.brokenLinkCount,
        accumulator.orphanPageCount,
        accumulator.sourceIssueCount,
        accumulator.thinContentCount,
        run.getSafeSummary(),
        run.getSafeError(),
        run.getStartedAt(),
        run.getFinishedAt());
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

  private String issueId(WikiPage page, String issueType, String token) {
    String safeToken =
        (token == null ? "" : token)
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-|-$", "");
    if (safeToken.isBlank()) {
      safeToken = "issue";
    }
    return "wiki-issue-" + page.getId() + "-" + issueType.toLowerCase(Locale.ROOT).replace('_', '-') + "-" + safeToken;
  }

  private String firstPageId(List<String> pageIds) {
    return pageIds.isEmpty() ? null : pageIds.getFirst();
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

  private String normalizeTerm(String value) {
    return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
  }

  private boolean containsTerm(String normalizedBody, String normalizedTerm) {
    return normalizedBody.equals(normalizedTerm)
        || normalizedBody.startsWith(normalizedTerm + " ")
        || normalizedBody.endsWith(" " + normalizedTerm)
        || normalizedBody.contains(" " + normalizedTerm + " ");
  }

  private List<String> sortedUnique(Iterable<String> values) {
    List<String> output = new ArrayList<>();
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        output.add(WikiMarkdownLinkifier.normalizeSlug(value));
      }
    }
    return output.stream().filter(value -> !value.isBlank()).distinct().sorted().toList();
  }

  private record Options(
      List<String> pageIds, String requestedBy, boolean dryRun, boolean linkify, boolean lint) {}

  private record TargetIndex(List<WikiMarkdownLinkifier.Target> targets, Set<String> ambiguousTerms) {}

  private static final class RunAccumulator {
    private final List<String> updatedPageIds = new ArrayList<>();
    private final List<String> issueIds = new ArrayList<>();
    private final Map<String, Boolean> artifactChangedBySlug = new LinkedHashMap<>();
    private final Map<String, String> markdownBySlug = new LinkedHashMap<>();
    private int insertedLinkCount;
    private int brokenLinkCount;
    private int orphanPageCount;
    private int sourceIssueCount;
    private int thinContentCount;
    private Options options;
  }
}
