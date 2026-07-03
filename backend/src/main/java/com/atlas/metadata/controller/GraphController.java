package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.CreateGraphEdgeReviewRequest;
import com.atlas.metadata.dto.CreateGraphProjectionRunRequest;
import com.atlas.metadata.dto.ErrorBody;
import com.atlas.metadata.dto.GraphNodeDetailResponse;
import com.atlas.metadata.dto.GraphProjectionRunResponse;
import com.atlas.metadata.dto.GraphViewResponse;
import com.atlas.metadata.dto.ReviewResponse;
import com.atlas.metadata.enums.GraphEdgeType;
import com.atlas.metadata.enums.GraphNodeType;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.service.GraphService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for trusted knowledge graph projection and query. */
@RestController
@RequestMapping("/api")
public class GraphController {

  private static final Set<String> READ_ROLES = Set.of("VIEWER", "REVIEWER", "ADMIN");
  private static final Set<String> WRITE_ROLES = Set.of("REVIEWER", "ADMIN");
  private static final Set<String> ADMIN_ROLES = Set.of("ADMIN");

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
      @RequestParam(required = false) Integer limit,
      @RequestHeader(value = "X-Atlas-User", required = false) String user,
      @RequestHeader(value = "X-Atlas-Role", required = false) String role,
      HttpServletRequest request) {
    ResponseEntity<ApiEnvelope<GraphViewResponse>> denied = denyUnless(spaceId, user, role, READ_ROLES, request);
    if (denied != null) {
      return denied;
    }
    return ResponseEntity.ok(
        ApiEnvelope.ok(graphService.graphView(spaceId, q, nodeType, edgeType, reviewStatus, evidenceOnly, limit)));
  }

  @GetMapping("/spaces/{spaceId}/graph/nodes/{nodeId}")
  public ResponseEntity<ApiEnvelope<GraphNodeDetailResponse>> nodeDetail(
      @PathVariable String spaceId,
      @PathVariable String nodeId,
      @RequestHeader(value = "X-Atlas-User", required = false) String user,
      @RequestHeader(value = "X-Atlas-Role", required = false) String role,
      HttpServletRequest request) {
    ResponseEntity<ApiEnvelope<GraphNodeDetailResponse>> denied = denyUnless(spaceId, user, role, READ_ROLES, request);
    if (denied != null) {
      return denied;
    }
    return ResponseEntity.ok(ApiEnvelope.ok(graphService.nodeDetail(spaceId, nodeId)));
  }

  @PostMapping("/spaces/{spaceId}/graph/projection-runs")
  public ResponseEntity<ApiEnvelope<GraphProjectionRunResponse>> createProjectionRun(
      @PathVariable String spaceId,
      @Valid @RequestBody CreateGraphProjectionRunRequest body,
      @RequestHeader(value = "X-Atlas-User", required = false) String user,
      @RequestHeader(value = "X-Atlas-Role", required = false) String role,
      HttpServletRequest request) {
    ResponseEntity<ApiEnvelope<GraphProjectionRunResponse>> denied =
        denyUnless(spaceId, user, role, ADMIN_ROLES, request);
    if (denied != null) {
      return denied;
    }
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiEnvelope.ok(graphService.createProjectionRun(spaceId, body)));
  }

  @GetMapping("/graph/projection-runs/{runId}")
  public ResponseEntity<ApiEnvelope<GraphProjectionRunResponse>> getProjectionRun(
      @PathVariable String runId,
      @RequestHeader(value = "X-Atlas-User", required = false) String user,
      @RequestHeader(value = "X-Atlas-Role", required = false) String role,
      HttpServletRequest request) {
    ResponseEntity<ApiEnvelope<GraphProjectionRunResponse>> denied = denyUnless(user, role, READ_ROLES, request);
    if (denied != null) {
      return denied;
    }
    return ResponseEntity.ok(ApiEnvelope.ok(graphService.getProjectionRun(runId)));
  }

  @PostMapping("/spaces/{spaceId}/graph/edges/{edgeId}/review-actions")
  public ResponseEntity<ApiEnvelope<ReviewResponse>> reviewEdge(
      @PathVariable String spaceId,
      @PathVariable String edgeId,
      @Valid @RequestBody CreateGraphEdgeReviewRequest body,
      @RequestHeader(value = "X-Atlas-User", required = false) String user,
      @RequestHeader(value = "X-Atlas-Role", required = false) String role,
      HttpServletRequest request) {
    ResponseEntity<ApiEnvelope<ReviewResponse>> denied = denyUnless(spaceId, user, role, WRITE_ROLES, request);
    if (denied != null) {
      return denied;
    }
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiEnvelope.ok(graphService.reviewEdge(spaceId, edgeId, body, user)));
  }

  private <T> ResponseEntity<ApiEnvelope<T>> denyUnless(
      String spaceId, String user, String role, Set<String> allowedRoles, HttpServletRequest request) {
    ResponseEntity<ApiEnvelope<T>> denied = denyUnless(user, role, allowedRoles, request);
    if (denied != null) {
      graphService.auditAccessDenied(
          spaceId, user, denied.getStatusCode().toString(), request.getRequestURI());
    }
    return denied;
  }

  private <T> ResponseEntity<ApiEnvelope<T>> denyUnless(
      String user, String role, Set<String> allowedRoles, HttpServletRequest request) {
    if (user == null || user.isBlank()) {
      return error(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication is required.", request);
    }
    if (role == null || !allowedRoles.contains(role)) {
      return error(HttpStatus.FORBIDDEN, "FORBIDDEN", "Graph access is not allowed.", request);
    }
    return null;
  }

  private <T> ResponseEntity<ApiEnvelope<T>> error(
      HttpStatus status, String code, String message, HttpServletRequest request) {
    return ResponseEntity.status(status)
        .body(
            ApiEnvelope.fail(
                new ErrorBody(code, message, null, Instant.now().toEpochMilli(), request.getRequestURI())));
  }
}
