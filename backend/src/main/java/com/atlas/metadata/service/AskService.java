package com.atlas.metadata.service;

import com.atlas.metadata.domain.AskEvidence;
import com.atlas.metadata.domain.AskRun;
import com.atlas.metadata.domain.AskSession;
import com.atlas.metadata.domain.Batch;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.dto.AskRunResponse;
import com.atlas.metadata.dto.AskSessionDetailResponse;
import com.atlas.metadata.dto.AskSessionSummaryResponse;
import com.atlas.metadata.dto.CreateAskRequest;
import com.atlas.metadata.dto.CreateModelRunRequest;
import com.atlas.metadata.dto.ModelOutputResponse;
import com.atlas.metadata.dto.ModelRunResponse;
import com.atlas.metadata.dto.ModelSourceReferenceRequest;
import com.atlas.metadata.dto.ReviewAskAnswerRequest;
import com.atlas.metadata.dto.VectorQueryMatchResponse;
import com.atlas.metadata.dto.VectorQueryRequestDto;
import com.atlas.metadata.dto.VectorQueryResponse;
import com.atlas.metadata.dto.mapping.AskMapper;
import com.atlas.metadata.enums.AnswerReviewStatus;
import com.atlas.metadata.enums.AskCitationStatus;
import com.atlas.metadata.enums.AskReviewPolicy;
import com.atlas.metadata.enums.AskRunStatus;
import com.atlas.metadata.enums.AuditCategory;
import com.atlas.metadata.enums.AuditResult;
import com.atlas.metadata.enums.AuditSeverity;
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
import com.atlas.metadata.repository.AskSessionRepository;
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
  private static final int MAX_SESSION_TITLE_LENGTH = 120;
  private static final BigDecimal LOW_CONFIDENCE_THRESHOLD = new BigDecimal("0.800");
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
  private final AskSessionRepository askSessionRepository;
  private final AskSummaryCalculator summaryCalculator;
  private final AuditLogService auditLogService;
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
      AskSessionRepository askSessionRepository,
      AskSummaryCalculator summaryCalculator,
      AuditLogService auditLogService) {
    this(
        spaceRepository,
        batchRepository,
        fileItemRepository,
        vectorService,
        modelService,
        askRunRepository,
        askEvidenceRepository,
        askSessionRepository,
        summaryCalculator,
        auditLogService,
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
    this(
        spaceRepository,
        batchRepository,
        fileItemRepository,
        vectorService,
        modelService,
        askRunRepository,
        askEvidenceRepository,
        null,
        summaryCalculator,
        null,
        clock);
  }

  AskService(
      SpaceRepository spaceRepository,
      BatchRepository batchRepository,
      FileItemRepository fileItemRepository,
      VectorService vectorService,
      ModelService modelService,
      AskRunRepository askRunRepository,
      AskEvidenceRepository askEvidenceRepository,
      AskSessionRepository askSessionRepository,
      AskSummaryCalculator summaryCalculator,
      AuditLogService auditLogService,
      Clock clock) {
    this.spaceRepository = spaceRepository;
    this.batchRepository = batchRepository;
    this.fileItemRepository = fileItemRepository;
    this.vectorService = vectorService;
    this.modelService = modelService;
    this.askRunRepository = askRunRepository;
    this.askEvidenceRepository = askEvidenceRepository;
    this.askSessionRepository = askSessionRepository;
    this.summaryCalculator = summaryCalculator;
    this.auditLogService = auditLogService;
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
    AskSession session = resolveSession(spaceId, validated, now);
    AskRun run =
        askRunRepository.save(
            AskRun.create(
                "ask-" + UUID.randomUUID(),
                spaceId,
                session.getId(),
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
        auditAsk(run, validated, AuditResult.SAFE_NOT_FOUND, AuditSeverity.NOTICE, 0);
        touchSession(session);
        return response(run, session);
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
      auditAsk(run, validated, AuditResult.SUCCEEDED, AuditSeverity.INFO, evidence.size());
      touchSession(session);
      return response(run, session);
    } catch (RuntimeException ex) {
      run.complete(
          AskRunStatus.FAILED,
          null,
          null,
          null,
          sanitizeFailure(ex.getMessage()),
          OffsetDateTime.now(clock));
      auditAsk(run, validated, AuditResult.FAILED, AuditSeverity.WARNING, 0);
      touchSession(session);
      return response(run, session);
    }
  }

  private void auditAsk(
      AskRun run,
      ValidatedAsk validated,
      AuditResult result,
      AuditSeverity severity,
      int evidenceCount) {
    if (auditLogService == null) {
      return;
    }
    auditLogService.recordIfEnabled(
        new AuditLogService.CreateAuditEventCommand(
            validated.requestedBy(),
            validated.requestedBy(),
            "TRUSTED_ASK_RUN",
            AuditCategory.ASK,
            result,
            severity,
            run.getSpaceId(),
            "ask_run",
            run.getId(),
            null,
            run.getSafeMessage() == null ? "Trusted Ask run completed." : run.getSafeMessage(),
            Map.of(
                "status", run.getStatus().name(),
                "reviewPolicy", validated.reviewPolicy().name(),
                "mode", validated.mode(),
                "evidenceCount", evidenceCount)));
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

  /** Updates reviewer-safe answer governance metadata for one trusted ask run. */
  @Transactional
  public AskRunResponse reviewAnswer(String spaceId, String runId, ReviewAskAnswerRequest request) {
    AskRun run =
        askRunRepository
            .findById(runId)
            .filter(candidate -> candidate.getSpaceId().equals(spaceId))
            .orElseThrow(() -> new NotFoundException("Ask run was not found."));
    List<AskEvidence> evidence = askEvidenceRepository.findByAskRunIdOrderByCreatedAtAsc(run.getId());
    ValidatedAnswerReview review = validateReview(run, evidence, request);
    run.reviewAnswer(review.status(), review.reviewer(), review.reason(), OffsetDateTime.now(clock));
    askRunRepository.save(run);
    return response(run);
  }

  private AskRunResponse response(AskRun run) {
    AskSession session = findSession(run.getSessionId()).orElse(null);
    return response(run, session);
  }

  private AskRunResponse response(AskRun run, AskSession session) {
    return AskMapper.toResponse(
        run, session, askEvidenceRepository.findByAskRunIdOrderByCreatedAtAsc(run.getId()));
  }

  /** Lists recent Trusted Ask sessions for a Knowledge Space. */
  @Transactional(readOnly = true)
  public List<AskSessionSummaryResponse> listSessions(String spaceId) {
    if (!spaceRepository.existsById(spaceId)) {
      throw new NotFoundException("Knowledge Space was not found.");
    }
    return requireSessionRepository().findTop20BySpaceIdOrderByUpdatedAtDesc(spaceId).stream()
        .map(this::sessionSummary)
        .toList();
  }

  /** Gets one Trusted Ask session with ordered answer history. */
  @Transactional(readOnly = true)
  public AskSessionDetailResponse getSession(String sessionId) {
    AskSession session =
        requireSessionRepository()
            .findById(sessionId)
            .orElseThrow(() -> new NotFoundException("Ask session was not found."));
    List<AskRunResponse> runs =
        askRunRepository.findBySessionIdOrderByCreatedAtAsc(session.getId()).stream()
            .map(run -> response(run, session))
            .toList();
    return new AskSessionDetailResponse(
        session.getId(),
        session.getSpaceId(),
        session.getTitle(),
        session.getCreatedBy(),
        session.getCreatedAt(),
        session.getUpdatedAt(),
        runs);
  }

  private List<AskEvidence> persistEvidence(String runId, List<VectorQueryMatchResponse> matches) {
    OffsetDateTime createdAt = OffsetDateTime.now(clock);
    List<AskEvidence> evidence =
        matches.stream()
            .map(
                match -> {
                  String evidenceId = "ask-ev-" + UUID.randomUUID();
                  CitationFields citation = citationFields(evidenceId, match);
                  return AskEvidence.create(
                        evidenceId,
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
                        citation.citationId(),
                        citation.evidenceLabel(),
                        citation.sourceLocator(),
                        citation.citationStatus(),
                        citation.reviewEligible(),
                        citation.excludedReason(),
                        createdAt);
                })
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
        configuredMode ? configuredModelProvider() : null,
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

  private String configuredModelProvider() {
    String provider = System.getenv().getOrDefault("ATLAS_MODEL_PROVIDER", "deepseek");
    String normalized = provider.trim().toLowerCase().replace('_', '-');
    return normalized.isBlank() ? "deepseek" : normalized;
  }

  private String safeLabel(AskEvidence evidence) {
    String label =
        evidence.getEvidenceLabel() == null
            ? safeLabel(evidence.getSourceFile(), evidence.getPage())
            : evidence.getEvidenceLabel();
    return truncate(label, 160);
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

  private ValidatedAnswerReview validateReview(
      AskRun run, List<AskEvidence> evidence, ReviewAskAnswerRequest request) {
    Map<String, String> fields = new LinkedHashMap<>();
    if (request == null) {
      throw new RequestValidationException(Map.of("body", "is required"));
    }
    AnswerReviewStatus status = request.status();
    if (status == null) {
      fields.put("status", "is required");
    }
    String reviewer = normalize(request.reviewer());
    if (reviewer == null) {
      fields.put("reviewer", "is required");
    } else if (!isSafeText(reviewer)) {
      fields.put("reviewer", "must not include secrets, endpoints, or private paths");
    }
    String reason = normalize(request.reason());
    if ((status == AnswerReviewStatus.REJECTED || status == AnswerReviewStatus.NEEDS_REVISION)
        && reason == null) {
      fields.put("reason", "is required for rejected or needs-revision answers");
    } else if (reason != null && !isSafeText(reason)) {
      fields.put("reason", "must not include secrets, endpoints, or private paths");
    }
    if (status == AnswerReviewStatus.APPROVED && !canApprove(run, evidence)) {
      fields.put("status", "approved answers require successful answer text and eligible evidence");
    }
    if (!fields.isEmpty()) {
      throw new RequestValidationException(fields);
    }
    return new ValidatedAnswerReview(status, reviewer, reason);
  }

  private boolean canApprove(AskRun run, List<AskEvidence> evidence) {
    boolean terminalWithAnswer =
        (run.getStatus() == AskRunStatus.SUCCEEDED || run.getStatus() == AskRunStatus.PARTIAL_FAILED)
            && run.getAnswer() != null
            && !run.getAnswer().isBlank();
    return terminalWithAnswer && evidence.stream().anyMatch(AskEvidence::isReviewEligible);
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
    String sessionId = normalize(request.sessionId());
    if (sessionId != null && !isSafeText(sessionId)) {
      fields.put("sessionId", "must not include secrets, endpoints, or private paths");
    }
    String sessionTitle = normalize(request.sessionTitle());
    if (sessionTitle != null && sessionTitle.length() > MAX_SESSION_TITLE_LENGTH) {
      fields.put("sessionTitle", "must be 120 characters or fewer");
    } else if (sessionTitle != null && !isSafeText(sessionTitle)) {
      fields.put("sessionTitle", "must not include secrets, endpoints, or private paths");
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
        sessionId,
        sessionTitle,
        Set.copyOf(fileItemIds),
        Set.copyOf(sourceTypes),
        fileTypes);
  }

  private AskSession resolveSession(String spaceId, ValidatedAsk validated, OffsetDateTime now) {
    if (validated.sessionId() != null) {
      AskSession session =
          requireSessionRepository()
              .findById(validated.sessionId())
              .orElseThrow(() -> new NotFoundException("Ask session was not found."));
      if (!spaceId.equals(session.getSpaceId())) {
        throw new RequestValidationException(Map.of("sessionId", "must belong to the requested Knowledge Space"));
      }
      return session;
    }
    AskSession session =
        AskSession.create(
            "ask-session-" + UUID.randomUUID(),
            spaceId,
            defaultSessionTitle(validated),
            validated.requestedBy(),
            now);
    return askSessionRepository == null ? session : askSessionRepository.save(session);
  }

  private String defaultSessionTitle(ValidatedAsk validated) {
    String title = validated.sessionTitle() == null ? validated.question() : validated.sessionTitle();
    return truncate(title, MAX_SESSION_TITLE_LENGTH);
  }

  private void touchSession(AskSession session) {
    session.touch(OffsetDateTime.now(clock));
    if (askSessionRepository != null) {
      askSessionRepository.save(session);
    }
  }

  private Optional<AskSession> findSession(String sessionId) {
    if (askSessionRepository == null || sessionId == null) {
      return Optional.empty();
    }
    return askSessionRepository.findById(sessionId);
  }

  private AskSessionRepository requireSessionRepository() {
    if (askSessionRepository == null) {
      throw new IllegalStateException("Ask session repository is required.");
    }
    return askSessionRepository;
  }

  private AskSessionSummaryResponse sessionSummary(AskSession session) {
    List<AskRun> runs = askRunRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());
    AskRun latest = runs.isEmpty() ? null : runs.get(runs.size() - 1);
    return new AskSessionSummaryResponse(
        session.getId(),
        session.getSpaceId(),
        session.getTitle(),
        session.getCreatedBy(),
        runs.size(),
        latest == null ? null : latest.getStatus(),
        latest == null ? null : latest.getAnswerReviewStatus(),
        session.getCreatedAt(),
        session.getUpdatedAt());
  }

  private CitationFields citationFields(String evidenceId, VectorQueryMatchResponse match) {
    boolean hasTrace = normalize(match.sourceChunkId()) != null && normalize(match.fileItemId()) != null;
    AskCitationStatus status;
    String excludedReason = null;
    if (!hasTrace) {
      status = AskCitationStatus.MISSING_SOURCE_TRACE;
      excludedReason = "Source trace is missing.";
    } else if (match.confidence() != null && match.confidence().compareTo(LOW_CONFIDENCE_THRESHOLD) < 0) {
      status = AskCitationStatus.LOW_CONFIDENCE;
      excludedReason = "Evidence confidence is low.";
    } else if (match.reviewStatus() == ReviewStatus.REVIEW_REQUIRED) {
      status = AskCitationStatus.REVIEW_REQUIRED;
      excludedReason = "Evidence requires review.";
    } else {
      status = AskCitationStatus.ELIGIBLE;
    }
    return new CitationFields(
        "ask-cite-" + evidenceId.substring("ask-ev-".length()),
        safeLabel(match.sourceFile(), match.page()),
        sourceLocator(match),
        status,
        status == AskCitationStatus.ELIGIBLE,
        excludedReason);
  }

  private String safeLabel(String sourceFile, Integer page) {
    String safeSource = sanitizeFailure(sourceFile == null ? "source unavailable" : sourceFile);
    String pageLabel = page == null ? "" : " page " + page;
    return truncate(safeSource + pageLabel, 160);
  }

  private String sourceLocator(VectorQueryMatchResponse match) {
    String page = match.page() == null ? "page n/a" : "page " + match.page();
    String section = normalize(match.section()) == null ? "section n/a" : sanitizeFailure(match.section());
    return truncate(page + " / " + section + " / chunk " + match.sourceChunkId(), 200);
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
      String sessionId,
      String sessionTitle,
      Set<String> fileItemIds,
      Set<SourceType> sourceTypes,
      Map<String, SourceType> fileTypes) {}

  private record CitationFields(
      String citationId,
      String evidenceLabel,
      String sourceLocator,
      AskCitationStatus citationStatus,
      boolean reviewEligible,
      String excludedReason) {}

  private record ValidatedAnswerReview(AnswerReviewStatus status, String reviewer, String reason) {}
}
