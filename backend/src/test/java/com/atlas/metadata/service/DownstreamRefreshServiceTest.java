package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlas.metadata.dto.CreateGraphProjectionRunRequest;
import com.atlas.metadata.dto.CreateVectorRunRequest;
import com.atlas.metadata.dto.DownstreamRefreshRequest;
import com.atlas.metadata.dto.GraphProjectionRunResponse;
import com.atlas.metadata.dto.GraphSummaryResponse;
import com.atlas.metadata.dto.VectorRunResponse;
import com.atlas.metadata.dto.VectorRunSummaryResponse;
import com.atlas.metadata.enums.GraphProjectionStatus;
import com.atlas.metadata.enums.VectorReviewPolicy;
import com.atlas.metadata.enums.VectorRunOperation;
import com.atlas.metadata.enums.VectorRunStatus;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for backend downstream refresh orchestration. */
@ExtendWith(MockitoExtension.class)
class DownstreamRefreshServiceTest {

  @Mock private GraphService graphService;
  @Mock private VectorService vectorService;

  @Test
  void refreshesGraphAndVectorEvidenceThroughBackendServices() {
    DownstreamRefreshService service = new DownstreamRefreshService(graphService, vectorService);
    GraphProjectionRunResponse graphRun =
        new GraphProjectionRunResponse(
            "graph-run",
            "space-1",
            "deterministic-graph",
            "APPROVED_ONLY",
            GraphProjectionStatus.SUCCEEDED,
            "system",
            "Graph done.",
            new GraphSummaryResponse(1, 0, 0, 0),
            List.of(),
            OffsetDateTime.parse("2026-07-05T00:00:00Z"),
            OffsetDateTime.parse("2026-07-05T00:00:01Z"));
    VectorRunResponse vectorRun =
        new VectorRunResponse(
            "vector-run",
            "space-1",
            "batch-1",
            "mock-vector",
            "mock-1",
            VectorRunOperation.INDEX,
            VectorRunStatus.SUCCEEDED,
            "mock",
            VectorReviewPolicy.APPROVED_ONLY,
            3,
            "user",
            "Vector done.",
            new VectorRunSummaryResponse(1, 1, 0, 0, 0),
            List.of(),
            OffsetDateTime.parse("2026-07-05T00:00:00Z"),
            OffsetDateTime.parse("2026-07-05T00:00:01Z"));
    when(graphService.createProjectionRun(eq("space-1"), any(CreateGraphProjectionRunRequest.class)))
        .thenReturn(graphRun);
    when(vectorService.createRun(eq("space-1"), any(CreateVectorRunRequest.class))).thenReturn(vectorRun);

    var response =
        service.refresh(
            "space-1",
            new DownstreamRefreshRequest("batch-1", "file-1", "user"));

    assertThat(response.graphRun().runId()).isEqualTo("graph-run");
    assertThat(response.vectorRun().runId()).isEqualTo("vector-run");

    ArgumentCaptor<CreateGraphProjectionRunRequest> graphRequest =
        ArgumentCaptor.forClass(CreateGraphProjectionRunRequest.class);
    verify(graphService).createProjectionRun(eq("space-1"), graphRequest.capture());
    assertThat(graphRequest.getValue().scope()).isEqualTo("APPROVED_ONLY");

    ArgumentCaptor<CreateVectorRunRequest> vectorRequest = ArgumentCaptor.forClass(CreateVectorRunRequest.class);
    verify(vectorService).createRun(eq("space-1"), vectorRequest.capture());
    assertThat(vectorRequest.getValue().adapterKey()).isEqualTo("mock-vector");
    assertThat(vectorRequest.getValue().operation()).isEqualTo(VectorRunOperation.INDEX);
    assertThat(vectorRequest.getValue().batchId()).isEqualTo("batch-1");
    assertThat(vectorRequest.getValue().fileItemIds()).containsExactly("file-1");
    assertThat(vectorRequest.getValue().reviewPolicy()).isEqualTo(VectorReviewPolicy.APPROVED_ONLY);
    assertThat(vectorRequest.getValue().dimension()).isEqualTo(3);
    assertThat(vectorRequest.getValue().mode()).isEqualTo("mock");
  }
}
