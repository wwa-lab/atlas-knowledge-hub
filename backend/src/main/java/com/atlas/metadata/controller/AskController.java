package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.AskRunResponse;
import com.atlas.metadata.dto.AskSessionDetailResponse;
import com.atlas.metadata.dto.AskSessionSummaryResponse;
import com.atlas.metadata.dto.CreateAskRequest;
import com.atlas.metadata.service.AskService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for trusted ask runs. */
@RestController
@RequestMapping("/api")
public class AskController {

  private final AskService askService;

  /** Creates the controller. */
  public AskController(AskService askService) {
    this.askService = askService;
  }

  /** Creates and executes a trusted ask run for a Knowledge Space. */
  @PostMapping("/spaces/{spaceId}/ask")
  public ResponseEntity<ApiEnvelope<AskRunResponse>> createAskRun(
      @PathVariable String spaceId, @RequestBody CreateAskRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiEnvelope.ok(askService.createRun(spaceId, request)));
  }

  /** Gets a trusted ask run report by id. */
  @GetMapping("/ask-runs/{runId}")
  public ApiEnvelope<AskRunResponse> getAskRun(@PathVariable String runId) {
    return ApiEnvelope.ok(askService.getRun(runId));
  }

  /** Lists recent trusted ask sessions for a Knowledge Space. */
  @GetMapping("/spaces/{spaceId}/ask-sessions")
  public ApiEnvelope<List<AskSessionSummaryResponse>> listAskSessions(@PathVariable String spaceId) {
    return ApiEnvelope.ok(askService.listSessions(spaceId));
  }

  /** Gets a trusted ask session with answer history. */
  @GetMapping("/ask-sessions/{sessionId}")
  public ApiEnvelope<AskSessionDetailResponse> getAskSession(@PathVariable String sessionId) {
    return ApiEnvelope.ok(askService.getSession(sessionId));
  }
}
