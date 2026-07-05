package com.atlas.metadata.service;

import com.atlas.metadata.dto.CreateGraphProjectionRunRequest;
import com.atlas.metadata.dto.CreateVectorRunRequest;
import com.atlas.metadata.dto.DownstreamRefreshRequest;
import com.atlas.metadata.dto.DownstreamRefreshResponse;
import com.atlas.metadata.dto.GraphProjectionRunResponse;
import com.atlas.metadata.dto.VectorRunResponse;
import com.atlas.metadata.enums.VectorReviewPolicy;
import com.atlas.metadata.enums.VectorRunOperation;
import java.util.List;
import org.springframework.stereotype.Service;

/** Backend orchestration for graph and vector evidence refresh. */
@Service
public class DownstreamRefreshService {

  private final GraphService graphService;
  private final VectorService vectorService;

  /** Creates the downstream refresh service. */
  public DownstreamRefreshService(GraphService graphService, VectorService vectorService) {
    this.graphService = graphService;
    this.vectorService = vectorService;
  }

  /** Refreshes approved-only graph projection and vector index evidence. */
  public DownstreamRefreshResponse refresh(String spaceId, DownstreamRefreshRequest request) {
    DownstreamRefreshRequest safeRequest =
        request == null ? new DownstreamRefreshRequest(null, null, null) : request;
    String requestedBy =
        safeRequest.requestedBy() == null || safeRequest.requestedBy().isBlank()
            ? "frontend-user"
            : safeRequest.requestedBy().trim();
    GraphProjectionRunResponse graphRun =
        graphService.createProjectionRun(
            spaceId, new CreateGraphProjectionRunRequest("APPROVED_ONLY", null, false));
    VectorRunResponse vectorRun =
        vectorService.createRun(
            spaceId,
            new CreateVectorRunRequest(
                "mock-vector",
                VectorRunOperation.INDEX,
                blankToNull(safeRequest.batchId()),
                safeRequest.fileItemId() == null || safeRequest.fileItemId().isBlank()
                    ? null
                    : List.of(safeRequest.fileItemId().trim()),
                null,
                VectorReviewPolicy.APPROVED_ONLY,
                3,
                null,
                requestedBy,
                "mock"));
    return new DownstreamRefreshResponse(
        graphRun, vectorRun, "Downstream graph and vector evidence refresh completed.");
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
