package com.atlas.metadata.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atlas.metadata.adapter.ParserAdapter;
import com.atlas.metadata.adapter.ParserCapability;
import com.atlas.metadata.adapter.ParserRequest;
import com.atlas.metadata.adapter.ParserResult;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ParserAdapterStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceType;
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

/** Parser adapter API contract tests against Flyway-managed PostgreSQL. */
class ParserApiContractIT extends AbstractPostgresIT {

  @Autowired private MockMvc mockMvc;

  @TestConfiguration
  static class FaultyAdapterConfig {

    @Bean
    ParserAdapter faultyParserAdapter() {
      return new ParserAdapter() {
        @Override
        public ParserCapability capability() {
          return FaultyAdapterConfig.capability("faulty-parser");
        }

        @Override
        public ParserResult parse(ParserRequest request) {
          throw new IllegalStateException(
              "parser crash token=${PARSER_TEST_TOKEN} at /private/runtime/parse");
        }
      };
    }

    @Bean
    ParserAdapter invalidChunkParserAdapter() {
      return new ParserAdapter() {
        @Override
        public ParserCapability capability() {
          return FaultyAdapterConfig.capability("invalid-chunk-parser");
        }

        @Override
        public ParserResult parse(ParserRequest request) {
          return new ParserResult(
              "invalid-chunk-parser",
              request.files().stream()
                  .map(
                      file ->
                          new ParserResult.ParserFileResult(
                              file.fileId(),
                              FileStatus.MARKDOWN_GENERATED,
                              "generated/markdown/invalid.md",
                              "generated/assets/invalid",
                              new BigDecimal("0.920"),
                              null,
                              List.of(
                                  new ParserResult.ParserChunkResult(
                                      "duplicate-chunk",
                                      0,
                                      "Invalid",
                                      new BigDecimal("0.920"),
                                      ReviewStatus.REVIEW_REQUIRED))))
                  .toList(),
              "Invalid chunk test result.");
        }
      };
    }

    private static ParserCapability capability(String key) {
      return new ParserCapability(
          key,
          "Test Parser",
          "test",
          List.of(SourceType.pdf),
          List.of("markdown", "assets"),
          false,
          ParserAdapterStatus.AVAILABLE,
          new BigDecimal("0.800"),
          Map.of("command", "configured", "externalNetwork", "disabled"));
    }
  }

