package com.atlas.metadata.service;

import com.atlas.metadata.adapter.ConnectorAdapter;
import com.atlas.metadata.adapter.ConnectorSyncRequest;
import com.atlas.metadata.adapter.ConnectorSyncResult;
import com.atlas.metadata.domain.ConnectorDefinition;
import com.atlas.metadata.domain.ConnectorOutputArtifact;
import com.atlas.metadata.domain.ConnectorSyncItem;
import com.atlas.metadata.domain.ConnectorSyncJob;
import com.atlas.metadata.domain.ConnectorSyncRun;
import com.atlas.metadata.dto.ConnectorDefinitionResponse;
import com.atlas.metadata.dto.ConnectorSyncItemResponse;
import com.atlas.metadata.dto.ConnectorSyncRunResponse;
import com.atlas.metadata.dto.CreateConnectorSyncJobRequest;
import com.atlas.metadata.dto.mapping.ConnectorSyncMapper;
import com.atlas.metadata.enums.ConnectorItemStatus;
import com.atlas.metadata.enums.ConnectorRunStatus;
import com.atlas.metadata.enums.ConnectorSafeErrorCategory;
import com.atlas.metadata.exception.NotFoundException;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.exception.SafeErrorSanitizer;
import com.atlas.metadata.repository.ConnectorDefinitionRepository;
import com.atlas.metadata.repository.ConnectorOutputArtifactRepository;
import com.atlas.metadata.repository.ConnectorSyncItemRepository;
import com.atlas.metadata.repository.ConnectorSyncJobRepository;
import com.atlas.metadata.repository.ConnectorSyncRunRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for connector sync v0. */
@Service
public class ConnectorSyncService {

  private final SpaceService spaceService;
  private final ConnectorDefinitionRepository definitionRepository;
  private final ConnectorSyncJobRepository jobRepository;
  private final ConnectorSyncRunRepository runRepository;
  private final ConnectorSyncItemRepository itemRepository;
  private final ConnectorOutputArtifactRepository artifactRepository;
  private final ConnectorAdapterRegistry adapterRegistry;
  private final SafeErrorSanitizer safeErrorSanitizer;
  private final Clock clock;

  /** Creates the service. */
  @Autowired
  public ConnectorSyncService(
      SpaceService spaceService,
      ConnectorDefinitionRepository definitionRepository,
      ConnectorSyncJobRepository jobRepository,
      ConnectorSyncRunRepository runRepository,
      ConnectorSyncItemRepository itemRepository,
      ConnectorOutputArtifactRepository artifactRepository,
      ConnectorAdapterRegistry adapterRegistry,
      SafeErrorSanitizer safeErrorSanitizer) {
    this(
        spaceService,
        definitionRepository,
        jobRepository,
        runRepository,
        itemRepository,
        artifactRepository,
        adapterRegistry,
        safeErrorSanitizer,
        Clock.systemUTC());
  }

  ConnectorSyncService(
      SpaceService spaceService,
      ConnectorDefinitionRepository definitionRepository,
      ConnectorSyncJobRepository jobRepository,
      ConnectorSyncRunRepository runRepository,
      ConnectorSyncItemRepository itemRepository,
      ConnectorOutputArtifactRepository artifactRepository,
      ConnectorAdapterRegistry adapterRegistry,
      SafeErrorSanitizer safeErrorSanitizer,
      Clock clock) {
    this.spaceService = spaceService;
    this.definitionRepository = definitionRepository;
    this.jobRepository = jobRepository;
    this.runRepository = runRepository;
    this.itemRepository = itemRepository;
    this.artifactRepository = artifactRepository;
    this.adapterRegistry = adapterRegistry;
    this.safeErrorSanitizer = safeErrorSanitizer;
    this.clock = clock;
  }

  /** Lists safe connector definitions. */
  @Transactional(readOnly = true)
  public List<ConnectorDefinitionResponse> listDefinitions() {
    return definitionRepository.findAll().stream()
        .map(ConnectorSyncMapper::toDefinitionResponse)
        .toList();
  }

