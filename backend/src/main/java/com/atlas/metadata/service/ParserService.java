package com.atlas.metadata.service;

import com.atlas.metadata.adapter.ParserAdapter;
import com.atlas.metadata.adapter.ParserCapability;
import com.atlas.metadata.adapter.ParserRequest;
import com.atlas.metadata.adapter.ParserResult;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.ParserFileResult;
import com.atlas.metadata.domain.ParserRun;
import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.dto.CreateParserRunRequest;
import com.atlas.metadata.dto.ParserCapabilityResponse;
import com.atlas.metadata.dto.ParserRunResponse;
import com.atlas.metadata.dto.ParserRunSummaryResponse;
import com.atlas.metadata.dto.mapping.ParserMapper;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ParserAdapterStatus;
import com.atlas.metadata.enums.ParserRunStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.exception.ConflictException;
import com.atlas.metadata.exception.NotFoundException;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.ParserFileResultRepository;
import com.atlas.metadata.repository.ParserRunRepository;
import com.atlas.metadata.repository.SourceChunkRepository;
import com.atlas.metadata.validation.RelativePathValidator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for parser adapter runs. */
@Service
public class ParserService {

  private static final Collection<ParserRunStatus> ACTIVE_STATUSES =
      List.of(ParserRunStatus.REQUESTED, ParserRunStatus.RUNNING);
  private static final List<FileStatus> PARSER_RESULT_STATUSES =
      List.of(
          FileStatus.MARKDOWN_GENERATED,
          FileStatus.LOW_CONFIDENCE,
          FileStatus.OCR_REQUIRED,
          FileStatus.FAILED,
          FileStatus.UNSUPPORTED);
  private static final int MAX_SAFE_ERROR_LENGTH = 240;

  private final BatchService batchService;
  private final FileItemRepository fileItemRepository;
  private final ParserRunRepository parserRunRepository;
  private final ParserFileResultRepository parserFileResultRepository;
  private final SourceChunkRepository sourceChunkRepository;
  private final ParserAdapterRegistry adapterRegistry;
  private final ParserSummaryCalculator summaryCalculator;
  private final RelativePathValidator relativePathValidator;
  private final Clock clock;

  /** Creates the service. */
  @Autowired
  public ParserService(
      BatchService batchService,
      FileItemRepository fileItemRepository,
      ParserRunRepository parserRunRepository,
      ParserFileResultRepository parserFileResultRepository,
      SourceChunkRepository sourceChunkRepository,
      ParserAdapterRegistry adapterRegistry) {
    this(
        batchService,
        fileItemRepository,
        parserRunRepository,
        parserFileResultRepository,
        sourceChunkRepository,
        adapterRegistry,
        new ParserSummaryCalculator(),
        new RelativePathValidator(),
        Clock.systemUTC());
  }

  ParserService(
      BatchService batchService,
      FileItemRepository fileItemRepository,
      ParserRunRepository parserRunRepository,
      ParserFileResultRepository parserFileResultRepository,
      SourceChunkRepository sourceChunkRepository,
      ParserAdapterRegistry adapterRegistry,
      ParserSummaryCalculator summaryCalculator,
      RelativePathValidator relativePathValidator,
      Clock clock) {
    this.batchService = batchService;
    this.fileItemRepository = fileItemRepository;
    this.parserRunRepository = parserRunRepository;
    this.parserFileResultRepository = parserFileResultRepository;
    this.sourceChunkRepository = sourceChunkRepository;
    this.adapterRegistry = adapterRegistry;
    this.summaryCalculator = summaryCalculator;
    this.relativePathValidator = relativePathValidator;
    this.clock = clock;
  }

  /** Lists parser capabilities with masked configuration. */
  @Transactional(readOnly = true)
  public List<ParserCapabilityResponse> listCapabilities() {
    return adapterRegistry.capabilities().stream().map(ParserMapper::toResponse).toList();
  }

