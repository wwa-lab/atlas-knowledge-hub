package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.RetrievalQualityMetricsSummaryResponse;
import com.atlas.metadata.dto.RetrievalRunQualityMetricsResponse;
import com.atlas.metadata.service.RetrievalQualityMetricsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only retrieval quality metrics API. */
@RestController
@RequestMapping("/api")
public class RetrievalQualityMetricsController {

  private final RetrievalQualityMetricsService service;

  /** Creates the controller. */
  public RetrievalQualityMetricsController(RetrievalQualityMetricsService service) {
    this.service = service;
  }

  /** Returns safe deterministic quality metrics for one Trusted Ask run. */
  @GetMapping("/ask-runs/{runId}/quality-metrics")
  public ApiEnvelope<RetrievalRunQualityMetricsResponse> getRunMetrics(@PathVariable String runId) {
    return ApiEnvelope.ok(service.getRunMetrics(runId));
  }

  /** Returns safe aggregate retrieval quality metrics for one Knowledge Space. */
  @GetMapping("/spaces/{spaceId}/retrieval-quality-metrics")
  public ApiEnvelope<RetrievalQualityMetricsSummaryResponse> getSpaceMetrics(@PathVariable String spaceId) {
    return ApiEnvelope.ok(service.getSpaceMetrics(spaceId));
  }
}
