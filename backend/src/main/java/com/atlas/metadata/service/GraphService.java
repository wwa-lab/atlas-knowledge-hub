package com.atlas.metadata.service;

import com.atlas.metadata.adapter.GraphProjectionAdapter;
import com.atlas.metadata.adapter.GraphProjectionAdapter.GraphSourceDescriptor;
import com.atlas.metadata.domain.Batch;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.GraphAuditRecord;
import com.atlas.metadata.domain.GraphEdge;
import com.atlas.metadata.domain.GraphNode;
import com.atlas.metadata.domain.GraphProjectionItem;
import com.atlas.metadata.domain.GraphProjectionRun;
import com.atlas.metadata.domain.ReviewRecord;
import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.dto.CreateGraphEdgeReviewRequest;
import com.atlas.metadata.dto.CreateGraphProjectionRunRequest;
import com.atlas.metadata.dto.GraphEdgeResponse;
import com.atlas.metadata.dto.GraphEvidenceReferenceResponse;
import com.atlas.metadata.dto.GraphNodeDetailResponse;
import com.atlas.metadata.dto.GraphNodeResponse;
import com.atlas.metadata.dto.GraphProjectionItemResponse;
import com.atlas.metadata.dto.GraphProjectionRunResponse;
import com.atlas.metadata.dto.GraphSummaryResponse;
import com.atlas.metadata.dto.GraphViewResponse;
import com.atlas.metadata.dto.ReviewResponse;
import com.atlas.metadata.dto.mapping.ReviewMapper;
import com.atlas.metadata.enums.GraphEdgeType;
import com.atlas.metadata.enums.GraphNodeType;
import com.atlas.metadata.enums.GraphProjectionItemStatus;
import com.atlas.metadata.enums.GraphProjectionStatus;
import com.atlas.metadata.enums.AuditCategory;
import com.atlas.metadata.enums.AuditResult;
import com.atlas.metadata.enums.AuditSeverity;
import com.atlas.metadata.enums.ReviewAction;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.exception.NotFoundException;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.repository.BatchRepository;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.GraphAuditRecordRepository;
import com.atlas.metadata.repository.GraphEdgeRepository;
import com.atlas.metadata.repository.GraphNodeRepository;
import com.atlas.metadata.repository.GraphProjectionItemRepository;
import com.atlas.metadata.repository.GraphProjectionRunRepository;
import com.atlas.metadata.repository.ReviewRecordRepository;
import com.atlas.metadata.repository.SourceChunkRepository;
import com.atlas.metadata.repository.SpaceRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for trusted knowledge graph projection and query. */
@Service
public class GraphService {

  private static final int DEFAULT_LIMIT = 200;
  private static final int MAX_LIMIT = 500;
  private static final List<ReviewStatus> TRUSTED_STATUSES =
      List.of(ReviewStatus.APPROVED, ReviewStatus.PUBLISHED);

  private final SpaceRepository spaceRepository;
  private final BatchRepository batchRepository;
  private final FileItemRepository fileItemRepository;
  private final SourceChunkRepository sourceChunkRepository;
  private final GraphNodeRepository graphNodeRepository;
  private final GraphEdgeRepository graphEdgeRepository;
  private final GraphProjectionRunRepository graphProjectionRunRepository;
  private final GraphProjectionItemRepository graphProjectionItemRepository;
  private final GraphAuditRecordRepository graphAuditRecordRepository;
  private final ReviewRecordRepository reviewRecordRepository;
  private final GraphProjectionAdapter projectionAdapter;
  private final AuditLogService auditLogService;
  private final Clock clock;

  @Autowired
  public GraphService(
      SpaceRepository spaceRepository,
      BatchRepository batchRepository,
      FileItemRepository fileItemRepository,
      SourceChunkRepository sourceChunkRepository,
      GraphNodeRepository graphNodeRepository,
      GraphEdgeRepository graphEdgeRepository,
      GraphProjectionRunRepository graphProjectionRunRepository,
      GraphProjectionItemRepository graphProjectionItemRepository,
      GraphAuditRecordRepository graphAuditRecordRepository,
      ReviewRecordRepository reviewRecordRepository,
      GraphProjectionAdapter projectionAdapter,
      AuditLogService auditLogService) {
    this(
        spaceRepository,
        batchRepository,
        fileItemRepository,
        sourceChunkRepository,
        graphNodeRepository,
        graphEdgeRepository,
        graphProjectionRunRepository,
        graphProjectionItemRepository,
        graphAuditRecordRepository,
        reviewRecordRepository,
        projectionAdapter,
        auditLogService,
        Clock.systemUTC());
  }

