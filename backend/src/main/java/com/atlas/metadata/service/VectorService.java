package com.atlas.metadata.service;

import com.atlas.metadata.adapter.VectorAdapter;
import com.atlas.metadata.adapter.VectorCapability;
import com.atlas.metadata.adapter.VectorDeleteRequest;
import com.atlas.metadata.adapter.VectorDeleteResult;
import com.atlas.metadata.adapter.VectorIndexRequest;
import com.atlas.metadata.adapter.VectorIndexResult;
import com.atlas.metadata.adapter.VectorQueryResult;
import com.atlas.metadata.domain.Batch;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.domain.VectorItemResult;
import com.atlas.metadata.domain.VectorRun;
import com.atlas.metadata.dto.CreateVectorRunRequest;
import com.atlas.metadata.dto.VectorCapabilityResponse;
import com.atlas.metadata.dto.VectorQueryRequestDto;
import com.atlas.metadata.dto.VectorQueryResponse;
import com.atlas.metadata.dto.VectorRunResponse;
import com.atlas.metadata.dto.VectorRunSummaryResponse;
import com.atlas.metadata.dto.mapping.VectorMapper;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.VectorAdapterStatus;
import com.atlas.metadata.enums.VectorItemStatus;
import com.atlas.metadata.enums.VectorReviewPolicy;
import com.atlas.metadata.enums.VectorRunOperation;
import com.atlas.metadata.enums.VectorRunStatus;
import com.atlas.metadata.exception.NotFoundException;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.repository.BatchRepository;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.SourceChunkRepository;
import com.atlas.metadata.repository.SpaceRepository;
import com.atlas.metadata.repository.VectorItemResultRepository;
import com.atlas.metadata.repository.VectorRunRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
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

/** Application service for vector adapter runs and query evidence. */
@Service
public class VectorService {

  private static final int DEFAULT_QUERY_LIMIT = 10;
  private static final int MAX_QUERY_LIMIT = 50;
  private static final int MAX_SAFE_MESSAGE_LENGTH = 240;

  private final SpaceRepository spaceRepository;
  private final BatchRepository batchRepository;
  private final FileItemRepository fileItemRepository;
  private final SourceChunkRepository sourceChunkRepository;
  private final VectorRunRepository vectorRunRepository;
  private final VectorItemResultRepository vectorItemResultRepository;
  private final VectorAdapterRegistry adapterRegistry;
  private final VectorSummaryCalculator summaryCalculator;
  private final Clock clock;

  /** Creates the service. */
  @Autowired
  public VectorService(
      SpaceRepository spaceRepository,
      BatchRepository batchRepository,
      FileItemRepository fileItemRepository,
      SourceChunkRepository sourceChunkRepository,
      VectorRunRepository vectorRunRepository,
      VectorItemResultRepository vectorItemResultRepository,
      VectorAdapterRegistry adapterRegistry) {
    this(
        spaceRepository,
        batchRepository,
        fileItemRepository,
        sourceChunkRepository,
        vectorRunRepository,
        vectorItemResultRepository,
        adapterRegistry,
        new VectorSummaryCalculator(),
        Clock.systemUTC());
  }

  VectorService(
      SpaceRepository spaceRepository,
      BatchRepository batchRepository,
      FileItemRepository fileItemRepository,
      SourceChunkRepository sourceChunkRepository,
      VectorRunRepository vectorRunRepository,
      VectorItemResultRepository vectorItemResultRepository,
      VectorAdapterRegistry adapterRegistry,
      VectorSummaryCalculator summaryCalculator,
      Clock clock) {
    this.spaceRepository = spaceRepository;
    this.batchRepository = batchRepository;
    this.fileItemRepository = fileItemRepository;
    this.sourceChunkRepository = sourceChunkRepository;
    this.vectorRunRepository = vectorRunRepository;
    this.vectorItemResultRepository = vectorItemResultRepository;
    this.adapterRegistry = adapterRegistry;
    this.summaryCalculator = summaryCalculator;
    this.clock = clock;
  }

