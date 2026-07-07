package com.atlas.metadata.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atlas.metadata.adapter.ModelAdapter;
import com.atlas.metadata.adapter.ModelCapability;
import com.atlas.metadata.adapter.ModelRequest;
import com.atlas.metadata.adapter.ModelResult;
import com.atlas.metadata.enums.ModelAdapterStatus;
import com.atlas.metadata.enums.ModelOperation;
import com.atlas.metadata.enums.ModelOutputKind;
import com.atlas.metadata.enums.ModelType;
import com.atlas.metadata.enums.ReviewStatus;
import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** Model adapter API contract tests against Flyway-managed PostgreSQL. */
class ModelApiContractIT extends AbstractPostgresIT {

  @Autowired private MockMvc mockMvc;

  @TestConfiguration
  static class FaultyAdapterConfig {

    @Bean
    ModelAdapter faultyModelAdapter() {
      return new ModelAdapter() {
        @Override
        public List<ModelCapability> capabilities() {
          return List.of(capability("faulty-model", "faulty-chat", ModelType.CHAT, ModelOperation.CHAT));
        }

        @Override
        public ModelResult execute(ModelRequest request) {
          throw new IllegalStateException(
              "provider crash token=${MODEL_TEST_TOKEN} at /private/model/runtime");
        }
      };
    }

    @Bean
    ModelAdapter invalidOutputModelAdapter() {
      return new ModelAdapter() {
        @Override
        public List<ModelCapability> capabilities() {
          return List.of(capability("invalid-output-model", "invalid-chat", ModelType.CHAT, ModelOperation.CHAT));
        }

        @Override
        public ModelResult execute(ModelRequest request) {
          return new ModelResult(
              "invalid-output-model",
              "invalid-chat",
              "Invalid output test.",
              new ModelResult.ModelUsage(1, 1),
              List.of(
                  new ModelResult.ModelOutput(
                      "invalid-output",
                      ModelOutputKind.TEXT_SUMMARY,
                      "/private/output.json",
                      "Mock invalid output.",
                      List.of(),
                      null,
                      null,
                      new BigDecimal("1.200"),
                      ReviewStatus.APPROVED,
                      null)));
        }
      };
    }

    private static ModelCapability capability(
        String adapterKey, String modelKey, ModelType modelType, ModelOperation operation) {
      return new ModelCapability(
          adapterKey,
          modelKey,
          "Test Model",
          "test",
          modelType,
          List.of(operation),
          false,
          ModelAdapterStatus.AVAILABLE,
          8192,
          Map.of("credential", "configured", "externalNetwork", "disabled"));
    }
  }

