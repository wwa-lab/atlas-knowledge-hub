package com.atlas.metadata.integration;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Contract tests for the governance-protected append-only audit log foundation. */
class AuditLogApiContractIT extends AbstractPostgresIT {

  @Autowired private MockMvc mockMvc;

  @Test
  void auditEventsRequireGovernanceReadAndCaptureDeniedAccessSafely() throws Exception {
    mockMvc
        .perform(
            get("/api/spaces/ibm-i-modernization/audit-events")
                .header("X-Atlas-User", "mock-viewer"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error.code").value("PERMISSION_DENIED"));

    mockMvc
        .perform(
            get("/api/spaces/ibm-i-modernization/audit-events")
                .header("X-Atlas-User", "mock-auditor"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[*].category", hasItem("AUTH")))
        .andExpect(jsonPath("$.data[*].result", hasItem("DENIED")))
        .andExpect(jsonPath("$.data[*].action", hasItem("AUTH_GOVERNANCE_READ_DENIED")))
        .andExpect(jsonPath("$").value(not(org.hamcrest.Matchers.containsString(System.getProperty("user.home")))))
        .andExpect(jsonPath("$").value(not(org.hamcrest.Matchers.containsString("password"))));
  }

  @Test
  void membershipChangesAndLastOwnerConflictsAreVisibleToAuditors() throws Exception {
    mockMvc
        .perform(
            post("/api/spaces/ibm-i-modernization/members")
                .header("X-Atlas-User", "mock-owner")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "audit.member@example.test",
                      "displayName": "Audit Member",
                      "role": "VIEWER",
                      "status": "ACTIVE"
                    }
                    """))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            get("/api/spaces/ibm-i-modernization/audit-events")
                .header("X-Atlas-User", "mock-auditor")
                .param("category", "MEMBERSHIP"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[*].action", hasItem("MEMBERSHIP_ADDED")))
        .andExpect(jsonPath("$.data[*].actorUserId", hasItem("mock-owner")))
        .andExpect(jsonPath("$.data[*].metadata.role", hasItem("VIEWER")));

    mockMvc
        .perform(
            delete("/api/spaces/claims-knowledge-base/members/membership-owner-claims")
                .header("X-Atlas-User", "mock-owner"))
        .andExpect(status().isConflict());

    mockMvc
        .perform(
            get("/api/spaces/claims-knowledge-base/audit-events")
                .header("X-Atlas-User", "mock-owner")
                .param("result", "CONFLICT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[*].action", hasItem("MEMBERSHIP_LAST_OWNER_GUARD")))
        .andExpect(jsonPath("$.data[*].severity", hasItem("WARNING")));
  }

  @Test
  void auditListSupportsTimeRangeFiltersAndRejectsInvalidIntervals() throws Exception {
    mockMvc
        .perform(
            get("/api/spaces/ibm-i-modernization/audit-events")
                .header("X-Atlas-User", "mock-viewer"))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(
            get("/api/spaces/ibm-i-modernization/audit-events")
                .header("X-Atlas-User", "mock-auditor")
                .param("createdFrom", "2100-01-01T00:00:00Z"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));

    mockMvc
        .perform(
            get("/api/spaces/ibm-i-modernization/audit-events")
                .header("X-Atlas-User", "mock-auditor")
                .param("createdFrom", "2026-07-06T01:00:00Z")
                .param("createdTo", "2026-07-06T00:00:00Z"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.error.fields.createdFrom").exists());
  }
}