  /** Lists vector capabilities with masked configuration. */
  @Transactional(readOnly = true)
  public List<VectorCapabilityResponse> listCapabilities() {
    return adapterRegistry.capabilities().stream().map(VectorMapper::toResponse).toList();
  }

  /** Creates and synchronously executes a vector index/deindex run. */
  @Transactional
  public VectorRunResponse createRun(String spaceId, CreateVectorRunRequest request) {
    validateSpace(spaceId);
    validateMode(request.mode());
    VectorAdapter adapter = adapterRegistry.resolve(request.adapterKey());
    VectorCapability capability = adapter.capability();
    validateDimension(capability, request.dimension());
    VectorRunOperation operation = request.operation() == null ? VectorRunOperation.INDEX : request.operation();
    VectorReviewPolicy reviewPolicy = effectiveReviewPolicy(request.reviewPolicy());
    List<SourceChunk> targetChunks = targetChunks(spaceId, request);
    validateVectorPayloads(targetChunks, request.items(), request.dimension());

    OffsetDateTime now = OffsetDateTime.now(clock);
    VectorRun run =
        vectorRunRepository.save(
            VectorRun.create(
                runId(now),
                spaceId,
                blankToNull(request.batchId()),
                capability.adapterKey(),
                capability.version(),
                operation,
                effectiveMode(request.mode()),
                reviewPolicy,
                request.dimension(),
                request.requestedBy(),
                now));
    if (capability.status() != VectorAdapterStatus.AVAILABLE) {
      run.complete(
          VectorRunStatus.FAILED,
          0,
          0,
          0,
          0,
          0,
          OffsetDateTime.now(clock),
          "Vector adapter is unavailable.");
      return response(vectorRunRepository.save(run));
    }

    run.markRunning();
    vectorRunRepository.save(run);
    List<VectorItemResult> results;
    try {
      results =
          operation == VectorRunOperation.DEINDEX
              ? deindex(run, adapter, targetChunks)
              : index(run, adapter, targetChunks, request.items());
    } catch (RuntimeException ex) {
      run.complete(
          VectorRunStatus.FAILED,
          0,
          0,
          0,
          0,
          0,
          OffsetDateTime.now(clock),
          safeAdapterFailureMessage(ex));
      return response(vectorRunRepository.save(run));
    }
    VectorRunSummaryResponse summary = summaryCalculator.compute(results);
    run.complete(
        terminalStatus(summary),
        summary.totalCount(),
        summary.indexedCount(),
        summary.deletedCount(),
        summary.skippedCount(),
        summary.failedCount(),
        OffsetDateTime.now(clock),
        "Vector " + operation.name().toLowerCase() + " completed.");
    return response(vectorRunRepository.save(run));
  }

  /** Gets a vector run report. */
  @Transactional(readOnly = true)
  public VectorRunResponse getRun(String runId) {
    return response(findRun(runId));
  }

  /** Runs an immediate vector similarity evidence query without persisting a run. */
  @Transactional(readOnly = true)
  public VectorQueryResponse query(String spaceId, VectorQueryRequestDto request) {
    validateSpace(spaceId);
    VectorAdapter adapter = adapterRegistry.resolve(request.adapterKey());
    VectorCapability capability = adapter.capability();
    if (capability.status() != VectorAdapterStatus.AVAILABLE) {
      throw new RequestValidationException(Map.of("adapterKey", "must reference an available vector adapter"));
    }
    int limit = effectiveLimit(request.limit());
    validateQueryInput(request, capability);
    VectorReviewPolicy reviewPolicy = effectiveReviewPolicy(request.reviewPolicy());
    VectorQueryResult result =
        adapter.query(
            new com.atlas.metadata.adapter.VectorQueryRequest(
                spaceId, request.queryVector(), request.mockQuery(), limit, reviewPolicy));
    List<VectorQueryResult.VectorMatch> safeMatches = safeSortedMatches(result.matches(), limit);
    Map<String, SourceChunk> chunksById = queryChunksById(spaceId, safeMatches, reviewPolicy);
    List<VectorQueryResult.VectorMatch> filteredMatches =
        safeMatches.stream().filter(match -> chunksById.containsKey(match.sourceChunkId())).toList();
    return VectorMapper.toQueryResponse(
        spaceId,
        capability.adapterKey(),
        "mock",
        reviewPolicy,
        limit,
        sanitize(result.safeMessage()),
        filteredMatches,
        chunksById);
  }