  public GraphService(
      SpaceRepository spaceRepository,
      BatchRepository batchRepository,
      FileItemRepository fileItemRepository,
      SourceChunkRepository sourceChunkRepository,
      GraphNodeRepository graphNodeRepository,
      GraphEdgeRepository graphEdgeRepository,
      GraphProjectionRunRepository graphProjectionRunRepository,
      GraphProjectionItemRepository graphProjectionItemRepository,
      GraphAuditRecordRepository graphAuditRecordRepository,
      GraphProjectionAdapter projectionAdapter,
      Clock clock) {
    this(
        spaceRepository,
        batchRepository,
        fileItemRepository,
        sourceChunkRepository,
        graphNodeRepository,
        graphEdgeRepository,
        graphProjectionRunRepository,
        graphProjectionItemRepository,
        graphAuditRecordRepository,
        null,
        projectionAdapter,
        null,
        clock);
  }

  GraphService(
      SpaceRepository spaceRepository,
      BatchRepository batchRepository,
      FileItemRepository fileItemRepository,
      SourceChunkRepository sourceChunkRepository,
      GraphNodeRepository graphNodeRepository,
      GraphEdgeRepository graphEdgeRepository,
      GraphProjectionRunRepository graphProjectionRunRepository,
      GraphProjectionItemRepository graphProjectionItemRepository,
      GraphAuditRecordRepository graphAuditRecordRepository,
      ReviewRecordRepository reviewRecordRepository,
      GraphProjectionAdapter projectionAdapter,
      AuditLogService auditLogService,
      Clock clock) {
    this.spaceRepository = spaceRepository;
    this.batchRepository = batchRepository;
    this.fileItemRepository = fileItemRepository;
    this.sourceChunkRepository = sourceChunkRepository;
    this.graphNodeRepository = graphNodeRepository;
    this.graphEdgeRepository = graphEdgeRepository;
    this.graphProjectionRunRepository = graphProjectionRunRepository;
    this.graphProjectionItemRepository = graphProjectionItemRepository;
    this.graphAuditRecordRepository = graphAuditRecordRepository;
    this.reviewRecordRepository = reviewRecordRepository;
    this.projectionAdapter = projectionAdapter;
    this.auditLogService = auditLogService;
    this.clock = clock;
  }