  /** Creates and executes a parser run for one batch. */
  @Transactional
  public ParserRunResponse createRun(String batchId, CreateParserRunRequest request) {
    batchService.findBatch(batchId);
    validateMode(request.mode());
    if (parserRunRepository.existsByBatchIdAndStatusIn(batchId, ACTIVE_STATUSES)) {
      throw new ConflictException("Batch already has an active parser run.");
    }

    ParserAdapter adapter = adapterRegistry.resolve(request.adapterKey());
    ParserCapability capability = adapter.capability();
    List<FileItem> files = targetFiles(batchId, request.fileIds());
    List<FileItem> eligibleFiles = eligibleFiles(files);
    List<FileItem> skippedFiles = skippedFiles(files);
    if (eligibleFiles.isEmpty()) {
      throw new RequestValidationException(
          Map.of("fileIds", "must include at least one PDF_CONVERTED file with safe pdfPath"));
    }

    OffsetDateTime now = OffsetDateTime.now(clock);
    ParserRun run =
        parserRunRepository.save(
            ParserRun.create(
                runId(now),
                batchId,
                capability.adapterKey(),
                capability.version(),
                request.requestedBy(),
                effectiveMode(request.mode()),
                scale(capability.lowConfidenceThreshold()),
                skippedFiles.size(),
                now));
    if (capability.status() != ParserAdapterStatus.AVAILABLE) {
      run.complete(ParserRunStatus.FAILED, OffsetDateTime.now(clock), "Parser adapter is unavailable.");
      return response(parserRunRepository.save(run));
    }

    run.markRunning();
    parserRunRepository.save(run);
    ParserResult adapterResult;
    try {
      adapterResult = adapter.parse(toAdapterRequest(run, eligibleFiles));
    } catch (RuntimeException ex) {
      run.complete(ParserRunStatus.FAILED, OffsetDateTime.now(clock), safeAdapterFailureMessage(ex));
      return response(parserRunRepository.save(run));
    }
    List<ParserFileResult> persistedResults = persistResults(run, adapterResult, eligibleFiles, skippedFiles);
    ParserRunSummaryResponse summary = summaryCalculator.compute(persistedResults);
    run.complete(terminalStatus(summary), OffsetDateTime.now(clock), sanitize(adapterResult.safeMessage()));
    return response(parserRunRepository.save(run));
  }

  /** Gets a parser run report. */
  @Transactional(readOnly = true)
  public ParserRunResponse getRun(String runId) {
    return response(findRun(runId));
  }

  private List<FileItem> targetFiles(String batchId, List<String> fileIds) {
    List<FileItem> files =
        fileIds == null || fileIds.isEmpty()
            ? fileItemRepository.findByBatchId(batchId)
            : fileItemRepository.findByBatchIdAndIdIn(batchId, fileIds);
    if (files.isEmpty()) {
      throw new RequestValidationException(Map.of("fileIds", "must reference files in the batch"));
    }
    if (fileIds != null && !fileIds.isEmpty() && files.size() != fileIds.size()) {
      throw new NotFoundException("File item not found.");
    }
    return files;
  }

  private List<FileItem> eligibleFiles(List<FileItem> files) {
    return files.stream()
        .filter(
            file ->
                isEligible(file))
        .toList();
  }

  private List<FileItem> skippedFiles(List<FileItem> files) {
    return files.stream().filter(file -> !isEligible(file)).toList();
  }

  private boolean isEligible(FileItem file) {
    return file.getStatus() == FileStatus.PDF_CONVERTED
        && relativePathValidator.isValid(file.getPdfPath(), null)
        && file.getPdfPath() != null
        && !file.getPdfPath().isBlank();
  }

  private ParserRequest toAdapterRequest(ParserRun run, List<FileItem> files) {
    List<ParserRequest.ParserFile> adapterFiles =
        files.stream()
            .map(
                file ->
                    new ParserRequest.ParserFile(
                        file.getId(),
                        file.getSourcePath(),
                        file.getSourceType(),
                        file.getStatus(),
                        file.getPdfPath()))
            .toList();
    return new ParserRequest(
        run.getId(),
        run.getBatchId(),
        adapterFiles,
        "generated/markdown",
        "generated/assets",
        run.getLowConfidenceThreshold());
  }

  private List<ParserFileResult> persistResults(
      ParserRun run, ParserResult adapterResult, List<FileItem> eligibleFiles, List<FileItem> skippedFiles) {
    Map<String, FileItem> filesById =
        eligibleFiles.stream().collect(Collectors.toMap(FileItem::getId, Function.identity(), (left, right) -> left));
    Map<String, String> validationErrors = new LinkedHashMap<>();
    HashSet<String> chunkIds = new HashSet<>();
    if (!run.getAdapterKey().equals(adapterResult.adapterKey())) {
      validationErrors.put("adapterKey", "must match the resolved parser adapter");
    }
    List<PreparedParserResult> prepared =
        adapterResult.files().stream()
            .map(result -> toPrepared(run, adapterResult.adapterKey(), result, filesById, chunkIds, validationErrors))
            .toList();
    List<PreparedParserResult> skipped =
        skippedFiles.stream().map(file -> toSkipped(run, run.getAdapterKey(), file)).toList();
    if (!validationErrors.isEmpty()) {
      throw new RequestValidationException(validationErrors);
    }
    return java.util.stream.Stream.concat(prepared.stream(), skipped.stream())
        .map(this::persistPrepared)
        .toList();
  }

