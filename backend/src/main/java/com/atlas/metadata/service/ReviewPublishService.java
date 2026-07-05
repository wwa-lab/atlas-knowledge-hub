package com.atlas.metadata.service;

import com.atlas.metadata.domain.Batch;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.domain.WikiPage;
import com.atlas.metadata.dto.CreateWikiPublishRequest;
import com.atlas.metadata.dto.ReviewQueueItemResponse;
import com.atlas.metadata.dto.ReviewQueueItemResponse.ReviewQueueType;
import com.atlas.metadata.dto.ReviewQueueRepresentativeResponse;
import com.atlas.metadata.dto.ReviewQueuesResponse;
import com.atlas.metadata.dto.WikiFolderResponse;
import com.atlas.metadata.dto.WikiGenerationRunResponse;
import com.atlas.metadata.dto.WikiLogEntryResponse;
import com.atlas.metadata.dto.WikiPageResponse;
import com.atlas.metadata.dto.WikiPageIssueResponse;
import com.atlas.metadata.dto.mapping.WikiPageMapper;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.exception.ConflictException;
import com.atlas.metadata.exception.NotFoundException;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.repository.BatchRepository;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.SourceChunkRepository;
import com.atlas.metadata.repository.WikiFolderRepository;
import com.atlas.metadata.repository.WikiGenerationRunRepository;
import com.atlas.metadata.repository.WikiLogEntryRepository;
import com.atlas.metadata.repository.WikiPageIssueRepository;
import com.atlas.metadata.repository.WikiPageRepository;
import com.atlas.metadata.validation.RelativePathValidator;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for review queues and Wiki publication metadata. */
@Service
public class ReviewPublishService {

  private static final RelativePathValidator RELATIVE_PATH_VALIDATOR = new RelativePathValidator();

  private final SpaceService spaceService;
  private final BatchRepository batchRepository;
  private final FileItemRepository fileItemRepository;
  private final SourceChunkRepository sourceChunkRepository;
  private final WikiPageRepository wikiPageRepository;
  private final WikiFolderRepository wikiFolderRepository;
  private final WikiGenerationRunRepository wikiGenerationRunRepository;
  private final WikiLogEntryRepository wikiLogEntryRepository;
  private final WikiPageIssueRepository wikiPageIssueRepository;
  private final Clock clock;

  /** Creates the service. */
  @Autowired
  public ReviewPublishService(
      SpaceService spaceService,
      BatchRepository batchRepository,
      FileItemRepository fileItemRepository,
      SourceChunkRepository sourceChunkRepository,
      WikiPageRepository wikiPageRepository,
      WikiFolderRepository wikiFolderRepository,
      WikiGenerationRunRepository wikiGenerationRunRepository,
      WikiLogEntryRepository wikiLogEntryRepository,
      WikiPageIssueRepository wikiPageIssueRepository) {
    this(
        spaceService,
        batchRepository,
        fileItemRepository,
        sourceChunkRepository,
        wikiPageRepository,
        wikiFolderRepository,
        wikiGenerationRunRepository,
        wikiLogEntryRepository,
        wikiPageIssueRepository,
        Clock.systemUTC());
  }

  ReviewPublishService(
      SpaceService spaceService,
      BatchRepository batchRepository,
      FileItemRepository fileItemRepository,
      SourceChunkRepository sourceChunkRepository,
      WikiPageRepository wikiPageRepository,
      WikiFolderRepository wikiFolderRepository,
      WikiGenerationRunRepository wikiGenerationRunRepository,
      WikiLogEntryRepository wikiLogEntryRepository,
      WikiPageIssueRepository wikiPageIssueRepository,
      Clock clock) {
    this.spaceService = spaceService;
    this.batchRepository = batchRepository;
    this.fileItemRepository = fileItemRepository;
    this.sourceChunkRepository = sourceChunkRepository;
    this.wikiPageRepository = wikiPageRepository;
    this.wikiFolderRepository = wikiFolderRepository;
    this.wikiGenerationRunRepository = wikiGenerationRunRepository;
    this.wikiLogEntryRepository = wikiLogEntryRepository;
    this.wikiPageIssueRepository = wikiPageIssueRepository;
    this.clock = clock;
  }

