package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.CreateModelRunRequest;
import com.atlas.metadata.dto.ModelCapabilityResponse;
import com.atlas.metadata.dto.ModelRunResponse;
import com.atlas.metadata.service.ModelService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for model adapter capabilities and model runs. */
@RestController
@RequestMapping("/api")
public class ModelController {

  private final ModelService modelService;

  /** Creates the controller. */
  public ModelController(ModelService modelService) {
    this.modelService = modelService;
  }

  /** Lists model adapter capabilities with masked configuration. */
  @GetMapping("/model-adapters")
  public ApiEnvelope<List<ModelCapabilityResponse>> listModelAdapters() {
    return ApiEnvelope.ok(modelService.listCapabilities());
  }

  /** Creates and executes a model run. */
  @PostMapping("/model-runs")
  public ResponseEntity<ApiEnvelope<ModelRunResponse>> createModelRun(
      @RequestBody CreateModelRunRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiEnvelope.ok(modelService.createRun(request)));
  }

  /** Gets a model run report. */
  @GetMapping("/model-runs/{runId}")
  public ApiEnvelope<ModelRunResponse> getModelRun(@PathVariable String runId) {
    return ApiEnvelope.ok(modelService.getRun(runId));
  }
}
