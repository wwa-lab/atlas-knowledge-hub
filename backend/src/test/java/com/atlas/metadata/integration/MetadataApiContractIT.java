package com.atlas.metadata.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/** Full API contract tests against Flyway-managed PostgreSQL. */
class MetadataApiContractIT extends AbstractPostgresIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private RequestMappingHandlerMapping handlerMapping;

  @Test
  void spacesListDetailCreateValidationAndNotFoundUseEnvelope() throws Exception {
    mockMvc
        .perform(get("/api/spaces"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.meta.total").value(2));

    mockMvc
        .perform(get("/api/spaces/ibm-i-modernization"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value("ibm-i-modernization"))
        .andExpect(jsonPath("$.data.reviewCount").value(3));

    mockMvc
        .perform(
            post("/api/spaces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "API Contract Test Space",
                      "description": "Mock metadata space.",
                      "type": "document",
                      "indexStrategy": "rag",
                      "owner": "QA Team"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value("api-contract-test-space"))
        .andExpect(jsonPath("$.data.status").value("HEALTHY"));

    mockMvc
        .perform(post("/api/spaces").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.error.fields.name").exists());

    mockMvc
        .perform(get("/api/spaces/unknown-space"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.data").doesNotExist())
        .andExpect(jsonPath("$.error.code").value("NOT_FOUND"))
        .andExpect(jsonPath("$.error.path").value("/api/spaces/unknown-space"))
        .andExpect(jsonPath("$.error.message").value("Knowledge Space not found."))
        .andExpect(jsonPath("$").value(not(containsString("Exception"))))
        .andExpect(jsonPath("$").value(not(containsString("org.postgresql"))))
        .andExpect(jsonPath("$").value(not(containsString(System.getProperty("user.home")))))
        .andExpect(jsonPath("$").value(not(containsString("password"))));
  }

  @Test
  void batchEndpointsDeriveMetricsAndCreateFromInventoryMetadataOnly() throws Exception {
    mockMvc
        .perform(get("/api/spaces/ibm-i-modernization/batches"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(
            jsonPath("$.data[?(@.id == 'batch-2026-06-20-001')].metrics.total", hasItem(6)))
        .andExpect(
            jsonPath(
                    "$.data[?(@.id == 'batch-2026-06-20-001')].metrics.markdownGenerated",
                    hasItem(1)))
        .andExpect(
            jsonPath("$.data[?(@.id == 'batch-2026-06-20-001')].metrics.failed", hasItem(1)))
        .andExpect(
            jsonPath(
                    "$.data[?(@.id == 'batch-2026-06-20-001')].metrics.unsupported",
                    hasItem(1)));

    mockMvc
        .perform(get("/api/batches/batch-2026-06-20-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.metrics.total").value(6))
        .andExpect(jsonPath("$.data.metrics.reviewRequired").value(6));

    MvcResult result =
        mockMvc
            .perform(
                post("/api/spaces/ibm-i-modernization/batches")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "name": "Inventory Only Package",
                          "sourceKind": "folder",
                          "owner": "Delivery Lead",
                          "files": [
                            {
                              "sourcePath": "Inventory/new-generated.docx",
                              "sourceType": "docx",
                              "status": "MARKDOWN_GENERATED",
                              "confidence": 0.84,
                              "reviewStatus": "REVIEW_REQUIRED",
                              "pdfPath": "generated/pdf/new-generated.pdf",
                              "markdownPath": "generated/md/new-generated.md",
                              "assetsPath": "generated/assets/new-generated/",
                              "chunks": [
                                {
                                  "sourceFile": "new-generated.docx",
                                  "page": 2,
                                  "section": "Overview",
                                  "confidence": 0.84,
                                  "reviewStatus": "REVIEW_REQUIRED"
                                }
                              ]
                            },
                            {
                              "sourcePath": "Inventory/unsupported.txt",
                              "sourceType": "unsupported",
                              "status": "UNSUPPORTED",
                              "confidence": 0,
                              "reviewStatus": "REVIEW_REQUIRED",
                              "errorMessage": "Unsupported mock file."
                            }
                          ]
                        }
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.metrics.total").value(2))
            .andExpect(jsonPath("$.data.metrics.markdownGenerated").value(1))
            .andExpect(jsonPath("$.data.metrics.unsupported").value(1))
            .andReturn();

    String batchId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
    mockMvc
        .perform(get("/api/batches/" + batchId + "/files"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[*].sourcePath", hasItem("Inventory/new-generated.docx")))
        .andExpect(jsonPath("$.data[*].reviewStatus", hasItem("REVIEW_REQUIRED")))
        .andExpect(
            jsonPath("$.data[*].sourcePath")
                .value(not(containsString(System.getProperty("user.home")))));

    mockMvc
        .perform(get("/api/batches/" + batchId + "/files?status=UNSUPPORTED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.meta.total").value(1))
        .andExpect(jsonPath("$.data[0].status").value("UNSUPPORTED"));
  }

  @Test
  void createBatchRejectsTraversalAndApprovedReviewStatus() throws Exception {
    mockMvc
        .perform(
            post("/api/spaces/ibm-i-modernization/batches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "Bad Package",
                      "sourceKind": "folder",
                      "files": [
                        {
                          "sourcePath": "../secret.docx",
                          "sourceType": "docx",
                          "status": "MARKDOWN_GENERATED",
                          "confidence": 0.84,
                          "reviewStatus": "REVIEW_REQUIRED"
                        }
                      ]
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.error.fields['files[0].sourcePath']").exists());

    mockMvc
        .perform(
            post("/api/spaces/ibm-i-modernization/batches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "Approved Package",
                      "sourceKind": "folder",
                      "files": [
                        {
                          "sourcePath": "Inventory/approved.docx",
                          "sourceType": "docx",
                          "status": "MARKDOWN_GENERATED",
                          "confidence": 0.84,
                          "reviewStatus": "APPROVED"
                        }
                      ]
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields['files[0].reviewStatus']").exists());
  }

  @Test
  void fileChunkAndReviewEndpointsPreserveTraceAndChronologicalHistory() throws Exception {
    mockMvc
        .perform(get("/api/batches/batch-2026-06-20-001/files?status=MARKDOWN_GENERATED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.meta.total").value(1))
        .andExpect(jsonPath("$.data[0].id").value("file-001"))
        .andExpect(jsonPath("$.data[0].sourcePath").value("Discovery/BRD/BRD.docx"))
        .andExpect(jsonPath("$.data[0].confidence").value(0.820))
        .andExpect(jsonPath("$.data[0].reviewStatus").value("REVIEW_REQUIRED"));

    mockMvc
        .perform(get("/api/files/file-001/chunks"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].id").value("chunk-file-001-p12-b02"))
        .andExpect(jsonPath("$.data[0].sourceFile").value("BRD.docx"))
        .andExpect(jsonPath("$.data[0].page").value(12))
        .andExpect(jsonPath("$.data[0].reviewStatus").value("REVIEW_REQUIRED"));

    mockMvc
        .perform(
            post("/api/files/file-001/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "action": "OCR_REQUIRED",
                      "reviewer": "sme.casey",
                      "comment": "Needs OCR evidence check.",
                      "affectedChunks": ["chunk-file-001-p12-b02"]
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.action").value("OCR_REQUIRED"));

    mockMvc
        .perform(get("/api/files/file-001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.reviewStatus").value("OCR_REQUIRED"));

    mockMvc
        .perform(get("/api/files/file-001/reviews"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].action").value("NEED_FIX"))
        .andExpect(jsonPath("$.data[1].action").value("OCR_REQUIRED"));
  }

  @Test
  void seedMigrationCreatesAllAdapterTablesAndMockRows() {
    Set<String> tables =
        Set.copyOf(
            jdbcTemplate.queryForList(
                """
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'atlas'
                  AND table_name <> 'flyway_schema_history'
                """,
                String.class));

    assertThat(tables)
        .containsExactlyInAnyOrder(
            "space",
            "batch",
            "file_item",
            "source_chunk",
            "review_record",
            "wiki_page",
            "graph_node",
            "graph_edge",
            "conversion_run",
            "conversion_file_result",
            "parser_run",
            "parser_file_result",
            "storage_operation",
            "storage_object",
            "vector_run",
            "vector_item_result",
            "model_run",
            "model_run_output",
            "model_run_source_reference");
    assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM atlas.file_item", Integer.class))
        .isGreaterThanOrEqualTo(6);
    assertThat(
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM atlas.file_item WHERE review_status = 'APPROVED'",
                Integer.class))
        .isZero();
  }

  @Test
  void deferredWikiGraphAskRoutesAreNotMapped() {
    Set<String> routes =
        handlerMapping.getHandlerMethods().keySet().stream()
            .flatMap(info -> info.getPatternValues().stream())
            .collect(java.util.stream.Collectors.toSet());

    assertThat(routes).noneMatch(route -> route.contains("wiki"));
    assertThat(routes).noneMatch(route -> route.contains("graph"));
    assertThat(routes).noneMatch(route -> route.contains("ask"));
  }
}
