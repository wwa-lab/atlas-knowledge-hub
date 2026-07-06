package com.atlas.metadata.integration;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

class AuthSpaceRbacApiContractIT extends AbstractPostgresIT {

  @Autowired private MockMvc mockMvc;

  @Test
  void currentUserRequiresAuthentication() throws Exception {
    mockMvc
        .perform(get("/api/auth/me").header("X-Atlas-User", "__missing__"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success", is(false)))
        .andExpect(jsonPath("$.error.code", is("UNAUTHORIZED")));
  }

  @Test
  void currentUserReturnsMembershipsAndCapabilities() throws Exception {
    mockMvc
        .perform(get("/api/auth/me").header("X-Atlas-User", "mock-owner"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.user.id", is("mock-owner")))
        .andExpect(jsonPath("$.data.memberships[*].spaceId", hasItem("ibm-i-modernization")))
        .andExpect(jsonPath("$.data.capabilities", hasItem("MEMBER_MANAGE")));
  }

  @Test
  void viewerCanReadSpaceButCannotCreateBatch() throws Exception {
    mockMvc
        .perform(get("/api/spaces/ibm-i-modernization").header("X-Atlas-User", "mock-viewer"))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/spaces/ibm-i-modernization/batches")
                .header("X-Atlas-User", "mock-viewer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "Viewer denied",
                      "sourceKind": "folder",
                      "owner": "viewer",
                      "files": []
                    }
                    """))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error.code", is("FORBIDDEN")));
  }

  @Test
  void spaceMembershipManagementIsOwnerOnly() throws Exception {
    mockMvc
        .perform(get("/api/spaces/ibm-i-modernization/members").header("X-Atlas-User", "mock-viewer"))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(get("/api/spaces/ibm-i-modernization/members").header("X-Atlas-User", "mock-owner"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[*].userId", hasItem("mock-owner")));
  }

  @Test
  void ownerCanCreateAndPatchMembershipByMembershipId() throws Exception {
    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/spaces/ibm-i-modernization/members")
                    .header("X-Atlas-User", "mock-owner")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "email": "contract-reviewer@example.test",
                          "displayName": "Contract Reviewer",
                          "role": "VIEWER",
                          "status": "INVITED"
                        }
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.id").exists())
            .andExpect(jsonPath("$.data.userId").exists())
            .andExpect(jsonPath("$.data.email", is("contract-reviewer@example.test")))
            .andExpect(jsonPath("$.data.role", is("VIEWER")))
            .andExpect(jsonPath("$.data.status", is("INVITED")))
            .andReturn();
    String membershipId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.id");

    mockMvc
        .perform(
            patch("/api/spaces/ibm-i-modernization/members/" + membershipId)
                .header("X-Atlas-User", "mock-owner")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "role": "EDITOR",
                      "status": "ACTIVE"
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id", is(membershipId)))
        .andExpect(jsonPath("$.data.role", is("EDITOR")))
        .andExpect(jsonPath("$.data.status", is("ACTIVE")));
  }

  @Test
  void lastSpaceOwnerCannotBeRemoved() throws Exception {
    mockMvc
        .perform(
            delete("/api/spaces/claims-knowledge-base/members/membership-owner-claims")
                .header("X-Atlas-User", "mock-owner"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error.code", is("CONFLICT")));
  }

  @Test
  void lastSpaceOwnerCannotBeDemoted() throws Exception {
    mockMvc
        .perform(
            patch("/api/spaces/claims-knowledge-base/members/membership-owner-claims")
                .header("X-Atlas-User", "mock-owner")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "role": "VIEWER",
                      "status": "ACTIVE"
                    }
                    """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error.code", is("CONFLICT")));
  }
}