  private List<VectorItemResult> index(
      VectorRun run,
      VectorAdapter adapter,
      List<SourceChunk> targetChunks,
      List<CreateVectorRunRequest.VectorInputItem> inputItems) {
    Map<String, List<BigDecimal>> vectorsByChunkId = vectorsByChunkId(inputItems);
    List<SourceChunk> skippedChunks =
        targetChunks.stream().filter(chunk -> !isEligibleForIndex(run.getReviewPolicy(), chunk)).toList();
    List<SourceChunk> eligibleChunks =
        targetChunks.stream().filter(chunk -> isEligibleForIndex(run.getReviewPolicy(), chunk)).toList();
    List<VectorItemResult> skippedResults =
        skippedChunks.stream()
            .map(chunk -> skippedResult(run, chunk, "Skipped: source chunk is not approved."))
            .toList();
    VectorIndexResult adapterResult =
        adapter.index(
            new VectorIndexRequest(
                run.getId(),
                run.getSpaceId(),
                run.getBatchId(),
                run.getMode(),
                run.getReviewPolicy(),
                run.getDimension(),
                eligibleChunks.stream()
                    .map(chunk -> toIndexItem(chunk, vectorsByChunkId.get(chunk.getId())))
                    .toList()));
    validateAdapterKey(run.getAdapterKey(), adapterResult.adapterKey());
    Map<String, VectorIndexResult.VectorItemResult> resultsByChunkId =
        adapterResult.items().stream()
            .collect(
                Collectors.toMap(
                    VectorIndexResult.VectorItemResult::sourceChunkId,
                    Function.identity(),
                    (left, right) -> left));
    List<VectorItemResult> adapterResults =
        eligibleChunks.stream()
            .map(chunk -> indexResult(run, chunk, resultsByChunkId.get(chunk.getId())))
            .toList();
    return java.util.stream.Stream.concat(adapterResults.stream(), skippedResults.stream())
        .map(vectorItemResultRepository::save)
        .toList();
  }

  private List<VectorItemResult> deindex(VectorRun run, VectorAdapter adapter, List<SourceChunk> targetChunks) {
    VectorDeleteResult adapterResult =
        adapter.delete(
            new VectorDeleteRequest(
                run.getId(),
                run.getSpaceId(),
                run.getBatchId(),
                targetChunks.stream().map(SourceChunk::getId).toList()));
    validateAdapterKey(run.getAdapterKey(), adapterResult.adapterKey());
    Map<String, VectorDeleteResult.VectorDeletedItem> resultsByChunkId =
        adapterResult.items().stream()
            .collect(
                Collectors.toMap(
                    VectorDeleteResult.VectorDeletedItem::sourceChunkId,
                    Function.identity(),
                    (left, right) -> left));
    return targetChunks.stream()
        .map(chunk -> deleteResult(run, chunk, resultsByChunkId.get(chunk.getId())))
        .map(vectorItemResultRepository::save)
        .toList();
  }

  private VectorIndexRequest.VectorIndexItem toIndexItem(
      SourceChunk chunk, List<BigDecimal> vector) {
    return new VectorIndexRequest.VectorIndexItem(
        chunk.getId(),
        chunk.getFileItemId(),
        chunk.getSourceFile(),
        chunk.getPage(),
        chunk.getSection(),
        chunk.getReviewStatus(),
        scale(chunk.getConfidence()),
        vector == null ? null : vector.stream().map(this::scale).toList());
  }

