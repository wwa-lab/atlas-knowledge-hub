package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.GraphEdgeType;
import com.atlas.metadata.enums.GraphNodeType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            List.of(),
            null));
    if (request.wikiPages() != null && !request.wikiPages().isEmpty()) {
      projectWikiPages(request, spaceNodeId, nodes, edges);
      return new GraphProjectionResult(nodes, edges);
    }
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
              List.of(),
              source.confidence()));
      edges.add(
          new ProjectedEdge(
              "edge-source-mentions-" + source.sourceChunkId(),
              spaceNodeId,
              conceptNodeId,
              GraphEdgeType.MENTIONS,
              source.reviewStatus(),
              List.of(source.sourceChunkId()),
              List.of(),
              source.confidence()));
    }
    return new GraphProjectionResult(nodes, edges);
  }

  private void projectWikiPages(
      GraphProjectionRequest request,
      String spaceNodeId,
      List<ProjectedNode> nodes,
      List<ProjectedEdge> edges) {
    Map<String, String> pageNodeIds = new HashMap<>();
    request.wikiPages().stream()
        .sorted(Comparator.comparing(GraphWikiPageDescriptor::wikiPageId))
        .forEach(
            page -> {
              String pageNodeId = wikiPageNodeId(request.spaceId(), page);
              pageNodeIds.put(page.wikiPageId(), pageNodeId);
              pageNodeIds.put(page.slug(), pageNodeId);
              nodes.add(
                  new ProjectedNode(
                      pageNodeId,
                      page.title(),
                      GraphNodeType.WIKI_PAGE,
                      page.reviewStatus(),
                      chunkIds(page),
                      List.of(page.wikiPageId()),
                      page.confidence()));
              edges.add(
                  new ProjectedEdge(
                      "edge-space-contains-wiki-" + slug(request.spaceId()) + "-" + slug(page.wikiPageId()),
                      spaceNodeId,
                      pageNodeId,
                      GraphEdgeType.CONTAINS,
                      page.reviewStatus(),
                      List.of(),
                      List.of(page.wikiPageId()),
                      page.confidence()));
              for (String sourceDocumentId : sorted(page.sourceDocumentIds())) {
                String documentNodeId =
                    "node-document-" + slug(request.spaceId()) + "-" + slug(sourceDocumentId);
                nodes.add(
                    new ProjectedNode(
                        documentNodeId,
                        sourceDocumentId,
                        GraphNodeType.DOCUMENT,
                        page.reviewStatus(),
                        List.of(),
                        List.of(page.wikiPageId()),
                        page.confidence()));
                edges.add(
                    new ProjectedEdge(
                        "edge-wiki-derived-"
                            + slug(request.spaceId())
                            + "-"
                            + slug(page.wikiPageId())
                            + "-"
                            + slug(sourceDocumentId),
                        pageNodeId,
                        documentNodeId,
                        GraphEdgeType.DERIVED_FROM,
                        page.reviewStatus(),
                        List.of(),
                        List.of(page.wikiPageId()),
                        page.confidence()));
              }
              for (GraphWikiEvidenceDescriptor chunkRef : sortedRefs(page.chunkRefs())) {
                String conceptLabel = label(chunkRef);
                String conceptNodeId =
                    "node-concept-"
                        + slug(request.spaceId())
                        + "-"
                        + slug(page.wikiPageId())
                        + "-"
                        + slug(chunkRef.referenceId());
                nodes.add(
                    new ProjectedNode(
                        conceptNodeId,
                        conceptLabel,
                        GraphNodeType.CONCEPT,
                        page.reviewStatus(),
                        List.of(chunkRef.referenceId()),
                        List.of(page.wikiPageId()),
                        page.confidence()));
                edges.add(
                    new ProjectedEdge(
                        "edge-wiki-mentions-"
                            + slug(request.spaceId())
                            + "-"
                            + slug(page.wikiPageId())
                            + "-"
                            + slug(chunkRef.referenceId()),
                        pageNodeId,
                        conceptNodeId,
                        GraphEdgeType.MENTIONS,
                        page.reviewStatus(),
                        List.of(chunkRef.referenceId()),
                        List.of(page.wikiPageId()),
                        page.confidence()));
              }
            });
    request.wikiPages().stream()
        .sorted(Comparator.comparing(GraphWikiPageDescriptor::wikiPageId))
        .forEach(
            page -> {
              String sourceNodeId = pageNodeIds.get(page.wikiPageId());
              for (String outLink : sorted(page.outLinks())) {
                String targetNodeId = pageNodeIds.get(outLink);
                if (targetNodeId == null || targetNodeId.equals(sourceNodeId)) {
                  continue;
                }
                edges.add(
                    new ProjectedEdge(
                        "edge-wiki-related-"
                            + slug(request.spaceId())
                            + "-"
                            + slug(page.wikiPageId())
                            + "-"
                            + slug(outLink),
                        sourceNodeId,
                        targetNodeId,
                        GraphEdgeType.RELATED_TO,
                        page.reviewStatus(),
                        chunkIds(page),
                        List.of(page.wikiPageId()),
                        page.confidence()));
              }
            });
  }

  private String wikiPageNodeId(String spaceId, GraphWikiPageDescriptor page) {
    return "node-wiki-" + slug(spaceId) + "-" + slug(page.wikiPageId());
  }

  private List<String> chunkIds(GraphWikiPageDescriptor page) {
    return sortedRefs(page.chunkRefs()).stream().map(GraphWikiEvidenceDescriptor::referenceId).toList();
  }

  private List<String> sorted(List<String> values) {
    return values == null ? List.of() : values.stream().filter(value -> value != null && !value.isBlank()).sorted().toList();
  }

  private List<GraphWikiEvidenceDescriptor> sortedRefs(List<GraphWikiEvidenceDescriptor> values) {
    return values == null
        ? List.of()
        : values.stream()
            .filter(value -> value.referenceId() != null && !value.referenceId().isBlank())
            .sorted(Comparator.comparing(GraphWikiEvidenceDescriptor::referenceId))
            .toList();
  }

  private String label(GraphWikiEvidenceDescriptor reference) {
    if (reference.label() != null && !reference.label().isBlank() && !"source chunk".equals(reference.label())) {
      return reference.label();
    }
    if (reference.locator() != null && !reference.locator().isBlank()) {
      return reference.locator();
    }
    return reference.referenceId();
  }

  private String slug(String value) {
    return value == null ? "unknown" : value.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
  }
}
