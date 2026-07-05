package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.CreateWikiIngestRunRequest;
import com.atlas.metadata.dto.WikiIngestRunResponse;
import com.atlas.metadata.service.WikiIngestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for deterministic Auto Wiki ingest v0. */
@RestController
@RequestMapping("/api")
public class WikiIngestController {

  private final WikiIngestService wikiIngestService;

  /** Creates the controller. */
  public WikiIngestController(WikiIngestService wikiIngestService) {
    this.wikiIngestService = wikiIngestService;
  }

  /** Starts a deterministic Auto Wiki ingest run. */
  @PostMapping("/spaces/{spaceId}/wiki-ingest-runs")
  public ResponseEntity<ApiEnvelope<WikiIngestRunResponse>> startIngestRun(
      @PathVariable String spaceId, @RequestBody(required = false) CreateWikiIngestRunRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiEnvelope.ok(wikiIngestService.startIngestRun(spaceId, request)));
  }

  /** Gets one safe Wiki ingest run summary. */
  @GetMapping("/spaces/{spaceId}/wiki-generation-runs/{runId}")
  public ApiEnvelope<WikiIngestRunResponse> getIngestRun(
      @PathVariable String spaceId, @PathVariable String runId) {
    return ApiEnvelope.ok(wikiIngestService.getIngestRun(spaceId, runId));
  }
}