  @Test
  void capabilityEndpointReturnsMaskedDefaultParser() throws Exception {
    mockMvc
        .perform(get("/api/parser-adapters"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].adapterKey").value("document-normalize"))
        .andExpect(jsonPath("$.data[0].defaultAdapter").value(true))
        .andExpect(jsonPath("$.data[0].status").value("AVAILABLE"))
        .andExpect(jsonPath("$.data[0].inputTypes", hasItem("pdf")))
        .andExpect(jsonPath("$.data[0].outputTypes", hasItem("markdown")))
        .andExpect(jsonPath("$.data[0].lowConfidenceThreshold").value(0.800))
        .andExpect(jsonPath("$.data[0].maskedConfigSummary.command").value("configured"))
        .andExpect(jsonPath("$").value(not(containsString(System.getProperty("user.home")))))
        .andExpect(jsonPath("$").value(not(containsString("password"))));
  }

  @Test
  void mockParserRunUpdatesFileMetadataPersistsChunksAndReport() throws Exception {
    String batchId =
        createBatch(
            """
            [
              {
                "sourcePath": "Parser/BRD.pdf",
                "sourceType": "pdf",
                "status": "PDF_CONVERTED",
                "confidence": 1,
                "reviewStatus": "REVIEW_REQUIRED",
                "pdfPath": "generated/pdf/BRD.pdf"
              },
              {
                "sourcePath": "Parser/low-confidence.pdf",
                "sourceType": "pdf",
                "status": "PDF_CONVERTED",
                "confidence": 1,
                "reviewStatus": "REVIEW_REQUIRED",
                "pdfPath": "generated/pdf/low-confidence.pdf"
              },
              {
                "sourcePath": "Parser/needs-ocr.pdf",
                "sourceType": "pdf",
                "status": "PDF_CONVERTED",
                "confidence": 1,
                "reviewStatus": "REVIEW_REQUIRED",
                "pdfPath": "generated/pdf/needs-ocr.pdf"
              },
              {
                "sourcePath": "Parser/not-ready.pdf",
                "sourceType": "pdf",
                "status": "UPLOADED",
                "confidence": 0,
                "reviewStatus": "REVIEW_REQUIRED",
                "pdfPath": "generated/pdf/not-ready.pdf"
              }
            ]
            """);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/batches/" + batchId + "/parser-runs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "adapterKey": "document-normalize",
                          "requestedBy": "delivery-lead",
                          "mode": "mock"
                        }
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.adapterKey").value("document-normalize"))
            .andExpect(jsonPath("$.data.status").value("PARTIAL_FAILED"))
            .andExpect(jsonPath("$.data.summary.total").value(4))
            .andExpect(jsonPath("$.data.summary.markdownGenerated").value(1))
            .andExpect(jsonPath("$.data.summary.lowConfidence").value(1))
            .andExpect(jsonPath("$.data.summary.ocrRequired").value(1))
            .andExpect(jsonPath("$.data.summary.skipped").value(1))
            .andExpect(jsonPath("$.data.results[*].reviewStatus", hasItem("REVIEW_REQUIRED")))
            .andExpect(jsonPath("$.data.chunks[0].reviewStatus").value("REVIEW_REQUIRED"))
            .andReturn();

    String runId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.runId");

    mockMvc
        .perform(get("/api/parser-runs/" + runId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.runId").value(runId))
        .andExpect(jsonPath("$.data.results[*].sourcePath", hasItem("Parser/BRD.pdf")))
        .andExpect(jsonPath("$.data.results[*].status", hasItem("MARKDOWN_GENERATED")))
        .andExpect(jsonPath("$.data.chunks[*].sourceFile", hasItem("Parser/BRD.pdf")));

    MvcResult filesResult =
        mockMvc
            .perform(get("/api/batches/" + batchId + "/files"))
            .andExpect(status().isOk())
            .andReturn();
    List<String> parsedFileIds =
        JsonPath.read(
            filesResult.getResponse().getContentAsString(),
            "$.data[?(@.sourcePath == 'Parser/BRD.pdf')].id");
    String parsedFileId = parsedFileIds.getFirst();

    mockMvc
        .perform(get("/api/files/" + parsedFileId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("MARKDOWN_GENERATED"))
        .andExpect(jsonPath("$.data.pdfPath").value("generated/pdf/BRD.pdf"))
        .andExpect(jsonPath("$.data.markdownPath").value("generated/markdown/BRD.md"))
        .andExpect(jsonPath("$.data.assetsPath").value("generated/assets/BRD"))
        .andExpect(jsonPath("$.data.reviewStatus").value("REVIEW_REQUIRED"));

    mockMvc
        .perform(get("/api/files/" + parsedFileId + "/chunks"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].sourceFile").value("Parser/BRD.pdf"))
        .andExpect(jsonPath("$.data[0].reviewStatus").value("REVIEW_REQUIRED"));
  }

  @Test
  void configuredParserModeFailsSafelyWhenRuntimeIsDisabled() throws Exception {
    String batchId =
        createBatch(
            """
            [
              {
                "sourcePath": "Configured/BRD.pdf",
                "sourceType": "pdf",
                "status": "PDF_CONVERTED",
                "confidence": 1,
                "reviewStatus": "REVIEW_REQUIRED",
                "pdfPath": "generated/pdf/BRD.pdf"
              }
            ]
            """);
    String fileId = firstFileId(batchId);

    mockMvc
        .perform(
            post("/api/batches/" + batchId + "/parser-runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "document-normalize",
                      "requestedBy": "delivery-lead",
                      "mode": "configured"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.adapterKey").value("document-normalize"))
        .andExpect(jsonPath("$.data.status").value("FAILED"))
        .andExpect(jsonPath("$.data.safeMessage").value("Parser adapter is unavailable."))
        .andExpect(jsonPath("$.data.results").isEmpty());

    mockMvc
        .perform(get("/api/files/" + fileId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("PDF_CONVERTED"))
        .andExpect(jsonPath("$.data.markdownPath").doesNotExist())
        .andExpect(jsonPath("$.data.reviewStatus").value("REVIEW_REQUIRED"));
  }

  @Test
  void adapterFaultFailsRunSafelyAndLeavesFileUnchanged() throws Exception {
    String batchId =
        createBatch(
            """
            [
              {
                "sourcePath": "Fault/BRD.pdf",
                "sourceType": "pdf",
                "status": "PDF_CONVERTED",
                "confidence": 1,
                "reviewStatus": "REVIEW_REQUIRED",
                "pdfPath": "generated/pdf/BRD.pdf"
              }
            ]
            """);
    String fileId = firstFileId(batchId);

    mockMvc
        .perform(
            post("/api/batches/" + batchId + "/parser-runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "faulty-parser",
                      "requestedBy": "delivery-lead",
                      "mode": "mock"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.adapterKey").value("faulty-parser"))
        .andExpect(jsonPath("$.data.status").value("FAILED"))
        .andExpect(jsonPath("$.data.safeMessage").value(not(containsString("${PARSER_TEST_TOKEN}"))))
        .andExpect(jsonPath("$.data.safeMessage").value(not(containsString("/private/runtime"))))
        .andExpect(jsonPath("$.data.results").isEmpty());

    mockMvc
        .perform(get("/api/files/" + fileId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("PDF_CONVERTED"))
        .andExpect(jsonPath("$.data.markdownPath").doesNotExist())
        .andExpect(jsonPath("$.data.reviewStatus").value("REVIEW_REQUIRED"));
  }

  @Test
  void createParserRunRejectsUnknownAdapterUnsafeModeAndInvalidChunkEvidence() throws Exception {
    mockMvc
        .perform(
            post("/api/batches/batch-2026-06-20-001/parser-runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "unknown",
                      "fileIds": ["file-001"],
                      "mode": "mock"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.error.fields.adapterKey").exists());

    mockMvc
        .perform(
            post("/api/batches/batch-2026-06-20-001/parser-runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "document-normalize",
                      "fileIds": ["file-001"],
                      "mode": "network"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.mode").exists());

    String batchId =
        createBatch(
            """
            [
              {
                "sourcePath": "Invalid/BRD.pdf",
                "sourceType": "pdf",
                "status": "PDF_CONVERTED",
                "confidence": 1,
                "reviewStatus": "REVIEW_REQUIRED",
                "pdfPath": "generated/pdf/BRD.pdf"
              }
            ]
            """);

    mockMvc
        .perform(
            post("/api/batches/" + batchId + "/parser-runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "invalid-chunk-parser",
                      "requestedBy": "delivery-lead",
                      "mode": "mock"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.error.fields").value(org.hamcrest.Matchers.hasValue("must be positive")));
  }

  private String createBatch(String filesJson) throws Exception {
    MvcResult batchResult =
        mockMvc
            .perform(
                post("/api/spaces/ibm-i-modernization/batches")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "name": "Parser Contract Package",
                          "sourceKind": "folder",
                          "owner": "Delivery Lead",
                          "files": %s
                        }
                        """
                            .formatted(filesJson)))
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
}