  /** Returns Processing Center queue counts for one Knowledge Space. */
  @Transactional(readOnly = true)
  public ReviewQueuesResponse getReviewQueues(String spaceId) {
    spaceService.findSpace(spaceId);
    List<Batch> batches = batchRepository.findBySpaceId(spaceId);
    List<String> batchIds = batches.stream().map(Batch::getId).toList();
    List<FileItem> files = batchIds.isEmpty() ? List.of() : fileItemRepository.findByBatchIdIn(batchIds);
    Set<String> tracedFileIds =
        files.isEmpty()
            ? Set.of()
            : new HashSet<>(
                sourceChunkRepository.findByFileItemIdIn(files.stream().map(FileItem::getId).toList()).stream()
                    .map(SourceChunk::getFileItemId)
                    .toList());

    Predicate<FileItem> missingTrace =
        file -> requiresTrace(file) && !tracedFileIds.contains(file.getId());
    Predicate<FileItem> publishReady = file -> isPublishEligible(file, tracedFileIds);

    List<ReviewQueueItemResponse> queues =
        List.of(
            queue(ReviewQueueType.PARSER_FAILURE, files, this::isParserFailure, tracedFileIds, true),
            queue(ReviewQueueType.OCR_REQUIRED, files, this::isOcrRequired, tracedFileIds, true),
            queue(ReviewQueueType.LOW_CONFIDENCE, files, this::isLowConfidence, tracedFileIds, true),
            queue(ReviewQueueType.MISSING_SOURCE_TRACE, files, missingTrace, tracedFileIds, true),
            queue(ReviewQueueType.LLM_GENERATED_REVIEW_REQUIRED, List.of(), file -> false, tracedFileIds, true),
            queue(ReviewQueueType.READY_TO_PUBLISH, files, publishReady, tracedFileIds, false));
    return new ReviewQueuesResponse(spaceId, queues);
  }

  /** Publishes an approved Markdown file to Wiki metadata. */
  @Transactional
  public WikiPageResponse publishFile(String fileId, CreateWikiPublishRequest request) {
    FileItem file = findFile(fileId);
    Batch batch = findBatch(file.getBatchId());
    validatePublishEligibility(file);
    List<SourceChunk> chunks = sourceChunkRepository.findByFileItemId(fileId);
    if (chunks.isEmpty()) {
      throw new ConflictException("Publish blocked: source trace is required.");
    }

    WikiPage page =
        wikiPageRepository
            .findBySourceDocumentIdsContaining(fileId)
            .map(
                existing -> {
                  existing.republish(
                      request.title(),
                      file.getMarkdownPath(),
                      new String[] {fileId},
                      chunks,
                      file.getConfidence(),
                      request.owner(),
                      OffsetDateTime.now(clock));
                  return existing;
                })
            .orElseGet(
                () ->
                    WikiPage.publish(
                        "wiki-" + fileId,
                        batch.getSpaceId(),
                        request.title(),
                        file.getMarkdownPath(),
                        new String[] {fileId},
                        chunks,
                        file.getConfidence(),
                        request.owner(),
                        OffsetDateTime.now(clock)));
    return WikiPageMapper.toResponse(wikiPageRepository.save(page));
  }

  /** Lists published Wiki pages for one Knowledge Space. */
  @Transactional(readOnly = true)
  public List<WikiPageResponse> listPublishedWikiPages(String spaceId) {
    return listWikiPages(spaceId, false);
  }

  /** Lists Wiki pages, optionally including generated review-required drafts. */
  @Transactional(readOnly = true)
  public List<WikiPageResponse> listWikiPages(String spaceId, boolean includeDrafts) {
    spaceService.findSpace(spaceId);
    List<WikiPage> pages =
        includeDrafts
            ? wikiPageRepository.findBySpaceIdOrderByTitleAsc(spaceId)
            : wikiPageRepository.findBySpaceIdAndReviewStatusOrderByTitleAsc(spaceId, ReviewStatus.PUBLISHED);
    return pages.stream()
        .map(WikiPageMapper::toResponse)
        .toList();
  }

  /** Gets one published Wiki page. */
  @Transactional(readOnly = true)
  public WikiPageResponse getPublishedWikiPage(String wikiPageId) {
    WikiPage page =
        wikiPageRepository
            .findById(wikiPageId)
            .orElseThrow(() -> new NotFoundException("Wiki page not found."));
    if (page.getReviewStatus() != ReviewStatus.PUBLISHED) {
      throw new NotFoundException("Wiki page not found.");
    }
    return WikiPageMapper.toResponse(page);
  }

  /** Gets one published Wiki page by a space-scoped slug. */
  @Transactional(readOnly = true)
  public WikiPageResponse getPublishedWikiPageBySlug(String spaceId, String slug) {
    spaceService.findSpace(spaceId);
    WikiPage page =
        wikiPageRepository
            .findBySpaceIdAndSlugAndReviewStatus(spaceId, slug, ReviewStatus.PUBLISHED)
            .orElseThrow(() -> new NotFoundException("Wiki page not found."));
    return WikiPageMapper.toResponse(page);
  }

  /** Lists Wiki folders for one Knowledge Space. */
  @Transactional(readOnly = true)
  public List<WikiFolderResponse> listWikiFolders(String spaceId) {
    spaceService.findSpace(spaceId);
    return wikiFolderRepository.findBySpaceIdOrderBySortOrderAscNameAsc(spaceId).stream()
        .map(WikiPageMapper::toResponse)
        .toList();
  }

  /** Lists safe Wiki generation run metadata for one Knowledge Space. */
  @Transactional(readOnly = true)
  public List<WikiGenerationRunResponse> listWikiGenerationRuns(String spaceId) {
    spaceService.findSpace(spaceId);
    return wikiGenerationRunRepository.findTop50BySpaceIdOrderByStartedAtDescIdAsc(spaceId).stream()
        .map(WikiPageMapper::toResponse)
        .toList();
  }

