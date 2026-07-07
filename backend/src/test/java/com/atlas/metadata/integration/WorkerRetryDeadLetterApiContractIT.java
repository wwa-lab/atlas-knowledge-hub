package com.atlas.metadata.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** API contract tests for local worker retry and dead-letter inspection. */
class WorkerRetryDeadLetterApiContractIT extends AbstractPostgresIT {

  @Autowired private MockMvc mockMvc;

  @Test
  void listsAndDetailsSeededDeadLetterWithoutRawInternals() throws Exception {
    mockMvc
        .perform(get("/api/dead-letter-entries"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success", is(true)))
        .andExpect(
            jsonPath("$.data[?(@.id=='dead-letter-local-fixture-001')].status", contains("OPEN")))
        .andExpect(
            jsonPath(
                "$.data[?(@.id=='dead-letter-local-fixture-001')].jobType",
                contains("CONNECTOR_SYNC")))
        .andExpect(
            jsonPath(
                "$.data[?(@.id=='dead-letter-local-fixture-001')].safeErrorCategory",
                contains("SOURCE_UNREADABLE")))
        .andExpect(
            jsonPath(
                "$.data[?(@.id=='dead-letter-local-fixture-001')].sourceTrace.connectorKey",
                contains("mock-local-fixture")))
        .andExpect(jsonPath("$", not(containsString("Exception"))))
        .andExpect(jsonPath("$", not(containsString("/" + "Users"))))
        .andExpect(jsonPath("$", not(containsString("api" + "Key"))));

    mockMvc
        .perform(get("/api/dead-letter-entries/dead-letter-local-fixture-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.attempts[0].attemptNumber", is(1)))
        .andExpect(jsonPath("$.data.attempts[0].status", is("FAILED_RETRYABLE")))
        .andExpect(jsonPath("$.data.job.status", anyOf(is("DEAD_LETTERED"), is("ACKNOWLEDGED"))));
  }

  @Test
  void manualRetryAndAcknowledgeUseSafePredictableTransitions() throws Exception {
    mockMvc
        .perform(
            post("/api/dead-letter-entries/dead-letter-action-fixture-001/retry")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"operator\":\"api-contract\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status", is("RETRIED")))
        .andExpect(jsonPath("$.data.job.status", is("WAITING_RETRY")))
        .andExpect(jsonPath("$.data.job.nextRetryAt", notNullValue()));

    mockMvc
        .perform(
            post("/api/dead-letter-entries/dead-letter-action-fixture-001/acknowledge")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"operator\":\"api-contract\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status", is("ACKNOWLEDGED")))
        .andExpect(jsonPath("$.data.job.status", is("ACKNOWLEDGED")));
  }

  @Test
  void missingRecordsUseSafeEnvelope() throws Exception {
    mockMvc
        .perform(get("/api/dead-letter-entries/missing-entry"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code", is("NOT_FOUND")))
        .andExpect(jsonPath("$", not(containsString("Exception"))));
  }
}