  /** Creates and executes one deterministic local connector sync run. */
  @Transactional
  public ConnectorSyncRunResponse createSyncJob(
      String spaceId, CreateConnectorSyncJobRequest request) {
    spaceService.findSpace(spaceId);
    ConnectorDefinition definition = findDefinitionByKey(request.connectorKey());
    ConnectorAdapter adapter = adapterRegistry.resolve(definition.getConnectorKey());
    OffsetDateTime now = OffsetDateTime.now(clock);
    String jobId = id("connector-job", now);
    String runId = id("connector-run", now);
    ConnectorSyncJob job =
        jobRepository.save(
            ConnectorSyncJob.create(
                jobId,
                spaceId,
                definition.getId(),
                safeText(defaultText(request.requestedBy(), "connector-user")),
                safeText(defaultText(request.sourceScope(), "sample-fixture")),
                now));
    ConnectorSyncRun run =
        runRepository.save(ConnectorSyncRun.create(runId, job.getId(), definition.getId(), now));
    run.markRunning();
    runRepository.save(run);

    ConnectorSyncResult result;
    try {
      result =
          adapter.sync(
              new ConnectorSyncRequest(
                  run.getId(), spaceId, definition.getConnectorKey(), job.getSourceScope()));
    } catch (RuntimeException ex) {
      String safeMessage = safeText(ex.getMessage());
      run.complete(ConnectorRunStatus.FAILED, 0, 0, 0, OffsetDateTime.now(clock), safeMessage);
      job.complete(ConnectorRunStatus.FAILED, safeMessage);
      jobRepository.save(job);
      return response(runRepository.save(run), job, definition);
    }

    List<ConnectorSyncItem> items = persistItems(run, result);
    int reviewRequiredCount =
        (int) items.stream().filter(item -> item.getItemStatus() == ConnectorItemStatus.REVIEW_REQUIRED).count();
    int failedCount =
        (int) items.stream().filter(item -> item.getItemStatus() == ConnectorItemStatus.FAILED).count();
    ConnectorRunStatus terminalStatus = terminalStatus(items.size(), reviewRequiredCount, failedCount);
    String safeMessage = safeText(result.safeMessage());
    run.complete(
        terminalStatus,
        items.size(),
        reviewRequiredCount,
        failedCount,
        OffsetDateTime.now(clock),
        safeMessage);
    job.complete(terminalStatus, safeMessage);
    jobRepository.save(job);
    return response(runRepository.save(run), job, definition);
  }

  /** Gets one connector sync run. */
  @Transactional(readOnly = true)
  public ConnectorSyncRunResponse getRun(String runId) {
    ConnectorSyncRun run = findRun(runId);
    ConnectorSyncJob job = findJob(run.getJobId());
    ConnectorDefinition definition = findDefinition(job.getConnectorDefinitionId());
    return response(run, job, definition);
  }

  /** Lists items for one connector sync run. */
  @Transactional(readOnly = true)
  public List<ConnectorSyncItemResponse> listItems(String runId) {
    findRun(runId);
    List<ConnectorSyncItem> items = itemRepository.findByRunIdOrderByIdAsc(runId);
    Map<String, List<ConnectorOutputArtifact>> artifactsByItemId =
        artifactRepository.findByItemIdIn(items.stream().map(ConnectorSyncItem::getId).toList()).stream()
            .collect(Collectors.groupingBy(ConnectorOutputArtifact::getItemId));
    return items.stream()
        .map(item -> ConnectorSyncMapper.toItemResponse(item, artifactsByItemId))
        .toList();
  }

  private List<ConnectorSyncItem> persistItems(ConnectorSyncRun run, ConnectorSyncResult result) {
    if (!run.getConnectorDefinitionId().isBlank()
        && (result.items() == null || result.items().isEmpty())) {
      throw new RequestValidationException(Map.of("items", "connector result must include items"));
    }
    List<ConnectorSyncResult.Item> resultItems = result.items();
    return IntStream.range(0, resultItems.size())
        .mapToObj(index -> persistItem(run, resultItems.get(index), index + 1))
        .toList();
  }

