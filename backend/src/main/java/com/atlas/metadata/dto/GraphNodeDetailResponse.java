package com.atlas.metadata.dto;

import java.util.List;

/** Selected graph node detail and adjacent evidence. */
public record GraphNodeDetailResponse(
    GraphNodeResponse node,
    List<GraphNodeResponse> adjacentNodes,
    List<GraphEdgeResponse> adjacentEdges,
    List<GraphEvidenceReferenceResponse> evidenceReferences) {}
