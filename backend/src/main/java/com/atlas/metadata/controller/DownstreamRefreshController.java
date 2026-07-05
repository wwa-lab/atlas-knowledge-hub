package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.DownstreamRefreshRequest;
import com.atlas.metadata.dto.DownstreamRefreshResponse;
import com.atlas.metadata.service.DownstreamRefreshService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for backend-orchestrated graph/vector evidence refresh. */
@RestController
@RequestMapping("/api")
public class DownstreamRefreshController {

  private final DownstreamRefreshService downstreamRefreshService;

  /** Creates the controller. */
  public DownstreamRefreshController(DownstreamRefreshService downstreamRefreshService) {
    this.downstreamRefreshService = downstreamRefreshService;
  }

  /** Refreshes graph and vector evidence for a Knowledge Space scope. */
  @PostMapping("/spaces/{spaceId}/downstream-refresh")
  public ApiEnvelope<DownstreamRefreshResponse> refresh(
      @PathVariable String spaceId, @RequestBody(required = false) DownstreamRefreshRequest request) {
    return ApiEnvelope.ok(downstreamRefreshService.refresh(spaceId, request));
  }
}
