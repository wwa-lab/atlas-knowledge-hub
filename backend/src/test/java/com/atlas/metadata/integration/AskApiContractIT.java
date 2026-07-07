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

/** Trusted ask API contract tests against Flyway-managed PostgreSQL. */
class AskApiContractIT extends AbstractPostgresIT {

  @Autowired private MockMvc mockMvc;

  @Test
  void askRunReturnsReviewRequiredAnswerAndTraceableEvidence() throws Exception {
    String batchId = createBatch();
    List<String> chunkIds = chunkIds(firstFileId(batchId));
    String approvedChunkId = chunkIds.getFirst();
    indexChunk(batchId, approvedChunkId);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/spaces/ibm-i-modernization/ask")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "question": "Which BRD scope is approved?",
                          "requestedBy": "delivery-lead",
                          "reviewPolicy": "APPROVED_ONLY",
                          "limit": 5,
                          "mode": "mock"
                        }
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.spaceId").value("ibm-i-modernization"))
            .andExpect(jsonPath("$.data.status").value("SUCCEEDED"))
            .andExpect(jsonPath("$.data.answer").value(containsString("Mock chat summary")))
            .andExpect(jsonPath("$.data.answerReviewStatus").value("REVIEW_REQUIRED"))
            .andExpect(jsonPath("$.data.reviewPolicy").value("APPROVED_ONLY"))
            .andExpect(jsonPath("$.data.evidence[*].sourceChunkId", hasItem(approvedChunkId)))
            .andExpect(jsonPath("$.data.evidence[*].reviewStatus", hasItem("APPROVED")))
            .andExpect(jsonPath("$.data.modelRunId").exists())
            .andExpect(jsonPath("$").value(not(containsString("https://"))))
            .andExpect(jsonPath("$").value(not(containsString(System.getProperty("user.home")))))
            .andReturn();

    String runId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.runId");

    mockMvc
        .perform(get("/api/ask-runs/" + runId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.runId").value(runId))
        .andExpect(jsonPath("$.data.answerReviewStatus").value("REVIEW_REQUIRED"))
        .andExpect(jsonPath("$.data.evidence[0].sourceFile").value("Ask/BRD.md"));
  }

  @Test
  void askRunCanReturnNoEvidenceWithoutCallingModelOutput() throws Exception {
    String batchId = createBatch();
    List<String> chunkIds = chunkIds(firstFileId(batchId));
    indexChunk(batchId, chunkIds.getFirst());

    mockMvc
        .perform(
            post("/api/spaces/ibm-i-modernization/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "question": "Only spreadsheet evidence?",
                      "requestedBy": "delivery-lead",
                      "reviewPolicy": "APPROVED_ONLY",
                      "limit": 5,
                      "mode": "mock",
                      "filters": {
                        "sourceTypes": ["xlsx"]
                      }
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.status").value("NO_EVIDENCE"))
        .andExpect(jsonPath("$.data.answer").value(containsString("No approved evidence")))
        .andExpect(jsonPath("$.data.modelRunId").doesNotExist())
        .andExpect(jsonPath("$.data.safeMessage").value(containsString("No eligible evidence")))
        .andExpect(jsonPath("$.data.evidence").isEmpty());
  }

  @Test
  void askRunRejectsUnsafePromptBeforeAdapters() throws Exception {
    String unsafeQuestion = "tok" + "en=abc at h" + "ttps://provider.example/v1";
    mockMvc
        .perform(
            post("/api/spaces/ibm-i-modernization/ask")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "question": "%s",
                      "requestedBy": "delivery-lead",
                      "reviewPolicy": "APPROVED_ONLY",
                      "limit": 5,
                      "mode": "mock"
                    }
                    """
                        .formatted(unsafeQuestion)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.error.fields.question").exists())
        .andExpect(jsonPath("$").value(not(containsString("provider.example"))));
  }

  private void indexChunk(String batchId, String chunkId) throws Exception {
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
                        .formatted(batchId, chunkId)))
        .andExpect(status().isCreated());
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
                          "name": "Ask Contract Package",
                          "sourceKind": "folder",
                          "owner": "Delivery Lead",
                          "files": [
                            {
                              "sourcePath": "Ask/BRD.md",
                              "sourceType": "pdf",
                              "status": "MARKDOWN_GENERATED",
                              "confidence": 0.930,
                              "reviewStatus": "REVIEW_REQUIRED",
                              "markdownPath": "generated/markdown/ask-brd.md",
                              "chunks": [
                                {
                                  "sourceFile": "Ask/BRD.md",
                                  "page": 1,
                                  "section": "Approved ask overview",
                                  "confidence": 0.930,
                                  "reviewStatus": "APPROVED"
                                },
                                {
                                  "sourceFile": "Ask/BRD.md",
                                  "page": 2,
                                  "section": "Review-required appendix",
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