  /** Creates and executes an approved-only graph projection run. */
  @Transactional
  public GraphProjectionRunResponse createProjectionRun(
      String spaceId, CreateGraphProjectionRunRequest request) {
    validateSpace(spaceId);
    String adapterId = request.adapterId() == null || request.adapterId().isBlank()
        ? projectionAdapter.adapterId()
        : request.adapterId();
    if (!projectionAdapter.adapterId().equals(adapterId)) {
      throw new RequestValidationException(Map.of("adapterId", "must reference an available graph adapter"));
    }
    String scope = request.scope() == null || request.scope().isBlank() ? "APPROVED_ONLY" : request.scope();
    if (!"APPROVED_ONLY".equals(scope)) {
      throw new RequestValidationException(Map.of("scope", "must be APPROVED_ONLY"));
    }
    OffsetDateTime now = OffsetDateTime.now(clock);
    GraphProjectionRun run =
        graphProjectionRunRepository.save(
            GraphProjectionRun.create(runId(now), spaceId, adapterId, scope, "system", now));
    run.markRunning();
    graphProjectionRunRepository.save(run);

    List<SourceChunk> chunks = targetChunks(spaceId);
    List<SourceChunk> eligible =
        chunks.stream()
            .filter(chunk -> TRUSTED_STATUSES.contains(chunk.getReviewStatus()))
            .filter(this::hasSourceTrace)
            .toList();
    List<GraphProjectionItem> skipped =
        chunks.stream()
            .filter(chunk -> !TRUSTED_STATUSES.contains(chunk.getReviewStatus()) || !hasSourceTrace(chunk))
            .map(
                chunk ->
                    saveItem(
                        run,
                        "source_chunk",
                        chunk.getId(),
                        "skip",
                        null,
                        GraphProjectionItemStatus.SKIPPED,
                        skipReason(chunk)))
            .toList();

    var result =
        projectionAdapter.project(
            new GraphProjectionAdapter.GraphProjectionRequest(
                spaceId,
                eligible.stream()
                    .map(
                        chunk ->
                            new GraphSourceDescriptor(
                                chunk.getId(),
                                chunk.getFileItemId(),
                                chunk.getSourceFile(),
                                chunk.getPage(),
                                chunk.getSection(),
                                chunk.getConfidence(),
                                chunk.getReviewStatus()))
                    .toList()));

    int created = 0;
    for (var projected : result.nodes()) {
      GraphNode node =
          GraphNode.create(
              projected.id(),
              spaceId,
              projected.label(),
              projected.type(),
              projected.reviewStatus(),
              toArray(projected.evidenceChunkIds()),
              null,
              projected.confidence(),
              OffsetDateTime.now(clock));
      graphNodeRepository.save(node);
      saveItem(
          run,
          "source_chunk",
          firstOrFallback(projected.evidenceChunkIds(), projected.id()),
          "node",
          projected.id(),
          GraphProjectionItemStatus.CREATED,
          null);
      created++;
    }
    for (var projected : result.edges()) {
      if (projected.evidenceChunkIds() == null || projected.evidenceChunkIds().isEmpty()) {
        saveItem(
            run,
            "candidate_edge",
            projected.id(),
            "skip",
            null,
            GraphProjectionItemStatus.SKIPPED,
            "NO_EVIDENCE");
        continue;
      }
      GraphEdge edge =
          GraphEdge.create(
              projected.id(),
              spaceId,
              projected.sourceNodeId(),
              projected.targetNodeId(),
              projected.type(),
              toArray(projected.evidenceChunkIds()),
              null,
              projected.confidence(),
              projected.reviewStatus(),
              OffsetDateTime.now(clock));
      graphEdgeRepository.save(edge);
      saveItem(
          run,
          "source_chunk",
          firstOrFallback(projected.evidenceChunkIds(), projected.id()),
          "edge",
          projected.id(),
          GraphProjectionItemStatus.CREATED,
          null);
      created++;
    }

    int skippedCount = skipped.size();
    GraphProjectionStatus status =
        skippedCount > 0 ? GraphProjectionStatus.PARTIAL_FAILED : GraphProjectionStatus.SUCCEEDED;
    run.complete(status, created, 0, skippedCount, 0, OffsetDateTime.now(clock), "Graph projection completed.");
    graphProjectionRunRepository.save(run);
    graphAuditRecordRepository.save(
        GraphAuditRecord.create(
            auditId(),
            spaceId,
            "system",
            "PROJECTION_RUN",
            "projection_run",
            run.getId(),
            "Created graph projection run with approved-only evidence.",
            OffsetDateTime.now(clock)));
    audit(
        "GRAPH_PROJECTION_RUN",
        spaceId,
        "system",
        AuditResult.SUCCEEDED,
        AuditSeverity.NOTICE,
        "projection_run",
        run.getId(),
        "Created graph projection run with approved-only evidence.",
        Map.of(
            "status", run.getStatus().name(),
            "createdCount", run.getCreatedCount(),
            "skippedCount", run.getSkippedCount()));
    return projectionRunResponse(run);
  }

