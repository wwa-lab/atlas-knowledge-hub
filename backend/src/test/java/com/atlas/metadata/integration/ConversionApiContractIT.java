package com.atlas.metadata.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atlas.metadata.adapter.ConverterAdapter;
import com.atlas.metadata.adapter.ConverterCapability;
import com.atlas.metadata.adapter.ConverterRequest;
import com.atlas.metadata.adapter.ConverterResult;
import com.atlas.metadata.enums.ConverterAdapterStatus;
import com.atlas.metadata.enums.FileStatus;
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

/** Converter adapter API contract tests against Flyway-managed PostgreSQL. */
class ConversionApiContractIT extends AbstractPostgresIT {

  @Autowired private MockMvc mockMvc;

  @TestConfiguration
  static class FaultyAdapterConfig {

    @Bean
    ConverterAdapter faultyConverterAdapter() {
      return new ConverterAdapter() {
        @Override
        public ConverterCapability capability() {
          return new ConverterCapability(
              "faulty-converter",
              "Faulty Test Converter",
              "test",
              "pdf",
              List.of(SourceType.docx),
              false,
              ConverterAdapterStatus.AVAILABLE,
              Map.of("command", "configured", "externalNetwork", "disabled"));
        }

        @Override
        public ConverterResult convert(ConverterRequest request) {
          throw new IllegalStateException(
              "adapter crash token=${CONVERTER_TEST_TOKEN} at /private/runtime/convert");
        }
      };
    }

    @Bean
    ConverterAdapter invalidConfidenceConverterAdapter() {
      return new ConverterAdapter() {
        @Override
        public ConverterCapability capability() {
          return new ConverterCapability(
              "invalid-confidence-converter",
              "Invalid Confidence Test Converter",
              "test",
              "pdf",
              List.of(SourceType.docx),
              false,
              ConverterAdapterStatus.AVAILABLE,
              Map.of("command", "configured", "externalNetwork", "disabled"));
        }

        @Override
        public ConverterResult convert(ConverterRequest request) {
          return new ConverterResult(
              "invalid-confidence-converter",
              request.files().stream()
                  .map(
                      file ->
                          new ConverterResult.ConverterFileResult(
                              file.fileId(),
                              FileStatus.PDF_CONVERTED,
                              "generated/pdf/invalid-confidence.pdf",
                              new BigDecimal("1.250"),
                              null))
                  .toList(),
              "Invalid confidence test result.");
        }
      };
    }
  }

  @Test
  void capabilityEndpointReturnsMaskedDefaultConverter() throws Exception {
    mockMvc
        .perform(get("/api/converter-adapters"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].adapterKey").value("trinity-office"))
        .andExpect(jsonPath("$.data[0].defaultAdapter").value(true))
        .andExpect(jsonPath("$.data[0].status").value("AVAILABLE"))
        .andExpect(jsonPath("$.data[0].supportedSourceTypes", hasItem("docx")))
        .andExpect(jsonPath("$.data[0].maskedConfigSummary.command").value("configured"))
        .andExpect(jsonPath("$").value(not(containsString(System.getProperty("user.home")))))
        .andExpect(jsonPath("$").value(not(containsString("password"))));
  }

  @Test
  void mockConversionRunUpdatesFileMetadataAndPersistsReport() throws Exception {
    MvcResult batchResult =
        mockMvc
            .perform(
                post("/api/spaces/ibm-i-modernization/batches")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "name": "Conversion Contract Package",
                          "sourceKind": "folder",
                          "owner": "Delivery Lead",
                          "files": [
                            {
                              "sourcePath": "Conversion/BRD.docx",
                              "sourceType": "docx",
                              "status": "UPLOADED",
                              "confidence": 0,
                              "reviewStatus": "REVIEW_REQUIRED"
                            },
                            {
                              "sourcePath": "Conversion/legacy-screen.png",
                              "sourceType": "image",
                              "status": "UPLOADED",
                              "confidence": 0,
                              "reviewStatus": "REVIEW_REQUIRED"
                            },
                            {
                              "sourcePath": "Conversion/notes.txt",
                              "sourceType": "unsupported",
                              "status": "UPLOADED",
                              "confidence": 0,
                              "reviewStatus": "REVIEW_REQUIRED"
                            },
                            {
                              "sourcePath": "Conversion/broken.pptx",
                              "sourceType": "pptx",
                              "status": "UPLOADED",
                              "confidence": 0,
                              "reviewStatus": "REVIEW_REQUIRED"
                            }
                          ]
                        }
                        """))
            .andExpect(status().isCreated())
            .andReturn();
    String batchId = JsonPath.read(batchResult.getResponse().getContentAsString(), "$.data.id");

    MvcResult result =
        mockMvc
            .perform(
                post("/api/batches/" + batchId + "/conversion-runs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "adapterKey": "trinity-office",
                          "requestedBy": "delivery-lead",
                          "mode": "mock"
                        }
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.adapterKey").value("trinity-office"))
            .andExpect(jsonPath("$.data.status").value("PARTIAL_FAILED"))
            .andExpect(jsonPath("$.data.summary.total").value(4))
            .andExpect(jsonPath("$.data.summary.pdfConverted").value(1))
            .andExpect(jsonPath("$.data.summary.pdfConvertFailed").value(1))
            .andExpect(jsonPath("$.data.summary.ocrRequired").value(1))
            .andExpect(jsonPath("$.data.summary.unsupported").value(1))
            .andExpect(jsonPath("$.data.results[0].reviewStatus").value("REVIEW_REQUIRED"))
            .andReturn();

    String runId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.runId");

    mockMvc
        .perform(get("/api/conversion-runs/" + runId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.runId").value(runId))
        .andExpect(jsonPath("$.data.results[*].sourcePath", hasItem("Conversion/BRD.docx")))
        .andExpect(jsonPath("$.data.results[*].status", hasItem("PDF_CONVERTED")));

    MvcResult filesResult =
        mockMvc
            .perform(get("/api/batches/" + batchId + "/files"))
            .andExpect(status().isOk())
            .andReturn();
    List<String> convertedFileIds =
        JsonPath.read(
            filesResult.getResponse().getContentAsString(),
            "$.data[?(@.sourcePath == 'Conversion/BRD.docx')].id");
    String convertedFileId = convertedFileIds.getFirst();

    mockMvc
        .perform(get("/api/files/" + convertedFileId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("PDF_CONVERTED"))
        .andExpect(jsonPath("$.data.pdfPath").value("generated/pdf/BRD.pdf"))
        .andExpect(jsonPath("$.data.reviewStatus").value("REVIEW_REQUIRED"));
  }

  @Test
  void adapterFaultFailsRunSafelyAndLeavesFileUnchanged() throws Exception {
    MvcResult batchResult =
        mockMvc
            .perform(
                post("/api/spaces/ibm-i-modernization/batches")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "name": "Fault Contract Package",
                          "sourceKind": "folder",
                          "owner": "Delivery Lead",
                          "files": [
                            {
                              "sourcePath": "Fault/BRD.docx",
                              "sourceType": "docx",
                              "status": "UPLOADED",
                              "confidence": 0,
                              "reviewStatus": "REVIEW_REQUIRED"
                            }
                          ]
                        }
                        """))
            .andExpect(status().isCreated())
            .andReturn();
    String batchId = JsonPath.read(batchResult.getResponse().getContentAsString(), "$.data.id");

