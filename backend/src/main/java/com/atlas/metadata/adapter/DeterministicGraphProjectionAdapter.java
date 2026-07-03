package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.GraphEdgeType;
import com.atlas.metadata.enums.GraphNodeType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** Deterministic metadata-only graph projection adapter for CI and local verification. */
@Component
public class DeterministicGraphProjectionAdapter implements GraphProjectionAdapter {

  @Override
  public String adapterId() {
    return "deterministic";
  }

  @Override
  public GraphProjectionResult project(GraphProjectionRequest request) {
    String spaceNodeId = "node-space-" + request.spaceId();
    List<ProjectedNode> nodes = new ArrayList<>();
    List<ProjectedEdge> edges = new ArrayList<>();
    nodes.add(
        new ProjectedNode(
            spaceNodeId,
            request.spaceId(),
            GraphNodeType.KNOWLEDGE_SPACE,
            com.atlas.metadata.enums.ReviewStatus.APPROVED,
            List.of(),
            null));
    for (GraphSourceDescriptor source : request.sources()) {
      String conceptNodeId =
          "node-concept-"
              + slug(request.spaceId())
              + "-"
              + slug(source.section() == null ? source.sourceChunkId() : source.section());
      nodes.add(
          new ProjectedNode(
              conceptNodeId,
              source.section() == null ? source.sourceChunkId() : source.section(),
              GraphNodeType.CONCEPT,
              source.reviewStatus(),
              List.of(source.sourceChunkId()),
              source.confidence()));
      edges.add(
          new ProjectedEdge(
              "edge-source-mentions-" + source.sourceChunkId(),
              spaceNodeId,
              conceptNodeId,
              GraphEdgeType.MENTIONS,
              source.reviewStatus(),
              List.of(source.sourceChunkId()),
              source.confidence()));
    }
    return new GraphProjectionResult(nodes, edges);
  }

  private String slug(String value) {
    return value.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
  }
}
