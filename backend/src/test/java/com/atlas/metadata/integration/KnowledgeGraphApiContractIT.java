package com.atlas.metadata.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atlas.metadata.domain.Space;
import com.atlas.metadata.domain.WikiPage;
import com.atlas.metadata.enums.IndexStrategy;
import com.atlas.metadata.enums.SpaceType;
import com.atlas.metadata.repository.SourceChunkRepository;
import com.atlas.metadata.repository.SpaceRepository;
import com.atlas.metadata.repository.WikiPageRepository;
import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** Knowledge graph API contract tests against Flyway-managed PostgreSQL. */
class KnowledgeGraphApiContractIT extends AbstractPostgresIT {

  private static final String GRAPH_SPACE_ID = "graph-contract-space";

  @Autowired private MockMvc mockMvc;
  @Autowired private SpaceRepository spaceRepository;
  @Autowired private SourceChunkRepository sourceChunkRepository;
  @Autowired private WikiPageRepository wikiPageRepository;

  @Test
  void graphEndpointsRequireAuthenticationAndAllowAuthorizedViewerReads() throws Exception {
    mockMvc
        .perform(get("/api/spaces/ibm-i-modernization/graph").header("X-Atlas-User", "__missing__"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"));

    mockMvc
        .perform(get("/api/spaces/ibm-i-modernization/graph").header("X-Atlas-User", "viewer"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.spaceId").value("ibm-i-modernization"))
        .andExpect(jsonPath("$").value(not(containsString(System.getProperty("user.home")))));
  }

  @Test
  void projectionQueryDetailAndReviewPreserveEvidenceAndSafeEnvelope() throws Exception {
    createGraphSpace();
    String batchId = createApprovedBatch(GRAPH_SPACE_ID);
    String approvedChunkId = chunkIds(firstFileId(batchId)).getFirst();

    MvcResult runResult =
        mockMvc
            .perform(
                post("/api/spaces/" + GRAPH_SPACE_ID + "/graph/projection-runs")
                    .header("X-Atlas-User", "delivery-lead")
                    .header("X-Atlas-Role", "ADMIN")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "scope": "APPROVED_ONLY",
                          "adapterId": "deterministic",
                          "dryRun": false
                        }
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("PARTIAL_FAILED"))
            .andExpect(jsonPath("$.data.summary.createdCount").value(greaterThanOrEqualTo(3)))
            .andExpect(jsonPath("$.data.summary.skippedCount").value(greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.data.items[*].reasonCode", hasItem("UNAPPROVED_SOURCE")))
            .andExpect(jsonPath("$").value(not(containsString("https://"))))
            .andReturn();

    String runId = JsonPath.read(runResult.getResponse().getContentAsString(), "$.data.runId");

    mockMvc
        .perform(
            get("/api/graph/projection-runs/" + runId)
                .header("X-Atlas-User", "delivery-lead")
                .header("X-Atlas-Role", "VIEWER"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.runId").value(runId))
        .andExpect(jsonPath("$.data.items[*].sourceId", hasItem(approvedChunkId)));

    MvcResult graphResult =
        mockMvc
            .perform(
                get("/api/spaces/" + GRAPH_SPACE_ID + "/graph")
                    .header("X-Atlas-User", "viewer")
                    .header("X-Atlas-Role", "VIEWER")
                    .param("limit", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.nodes[*].type", hasItem("CONCEPT")))
            .andExpect(jsonPath("$.data.edges[*].type", hasItem("MENTIONS")))
            .andExpect(jsonPath("$.data.edges[*].reviewStatus", hasItem("APPROVED")))
            .andExpect(jsonPath("$.data.edges[*].evidenceCount", hasItem(1)))
            .andReturn();

    List<String> nodeIds = JsonPath.read(graphResult.getResponse().getContentAsString(), "$.data.nodes[*].id");
    String conceptNodeId = nodeIds.stream().filter(id -> id.contains("concept")).findFirst().orElseThrow();
    List<String> edgeIds = JsonPath.read(graphResult.getResponse().getContentAsString(), "$.data.edges[*].id");
    String edgeId = edgeIds.getFirst();

    mockMvc
        .perform(
            get("/api/spaces/" + GRAPH_SPACE_ID + "/graph/nodes/" + conceptNodeId)
                .header("X-Atlas-User", "viewer")
                .header("X-Atlas-Role", "VIEWER"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.node.id").value(conceptNodeId))
        .andExpect(jsonPath("$.data.evidenceReferences[0].sourceChunkId").value(approvedChunkId))
        .andExpect(jsonPath("$.data.evidenceReferences[0].confidence").value(0.93))
        .andExpect(jsonPath("$.data.evidenceReferences[0].reviewStatus").value("APPROVED"));

    mockMvc
        .perform(
            post("/api/spaces/" + GRAPH_SPACE_ID + "/graph/edges/" + edgeId + "/review-actions")
                .header("X-Atlas-User", "sme.alex")
                .header("X-Atlas-Role", "REVIEWER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "action": "NEED_FIX",
                      "comment": "Needs a clearer cited relationship."
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.targetType").value("graph_edge"))
        .andExpect(jsonPath("$.data.targetId").value(edgeId))
        .andExpect(jsonPath("$.data.action").value("NEED_FIX"));

    mockMvc
        .perform(
            post("/api/spaces/" + GRAPH_SPACE_ID + "/graph/edges/" + edgeId + "/review-actions")
                .header("X-Atlas-User", "sme.alex")
                .header("X-Atlas-Role", "REVIEWER")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.error.fields.action").exists());
  }

  @Test
  void graphProjectionExtractsFromEligibleWikiPagesAndReturnsMixedEvidence() throws Exception {
    String spaceId = "graph-wiki-extraction-space";
    createGraphSpace(spaceId);
    String batchId = createApprovedBatch(spaceId);
    String fileId = firstFileId(batchId);
    String approvedChunkId = chunkIds(fileId).getFirst();
    var approvedChunk = sourceChunkRepository.findById(approvedChunkId).orElseThrow();
    wikiPageRepository.save(
        WikiPage.publish(
            "wiki-graph-derived",
            spaceId,
            "Wiki Derived Architecture",
            "generated/markdown/wiki-derived-architecture.md",
            new String[] {fileId},
            List.of(approvedChunk),
            new BigDecimal("0.930"),
            "sme",
            OffsetDateTime.now()));
    wikiPageRepository.save(
        WikiPage.publish(
            "wiki-low-confidence",
            spaceId,
            "Low Confidence Wiki",
            "generated/markdown/low-confidence.md",
            new String[] {fileId},
            List.of(approvedChunk),
            new BigDecimal("0.700"),
            "sme",
            OffsetDateTime.now()));

    MvcResult runResult =
        mockMvc
            .perform(
                post("/api/spaces/" + spaceId + "/graph/projection-runs")
                    .header("X-Atlas-User", "delivery-lead")
                    .header("X-Atlas-Role", "ADMIN")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "scope": "APPROVED_ONLY",
                          "adapterId": "deterministic",
                          "dryRun": false
                        }
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.summary.createdCount").value(greaterThanOrEqualTo(4)))
            .andExpect(jsonPath("$.data.summary.skippedCount").value(1))
            .andExpect(jsonPath("$.data.items[*].reasonCode", hasItem("LOW_CONFIDENCE_WIKI_PAGE")))
            .andReturn();

    mockMvc
        .perform(
            post("/api/spaces/" + spaceId + "/graph/projection-runs")
                .header("X-Atlas-User", "delivery-lead")
                .header("X-Atlas-Role", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "scope": "APPROVED_ONLY",
                      "adapterId": "deterministic",
                      "dryRun": false
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.summary.updatedCount").value(greaterThanOrEqualTo(4)));

    MvcResult graphResult =
        mockMvc
            .perform(
                get("/api/spaces/" + spaceId + "/graph")
                    .header("X-Atlas-User", "viewer")
                    .header("X-Atlas-Role", "VIEWER")
                    .param("q", "Wiki Derived")
                    .param("limit", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.nodes[*].type", hasItem("WIKI_PAGE")))
            .andExpect(jsonPath("$.data.nodes[*].label", hasItem("Wiki Derived Architecture")))
            .andReturn();

    List<String> nodeIds = JsonPath.read(graphResult.getResponse().getContentAsString(), "$.data.nodes[*].id");
    String wikiNodeId = nodeIds.stream().filter(id -> id.contains("wiki-graph-derived")).findFirst().orElseThrow();

    mockMvc
        .perform(
            get("/api/spaces/" + spaceId + "/graph/nodes/" + wikiNodeId)
                .header("X-Atlas-User", "viewer")
                .header("X-Atlas-Role", "VIEWER"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.evidenceReferences[*].referenceType", hasItem("WIKI_PAGE")))
        .andExpect(jsonPath("$.data.evidenceReferences[*].referenceType", hasItem("SOURCE_CHUNK")))
        .andExpect(jsonPath("$.data.evidenceReferences[*].wikiPageId", hasItem("wiki-graph-derived")))
        .andExpect(jsonPath("$.data.evidenceReferences[*].sourceChunkId", hasItem(approvedChunkId)))
        .andExpect(jsonPath("$").value(not(containsString(System.getProperty("user.home")))))
        .andExpect(jsonPath("$").value(not(containsString("https://"))));

    String runId = JsonPath.read(runResult.getResponse().getContentAsString(), "$.data.runId");
    assertThat(runId).startsWith("graph-run-");
  }

  private void createGraphSpace() {
    createGraphSpace(GRAPH_SPACE_ID);
  }

  private void createGraphSpace(String spaceId) {
    spaceRepository.save(
        Space.create(
            spaceId,
            "Graph Contract Space",
            "Mock-only knowledge graph contract space.",
            SpaceType.document,
            IndexStrategy.rag,
            "QA Team",
            OffsetDateTime.now()));
  }

  private String createApprovedBatch(String spaceId) throws Exception {
    MvcResult batchResult =
        mockMvc
            .perform(
                post("/api/spaces/" + spaceId + "/batches")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "name": "Graph Contract Package",
                          "sourceKind": "folder",
                          "owner": "Delivery Lead",
                          "files": [
                            {
                              "sourcePath": "Graph/Modernization.md",
                              "sourceType": "pdf",
                              "status": "APPROVED",
                              "confidence": 0.930,
                              "reviewStatus": "REVIEW_REQUIRED",
                              "markdownPath": "generated/markdown/graph-modernization.md",
                              "chunks": [
                                {
                                  "sourceFile": "Graph/Modernization.md",
                                  "page": 1,
                                  "section": "RPGLE modernization",
                                  "confidence": 0.930,
                                  "reviewStatus": "APPROVED"
                                },
                                {
                                  "sourceFile": "Graph/Modernization.md",
                                  "page": 2,
                                  "section": "Draft migration appendix",
                                  "confidence": 0.710,
                                  "reviewStatus": "REVIEW_REQUIRED"
                                }
                              ]
                            }
                          ]
                        }
                        """))
            .andExpect(status().isCreated())
            .andReturn();
    return JsonPath.read(batchResult.getResponse().getContentAsString(), "$.data.id");
  }

  private String firstFileId(String batchId) throws Exception {
    MvcResult filesResult =
        mockMvc.perform(get("/api/batches/" + batchId + "/files")).andExpect(status().isOk()).andReturn();
    List<String> fileIds = JsonPath.read(filesResult.getResponse().getContentAsString(), "$.data[*].id");
    return fileIds.getFirst();
  }

  private List<String> chunkIds(String fileId) throws Exception {
    MvcResult chunksResult =
        mockMvc.perform(get("/api/files/" + fileId + "/chunks")).andExpect(status().isOk()).andReturn();
    return JsonPath.read(chunksResult.getResponse().getContentAsString(), "$.data[*].id");
  }
}