  @Test
  void capabilityEndpointReturnsMaskedDefaultModels() throws Exception {
    mockMvc
        .perform(get("/api/model-adapters"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[*].adapterKey", hasItem("mock-model")))
        .andExpect(jsonPath("$.data[*].modelKey", hasItem("deepseek-flash")))
        .andExpect(jsonPath("$.data[*].modelType", hasItem("CHAT")))
        .andExpect(jsonPath("$.data[*].supportedOperations[0]", hasItem("CHAT")))
        .andExpect(jsonPath("$.data[*].secretStatuses[*].reference.key", hasItem("credential")))
        .andExpect(jsonPath("$.data[0].maskedConfigSummary.externalNetwork").value("disabled"))
        .andExpect(jsonPath("$").value(not(containsString(System.getProperty("user.home")))))
        .andExpect(jsonPath("$").value(not(containsString("password"))));
  }

  @Test
  void modelConfigurationEndpointsReturnSecretReferencesOnly() throws Exception {
    MvcResult saved =
        mockMvc
            .perform(
                put("/api/model-configurations/deepseek")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "provider": "deepseek",
                          "endpoint": "https://public.example.invalid/v1",
                          "modelName": "deepseek-chat",
                          "apiKey": "redaction-check-value"
                        }
                        """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.credentialStatus").value("CONFIGURED"))
            .andExpect(jsonPath("$.data.secretStatuses[*].reference.key", hasItem("credential")))
            .andExpect(jsonPath("$.data.secretStatuses[*].status", hasItem("CONFIGURED")))
            .andReturn();

    assertThat(saved.getResponse().getContentAsString())
        .doesNotContain("redaction-check-value", "public.example.invalid");

    mockMvc
        .perform(get("/api/model-configurations/deepseek"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.secretStatuses[*].reference.scope", hasItem("model-configuration")))
        .andExpect(jsonPath("$").value(not(containsString("redaction-check-value"))))
        .andExpect(jsonPath("$").value(not(containsString("public.example.invalid"))));

    mockMvc
        .perform(delete("/api/model-configurations/deepseek"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.secretStatuses[*].reference.key", hasItem("credential")));
  }

  @Test
  void mockChatRunReturnsReviewRequiredOutputAndSourceReferences() throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/model-runs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "adapterKey": "mock-model",
                          "modelKey": "deepseek-flash",
                          "operationType": "CHAT",
                          "purpose": "review-assist",
                          "requestedBy": "delivery-lead",
                          "mode": "mock",
                          "inputReference": "source-chunk:chunk-file-001-p12-b02",
                          "safeMockInput": "Summarize the referenced source chunk.",
                          "sourceReferences": [
                            {
                              "refType": "SOURCE_CHUNK",
                              "refId": "chunk-file-001-p12-b02",
                              "label": "BRD page 12"
                            }
                          ]
                        }
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.adapterKey").value("mock-model"))
            .andExpect(jsonPath("$.data.modelKey").value("deepseek-flash"))
            .andExpect(jsonPath("$.data.status").value("SUCCEEDED"))
            .andExpect(jsonPath("$.data.outputs[0].kind").value("TEXT_SUMMARY"))
            .andExpect(jsonPath("$.data.outputs[0].reviewStatus").value("REVIEW_REQUIRED"))
            .andExpect(jsonPath("$.data.sourceReferences[0].refType").value("SOURCE_CHUNK"))
            .andExpect(jsonPath("$.data.sourceReferences[0].confidence").value(0.820))
            .andReturn();

    String runId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.runId");

    mockMvc
        .perform(get("/api/model-runs/" + runId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.runId").value(runId))
        .andExpect(jsonPath("$.data.outputs[0].safeSummary").value(containsString("Mock")))
        .andExpect(jsonPath("$.data.usage.outputCount").value(1));
  }

  @Test
  void embeddingRunReturnsMetadataOnly() throws Exception {
    MvcResult result =
        mockMvc
        .perform(
            post("/api/model-runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "mock-model",
                      "modelKey": "text-embedding-v4",
                      "operationType": "EMBEDDING",
                      "purpose": "index-preview",
                      "requestedBy": "delivery-lead",
                      "mode": "mock",
                      "inputReference": "file:file-001",
                      "safeMockInput": "Create mock embedding metadata.",
                      "sourceReferences": [
                        {
                          "refType": "FILE_ITEM",
                          "refId": "file-001",
                          "label": "BRD"
                        }
                      ]
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.outputs[0].kind").value("EMBEDDING_METADATA"))
        .andExpect(jsonPath("$.data.outputs[0].embeddingDimension").value(1024))
        .andExpect(jsonPath("$.data.outputs[0].embeddingItemCount").value(1))
        .andExpect(jsonPath("$.data.outputs[0].reviewStatus").value("REVIEW_REQUIRED"))
        .andReturn();

    assertThat(result.getResponse().getContentAsString())
        .doesNotContain("embeddingVector", "vectorValues", "providerPayload");
  }

  @Test
  void modelRunRejectsUnknownAdapterUnsafeInputAndInvalidOutput() throws Exception {
    mockMvc
        .perform(
            post("/api/model-runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "unknown",
                      "modelKey": "deepseek-flash",
                      "operationType": "CHAT",
                      "purpose": "review-assist",
                      "mode": "mock",
                      "inputReference": "source-chunk:chunk-file-001-p12-b02",
                      "safeMockInput": "Summarize safely."
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.error.fields.adapterKey").exists());

    mockMvc
        .perform(
            post("/api/model-runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "mock-model",
                      "modelKey": "deepseek-flash",
                      "operationType": "CHAT",
                      "purpose": "review-assist",
                      "mode": "mock",
                      "inputReference": "https://provider.example/raw",
                      "safeMockInput": "passWORD=secret"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.inputReference").exists())
        .andExpect(jsonPath("$.error.fields.safeMockInput").exists());

    mockMvc
        .perform(
            post("/api/model-runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "invalid-output-model",
                      "modelKey": "invalid-chat",
                      "operationType": "CHAT",
                      "purpose": "review-assist",
                      "mode": "mock",
                      "inputReference": "source-chunk:chunk-file-001-p12-b02",
                      "safeMockInput": "Summarize safely."
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"));
  }

  @Test
  void adapterFaultFailsRunSafely() throws Exception {
    mockMvc
        .perform(
            post("/api/model-runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "faulty-model",
                      "modelKey": "faulty-chat",
                      "operationType": "CHAT",
                      "purpose": "review-assist",
                      "mode": "mock",
                      "inputReference": "source-chunk:chunk-file-001-p12-b02",
                      "safeMockInput": "Summarize safely."
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.status").value("FAILED"))
        .andExpect(jsonPath("$.data.safeMessage").value(not(containsString("${MODEL_TEST_TOKEN}"))))
        .andExpect(jsonPath("$.data.safeMessage").value(not(containsString("/private/model"))))
        .andExpect(jsonPath("$.data.outputs").isEmpty());
  }
}