  private ConnectorSyncItem persistItem(
      ConnectorSyncRun run, ConnectorSyncResult.Item result, int itemNumber) {
    OffsetDateTime now = OffsetDateTime.now(clock);
    ConnectorSafeErrorCategory safeCategory =
        result.safeErrorCategory() == null ? ConnectorSafeErrorCategory.NONE : result.safeErrorCategory();
    boolean reviewEligible = safeCategory == ConnectorSafeErrorCategory.NONE && result.reviewRequired();
    ConnectorItemStatus status =
        safeCategory == ConnectorSafeErrorCategory.NONE
            ? (reviewEligible ? ConnectorItemStatus.REVIEW_REQUIRED : ConnectorItemStatus.OUTPUT_CREATED)
            : ConnectorItemStatus.FAILED;
    String itemId = run.getId() + "-item-" + String.format("%03d", itemNumber);
    ConnectorSyncItem item =
        itemRepository.save(
            ConnectorSyncItem.create(
                itemId,
                run.getId(),
                safeText(result.externalId()),
                safeText(result.title()),
                status,
                safeText(result.sourceReference()),
                sanitizeMap(result.sourceTrace()),
                sanitizeMap(result.provenance()),
                scale(result.confidence()),
                reviewEligible,
                safeCategory,
                safeText(result.safeErrorMessage()),
                now));
    if (status == ConnectorItemStatus.REVIEW_REQUIRED && result.outputTitle() != null) {
      artifactRepository.save(
          ConnectorOutputArtifact.reviewRequired(
              item.getId() + "-artifact-001",
              item.getId(),
              safeText(result.outputTitle()),
              safeText(result.targetPath()),
              item.getSourceTrace(),
              item.getProvenance(),
              now));
    }
    return item;
  }

  private ConnectorRunStatus terminalStatus(
      int itemCount, int reviewRequiredCount, int failedCount) {
    if (itemCount == 0 || failedCount == itemCount) {
      return ConnectorRunStatus.FAILED;
    }
    if (reviewRequiredCount > 0) {
      return ConnectorRunStatus.REVIEW_REQUIRED;
    }
    return ConnectorRunStatus.COMPLETED;
  }

  private ConnectorSyncRunResponse response(
      ConnectorSyncRun run, ConnectorSyncJob job, ConnectorDefinition definition) {
    return ConnectorSyncMapper.toRunResponse(run, job, definition);
  }

  private ConnectorDefinition findDefinitionByKey(String connectorKey) {
    return definitionRepository
        .findByConnectorKey(connectorKey)
        .orElseThrow(() -> new NotFoundException("Connector definition not found."));
  }

  private ConnectorDefinition findDefinition(String definitionId) {
    return definitionRepository
        .findById(definitionId)
        .orElseThrow(() -> new NotFoundException("Connector definition not found."));
  }

  private ConnectorSyncJob findJob(String jobId) {
    return jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("Connector job not found."));
  }

  private ConnectorSyncRun findRun(String runId) {
    return runRepository.findById(runId).orElseThrow(() -> new NotFoundException("Connector run not found."));
  }

  private String id(String prefix, OffsetDateTime now) {
    return prefix
        + "-"
        + now.format(DateTimeFormatter.ISO_LOCAL_DATE)
        + "-"
        + UUID.randomUUID().toString().substring(0, 8);
  }

  private String defaultText(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value;
  }

  private String safeText(String value) {
    return safeErrorSanitizer.sanitize(value);
  }

  private Map<String, String> sanitizeMap(Map<String, String> values) {
    if (values == null || values.isEmpty()) {
      return Map.of();
    }
    return values.entrySet().stream()
        .collect(Collectors.toUnmodifiableMap(entry -> safeText(entry.getKey()), entry -> safeText(entry.getValue())));
  }

  private BigDecimal scale(BigDecimal value) {
    return value == null ? null : value.setScale(3, RoundingMode.HALF_UP);
  }
}
