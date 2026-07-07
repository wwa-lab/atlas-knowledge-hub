package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.ConnectorDefinitionResponse;
import com.atlas.metadata.dto.ConnectorSyncItemResponse;
import com.atlas.metadata.dto.ConnectorSyncRunResponse;
import com.atlas.metadata.dto.CreateConnectorSyncJobRequest;
import com.atlas.metadata.service.ConnectorSyncService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for connector sync v0. */
@RestController
@RequestMapping("/api")
public class ConnectorSyncController {

  private final ConnectorSyncService connectorSyncService;

  public ConnectorSyncController(ConnectorSyncService connectorSyncService) {
    this.connectorSyncService = connectorSyncService;
  }

  /** Lists safe connector definitions. */
  @GetMapping("/connector-definitions")
  public ApiEnvelope<List<ConnectorDefinitionResponse>> listConnectorDefinitions() {
    return ApiEnvelope.ok(connectorSyncService.listDefinitions());
  }

  /** Creates and executes a mock/local fixture connector sync run. */
  @PostMapping("/spaces/{spaceId}/connector-sync-jobs")
  public ResponseEntity<ApiEnvelope<ConnectorSyncRunResponse>> createSyncJob(
      @PathVariable String spaceId, @Valid @RequestBody CreateConnectorSyncJobRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiEnvelope.ok(connectorSyncService.createSyncJob(spaceId, request)));
  }

  /** Gets a connector sync run. */
  @GetMapping("/connector-sync-runs/{runId}")
  public ApiEnvelope<ConnectorSyncRunResponse> getSyncRun(@PathVariable String runId) {
    return ApiEnvelope.ok(connectorSyncService.getRun(runId));
  }

  /** Lists items for a connector sync run. */
  @GetMapping("/connector-sync-runs/{runId}/items")
  public ApiEnvelope<List<ConnectorSyncItemResponse>> listSyncItems(@PathVariable String runId) {
    return ApiEnvelope.ok(connectorSyncService.listItems(runId));
  }
}
