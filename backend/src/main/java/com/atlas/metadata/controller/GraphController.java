package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.CreateGraphEdgeReviewRequest;
import com.atlas.metadata.dto.CreateGraphProjectionRunRequest;
import com.atlas.metadata.dto.GraphNodeDetailResponse;
import com.atlas.metadata.dto.GraphProjectionRunResponse;
import com.atlas.metadata.dto.GraphViewResponse;
import com.atlas.metadata.dto.ReviewResponse;
import com.atlas.metadata.enums.GraphEdgeType;
import com.atlas.metadata.enums.GraphNodeType;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.service.GraphService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for trusted knowledge graph projection and query. */
@RestController
@RequestMapping("/api")
public class GraphController {

  private final GraphService graphService;

  public GraphController(GraphService graphService) {
    this.graphService = graphService;
  }

  @GetMapping("/spaces/{spaceId}/graph")
  public ResponseEntity<ApiEnvelope<GraphViewResponse>> graphView(
      @PathVariable String spaceId,
      @RequestParam(required = false) String q,
      @RequestParam(required = false) GraphNodeType nodeType,
      @RequestParam(required = false) GraphEdgeType edgeType,
      @RequestParam(required = false) ReviewStatus reviewStatus,
      @RequestParam(required = false) Boolean evidenceOnly,
      @RequestParam(required = false) Integer limit) {
    return ResponseEntity.ok(
        ApiEnvelope.ok(graphService.graphView(spaceId, q, nodeType, edgeType, reviewStatus, evidenceOnly, limit)));
  }

  @GetMapping("/spaces/{spaceId}/graph/nodes/{nodeId}")
  public ResponseEntity<ApiEnvelope<GraphNodeDetailResponse>> nodeDetail(
      @PathVariable String spaceId,
      @PathVariable String nodeId) {
    return ResponseEntity.ok(ApiEnvelope.ok(graphService.nodeDetail(spaceId, nodeId)));
  }

  @PostMapping("/spaces/{spaceId}/graph/projection-runs")
  public ResponseEntity<ApiEnvelope<GraphProjectionRunResponse>> createProjectionRun(
      @PathVariable String spaceId,
      @Valid @RequestBody CreateGraphProjectionRunRequest body) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiEnvelope.ok(graphService.createProjectionRun(spaceId, body)));
  }

  @GetMapping("/graph/projection-runs/{runId}")
  public ResponseEntity<ApiEnvelope<GraphProjectionRunResponse>> getProjectionRun(
      @PathVariable String runId) {
    return ResponseEntity.ok(ApiEnvelope.ok(graphService.getProjectionRun(runId)));
  }

  @PostMapping("/spaces/{spaceId}/graph/edges/{edgeId}/review-actions")
  public ResponseEntity<ApiEnvelope<ReviewResponse>> reviewEdge(
      @PathVariable String spaceId,
      @PathVariable String edgeId,
      @Valid @RequestBody CreateGraphEdgeReviewRequest body) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiEnvelope.ok(graphService.reviewEdge(spaceId, edgeId, body, "atlas-rbac")));
  }
}
