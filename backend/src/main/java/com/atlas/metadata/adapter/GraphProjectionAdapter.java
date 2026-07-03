package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.GraphEdgeType;
import com.atlas.metadata.enums.GraphNodeType;
import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;
import java.util.List;

/** Product-facing graph projection seam for deterministic and future engine-backed extraction. */
public interface GraphProjectionAdapter {

  String adapterId();

  GraphProjectionResult project(GraphProjectionRequest request);

  record GraphProjectionRequest(String spaceId, List<GraphSourceDescriptor> sources) {}

  record GraphSourceDescriptor(
      String sourceChunkId,
      String fileItemId,
      String sourceFile,
      Integer page,
      String section,
      BigDecimal confidence,
      ReviewStatus reviewStatus) {}

  record GraphProjectionResult(List<ProjectedNode> nodes, List<ProjectedEdge> edges) {}

  record ProjectedNode(
      String id,
      String label,
      GraphNodeType type,
      ReviewStatus reviewStatus,
      List<String> evidenceChunkIds,
      BigDecimal confidence) {}

  record ProjectedEdge(
      String id,
      String sourceNodeId,
      String targetNodeId,
      GraphEdgeType type,
      ReviewStatus reviewStatus,
      List<String> evidenceChunkIds,
      BigDecimal confidence) {}
}