    MvcResult filesResult =
        mockMvc
            .perform(get("/api/batches/" + batchId + "/files"))
            .andExpect(status().isOk())
            .andReturn();
    List<String> fileIds =
        JsonPath.read(filesResult.getResponse().getContentAsString(), "$.data[*].id");
    String fileId = fileIds.getFirst();

    mockMvc
        .perform(
            post("/api/batches/" + batchId + "/conversion-runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "faulty-converter",
                      "requestedBy": "delivery-lead",
                      "mode": "mock"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.adapterKey").value("faulty-converter"))
        .andExpect(jsonPath("$.data.status").value("FAILED"))
        .andExpect(jsonPath("$.data.safeMessage").value(not(containsString("${CONVERTER_TEST_TOKEN}"))))
        .andExpect(jsonPath("$.data.safeMessage").value(not(containsString("/private/runtime"))))
        .andExpect(jsonPath("$.data.results").isEmpty());

    mockMvc
        .perform(get("/api/files/" + fileId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("UPLOADED"))
        .andExpect(jsonPath("$.data.pdfPath").doesNotExist())
        .andExpect(jsonPath("$.data.reviewStatus").value("REVIEW_REQUIRED"));
  }

  @Test
  void createConversionRunRejectsUnknownAdapterAndUnsafeMode() throws Exception {
    mockMvc
        .perform(
            post("/api/batches/batch-2026-06-20-001/conversion-runs")
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
            post("/api/batches/batch-2026-06-20-001/conversion-runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "trinity-office",
                      "fileIds": ["file-001"],
                      "mode": "network"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields.mode").exists());
  }

  @Test
  void createConversionRunRejectsOutOfRangeAdapterConfidence() throws Exception {
    MvcResult batchResult =
        mockMvc
            .perform(
                post("/api/spaces/ibm-i-modernization/batches")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "name": "Invalid Confidence Package",
                          "sourceKind": "folder",
                          "owner": "Delivery Lead",
                          "files": [
                            {
                              "sourcePath": "Confidence/BRD.docx",
                              "sourceType": "docx",
                              "status": "UPLOADED",
                              "confidence": 0,
                              "reviewStatus": "REVIEW_REQUIRED"
                            }
                          ]
                        }
                        """))
            .andExpect(status().isCreated())
            .andReturn();
    String batchId = JsonPath.read(batchResult.getResponse().getContentAsString(), "$.data.id");

    mockMvc
        .perform(
            post("/api/batches/" + batchId + "/conversion-runs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "invalid-confidence-converter",
                      "requestedBy": "delivery-lead",
                      "mode": "mock"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.error.fields").value(org.hamcrest.Matchers.hasValue("must be between 0 and 1")));
  }
}
