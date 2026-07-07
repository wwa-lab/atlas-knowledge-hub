package com.atlas.metadata.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** API contract tests for connector sync v0. */
class ConnectorSyncV0ApiContractIT extends AbstractPostgresIT {

  @Autowired private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void listsConnectorDefinitionsWithoutCredentialsOrProviders() throws Exception {
    mockMvc
        .perform(get("/api/connector-definitions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success", is(true)))
        .andExpect(jsonPath("$.data[0].connectorKey", is("mock-local-fixture")))
        .andExpect(jsonPath("$.data[0].configurationState", is("MOCK_CONFIGURED")))
        .andExpect(jsonPath("$.data[0].reviewPolicy", is("REVIEW_REQUIRED")))
        .andExpect(jsonPath("$", not(containsString("api" + "Key"))))
        .andExpect(jsonPath("$", not(containsString("OAuth"))));
  }

  @Test
  void createsReviewRequiredSyncRunAndPreservesSourceTrace() throws Exception {
    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/spaces/ibm-i-modernization/connector-sync-jobs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "connectorKey": "mock-local-fixture",
                          "requestedBy": "connector-test",
                          "sourceScope": "sample-fixture"
                        }
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data.status", is("REVIEW_REQUIRED")))
            .andExpect(jsonPath("$.data.itemCount", is(3)))
            .andExpect(jsonPath("$.data.reviewRequiredCount", is(2)))
            .andExpect(jsonPath("$.data.failedCount", is(1)))
            .andReturn();

    String runId = data(createResult).get("runId").asText();

    mockMvc
        .perform(get("/api/connector-sync-runs/" + runId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.runId", is(runId)))
        .andExpect(jsonPath("$.data.connectorKey", is("mock-local-fixture")));

    mockMvc
        .perform(get("/api/connector-sync-runs/" + runId + "/items"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(3)))
        .andExpect(jsonPath("$.data[0].sourceTrace.connectorKey", is("mock-local-fixture")))
        .andExpect(jsonPath("$.data[0].provenance.syncRunId", is(runId)))
        .andExpect(jsonPath("$.data[0].reviewEligible", is(true)))
        .andExpect(jsonPath("$.data[0].outputArtifacts[0].reviewStatus", is("REVIEW_REQUIRED")))
        .andExpect(jsonPath("$.data[2].safeErrorCategory", is("SOURCE_UNREADABLE")))
        .andExpect(jsonPath("$.data[2].reviewEligible", is(false)))
        .andExpect(jsonPath("$.data[2].safeErrorMessage", notNullValue()))
        .andExpect(jsonPath("$", not(containsString("internal" + ".invalid"))))
        .andExpect(jsonPath("$", not(containsString("/" + "Users"))))
        .andExpect(jsonPath("$", not(containsString("to" + "ken=mock"))));
  }

  @Test
  void invalidRequestsAndUnknownRunsUseSafeErrors() throws Exception {
    mockMvc
        .perform(
            post("/api/spaces/ibm-i-modernization/connector-sync-jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code", is("VALIDATION_FAILED")));

    mockMvc
        .perform(get("/api/connector-sync-runs/missing-run"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code", is("NOT_FOUND")))
        .andExpect(jsonPath("$", not(containsString("Exception"))));
  }

  private JsonNode data(MvcResult result) throws Exception {
    return objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
  }
}
