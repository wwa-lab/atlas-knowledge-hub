package com.atlas.metadata.service;

import com.atlas.metadata.domain.AskEvidence;
import com.atlas.metadata.domain.AskRun;
import com.atlas.metadata.domain.Batch;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.dto.AskRunResponse;
import com.atlas.metadata.dto.CreateAskRequest;
import com.atlas.metadata.dto.CreateModelRunRequest;
import com.atlas.metadata.dto.ModelOutputResponse;
import com.atlas.metadata.dto.ModelRunResponse;
import com.atlas.metadata.dto.ModelSourceReferenceRequest;
import com.atlas.metadata.dto.VectorQueryMatchResponse;
import com.atlas.metadata.dto.VectorQueryRequestDto;
import com.atlas.metadata.dto.VectorQueryResponse;
import com.atlas.metadata.dto.mapping.AskMapper;
import com.atlas.metadata.enums.AskReviewPolicy;
import com.atlas.metadata.enums.AskRunStatus;
import com.atlas.metadata.enums.ModelOperation;
import com.atlas.metadata.enums.ModelRunStatus;
import com.atlas.metadata.enums.ModelSourceReferenceType;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceType;
import com.atlas.metadata.enums.VectorReviewPolicy;
import com.atlas.metadata.exception.NotFoundException;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.repository.AskEvidenceRepository;
import com.atlas.metadata.repository.AskRunRepository;
import com.atlas.metadata.repository.BatchRepository;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.SpaceRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Coordinates trusted ask retrieval and answer generation through internal services. */
@Service
public class AskService {

  private static final int DEFAULT_LIMIT = 5;
  private static final int MAX_LIMIT = 10;
  private static final int MAX_QUESTION_LENGTH = 500;
  private static final String NO_EVIDENCE_ANSWER =
      "No approved evidence was found for this question. Review or publish relevant evidence before using Trusted Ask.";
  private static final Pattern SECRET_PATTERN =
      Pattern.compile("(?i)(password|secret|token|api[_-]?key|bearer)\\s*[:=]");
  private static final Pattern URL_PATTERN = Pattern.compile("(?i)https?://|jdbc:");
  private static final Pattern PATH_PATTERN =
      Pattern.compile("(^|\\s)(/" + "Users/|/home/|/var/|/etc/|[A-Za-z]:\\\\|\\\\\\\\)");

  private final SpaceRepository spaceRepository;
  private final BatchRepository batchRepository;
  private final FileItemRepository fileItemRepository;
  private final VectorService vectorService;
  private final ModelService modelService;
  private final AskRunRepository askRunRepository;
  private final AskEvidenceRepository askEvidenceRepository;
  private final AskSummaryCalculator summaryCalculator;
  private final Clock clock;

  /** Creates the trusted ask service. */
  @Autowired
  public AskService(
      SpaceRepository spaceRepository,
      BatchRepository batchRepository,
      FileItemRepository fileItemRepository,
      VectorService vectorService,
      ModelService modelService,
      AskRunRepository askRunRepository,
      AskEvidenceRepository askEvidenceRepository,
      AskSummaryCalculator summaryCalculator) {
    this(
        spaceRepository,
        batchRepository,
        fileItemRepository,
        vectorService,
        modelService,
        askRunRepository,
        askEvidenceRepository,
        summaryCalculator,
        Clock.systemUTC());
  }

  AskService(
      SpaceRepository spaceRepository,
      BatchRepository batchRepository,
      FileItemRepository fileItemRepository,
      VectorService vectorService,
      ModelService modelService,
      AskRunRepository askRunRepository,
      AskEvidenceRepository askEvidenceRepository,
      AskSummaryCalculator summaryCalculator,
      Clock clock) {
    this.spaceRepository = spaceRepository;
    this.batchRepository = batchRepository;
    this.fileItemRepository = fileItemRepository;
    this.vectorService = vectorService;
    this.modelService = modelService;
    this.askRunRepository = askRunRepository;
    this.askEvidenceRepository = askEvidenceRepository;
    this.summaryCalculator = summaryCalculator;
    this.clock = clock;
  }