  private VectorItemResult indexResult(
      VectorRun run, SourceChunk chunk, VectorIndexResult.VectorItemResult adapterResult) {
    if (adapterResult == null) {
      return failedResult(run, chunk, "Vector adapter did not return a result for the source chunk.");
    }
    validateScore(adapterResult.score());
    if (adapterResult.status() == VectorItemStatus.INDEXED
        && (adapterResult.vectorItemKey() == null || adapterResult.vectorItemKey().isBlank())) {
      return failedResult(run, chunk, "Vector adapter did not return a vector item key.");
    }
    return VectorItemResult.create(
        resultId(),
        run.getId(),
        chunk.getId(),
        chunk.getFileItemId(),
        chunk.getSourceFile(),
        chunk.getPage(),
        sanitize(chunk.getSection()),
        chunk.getReviewStatus(),
        scale(chunk.getConfidence()),
        sanitize(adapterResult.vectorItemKey()),
        adapterResult.status(),
        scale(adapterResult.score()),
        sanitize(adapterResult.safeError()),
        OffsetDateTime.now(clock));
  }

  private VectorItemResult deleteResult(
      VectorRun run, SourceChunk chunk, VectorDeleteResult.VectorDeletedItem adapterResult) {
    if (adapterResult == null) {
      return failedResult(run, chunk, "Vector adapter did not return a result for the source chunk.");
    }
    return VectorItemResult.create(
        resultId(),
        run.getId(),
        chunk.getId(),
        chunk.getFileItemId(),
        chunk.getSourceFile(),
        chunk.getPage(),
        sanitize(chunk.getSection()),
        chunk.getReviewStatus(),
        scale(chunk.getConfidence()),
        sanitize(adapterResult.vectorItemKey()),
        adapterResult.status(),
        null,
        sanitize(adapterResult.safeError()),
        OffsetDateTime.now(clock));
  }

  private VectorItemResult skippedResult(VectorRun run, SourceChunk chunk, String safeError) {
    return VectorItemResult.create(
        resultId(),
        run.getId(),
        chunk.getId(),
        chunk.getFileItemId(),
        chunk.getSourceFile(),
        chunk.getPage(),
        sanitize(chunk.getSection()),
        chunk.getReviewStatus(),
        scale(chunk.getConfidence()),
        null,
        VectorItemStatus.SKIPPED,
        null,
        sanitize(safeError),
        OffsetDateTime.now(clock));
  }

  private VectorItemResult failedResult(VectorRun run, SourceChunk chunk, String safeError) {
    return VectorItemResult.create(
        resultId(),
        run.getId(),
        chunk.getId(),
        chunk.getFileItemId(),
        chunk.getSourceFile(),
        chunk.getPage(),
        sanitize(chunk.getSection()),
        chunk.getReviewStatus(),
        scale(chunk.getConfidence()),
        null,
        VectorItemStatus.FAILED,
        null,
        sanitize(safeError),
        OffsetDateTime.now(clock));
  }

  private List<SourceChunk> targetChunks(String spaceId, CreateVectorRunRequest request) {
    List<FileItem> files = targetFiles(spaceId, request.batchId(), request.fileItemIds());
    List<String> fileIds = files.stream().map(FileItem::getId).toList();
    List<SourceChunk> chunks =
        request.sourceChunkIds() == null || request.sourceChunkIds().isEmpty()
            ? sourceChunkRepository.findByFileItemIdIn(fileIds)
            : sourceChunkRepository.findAllById(request.sourceChunkIds());
    if (chunks.isEmpty()) {
      throw new RequestValidationException(Map.of("sourceChunkIds", "must reference source chunks in the scope"));
    }
    if (request.sourceChunkIds() != null
        && !request.sourceChunkIds().isEmpty()
        && chunks.size() != new HashSet<>(request.sourceChunkIds()).size()) {
      throw new NotFoundException("Source chunk not found.");
    }
    if (chunks.stream().anyMatch(chunk -> !fileIds.contains(chunk.getFileItemId()))) {
      throw new RequestValidationException(Map.of("sourceChunkIds", "must reference source chunks in the scope"));
    }
    return chunks.stream().sorted(Comparator.comparing(SourceChunk::getId)).toList();
  }

