package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.CreateParserRunRequest;
import com.atlas.metadata.dto.ParserCapabilityResponse;
import com.atlas.metadata.dto.ParserRunResponse;
import com.atlas.metadata.service.ParserService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for parser adapter capabilities and parser runs. */
@RestController
@RequestMapping("/api")
public class ParserController {

  private final ParserService parserService;

  /** Creates the controller. */
  public ParserController(ParserService parserService) {
    this.parserService = parserService;
  }

  /** Lists parser adapter capabilities with masked configuration. */
  @GetMapping("/parser-adapters")
  public ApiEnvelope<List<ParserCapabilityResponse>> listParserAdapters() {
    return ApiEnvelope.ok(parserService.listCapabilities());
  }

  /** Creates and executes a parser run for a batch. */
  @PostMapping("/batches/{batchId}/parser-runs")
  public ResponseEntity<ApiEnvelope<ParserRunResponse>> createParserRun(
      @PathVariable String batchId, @RequestBody CreateParserRunRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiEnvelope.ok(parserService.createRun(batchId, request)));
  }

  /** Gets a parser run report. */
  @GetMapping("/parser-runs/{runId}")
  public ApiEnvelope<ParserRunResponse> getParserRun(@PathVariable String runId) {
    return ApiEnvelope.ok(parserService.getRun(runId));
  }
}