  /** Returns a bounded trusted graph view. */
  @Transactional(readOnly = true)
  public GraphViewResponse graphView(
      String spaceId,
      String query,
      GraphNodeType nodeType,
      GraphEdgeType edgeType,
      ReviewStatus reviewStatus,
      Boolean evidenceOnly,
      Integer limit) {
    validateSpace(spaceId);
    int effectiveLimit = effectiveLimit(limit);
    List<ReviewStatus> statuses =
        reviewStatus == null ? TRUSTED_STATUSES : List.of(reviewStatus);
    List<GraphNode> nodes =
        (nodeType == null
                ? graphNodeRepository.findBySpaceIdAndReviewStatusIn(spaceId, statuses)
                : graphNodeRepository.findBySpaceIdAndTypeAndReviewStatusIn(spaceId, nodeType, statuses))
            .stream()
            .filter(node -> matches(query, node.getLabel()))
            .filter(
                node ->
                    !evidenceOnly(evidenceOnly)
                        || evidenceCount(node.getEvidenceChunkIds()) > 0
                        || node.getType() == GraphNodeType.KNOWLEDGE_SPACE)
            .sorted(Comparator.comparing(GraphNode::getId))
            .limit(effectiveLimit)
            .toList();
    Set<String> nodeIds = nodes.stream().map(GraphNode::getId).collect(Collectors.toSet());
    List<GraphEdge> edges =
        (edgeType == null
                ? graphEdgeRepository.findBySpaceIdAndReviewStatusIn(spaceId, statuses)
                : graphEdgeRepository.findBySpaceIdAndTypeAndReviewStatusIn(spaceId, edgeType, statuses))
            .stream()
            .filter(edge -> nodeIds.contains(edge.getSourceNodeId()) && nodeIds.contains(edge.getTargetNodeId()))
            .filter(edge -> !evidenceOnly(evidenceOnly) || evidenceCount(edge.getEvidenceChunkIds()) > 0)
            .sorted(Comparator.comparing(GraphEdge::getId))
            .limit(effectiveLimit)
            .toList();
    Map<String, Integer> counts = new LinkedHashMap<>();
    counts.put("nodes", nodes.size());
    counts.put("edges", edges.size());
    counts.put("excluded", graphProjectionItemRepository.findAll().stream()
        .filter(item -> item.getStatus() == GraphProjectionItemStatus.SKIPPED)
        .toList()
        .size());
    return new GraphViewResponse(
        spaceId,
        nodes.stream().map(this::nodeResponse).toList(),
        edges.stream().map(this::edgeResponse).toList(),
        counts);
  }

  /** Returns selected node detail with adjacent edges and evidence. */
  @Transactional(readOnly = true)
  public GraphNodeDetailResponse nodeDetail(String spaceId, String nodeId) {
    validateSpace(spaceId);
    GraphNode node =
        graphNodeRepository
            .findById(nodeId)
            .filter(item -> item.getSpaceId().equals(spaceId))
            .orElseThrow(() -> new NotFoundException("Graph node not found."));
    List<GraphEdge> adjacent =
        graphEdgeRepository.findBySpaceIdAndSourceNodeIdOrSpaceIdAndTargetNodeId(
            spaceId, nodeId, spaceId, nodeId);
    Map<String, GraphNode> nodesById =
        graphNodeRepository.findAllById(
                adjacent.stream()
                    .flatMap(edge -> List.of(edge.getSourceNodeId(), edge.getTargetNodeId()).stream())
                    .filter(id -> !id.equals(nodeId))
                    .toList())
            .stream()
            .collect(Collectors.toMap(GraphNode::getId, Function.identity(), (left, right) -> left));
    Map<String, SourceChunk> chunks = evidenceChunks(node.getEvidenceChunkIds(), adjacent);
    return new GraphNodeDetailResponse(
        nodeResponse(node),
        nodesById.values().stream().map(this::nodeResponse).toList(),
        adjacent.stream().map(this::edgeResponse).toList(),
        chunks.values().stream().map(this::evidenceResponse).toList());
  }

  /** Gets a projection run report. */
  @Transactional(readOnly = true)
  public GraphProjectionRunResponse getProjectionRun(String runId) {
    return projectionRunResponse(
        graphProjectionRunRepository
            .findById(runId)
            .orElseThrow(() -> new NotFoundException("Graph projection run not found.")));
  }