  private PreparedParserResult toPrepared(
      ParserRun run,
      String adapterKey,
      ParserResult.ParserFileResult result,
      Map<String, FileItem> filesById,
      HashSet<String> chunkIds,
      Map<String, String> validationErrors) {
    FileItem file = filesById.get(result.fileId());
    if (file == null) {
      validationErrors.put("results." + result.fileId(), "must reference a target file");
      return new PreparedParserResult(null, null, List.of());
    }
    if (!PARSER_RESULT_STATUSES.contains(result.status())) {
      validationErrors.put("results." + result.fileId() + ".status", "must be a parser result status");
    }
    String safeError = sanitize(result.safeError());
    BigDecimal confidence = scale(result.confidence());
    validateConfidence("results." + result.fileId() + ".confidence", confidence, validationErrors);
    FileStatus mappedStatus = mappedStatus(result.status(), confidence, run.getLowConfidenceThreshold());
    validateArtifactPaths(result, mappedStatus, validationErrors);

    List<SourceChunk> chunks =
        (result.chunks() == null ? List.<ParserResult.ParserChunkResult>of() : result.chunks())
            .stream()
            .map(chunk -> toChunk(file, chunk, chunkIds, validationErrors))
            .toList();
    ParserFileResult entity =
        ParserFileResult.create(
            "parse-result-" + UUID.randomUUID().toString().substring(0, 8),
            run.getId(),
            file.getId(),
            file.getSourcePath(),
            file.getPdfPath(),
            file.getSourceType(),
            mappedStatus,
            result.markdownPath(),
            result.assetsPath(),
            confidence,
            adapterKey,
            chunks.size(),
            false,
            safeError,
            OffsetDateTime.now(clock));
    return new PreparedParserResult(file, entity, chunks);
  }

  private PreparedParserResult toSkipped(ParserRun run, String adapterKey, FileItem file) {
    ParserFileResult entity =
        ParserFileResult.create(
            "parse-result-" + UUID.randomUUID().toString().substring(0, 8),
            run.getId(),
            file.getId(),
            file.getSourcePath(),
            file.getPdfPath(),
            file.getSourceType(),
            file.getStatus(),
            file.getMarkdownPath(),
            file.getAssetsPath(),
            file.getConfidence(),
            adapterKey,
            0,
            true,
            "Skipped: file is not eligible for parser run.",
            OffsetDateTime.now(clock));
    return new PreparedParserResult(file, entity, List.of());
  }

  private ParserFileResult persistPrepared(PreparedParserResult prepared) {
    FileItem file = prepared.file();
    ParserFileResult result = prepared.result();
    if (result.isSkipped()) {
      return parserFileResultRepository.save(result);
    }
    file.applyParserResult(
        result.getStatus(),
        result.getConfidence(),
        result.getMarkdownPath(),
        result.getAssetsPath(),
        result.getSafeError());
    fileItemRepository.save(file);
    prepared.chunks().forEach(sourceChunkRepository::save);
    return parserFileResultRepository.save(result);
  }

  private SourceChunk toChunk(
      FileItem file,
      ParserResult.ParserChunkResult chunk,
      HashSet<String> chunkIds,
      Map<String, String> validationErrors) {
    String chunkId = chunk.chunkId();
    if (chunkId == null || chunkId.isBlank()) {
      validationErrors.put("chunks." + file.getId() + ".chunkId", "must be present");
      chunkId = "invalid-" + UUID.randomUUID();
    } else if (!chunkIds.add(chunkId)) {
      validationErrors.put("chunks." + chunkId, "must be unique in the parser run");
    }
    if (chunk.page() != null && chunk.page() <= 0) {
      validationErrors.put("chunks." + chunkId + ".page", "must be positive");
    }
    BigDecimal confidence = scale(chunk.confidence());
    validateConfidence("chunks." + chunkId + ".confidence", confidence, validationErrors);
    return SourceChunk.create(
        chunkId,
        file.getId(),
        file.getSourcePath(),
        chunk.page(),
        sanitize(chunk.section()),
        confidence,
        chunk.reviewStatus() == null ? ReviewStatus.REVIEW_REQUIRED : chunk.reviewStatus());
  }