  private List<FileItem> targetFiles(String spaceId, String batchId, List<String> fileItemIds) {
    if (batchId != null && !batchId.isBlank()) {
      Batch batch =
          batchRepository.findById(batchId).orElseThrow(() -> new NotFoundException("Batch not found."));
      if (!spaceId.equals(batch.getSpaceId())) {
        throw new RequestValidationException(Map.of("batchId", "must reference a batch in the space"));
      }
      List<FileItem> files =
          fileItemIds == null || fileItemIds.isEmpty()
              ? fileItemRepository.findByBatchId(batchId)
              : fileItemRepository.findByBatchIdAndIdIn(batchId, fileItemIds);
      validateFileSelection(files, fileItemIds);
      return files;
    }
    List<String> batchIds = batchRepository.findBySpaceId(spaceId).stream().map(Batch::getId).toList();
    if (batchIds.isEmpty()) {
      throw new RequestValidationException(Map.of("batchId", "space must include at least one batch"));
    }
    List<FileItem> files =
        fileItemIds == null || fileItemIds.isEmpty()
            ? fileItemRepository.findByBatchIdIn(batchIds)
            : fileItemRepository.findAllById(fileItemIds);
    validateFileSelection(files, fileItemIds);
    if (files.stream().anyMatch(file -> !batchIds.contains(file.getBatchId()))) {
      throw new RequestValidationException(Map.of("fileItemIds", "must reference files in the space"));
    }
    return files;
  }

  private void validateFileSelection(List<FileItem> files, List<String> requestedFileIds) {
    if (files.isEmpty()) {
      throw new RequestValidationException(Map.of("fileItemIds", "must reference files in the scope"));
    }
    if (requestedFileIds != null
        && !requestedFileIds.isEmpty()
        && files.size() != new HashSet<>(requestedFileIds).size()) {
      throw new NotFoundException("File item not found.");
    }
  }

  private Map<String, List<BigDecimal>> vectorsByChunkId(
      List<CreateVectorRunRequest.VectorInputItem> items) {
    if (items == null || items.isEmpty()) {
      return Map.of();
    }
    return items.stream()
        .collect(
            Collectors.toMap(
                CreateVectorRunRequest.VectorInputItem::sourceChunkId,
                CreateVectorRunRequest.VectorInputItem::vector,
                (left, right) -> left,
                LinkedHashMap::new));
  }

  private void validateVectorPayloads(
      List<SourceChunk> targetChunks,
      List<CreateVectorRunRequest.VectorInputItem> items,
      Integer dimension) {
    if (items == null || items.isEmpty()) {
      return;
    }
    Map<String, SourceChunk> chunksById =
        targetChunks.stream().collect(Collectors.toMap(SourceChunk::getId, Function.identity()));
    Map<String, String> errors = new LinkedHashMap<>();
    for (CreateVectorRunRequest.VectorInputItem item : items) {
      if (item.sourceChunkId() == null || !chunksById.containsKey(item.sourceChunkId())) {
        errors.put("items.sourceChunkId", "must reference a source chunk in the request scope");
        continue;
      }
      if (item.vector() == null || item.vector().isEmpty()) {
        errors.put("items." + item.sourceChunkId() + ".vector", "must include at least one value");
        continue;
      }
      if (dimension != null && item.vector().size() != dimension) {
        errors.put("items." + item.sourceChunkId() + ".vector", "must match requested dimension");
      }
    }
    if (!errors.isEmpty()) {
      throw new RequestValidationException(errors);
    }
  }

