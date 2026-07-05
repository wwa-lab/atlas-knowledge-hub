package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.CreateModelRunRequest;
import com.atlas.metadata.dto.ModelCapabilityResponse;
import com.atlas.metadata.dto.ModelConfigurationResponse;
import com.atlas.metadata.dto.ModelRunResponse;
import com.atlas.metadata.dto.SaveModelConfigurationRequest;
import com.atlas.metadata.service.ModelRuntimeConfigurationService;
import com.atlas.metadata.service.ModelService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for model adapter capabilities and model runs. */
@RestController
@RequestMapping("/api")
public class ModelController {

  private final ModelService modelService;
  private final ModelRuntimeConfigurationService runtimeConfigurationService;

  /** Creates the controller. */
  public ModelController(
      ModelService modelService, ModelRuntimeConfigurationService runtimeConfigurationService) {
    this.modelService = modelService;
    this.runtimeConfigurationService = runtimeConfigurationService;
  }

  /** Lists model adapter capabilities with masked configuration. */
  @GetMapping("/model-adapters")
  public ApiEnvelope<List<ModelCapabilityResponse>> listModelAdapters() {
    return ApiEnvelope.ok(modelService.listCapabilities());
  }

  /** Reads masked runtime chat configuration state. */
  @GetMapping("/model-configurations/deepseek")
  public ApiEnvelope<ModelConfigurationResponse> getRuntimeChatConfiguration() {
    return ApiEnvelope.ok(runtimeConfigurationService.readChatConfiguration());
  }

  /** Saves runtime chat configuration without returning raw secrets. */
  @PutMapping("/model-configurations/deepseek")
  public ApiEnvelope<ModelConfigurationResponse> saveRuntimeChatConfiguration(
      @Valid @RequestBody SaveModelConfigurationRequest request) {
    return ApiEnvelope.ok(runtimeConfigurationService.saveChatConfiguration(request));
  }

  /** Clears runtime chat configuration and falls back to process environment if available. */
  @DeleteMapping("/model-configurations/deepseek")
  public ApiEnvelope<ModelConfigurationResponse> clearRuntimeChatConfiguration() {
    return ApiEnvelope.ok(runtimeConfigurationService.clearChatConfiguration());
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
