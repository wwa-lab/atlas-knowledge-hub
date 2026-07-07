package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.CreateManualUrlSourceRequest;
import com.atlas.metadata.dto.ManualUrlSourceResponse;
import com.atlas.metadata.service.ManualUrlSourceService;
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

/** REST controller for metadata-only manual URL sources. */
@RestController
@RequestMapping("/api")
public class ManualUrlSourceController {

  private final ManualUrlSourceService manualUrlSourceService;

  /** Creates the controller. */
  public ManualUrlSourceController(ManualUrlSourceService manualUrlSourceService) {
    this.manualUrlSourceService = manualUrlSourceService;
  }

  /** Registers a manual URL source for a Knowledge Space. */
  @PostMapping("/spaces/{spaceId}/manual-url-sources")
  public ResponseEntity<ApiEnvelope<ManualUrlSourceResponse>> create(
      @PathVariable String spaceId, @Valid @RequestBody CreateManualUrlSourceRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiEnvelope.ok(manualUrlSourceService.create(spaceId, request)));
  }

  /** Lists manual URL sources for a Knowledge Space. */
  @GetMapping("/spaces/{spaceId}/manual-url-sources")
  public ApiEnvelope<List<ManualUrlSourceResponse>> list(@PathVariable String spaceId) {
    return ApiEnvelope.ok(manualUrlSourceService.list(spaceId));
  }

  /** Gets one manual URL source by id. */
  @GetMapping("/manual-url-sources/{sourceId}")
  public ApiEnvelope<ManualUrlSourceResponse> get(@PathVariable String sourceId) {
    return ApiEnvelope.ok(manualUrlSourceService.get(sourceId));
  }
}
