package com.atlas.metadata.dto;

import java.util.List;
import java.util.Map;

/** Bounded graph view response. */
public record GraphViewResponse(
    String spaceId,
    List<GraphNodeResponse> nodes,
    List<GraphEdgeResponse> edges,
    Map<String, Integer> counts) {}