  /** Lists safe lifecycle logs for one Wiki page. */
  @Transactional(readOnly = true)
  public List<WikiLogEntryResponse> listWikiPageLogs(String wikiPageId) {
    requireWikiPage(wikiPageId);
    return wikiLogEntryRepository.findTop50ByPageIdOrderByCreatedAtDescIdAsc(wikiPageId).stream()
        .map(WikiPageMapper::toResponse)
        .toList();
  }

  /** Lists safe issue metadata for one Wiki page. */
  @Transactional(readOnly = true)
  public List<WikiPageIssueResponse> listWikiPageIssues(String wikiPageId) {
    requireWikiPage(wikiPageId);
    return wikiPageIssueRepository.findByPageIdOrderByCreatedAtDescIdAsc(wikiPageId).stream()
        .map(WikiPageMapper::toResponse)
        .toList();
  }

  private ReviewQueueItemResponse queue(
      ReviewQueueType type,
      List<FileItem> files,
      Predicate<FileItem> predicate,
      Set<String> tracedFileIds,
      boolean publishBlocked) {
    List<FileItem> matching = files.stream().filter(predicate).toList();
    return new ReviewQueueItemResponse(
        type,
        matching.size(),
        publishBlocked,
        matching.stream()
            .sorted(java.util.Comparator.comparing(FileItem::getId))
            .limit(3)
            .map(file -> representative(file, tracedFileIds))
            .toList());
  }

  private ReviewQueueRepresentativeResponse representative(FileItem file, Set<String> tracedFileIds) {
    return new ReviewQueueRepresentativeResponse(
        file.getId(),
        file.getStatus(),
        file.getReviewStatus(),
        file.getConfidence(),
        tracedFileIds.contains(file.getId()));
  }

  private FileItem findFile(String fileId) {
    return fileItemRepository
        .findById(fileId)
        .orElseThrow(() -> new NotFoundException("File item not found."));
  }

  private Batch findBatch(String batchId) {
    return batchRepository
        .findById(batchId)
        .orElseThrow(() -> new NotFoundException("Batch not found."));
  }

  private WikiPage requireWikiPage(String wikiPageId) {
    return wikiPageRepository
        .findById(wikiPageId)
        .orElseThrow(() -> new NotFoundException("Wiki page not found."));
  }

  private void validatePublishEligibility(FileItem file) {
    if (file.getReviewStatus() != ReviewStatus.APPROVED) {
      throw new ConflictException("Publish blocked: content must be approved first.");
    }
    if (file.getStatus() == FileStatus.OCR_REQUIRED || file.getReviewStatus() == ReviewStatus.OCR_REQUIRED) {
      throw new ConflictException("Publish blocked: OCR is required.");
    }
    if (file.getReviewStatus() == ReviewStatus.NEED_FIX) {
      throw new ConflictException("Publish blocked: content needs a fix.");
    }
    if (isParserFailure(file) || file.getStatus() == FileStatus.UNSUPPORTED) {
      throw new ConflictException("Publish blocked: parser failure or unsupported content.");
    }
    if (file.getConfidence() == null) {
      throw new ConflictException("Publish blocked: confidence is required.");
    }
    if (file.getMarkdownPath() == null || file.getMarkdownPath().isBlank()) {
      throw new RequestValidationException(Map.of("markdownPath", "is required for publish"));
    }
    if (!RELATIVE_PATH_VALIDATOR.isValid(file.getMarkdownPath(), null)) {
      throw new RequestValidationException(Map.of("markdownPath", "must be a safe relative path"));
    }
  }

  private boolean isPublishEligible(FileItem file, Set<String> tracedFileIds) {
    return file.getReviewStatus() == ReviewStatus.APPROVED
        && file.getMarkdownPath() != null
        && !file.getMarkdownPath().isBlank()
        && RELATIVE_PATH_VALIDATOR.isValid(file.getMarkdownPath(), null)
        && file.getConfidence() != null
        && !isParserFailure(file)
        && file.getStatus() != FileStatus.UNSUPPORTED
        && file.getStatus() != FileStatus.OCR_REQUIRED
        && file.getReviewStatus() != ReviewStatus.NEED_FIX
        && tracedFileIds.contains(file.getId());
  }

  private boolean requiresTrace(FileItem file) {
    return file.getMarkdownPath() != null
        || file.getStatus() == FileStatus.MARKDOWN_GENERATED
        || file.getStatus() == FileStatus.APPROVED
        || file.getStatus() == FileStatus.PUBLISHED;
  }

  private boolean isParserFailure(FileItem file) {
    return file.getStatus() == FileStatus.PDF_CONVERT_FAILED || file.getStatus() == FileStatus.FAILED;
  }

  private boolean isOcrRequired(FileItem file) {
    return file.getStatus() == FileStatus.OCR_REQUIRED || file.getReviewStatus() == ReviewStatus.OCR_REQUIRED;
  }

  private boolean isLowConfidence(FileItem file) {
    return !isParserFailure(file)
        && !isOcrRequired(file)
        && (file.getStatus() == FileStatus.LOW_CONFIDENCE
            || (file.getConfidence() != null
                && file.getConfidence().compareTo(new java.math.BigDecimal("0.700")) < 0));
  }
}
