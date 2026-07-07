package com.atlas.metadata.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atlas.metadata.config.AtlasAuthInterceptor;
import com.atlas.metadata.config.AtlasAuthWebConfig;
import com.atlas.metadata.config.LocalRateLimitInterceptor;
import com.atlas.metadata.controller.WikiIngestController;
import com.atlas.metadata.dto.CreateWikiIngestRunRequest;
import com.atlas.metadata.dto.WikiIngestRunResponse;
import com.atlas.metadata.exception.SafeErrorResponseFactory;
import com.atlas.metadata.exception.SafeErrorSanitizer;
import com.atlas.metadata.service.WikiIngestService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** API contract tests for deterministic Auto Wiki ingest v0. */
@WebMvcTest(
    controllers = WikiIngestController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {AtlasAuthInterceptor.class, AtlasAuthWebConfig.class, LocalRateLimitInterceptor.class}))
@Import({SafeErrorResponseFactory.class, SafeErrorSanitizer.class})
class WikiIngestApiContractIT {

  @Autowired private MockMvc mockMvc;
  @MockBean private WikiIngestService wikiIngestService;

  @Test
  void startIngestRunReturnsSafeAtlasEnvelope() throws Exception {
    when(wikiIngestService.startIngestRun(eq("ibm-i-modernization"), any(CreateWikiIngestRunRequest.class)))
        .thenReturn(response());

    mockMvc
        .perform(
            post("/api/spaces/ibm-i-modernization/wiki-ingest-runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "mode": "deterministic",
                      "sourceFileIds": ["file-001"],
                      "requestedBy": "knowledge-manager",
                      "dryRun": false
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.runId").value("wiki-ingest-run-001"))
        .andExpect(jsonPath("$.data.status").value("SUCCEEDED"))
        .andExpect(jsonPath("$.data.mode").value("deterministic"))
        .andExpect(jsonPath("$.data.createdPageIds[0]").value("wiki-auto-ibm-i-modernization-file-001"))
        .andExpect(jsonPath("$.data.eligibleChunkCount").value(1))
        .andExpect(jsonPath("$.data.excludedChunkCount").value(2))
        .andExpect(jsonPath("$").value(not(containsString(System.getProperty("user.home")))))
        .andExpect(jsonPath("$").value(not(containsString("password"))));
  }

  @Test
  void getIngestRunReturnsSafeRunSummary() throws Exception {
    when(wikiIngestService.getIngestRun("ibm-i-modernization", "wiki-ingest-run-001"))
        .thenReturn(response());

    mockMvc
        .perform(get("/api/spaces/ibm-i-modernization/wiki-generation-runs/wiki-ingest-run-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.runId").value("wiki-ingest-run-001"))
        .andExpect(jsonPath("$.data.safeSummary").value("Created safe review-required candidates."));
  }

  private WikiIngestRunResponse response() {
    return new WikiIngestRunResponse(
        "wiki-ingest-run-001",
        "ibm-i-modernization",
        "SUCCEEDED",
        "deterministic",
        List.of("wiki-auto-ibm-i-modernization-file-001"),
        List.of(),
        List.of(),
        1,
        2,
        "Created safe review-required candidates.",
        null,
        OffsetDateTime.parse("2026-07-05T00:00:00Z"),
        OffsetDateTime.parse("2026-07-05T00:00:01Z"));
  }
}