  /** Appends a review action for a graph edge. */
  @Transactional
  public ReviewResponse reviewEdge(
      String spaceId, String edgeId, CreateGraphEdgeReviewRequest request, String reviewer) {
    validateSpace(spaceId);
    GraphEdge edge =
        graphEdgeRepository
            .findById(edgeId)
            .filter(item -> item.getSpaceId().equals(spaceId))
            .orElseThrow(() -> new NotFoundException("Graph edge not found."));
    OffsetDateTime now = OffsetDateTime.now(clock);
    ReviewAction action = request.action();
    edge.applyReviewStatus(action.resultingStatus(), now);
    graphEdgeRepository.save(edge);
    ReviewRecord record =
        ReviewRecord.create(
            "graph_edge",
            edgeId,
            action,
            reviewer == null || reviewer.isBlank() ? "graph-reviewer" : reviewer,
            request.comment(),
            edge.getEvidenceChunkIds(),
            now);
    ReviewRecord saved = reviewRecordRepository.save(record);
    graphAuditRecordRepository.save(
        GraphAuditRecord.create(
            auditId(),
            spaceId,
            record.getReviewer(),
            "EDGE_REVIEW",
            "graph_edge",
            edgeId,
            "Graph edge review action recorded.",
            now));
    audit(
        "GRAPH_EDGE_REVIEW",
        spaceId,
        record.getReviewer(),
        AuditResult.SUCCEEDED,
        AuditSeverity.NOTICE,
        "graph_edge",
        edgeId,
        "Graph edge review action recorded.",
        Map.of("reviewAction", action.name(), "reviewStatus", edge.getReviewStatus().name()));
    return ReviewMapper.toResponse(saved);
  }

  /** Records a user-safe audit entry for denied graph access when the target space exists. */
  @Transactional
  public void auditAccessDenied(String spaceId, String actor, String reasonCode, String targetId) {
    if (spaceId == null || spaceId.isBlank() || !spaceRepository.existsById(spaceId)) {
      return;
    }
    graphAuditRecordRepository.save(
        GraphAuditRecord.create(
            auditId(),
            spaceId,
            actor == null || actor.isBlank() ? "anonymous" : actor,
            "ACCESS_DENIED",
            "graph_access",
            targetId,
            reasonCode,
            OffsetDateTime.now(clock)));
    audit(
        "GRAPH_ACCESS_DENIED",
        spaceId,
        actor == null || actor.isBlank() ? "anonymous" : actor,
        AuditResult.DENIED,
        AuditSeverity.SECURITY,
        "graph_access",
        targetId,
        reasonCode,
        Map.of("reasonCode", reasonCode == null ? "DENIED" : reasonCode));
  }

  private void audit(
      String action,
      String spaceId,
      String actor,
      AuditResult result,
      AuditSeverity severity,
      String targetType,
      String targetId,
      String summary,
      Map<String, Object> metadata) {
    if (auditLogService == null) {
      return;
    }
    auditLogService.recordIfEnabled(
        new AuditLogService.CreateAuditEventCommand(
            actor,
            actor,
            action,
            AuditCategory.GRAPH,
            result,
            severity,
            spaceId,
            targetType,
            targetId,
            null,
            summary,
            metadata));
  }

  private void validateSpace(String spaceId) {
    if (!spaceRepository.existsById(spaceId)) {
      throw new NotFoundException("Knowledge Space not found.");
    }
  }

  private List<SourceChunk> targetChunks(String spaceId) {
    List<String> batchIds = batchRepository.findBySpaceId(spaceId).stream().map(Batch::getId).toList();
    List<String> fileIds = fileItemRepository.findByBatchIdIn(batchIds).stream().map(FileItem::getId).toList();
    return fileIds.isEmpty() ? List.of() : sourceChunkRepository.findByFileItemIdIn(fileIds);
  }

  private GraphProjectionItem saveItem(
      GraphProjectionRun run,
      String sourceType,
      String sourceId,
      String targetType,
      String targetId,
      GraphProjectionItemStatus status,
      String reasonCode) {
    return graphProjectionItemRepository.save(
        GraphProjectionItem.create(
            run.getId() + "-" + sourceType + "-" + sourceId + "-" + targetType + "-" + status.name().toLowerCase(),
            run.getId(),
            sourceType,
            sourceId,
            targetType,
            targetId,
            status,
            reasonCode));
  }

