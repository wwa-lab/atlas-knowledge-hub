package com.atlas.metadata.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atlas.metadata.controller.WikiLinkifyLintController;
import com.atlas.metadata.dto.CreateWikiLinkifyLintRunRequest;
import com.atlas.metadata.dto.WikiLinkifyLintRunResponse;
import com.atlas.metadata.service.WikiLinkifyLintService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** API contract tests for deterministic Wiki linkify/lint runs. */
@WebMvcTest(WikiLinkifyLintController.class)
class WikiLinkifyLintApiContractIT {

  @Autowired private MockMvc mockMvc;
  @MockBean private WikiLinkifyLintService wikiLinkifyLintService;

  @Test
  void startLinkifyLintRunReturnsSafeAtlasEnvelope() throws Exception {
    when(wikiLinkifyLintService.startLinkifyLintRun(
            eq("ibm-i-modernization"), any(CreateWikiLinkifyLintRunRequest.class)))
        .thenReturn(response());

    mockMvc
        .perform(
            post("/api/spaces/ibm-i-modernization/wiki-linkify-lint-runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "pageIds": ["wiki-auto-modernization-scope"],
                      "requestedBy": "knowledge-manager",
                      "dryRun": false,
                      "linkify": true,
                      "lint": true
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.runId").value("wiki-linkify-lint-run-001"))
        .andExpect(jsonPath("$.data.mode").value("linkify-lint"))
        .andExpect(jsonPath("$.data.scannedPageCount").value(4))
        .andExpect(jsonPath("$.data.insertedLinkCount").value(3))
        .andExpect(jsonPath("$.data.brokenLinkCount").value(1))
        .andExpect(jsonPath("$").value(not(containsString(System.getProperty("user.home")))))
        .andExpect(jsonPath("$").value(not(containsString("password"))));
  }

  private WikiLinkifyLintRunResponse response() {
    return new WikiLinkifyLintRunResponse(
        "wiki-linkify-lint-run-001",
        "ibm-i-modernization",
        "SUCCEEDED",
        "linkify-lint",
        4,
        List.of("wiki-auto-modernization-scope"),
        List.of("wiki-issue-broken-link-001"),
        3,
        1,
        0,
        0,
        0,
        "Scanned 4 Wiki pages, inserted 3 links, and recorded 1 issue.",
        null,
        OffsetDateTime.parse("2026-07-05T00:00:00Z"),
        OffsetDateTime.parse("2026-07-05T00:00:01Z"));
  }
}
