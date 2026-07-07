package com.atlas.metadata.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** Retrieval quality metrics API contract tests against Flyway-managed PostgreSQL. */
class RetrievalQualityMetricsApiContractIT extends AbstractPostgresIT {

  @Autowired private MockMvc mockMvc;

  @Test
  void runAndSpaceMetricsExposeSafeQualitySignals() throws Exception {
    String spaceId = createSpace("Retrieval Metrics Space");
    String batchId = createBatch(spaceId);
    String approvedChunkId = chunkIds(firstFileId(batchId)).getFirst();
    indexChunk(spaceId, batchId, approvedChunkId);

    MvcResult askResult = createAsk(spaceId, "Which evidence is approved?", null);
    String runId = JsonPath.read(askResult.getResponse().getContentAsString(), "$.data.runId");

    mockMvc
        .perform(get("/api/ask-runs/" + runId + "/quality-metrics"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.runId").value(runId))
        .andExpect(jsonPath("$.data.status").value("SUCCEEDED"))
        .andExpect(jsonPath("$.data.evidenceCoverage.evidenceCount").value(1))
        .andExpect(jsonPath("$.data.evidenceCoverage.coverageRatio").value(1.0))
        .andExpect(jsonPath("$.data.citationHealth.unhealthy").value(false))
        .andExpect(jsonPath("$.data.confidence.band").value("MEDIUM"))
        .andExpect(jsonPath("$.data.reviewEligibility.reviewEligible").value(true))
        .andExpect(jsonPath("$.data.noEvidenceRefusal").value(false))
        .andExpect(jsonPath("$").value(not(containsString("Which evidence is approved?"))))
        .andExpect(jsonPath("$").value(not(containsString("Mock chat summary"))))
        .andExpect(jsonPath("$").value(not(containsString(System.getProperty("user.home")))));

    createAsk(spaceId, "Only spreadsheet evidence?", "xlsx");

    mockMvc
        .perform(get("/api/spaces/" + spaceId + "/retrieval-quality-metrics"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.spaceId").value(spaceId))
        .andExpect(jsonPath("$.data.totalRuns").value(2))
        .andExpect(jsonPath("$.data.succeededRuns").value(1))
        .andExpect(jsonPath("$.data.noEvidenceRefusals").value(1))
        .andExpect(jsonPath("$.data.reviewEligibleRuns").value(1))
        .andExpect(jsonPath("$.data.averageEvidenceCoverage").value(0.5))
        .andExpect(jsonPath("$").value(not(containsString("Which evidence is approved?"))))
        .andExpect(jsonPath("$").value(not(containsString("Only spreadsheet evidence?"))));
  }

  @Test
  void unknownRunAndSpaceReturnSafeErrors() throws Exception {
    mockMvc
        .perform(get("/api/ask-runs/missing-run/quality-metrics"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("NOT_FOUND"))
        .andExpect(jsonPath("$").value(not(containsString("Exception"))));

    mockMvc
        .perform(get("/api/spaces/missing-space/retrieval-quality-metrics"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code").value("NOT_FOUND"))
        .andExpect(jsonPath("$").value(not(containsString("Exception"))));
  }

  private MvcResult createAsk(String spaceId, String question, String sourceType) throws Exception {
    String filters =
        sourceType == null
            ? ""
            : """
            ,
            "filters": {
              "sourceTypes": ["%s"]
            }
            """
                .formatted(sourceType);
    return mockMvc
        .perform(
            post("/api/spaces/" + spaceId + "/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "question": "%s",
                      "requestedBy": "delivery-lead",
                      "reviewPolicy": "APPROVED_ONLY",
                      "limit": 5,
                      "mode": "mock"%s
                    }
                    """
                        .formatted(question, filters)))
        .andExpect(status().isCreated())
        .andReturn();
  }

  private void indexChunk(String spaceId, String batchId, String chunkId) throws Exception {
    mockMvc
        .perform(
            post("/api/spaces/" + spaceId + "/vector-runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "mock-vector",
                      "operation": "INDEX",
                      "batchId": "%s",
                      "reviewPolicy": "APPROVED_ONLY",
                      "dimension": 3,
                      "requestedBy": "delivery-lead",
                      "mode": "mock",
                      "items": [
                        { "sourceChunkId": "%s", "vector": [0.100, 0.200, 0.300] }
                      ]
                    }
                    """
                        .formatted(batchId, chunkId)))
        .andExpect(status().isCreated());
  }

  private String createSpace(String name) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/spaces")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "name": "%s",
                          "description": "Mock-safe retrieval metrics test space.",
                          "type": "document",
                          "indexStrategy": "rag",
                          "owner": "delivery-lead"
                        }
                        """
                            .formatted(name)))
            .andExpect(status().isCreated())
            .andReturn();
    return JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
  }

  private String createBatch(String spaceId) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/spaces/" + spaceId + "/batches")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "name": "Retrieval Metrics Package",
                          "sourceKind": "folder",
                          "owner": "Delivery Lead",
                          "files": [
                            {
                              "sourcePath": "Metrics/BRD.md",
                              "sourceType": "pdf",
                              "status": "MARKDOWN_GENERATED",
                              "confidence": 0.930,
                              "reviewStatus": "REVIEW_REQUIRED",
                              "markdownPath": "generated/markdown/metrics-brd.md",
                              "chunks": [
                                {
                                  "sourceFile": "Metrics/BRD.md",
                                  "page": 1,
                                  "section": "Approved metrics overview",
                                  "confidence": 0.930,
                                  "reviewStatus": "APPROVED"
                                }
                              ]
                            }
                          ]
                        }
                        """))
            .andExpect(status().isCreated())
            .andReturn();
    return JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
  }

  private String firstFileId(String batchId) throws Exception {
    MvcResult result =
        mockMvc.perform(get("/api/batches/" + batchId + "/files")).andExpect(status().isOk()).andReturn();
    List<String> fileIds = JsonPath.read(result.getResponse().getContentAsString(), "$.data[*].id");
    return fileIds.getFirst();
  }

  private List<String> chunkIds(String fileId) throws Exception {
    MvcResult result =
        mockMvc.perform(get("/api/files/" + fileId + "/chunks")).andExpect(status().isOk()).andReturn();
    return JsonPath.read(result.getResponse().getContentAsString(), "$.data[*].id");
  }
}
