package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.CreateVectorRunRequest;
import com.atlas.metadata.dto.VectorCapabilityResponse;
import com.atlas.metadata.dto.VectorQueryRequestDto;
import com.atlas.metadata.dto.VectorQueryResponse;
import com.atlas.metadata.dto.VectorRunResponse;
import com.atlas.metadata.service.VectorService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for vector adapter capabilities, runs, and query evidence. */
@RestController
@RequestMapping("/api")
public class VectorController {

  private final VectorService vectorService;

  /** Creates the controller. */
  public VectorController(VectorService vectorService) {
    this.vectorService = vectorService;
  }

  /** Lists vector adapter capabilities with masked configuration. */
  @GetMapping("/vector-adapters")
  public ApiEnvelope<List<VectorCapabilityResponse>> listVectorAdapters() {
    return ApiEnvelope.ok(vectorService.listCapabilities());
  }

  /** Creates and executes a vector index/deindex run for a Knowledge Space. */
  @PostMapping("/spaces/{spaceId}/vector-runs")
  public ResponseEntity<ApiEnvelope<VectorRunResponse>> createVectorRun(
      @PathVariable String spaceId, @RequestBody CreateVectorRunRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiEnvelope.ok(vectorService.createRun(spaceId, request)));
  }

  /** Gets a vector run report. */
  @GetMapping("/vector-runs/{runId}")
  public ApiEnvelope<VectorRunResponse> getVectorRun(@PathVariable String runId) {
    return ApiEnvelope.ok(vectorService.getRun(runId));
  }

  /** Runs an immediate bounded vector evidence query. */
  @PostMapping("/spaces/{spaceId}/vector-query")
  public ApiEnvelope<VectorQueryResponse> queryVectors(
      @PathVariable String spaceId, @RequestBody VectorQueryRequestDto request) {
    return ApiEnvelope.ok(vectorService.query(spaceId, request));
  }
}
