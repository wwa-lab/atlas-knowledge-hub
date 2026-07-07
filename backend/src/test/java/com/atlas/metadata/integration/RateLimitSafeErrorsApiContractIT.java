package com.atlas.metadata.integration;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atlas.metadata.service.LocalRateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** API contract coverage for local rate limiting and safe errors. */
@Import(RateLimitSafeErrorsApiContractIT.FaultController.class)
class RateLimitSafeErrorsApiContractIT extends AbstractPostgresIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private LocalRateLimitService rateLimitService;

  @BeforeEach
  void resetRateLimiter() {
    rateLimitService.reset();
  }

  @Test
  void validationAuthNotFoundAndSystemErrorsUseSafeCodes() throws Exception {
    mockMvc
        .perform(post("/api/spaces").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success", is(false)))
        .andExpect(jsonPath("$.error.code", is("VALIDATION_FAILED")))
        .andExpect(jsonPath("$.error.message", is("Invalid request.")))
        .andExpect(jsonPath("$", not(containsString("Exception"))));

    mockMvc
        .perform(get("/api/auth/me").header("X-Atlas-User", "__missing__"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.code", is("AUTHENTICATION_REQUIRED")));

    mockMvc
        .perform(
            post("/api/spaces/ibm-i-modernization/batches")
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
        .andExpect(jsonPath("$.error.code", is("PERMISSION_DENIED")));

    mockMvc
        .perform(get("/api/spaces/unknown-space"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error.code", is("NOT_FOUND")))
        .andExpect(jsonPath("$.error.message", is("Resource not found.")));

    mockMvc
        .perform(get("/api/test/safe-error"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.error.code", is("SAFE_SYSTEM_ERROR")))
        .andExpect(jsonPath("$.error.correlationId", notNullValue()))
        .andExpect(jsonPath("$", not(containsString("sensitive-value"))))
        .andExpect(jsonPath("$", not(containsString("/" + "Users/internal"))))
        .andExpect(jsonPath("$", not(containsString("IllegalStateException"))));
  }

  @Test
  void deterministicLocalRateLimitReturnsSafeRetryMetadata() throws Exception {
    String key = "rate-limit-contract";

    mockMvc
        .perform(
            get("/api/spaces")
                .header("X-Atlas-Test-Rate-Key", key)
                .header("X-Atlas-Test-Rate-Limit", "1")
                .header("X-Atlas-Test-Rate-Window-Millis", "60000"))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            get("/api/spaces")
                .header("X-Atlas-Test-Rate-Key", key)
                .header("X-Atlas-Test-Rate-Limit", "1")
                .header("X-Atlas-Test-Rate-Window-Millis", "60000"))
        .andExpect(status().isTooManyRequests())
        .andExpect(header().string("Retry-After", notNullValue()))
        .andExpect(jsonPath("$.success", is(false)))
        .andExpect(jsonPath("$.error.code", is("RATE_LIMITED")))
        .andExpect(jsonPath("$.error.retryAfterSeconds", greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$", not(containsString("Bucket"))));
  }

  @RestController
  static class FaultController {

    @GetMapping("/api/test/safe-error")
    String failSafely() {
      throw new IllegalStateException("api" + "Key=sensitive-value /" + "Users/internal/source.md");
    }
  }
}
