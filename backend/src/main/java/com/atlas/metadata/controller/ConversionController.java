package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.ConversionRunResponse;
import com.atlas.metadata.dto.ConverterCapabilityResponse;
import com.atlas.metadata.dto.CreateConversionRunRequest;
import com.atlas.metadata.service.ConversionService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for converter adapter capabilities and conversion runs. */
@RestController
@RequestMapping("/api")
public class ConversionController {

  private final ConversionService conversionService;

  /** Creates the controller. */
  public ConversionController(ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  /** Lists converter adapter capabilities with masked configuration. */
  @GetMapping("/converter-adapters")
  public ApiEnvelope<List<ConverterCapabilityResponse>> listConverterAdapters() {
    return ApiEnvelope.ok(conversionService.listCapabilities());
  }

  /** Creates and executes a conversion run for a batch. */
  @PostMapping("/batches/{batchId}/conversion-runs")
  public ResponseEntity<ApiEnvelope<ConversionRunResponse>> createConversionRun(
      @PathVariable String batchId, @RequestBody CreateConversionRunRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiEnvelope.ok(conversionService.createRun(batchId, request)));
  }

  /** Gets a conversion run report. */
  @GetMapping("/conversion-runs/{runId}")
  public ApiEnvelope<ConversionRunResponse> getConversionRun(@PathVariable String runId) {
    return ApiEnvelope.ok(conversionService.getRun(runId));
  }
}