  private GraphProjectionRunResponse projectionRunResponse(GraphProjectionRun run) {
    List<GraphProjectionItem> items = graphProjectionItemRepository.findByRunIdOrderByIdAsc(run.getId());
    return new GraphProjectionRunResponse(
        run.getId(),
        run.getSpaceId(),
        run.getAdapterId(),
        run.getScope(),
        run.getStatus(),
        run.getRequestedBy(),
        run.getSafeMessage(),
        new GraphSummaryResponse(
            run.getCreatedCount(), run.getUpdatedCount(), run.getSkippedCount(), run.getFailedCount()),
        items.stream().map(this::itemResponse).toList(),
        run.getStartedAt(),
        run.getCompletedAt());
  }

  private GraphProjectionItemResponse itemResponse(GraphProjectionItem item) {
    return new GraphProjectionItemResponse(
        item.getId(),
        item.getSourceType(),
        item.getSourceId(),
        item.getTargetType(),
        item.getTargetId(),
        item.getStatus(),
        item.getReasonCode());
  }

  private GraphNodeResponse nodeResponse(GraphNode node) {
    return new GraphNodeResponse(
        node.getId(),
        node.getLabel(),
        node.getType(),
        node.getReviewStatus(),
        node.getConfidence(),
        evidenceCount(node.getEvidenceChunkIds()) + evidenceCount(node.getEvidenceWikiPageIds()));
  }

  private GraphEdgeResponse edgeResponse(GraphEdge edge) {
    return new GraphEdgeResponse(
        edge.getId(),
        edge.getSourceNodeId(),
        edge.getTargetNodeId(),
        edge.getType(),
        edge.getReviewStatus(),
        edge.getConfidence(),
        evidenceCount(edge.getEvidenceChunkIds()) + evidenceCount(edge.getEvidenceWikiPageIds()));
  }

  private GraphEvidenceReferenceResponse evidenceResponse(SourceChunk chunk) {
    return new GraphEvidenceReferenceResponse(
        chunk.getId(),
        chunk.getSourceFile(),
        chunk.getPage(),
        chunk.getSection(),
        chunk.getConfidence(),
        chunk.getReviewStatus());
  }

  private Map<String, SourceChunk> evidenceChunks(String[] nodeEvidence, List<GraphEdge> adjacent) {
    List<String> ids =
        Arrays.stream(nodeEvidence == null ? new String[0] : nodeEvidence)
            .filter(Objects::nonNull)
            .toList();
    List<String> edgeIds =
        adjacent.stream()
            .flatMap(edge -> Arrays.stream(edge.getEvidenceChunkIds() == null ? new String[0] : edge.getEvidenceChunkIds()))
            .filter(Objects::nonNull)
            .toList();
    List<String> allIds = java.util.stream.Stream.concat(ids.stream(), edgeIds.stream()).distinct().toList();
    return sourceChunkRepository.findAllById(allIds).stream()
        .collect(Collectors.toMap(SourceChunk::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
  }

  private int effectiveLimit(Integer limit) {
    if (limit == null) {
      return DEFAULT_LIMIT;
    }
    if (limit < 1 || limit > MAX_LIMIT) {
      throw new RequestValidationException(Map.of("limit", "must be between 1 and 500"));
    }
    return limit;
  }

  private boolean matches(String query, String label) {
    return query == null || query.isBlank() || label.toLowerCase().contains(query.toLowerCase());
  }

  private boolean evidenceOnly(Boolean evidenceOnly) {
    return evidenceOnly == null || evidenceOnly;
  }

  private boolean hasSourceTrace(SourceChunk chunk) {
    return chunk.getSourceFile() != null && !chunk.getSourceFile().isBlank();
  }

  private String skipReason(SourceChunk chunk) {
    return TRUSTED_STATUSES.contains(chunk.getReviewStatus()) ? "MISSING_SOURCE_TRACE" : "UNAPPROVED_SOURCE";
  }

  private int evidenceCount(String[] values) {
    return values == null ? 0 : values.length;
  }

  private String[] toArray(List<String> values) {
    return values == null ? null : values.toArray(String[]::new);
  }

  private String firstOrFallback(List<String> values, String fallback) {
    return values == null || values.isEmpty() ? fallback : values.getFirst();
  }

  private String runId(OffsetDateTime now) {
    return "graph-run-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(now) + "-" + UUID.randomUUID();
  }

  private String auditId() {
    return "graph-audit-" + UUID.randomUUID();
  }
}
