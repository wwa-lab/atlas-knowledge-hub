package com.atlas.metadata.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
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

/** Vector adapter API contract tests against Flyway-managed PostgreSQL. */
class VectorApiContractIT extends AbstractPostgresIT {

  @Autowired private MockMvc mockMvc;

  @Test
  void capabilityEndpointReturnsMaskedReplaceableVectorAdapters() throws Exception {
    mockMvc
        .perform(get("/api/vector-adapters"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].adapterKey").value("mock-vector"))
        .andExpect(jsonPath("$.data[0].defaultAdapter").value(true))
        .andExpect(jsonPath("$.data[0].status").value("AVAILABLE"))
        .andExpect(jsonPath("$.data[0].supportedOperations", hasItem("INDEX")))
        .andExpect(jsonPath("$.data[0].supportedOperations", hasItem("QUERY")))
        .andExpect(jsonPath("$.data[0].maskedConfigSummary.endpoint").value("not_configured"))
        .andExpect(jsonPath("$.data[0].maskedConfigSummary.credentials").value("not_configured"))
        .andExpect(jsonPath("$.data[1].adapterKey").value("pgvector"))
        .andExpect(jsonPath("$.data[1].status").value("MISCONFIGURED"))
        .andExpect(jsonPath("$").value(not(containsString(System.getProperty("user.home")))))
        .andExpect(jsonPath("$").value(not(containsString("password"))))
        .andExpect(jsonPath("$").value(not(containsString("https://"))));
  }

  @Test
  void vectorIndexQueryAndDeindexPreserveTraceAndReviewState() throws Exception {
    String batchId = createBatch();
    List<String> chunkIds = chunkIds(firstFileId(batchId));
    String approvedChunkId = chunkIds.get(0);

    MvcResult runResult =
        mockMvc
            .perform(
                post("/api/spaces/ibm-i-modernization/vector-runs")
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
                            .formatted(batchId, approvedChunkId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.adapterKey").value("mock-vector"))
            .andExpect(jsonPath("$.data.operation").value("INDEX"))
            .andExpect(jsonPath("$.data.status").value("PARTIAL_FAILED"))
            .andExpect(jsonPath("$.data.summary.totalCount").value(2))
            .andExpect(jsonPath("$.data.summary.indexedCount").value(1))
            .andExpect(jsonPath("$.data.summary.skippedCount").value(1))
            .andExpect(jsonPath("$.data.results[*].sourceChunkId", hasItem(approvedChunkId)))
            .andExpect(jsonPath("$.data.results[*].reviewStatus", hasItem("APPROVED")))
            .andExpect(jsonPath("$.data.results[*].reviewStatus", hasItem("REVIEW_REQUIRED")))
            .andExpect(jsonPath("$.data.results[*].vectorItemKey", hasItem("ibm-i-modernization/" + approvedChunkId)))
            .andReturn();

    String runId = JsonPath.read(runResult.getResponse().getContentAsString(), "$.data.runId");

    mockMvc
        .perform(get("/api/vector-runs/" + runId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.runId").value(runId))
        .andExpect(jsonPath("$.data.results[*].sourceFile", hasItem("Vector/BRD.md")))
        .andExpect(jsonPath("$.data.results[*].confidence", hasItem(0.93)));

    mockMvc
        .perform(
            post("/api/spaces/ibm-i-modernization/vector-query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "mock-vector",
                      "queryVector": [0.100, 0.200, 0.300],
                      "limit": 5
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.reviewPolicy").value("APPROVED_ONLY"))
        .andExpect(jsonPath("$.data.matches[0].sourceChunkId").value(approvedChunkId))
        .andExpect(jsonPath("$.data.matches[0].score").value(1.000))
        .andExpect(jsonPath("$.data.matches[0].reviewStatus").value("APPROVED"))
        .andExpect(jsonPath("$.data.matches[0].safeMetadata.vectorItemKey").value("ibm-i-modernization/" + approvedChunkId))
        .andExpect(jsonPath("$.data.matches[0].safeMetadata").value(not(containsString("https://"))));

    mockMvc
        .perform(
            post("/api/spaces/ibm-i-modernization/vector-query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "mock-vector",
                      "mockQuery": "vector-contract-query",
                      "limit": 5,
                      "reviewPolicy": "INCLUDE_REVIEW_REQUIRED"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.reviewPolicy").value("INCLUDE_REVIEW_REQUIRED"))
        .andExpect(jsonPath("$.data.matches[*].reviewStatus", hasItem("APPROVED")));

    mockMvc
        .perform(
            post("/api/spaces/ibm-i-modernization/vector-runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "mock-vector",
                      "operation": "DEINDEX",
                      "batchId": "%s",
                      "sourceChunkIds": ["%s"],
                      "requestedBy": "delivery-lead",
                      "mode": "mock"
                    }
                    """
                        .formatted(batchId, approvedChunkId)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.operation").value("DEINDEX"))
        .andExpect(jsonPath("$.data.summary.deletedCount").value(1))
        .andExpect(jsonPath("$.data.results[0].sourceChunkId").value(approvedChunkId))
        .andExpect(jsonPath("$.data.results[0].reviewStatus").value("APPROVED"));
  }

  @Test
  void vectorApiRejectsInvalidDimensionAndReportsMisconfiguredAdapterSafely() throws Exception {
    String batchId = createBatch();

    mockMvc
        .perform(
            post("/api/spaces/ibm-i-modernization/vector-runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "mock-vector",
                      "operation": "INDEX",
                      "batchId": "%s",
                      "dimension": 42,
                      "mode": "mock"
                    }
                    """
                        .formatted(batchId)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.error.fields.dimension").exists());

    mockMvc
        .perform(
            post("/api/spaces/ibm-i-modernization/vector-runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "pgvector",
                      "operation": "INDEX",
                      "batchId": "%s",
                      "dimension": 384,
                      "mode": "mock"
                    }
                    """
                        .formatted(batchId)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.adapterKey").value("pgvector"))
        .andExpect(jsonPath("$.data.status").value("FAILED"))
        .andExpect(jsonPath("$.data.safeMessage").value("Vector adapter is unavailable."))
        .andExpect(jsonPath("$.data.results").isEmpty())
        .andExpect(jsonPath("$").value(not(containsString("https://"))))
        .andExpect(jsonPath("$").value(not(containsString(System.getProperty("user.home")))));
  }

  private String createBatch() throws Exception {
    MvcResult batchResult =
        mockMvc
            .perform(
                post("/api/spaces/ibm-i-modernization/batches")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "name": "Vector Contract Package",
                          "sourceKind": "folder",
                          "owner": "Delivery Lead",
                          "files": [
                            {
                              "sourcePath": "Vector/BRD.md",
                              "sourceType": "pdf",
                              "status": "MARKDOWN_GENERATED",
                              "confidence": 0.930,
                              "reviewStatus": "REVIEW_REQUIRED",
                              "markdownPath": "generated/markdown/vector-brd.md",
                              "chunks": [
                                {
                                  "sourceFile": "Vector/BRD.md",
                                  "page": 1,
                                  "section": "Approved overview",
                                  "confidence": 0.930,
                                  "reviewStatus": "APPROVED"
                                },
                                {
                                  "sourceFile": "Vector/BRD.md",
                                  "page": 2,
                                  "section": "Review required appendix",
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