  /** Creates and executes one trusted ask run. */
  @Transactional
  public AskRunResponse createRun(String spaceId, CreateAskRequest request) {
    if (!spaceRepository.existsById(spaceId)) {
      throw new NotFoundException("Knowledge Space was not found.");
    }
    ValidatedAsk validated = validate(spaceId, request);
    OffsetDateTime now = OffsetDateTime.now(clock);
    AskRun run =
        askRunRepository.save(
            AskRun.create(
                "ask-" + UUID.randomUUID(),
                spaceId,
                validated.question(),
                validated.reviewPolicy(),
                validated.mode(),
                validated.requestedBy(),
                now));

    try {
      run.markRetrieving();
      VectorQueryResponse vectorResponse =
          vectorService.query(
              spaceId,
              new VectorQueryRequestDto(
                  null,
                  null,
                  validated.question(),
                  validated.limit(),
                  toVectorPolicy(validated.reviewPolicy())));
      List<VectorQueryMatchResponse> matches = applyFilters(vectorResponse.matches(), validated);
      if (matches.isEmpty()) {
        run.complete(
            AskRunStatus.NO_EVIDENCE,
            NO_EVIDENCE_ANSWER,
            null,
            null,
            "No eligible evidence was found for the trusted ask request.",
            OffsetDateTime.now(clock));
        return response(run);
      }

      List<AskEvidence> evidence = persistEvidence(run.getId(), matches);
      run.markGenerating();
      ModelRunResponse modelRun = modelService.createRun(modelRequest(run, validated, evidence));
      Optional<ModelOutputResponse> firstOutput =
          modelRun.outputs().stream().filter(output -> output.safeError() == null).findFirst();
      AskRunStatus terminalStatus = toAskStatus(modelRun.status(), firstOutput.isPresent());
      BigDecimal confidence =
          summaryCalculator.answerConfidence(
              firstOutput.map(ModelOutputResponse::confidence).orElse(null), evidence);
      run.complete(
          terminalStatus,
          firstOutput.map(ModelOutputResponse::safeSummary).orElse(null),
          confidence,
          modelRun.runId(),
          safeModelMessage(modelRun),
          OffsetDateTime.now(clock));
      return response(run);
    } catch (RuntimeException ex) {
      run.complete(
          AskRunStatus.FAILED,
          null,
          null,
          null,
          sanitizeFailure(ex.getMessage()),
          OffsetDateTime.now(clock));
      return response(run);
    }
  }

  /** Gets one trusted ask run by id. */
  @Transactional(readOnly = true)
  public AskRunResponse getRun(String runId) {
    AskRun run =
        askRunRepository
            .findById(runId)
            .orElseThrow(() -> new NotFoundException("Ask run was not found."));
    return response(run);
  }

  private AskRunResponse response(AskRun run) {
    return AskMapper.toResponse(run, askEvidenceRepository.findByAskRunIdOrderByCreatedAtAsc(run.getId()));
  }

  private List<AskEvidence> persistEvidence(String runId, List<VectorQueryMatchResponse> matches) {
    OffsetDateTime createdAt = OffsetDateTime.now(clock);
    List<AskEvidence> evidence =
        matches.stream()
            .map(
                match ->
                    AskEvidence.create(
                        "ask-ev-" + UUID.randomUUID(),
                        runId,
                        match.sourceChunkId(),
                        match.fileItemId(),
                        match.sourceFile(),
                        match.page(),
                        match.section(),
                        match.reviewStatus(),
                        match.confidence(),
                        match.vectorItemKey(),
                        match.score(),
                        createdAt))
            .toList();
    return askEvidenceRepository.saveAll(evidence);
  }

  private CreateModelRunRequest modelRequest(
      AskRun run, ValidatedAsk validated, List<AskEvidence> evidence) {
    List<ModelSourceReferenceRequest> references =
        evidence.stream()
            .map(
                item ->
                    new ModelSourceReferenceRequest(
                        ModelSourceReferenceType.SOURCE_CHUNK,
                        item.getSourceChunkId(),
                        safeLabel(item)))
            .toList();
    boolean configuredMode = "configured".equals(validated.mode());
    return new CreateModelRunRequest(
        configuredMode ? "deepseek" : null,
        configuredMode ? null : "deepseek-flash",
        ModelOperation.CHAT,
        "trusted-ask",
        validated.requestedBy(),
        validated.mode(),
        "ask-context:" + run.getId(),
        "Question: "
            + validated.question()
            + "\nAnswer the trusted ask question using only the referenced Atlas evidence.",
        references);
  }

  private String safeLabel(AskEvidence evidence) {
    String page = evidence.getPage() == null ? "" : " page " + evidence.getPage();
    return truncate(evidence.getSourceFile() + page, 160);
  }

  private List<VectorQueryMatchResponse> applyFilters(
      List<VectorQueryMatchResponse> matches, ValidatedAsk validated) {
    return matches.stream()
        .filter(match -> isAllowedByPolicy(validated.reviewPolicy(), match.reviewStatus()))
        .filter(match -> validated.fileItemIds().isEmpty() || validated.fileItemIds().contains(match.fileItemId()))
        .filter(match -> validated.sourceTypes().isEmpty() || validated.fileTypes().get(match.fileItemId()) != null)
        .filter(
            match ->
                validated.sourceTypes().isEmpty()
                    || validated.sourceTypes().contains(validated.fileTypes().get(match.fileItemId())))
        .limit(validated.limit())
        .toList();
  }

  private boolean isAllowedByPolicy(AskReviewPolicy reviewPolicy, ReviewStatus reviewStatus) {
    if (reviewPolicy == AskReviewPolicy.INCLUDE_REVIEW_REQUIRED) {
      return reviewStatus == ReviewStatus.APPROVED
          || reviewStatus == ReviewStatus.PUBLISHED
          || reviewStatus == ReviewStatus.REVIEW_REQUIRED;
    }
    return reviewStatus == ReviewStatus.APPROVED || reviewStatus == ReviewStatus.PUBLISHED;
  }