  private void validateQueryInput(VectorQueryRequestDto request, VectorCapability capability) {
    boolean hasVector = request.queryVector() != null && !request.queryVector().isEmpty();
    boolean hasToken = request.mockQuery() != null && !request.mockQuery().isBlank();
    if (!hasVector && !hasToken) {
      throw new RequestValidationException(Map.of("query", "must include queryVector or mockQuery"));
    }
    if (hasVector && !capability.supportedDimensions().contains(request.queryVector().size())) {
      throw new RequestValidationException(Map.of("queryVector", "must match a supported vector dimension"));
    }
  }

  private void validateDimension(VectorCapability capability, Integer dimension) {
    if (dimension == null) {
      return;
    }
    if (dimension <= 0 || !capability.supportedDimensions().contains(dimension)) {
      throw new RequestValidationException(Map.of("dimension", "must match a supported vector dimension"));
    }
  }

  private void validateAdapterKey(String expectedAdapterKey, String actualAdapterKey) {
    if (!expectedAdapterKey.equals(actualAdapterKey)) {
      throw new RequestValidationException(Map.of("adapterKey", "must match the resolved vector adapter"));
    }
  }

  private Map<String, SourceChunk> queryChunksById(
      String spaceId, List<VectorQueryResult.VectorMatch> matches, VectorReviewPolicy reviewPolicy) {
    if (matches.isEmpty()) {
      return Map.of();
    }
    List<SourceChunk> chunks =
        sourceChunkRepository.findAllById(
            matches.stream().map(VectorQueryResult.VectorMatch::sourceChunkId).distinct().toList());
    List<String> batchIds = batchRepository.findBySpaceId(spaceId).stream().map(Batch::getId).toList();
    List<String> fileIds =
        batchIds.isEmpty()
            ? List.of()
            : fileItemRepository.findByBatchIdIn(batchIds).stream().map(FileItem::getId).toList();
    return chunks.stream()
        .filter(chunk -> fileIds.contains(chunk.getFileItemId()))
        .filter(chunk -> reviewPolicy == VectorReviewPolicy.INCLUDE_REVIEW_REQUIRED || isApproved(chunk))
        .collect(Collectors.toMap(SourceChunk::getId, Function.identity()));
  }

  private List<VectorQueryResult.VectorMatch> safeSortedMatches(
      List<VectorQueryResult.VectorMatch> matches, int limit) {
    return (matches == null ? List.<VectorQueryResult.VectorMatch>of() : matches)
        .stream()
        .map(this::safeMatch)
        .filter(match -> validateQueryScore(match.score()))
        .sorted(
            Comparator.comparing(VectorQueryResult.VectorMatch::score)
                .reversed()
                .thenComparing(VectorQueryResult.VectorMatch::sourceChunkId))
        .limit(limit)
        .toList();
  }