  private void validateArtifactPaths(
      ParserResult.ParserFileResult result, FileStatus status, Map<String, String> validationErrors) {
    boolean markdownRequired = status == FileStatus.MARKDOWN_GENERATED || status == FileStatus.LOW_CONFIDENCE;
    if (markdownRequired && (result.markdownPath() == null || result.markdownPath().isBlank())) {
      validationErrors.put("results." + result.fileId() + ".markdownPath", "must be present");
    }
    if (!relativePathValidator.isValid(result.markdownPath(), null)) {
      validationErrors.put("results." + result.fileId() + ".markdownPath", "must be a safe relative path");
    }
    if (!relativePathValidator.isValid(result.assetsPath(), null)) {
      validationErrors.put("results." + result.fileId() + ".assetsPath", "must be a safe relative path");
    }
  }

  private void validateConfidence(
      String field, BigDecimal confidence, Map<String, String> validationErrors) {
    if (confidence != null
        && (confidence.compareTo(BigDecimal.ZERO) < 0 || confidence.compareTo(BigDecimal.ONE) > 0)) {
      validationErrors.put(field, "must be between 0 and 1");
    }
  }

  private ParserRunResponse response(ParserRun run) {
    List<ParserFileResult> results = parserFileResultRepository.findByRunIdOrderByCreatedAtAsc(run.getId());
    Map<String, FileItem> filesById =
        fileItemRepository.findAllById(results.stream().map(ParserFileResult::getFileItemId).toList())
            .stream()
            .collect(Collectors.toMap(FileItem::getId, Function.identity()));
    List<SourceChunk> chunks =
        results.isEmpty()
            ? List.of()
            : sourceChunkRepository.findByFileItemIdIn(
                results.stream().map(ParserFileResult::getFileItemId).distinct().toList());
    return ParserMapper.toResponse(
        run,
        summaryCalculator.compute(results),
        results,
        filesById,
        chunks);
  }

  private ParserRun findRun(String runId) {
    return parserRunRepository
        .findById(runId)
        .orElseThrow(() -> new NotFoundException("Parser run not found."));
  }

  private ParserRunStatus terminalStatus(ParserRunSummaryResponse summary) {
    int generatedMarkdown = summary.markdownGenerated() + summary.lowConfidence();
    if (summary.total() == 0 || generatedMarkdown == 0) {
      return ParserRunStatus.FAILED;
    }
    if (summary.lowConfidence() > 0
        || summary.ocrRequired() > 0
        || summary.failed() > 0
        || summary.unsupported() > 0
        || summary.skipped() > 0) {
      return ParserRunStatus.PARTIAL_FAILED;
    }
    return ParserRunStatus.SUCCEEDED;
  }

  private FileStatus mappedStatus(FileStatus status, BigDecimal confidence, BigDecimal threshold) {
    if (status == FileStatus.MARKDOWN_GENERATED && confidence != null && confidence.compareTo(threshold) < 0) {
      return FileStatus.LOW_CONFIDENCE;
    }
    return status;
  }

  private void validateMode(String mode) {
    if (mode == null || mode.isBlank() || "mock".equals(mode) || "configured".equals(mode)) {
      return;
    }
    throw new RequestValidationException(Map.of("mode", "must be mock or configured"));
  }

  private String effectiveMode(String mode) {
    return mode == null || mode.isBlank() ? "mock" : mode;
  }

  private String sanitize(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String sanitized =
        value.replaceAll("(?i)(password|token|api[_-]?key)\\s*[:=]\\s*\\S+", "$1=[masked]")
            .replaceAll("(?i)\\b[a-z][a-z0-9+.-]*://[^\\s]+", "[endpoint]")
            .replaceAll("[A-Za-z]:[/\\\\][^\\s]+", "[path]")
            .replaceAll("(/[^\\s]+){2,}", "[path]")
            .replaceAll("\\bat\\s+[\\w.$]+\\([^)]*\\)", "[stack]")
            .replaceAll(
                "(?i)\\b[a-z0-9][a-z0-9-]*(?:\\.[a-z0-9][a-z0-9-]*)+(?::\\d+)?\\b",
                "[host]");
    return sanitized.length() <= MAX_SAFE_ERROR_LENGTH
        ? sanitized
        : sanitized.substring(0, MAX_SAFE_ERROR_LENGTH);
  }

  private String safeAdapterFailureMessage(RuntimeException ex) {
    String safeMessage = sanitize(ex.getMessage());
    return safeMessage == null ? "Parser adapter failed." : safeMessage;
  }

  private BigDecimal scale(BigDecimal value) {
    return value == null ? null : value.setScale(3, RoundingMode.HALF_UP);
  }

  private String runId(OffsetDateTime now) {
    return "parse-run-"
        + now.format(DateTimeFormatter.ISO_LOCAL_DATE)
        + "-"
        + UUID.randomUUID().toString().substring(0, 8);
  }

  private record PreparedParserResult(
      FileItem file, ParserFileResult result, List<SourceChunk> chunks) {}
}