  private ValidatedAsk validate(String spaceId, CreateAskRequest request) {
    Map<String, String> fields = new LinkedHashMap<>();
    if (request == null) {
      throw new RequestValidationException(Map.of("body", "is required"));
    }
    String question = normalize(request.question());
    if (question == null) {
      fields.put("question", "is required");
    } else if (question.length() > MAX_QUESTION_LENGTH) {
      fields.put("question", "must be 500 characters or fewer");
    } else if (!isSafeText(question)) {
      fields.put("question", "must not include secrets, endpoints, or private paths");
    }
    String requestedBy = normalize(request.requestedBy());
    if (requestedBy == null) {
      fields.put("requestedBy", "is required");
    } else if (!isSafeText(requestedBy)) {
      fields.put("requestedBy", "must not include secrets, endpoints, or private paths");
    }
    int limit = request.limit() == null ? DEFAULT_LIMIT : request.limit();
    if (limit < 1 || limit > MAX_LIMIT) {
      fields.put("limit", "must be between 1 and 10");
    }
    String mode = normalize(request.mode()) == null ? "mock" : normalize(request.mode());
    if (!mode.equals("mock") && !mode.equals("configured")) {
      fields.put("mode", "must be mock or configured");
    }

    AskReviewPolicy reviewPolicy =
        request.reviewPolicy() == null ? AskReviewPolicy.APPROVED_ONLY : request.reviewPolicy();
    List<String> fileItemIds =
        request.filters() == null || request.filters().fileItemIds() == null
            ? List.of()
            : request.filters().fileItemIds().stream().map(this::normalize).filter(id -> id != null).toList();
    List<SourceType> sourceTypes =
        request.filters() == null || request.filters().sourceTypes() == null
            ? List.of()
            : request.filters().sourceTypes();
    Map<String, SourceType> fileTypes = loadFileTypes(spaceId);
    if (!new HashSet<>(fileTypes.keySet()).containsAll(fileItemIds)) {
      fields.put("filters.fileItemIds", "must belong to the requested Knowledge Space");
    }
    if (!fields.isEmpty()) {
      throw new RequestValidationException(fields);
    }
    return new ValidatedAsk(
        question,
        requestedBy,
        reviewPolicy,
        limit,
        mode,
        Set.copyOf(fileItemIds),
        Set.copyOf(sourceTypes),
        fileTypes);
  }

  private Map<String, SourceType> loadFileTypes(String spaceId) {
    List<String> batchIds = batchRepository.findBySpaceId(spaceId).stream().map(Batch::getId).toList();
    if (batchIds.isEmpty()) {
      return Map.of();
    }
    Map<String, SourceType> fileTypes = new LinkedHashMap<>();
    fileItemRepository
        .findByBatchIdIn(batchIds)
        .forEach((FileItem item) -> fileTypes.put(item.getId(), item.getSourceType()));
    return fileTypes;
  }

  private AskRunStatus toAskStatus(ModelRunStatus status, boolean hasOutput) {
    if (status == ModelRunStatus.SUCCEEDED && hasOutput) {
      return AskRunStatus.SUCCEEDED;
    }
    if (status == ModelRunStatus.PARTIAL_FAILED && hasOutput) {
      return AskRunStatus.PARTIAL_FAILED;
    }
    return AskRunStatus.FAILED;
  }

  private VectorReviewPolicy toVectorPolicy(AskReviewPolicy reviewPolicy) {
    return reviewPolicy == AskReviewPolicy.INCLUDE_REVIEW_REQUIRED
        ? VectorReviewPolicy.INCLUDE_REVIEW_REQUIRED
        : VectorReviewPolicy.APPROVED_ONLY;
  }

  private String safeModelMessage(ModelRunResponse modelRun) {
    return sanitizeFailure(modelRun.safeMessage() == null ? "Trusted ask completed." : modelRun.safeMessage());
  }

  private String sanitizeFailure(String message) {
    if (message == null || message.isBlank()) {
      return "Trusted ask failed safely.";
    }
    String safe = SECRET_PATTERN.matcher(message).replaceAll("[redacted]=");
    safe = URL_PATTERN.matcher(safe).replaceAll("[redacted-url]");
    safe = PATH_PATTERN.matcher(safe).replaceAll(" [redacted-path]");
    return truncate(safe, 240);
  }

  private boolean isSafeText(String value) {
    return !SECRET_PATTERN.matcher(value).find()
        && !URL_PATTERN.matcher(value).find()
        && !PATH_PATTERN.matcher(value).find();
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String truncate(String value, int maxLength) {
    if (value == null || value.length() <= maxLength) {
      return value;
    }
    return value.substring(0, maxLength);
  }

  private record ValidatedAsk(
      String question,
      String requestedBy,
      AskReviewPolicy reviewPolicy,
      int limit,
      String mode,
      Set<String> fileItemIds,
      Set<SourceType> sourceTypes,
      Map<String, SourceType> fileTypes) {}
}