  private VectorQueryResult.VectorMatch safeMatch(VectorQueryResult.VectorMatch match) {
    Map<String, String> safeMetadata =
        match.safeMetadata() == null
            ? Map.of()
            : match.safeMetadata().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> sanitize(entry.getValue())));
    return new VectorQueryResult.VectorMatch(
        match.sourceChunkId(), sanitize(match.vectorItemKey()), scale(match.score()), safeMetadata);
  }

  private boolean validateScore(BigDecimal score) {
    if (score == null) {
      return true;
    }
    if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(BigDecimal.ONE) > 0) {
      throw new RequestValidationException(Map.of("score", "must be between 0 and 1"));
    }
    return true;
  }

  private boolean validateQueryScore(BigDecimal score) {
    if (score == null) {
      throw new RequestValidationException(Map.of("score", "must be present for query matches"));
    }
    return validateScore(score);
  }

  private boolean isApproved(SourceChunk chunk) {
    return chunk.getReviewStatus() == ReviewStatus.APPROVED || chunk.getReviewStatus() == ReviewStatus.PUBLISHED;
  }

  private boolean isEligibleForIndex(VectorReviewPolicy reviewPolicy, SourceChunk chunk) {
    return reviewPolicy == VectorReviewPolicy.INCLUDE_REVIEW_REQUIRED || isApproved(chunk);
  }

  private VectorRunResponse response(VectorRun run) {
    List<VectorItemResult> results = vectorItemResultRepository.findByRunIdOrderByCreatedAtAsc(run.getId());
    return VectorMapper.toResponse(run, summaryCalculator.compute(results), results);
  }

  private VectorRun findRun(String runId) {
    return vectorRunRepository
        .findById(runId)
        .orElseThrow(() -> new NotFoundException("Vector run not found."));
  }

  private VectorRunStatus terminalStatus(VectorRunSummaryResponse summary) {
    int successCount = summary.indexedCount() + summary.deletedCount();
    if (summary.totalCount() == 0 || successCount == 0) {
      return VectorRunStatus.FAILED;
    }
    if (summary.failedCount() > 0 || summary.skippedCount() > 0) {
      return VectorRunStatus.PARTIAL_FAILED;
    }
    return VectorRunStatus.SUCCEEDED;
  }

  private VectorReviewPolicy effectiveReviewPolicy(VectorReviewPolicy reviewPolicy) {
    return reviewPolicy == null ? VectorReviewPolicy.APPROVED_ONLY : reviewPolicy;
  }

  private int effectiveLimit(Integer requestedLimit) {
    int limit = requestedLimit == null ? DEFAULT_QUERY_LIMIT : requestedLimit;
    if (limit <= 0 || limit > MAX_QUERY_LIMIT) {
      throw new RequestValidationException(Map.of("limit", "must be between 1 and " + MAX_QUERY_LIMIT));
    }
    return limit;
  }

  private void validateSpace(String spaceId) {
    if (!spaceRepository.existsById(spaceId)) {
      throw new NotFoundException("Knowledge Space not found.");
    }
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

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  private String sanitize(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String sanitized =
        value.replaceAll("(?i)(password|token|api[_-]?key)\\s*[:=]\\s*\\S+", "$1=[masked]")
            .replaceAll("(?i)\\b[a-z][a-z0-9+.-]*://[^\\s]+", "[endpoint]")
            .replaceAll("(?i)\\b(endpoint|dsn|collection)\\s*[:=]\\s*\\S+", "$1=[masked]")
            .replaceAll("[A-Za-z]:[/\\\\][^\\s]+", "[path]")
            .replaceAll("(/[^\\s]+){2,}", "[path]")
            .replaceAll("\\bat\\s+[\\w.$]+\\([^)]*\\)", "[stack]")
            .replaceAll(
                "(?i)\\b[a-z0-9][a-z0-9-]*(?:\\.[a-z0-9][a-z0-9-]*)+(?::\\d+)?\\b",
                "[host]");
    return sanitized.length() <= MAX_SAFE_MESSAGE_LENGTH
        ? sanitized
        : sanitized.substring(0, MAX_SAFE_MESSAGE_LENGTH);
  }

  private String safeAdapterFailureMessage(RuntimeException ex) {
    String safeMessage = sanitize(ex.getMessage());
    return safeMessage == null ? "Vector adapter failed." : safeMessage;
  }

  private BigDecimal scale(BigDecimal value) {
    return value == null ? null : value.setScale(3, RoundingMode.HALF_UP);
  }

  private String runId(OffsetDateTime now) {
    return "vector-run-"
        + now.format(DateTimeFormatter.ISO_LOCAL_DATE)
        + "-"
        + UUID.randomUUID().toString().substring(0, 8);
  }

  private String resultId() {
    return "vector-result-" + UUID.randomUUID().toString().substring(0, 8);
  }
}
