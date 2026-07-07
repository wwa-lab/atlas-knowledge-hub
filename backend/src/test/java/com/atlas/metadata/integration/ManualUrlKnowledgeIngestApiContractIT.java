package com.atlas.metadata.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** API contract tests for metadata-only manual URL ingest. */
class ManualUrlKnowledgeIngestApiContractIT extends AbstractPostgresIT {

  @Autowired private MockMvc mockMvc;

  @Test
  void registersManualUrlSourceAsReviewRequiredTraceableMetadataOnlyArtifact() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/spaces/ibm-i-modernization/manual-url-sources")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "url": "https://example.com/reference/page",
                          "title": "Vendor reference page",
                          "description": "Sample-safe manual URL metadata.",
                          "fetchIntent": "FETCH_LATER",
                          "createdBy": "frontend-user"
                        }
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.displayUrl").value("https://example.com/reference/page"))
            .andExpect(jsonPath("$.data.host").value("example.com"))
            .andExpect(jsonPath("$.data.fetchPolicy").value("NO_FETCH_METADATA_ONLY"))
            .andExpect(jsonPath("$.data.ingestStatus").value("REVIEW_REQUIRED"))
            .andExpect(jsonPath("$.data.reviewStatus").value("REVIEW_REQUIRED"))
            .andExpect(jsonPath("$.data.eligibilityStatus").value("REVIEW_REQUIRED_ONLY"))
            .andExpect(jsonPath("$.data.confidence").value(0.300))
            .andExpect(jsonPath("$.data.sourceTrace").value(containsString("https://example.com/reference/page")))
            .andReturn();

    String sourceId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
    String batchId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.batchId");
    String fileItemId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.fileItemId");

    mockMvc
        .perform(get("/api/spaces/ibm-i-modernization/manual-url-sources"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[*].id", hasItem(sourceId)))
        .andExpect(jsonPath("$.data[*].displayUrl", hasItem("https://example.com/reference/page")));

    mockMvc
        .perform(get("/api/manual-url-sources/" + sourceId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(sourceId))
        .andExpect(jsonPath("$.data.batchId").value(batchId))
        .andExpect(jsonPath("$.data.fileItemId").value(fileItemId));

    mockMvc
        .perform(get("/api/batches/" + batchId + "/files"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].id").value(fileItemId))
        .andExpect(jsonPath("$.data[0].sourceType").value("url"))
        .andExpect(jsonPath("$.data[0].status").value("REVIEW_REQUIRED"))
        .andExpect(jsonPath("$.data[0].reviewStatus").value("REVIEW_REQUIRED"));

    mockMvc
        .perform(get("/api/files/" + fileItemId + "/chunks"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].sourceFile").value("https://example.com/reference/page"))
        .andExpect(jsonPath("$.data[0].section").value("Manual URL metadata"))
        .andExpect(jsonPath("$.data[0].reviewStatus").value("REVIEW_REQUIRED"));
  }

  @Test
  void rejectsUnsafeUrlsWithoutEchoingCredentialsPrivateHostsOrStackTraces() throws Exception {
    String unsafeUrl =
        "https://user:pass"
            + "word@localhost/private/path?"
            + "tok"
            + "en=secret-value#frag";

    mockMvc
        .perform(
            post("/api/spaces/ibm-i-modernization/manual-url-sources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "url": "%s",
                      "title": "Unsafe URL sample"
                    }
                    """
                        .formatted(unsafeUrl)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.error.fields.url").exists())
        .andExpect(jsonPath("$").value(not(containsString("secret-value"))))
        .andExpect(jsonPath("$").value(not(containsString("localhost/private"))))
        .andExpect(jsonPath("$").value(not(containsString("pass" + "word"))))
        .andExpect(jsonPath("$").value(not(containsString("Exception"))));
  }

  @Test
  void rejectsSensitiveTitleAndPathMetadata() throws Exception {
    mockMvc
        .perform(
            post("/api/spaces/ibm-i-modernization/manual-url-sources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "url": "https://example.com/reference/tokenized-page",
                      "title": "Contains API token"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.error.fields.url").exists())
        .andExpect(jsonPath("$").value(not(containsString("tokenized-page"))))
        .andExpect(jsonPath("$").value(not(containsString("API token"))));
  }
}
