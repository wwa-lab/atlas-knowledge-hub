package com.atlas.metadata.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atlas.metadata.adapter.StorageAdapter;
import com.atlas.metadata.adapter.StorageCapability;
import com.atlas.metadata.adapter.StorageDeleteResult;
import com.atlas.metadata.adapter.StorageListRequest;
import com.atlas.metadata.adapter.StorageListResult;
import com.atlas.metadata.adapter.StorageObjectDescriptor;
import com.atlas.metadata.adapter.StorageObjectRef;
import com.atlas.metadata.adapter.StoragePutRequest;
import com.atlas.metadata.enums.StorageAdapterStatus;
import com.atlas.metadata.enums.StorageLayer;
import com.atlas.metadata.enums.StorageObjectStatus;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** Storage adapter API contract tests against Flyway-managed PostgreSQL. */
class StorageApiContractIT extends AbstractPostgresIT {

  @Autowired private MockMvc mockMvc;

  @TestConfiguration
  static class FaultyStorageAdapterConfig {

    @Bean
    StorageAdapter faultyStorageAdapter() {
      return new StorageAdapter() {
        @Override
        public StorageCapability capability() {
          return new StorageCapability(
              "faulty-storage",
              "Faulty Storage",
              "test",
              List.of(StorageLayer.values()),
              false,
              StorageAdapterStatus.AVAILABLE,
              Map.of("externalNetwork", "disabled"));
        }

        @Override
        public StorageObjectDescriptor put(StoragePutRequest request) {
          throw new IllegalStateException(
              "storage crash token=${STORAGE_TEST_TOKEN} endpoint=https://storage.internal.local bucket=secret-bucket at /private/runtime/storage");
        }

        @Override
        public StorageObjectDescriptor get(StorageObjectRef ref) {
          return missing(ref);
        }

        @Override
        public boolean exists(StorageObjectRef ref) {
          return false;
        }

        @Override
        public StorageListResult list(StorageListRequest request) {
          return new StorageListResult(List.of(), null);
        }

        @Override
        public StorageDeleteResult delete(StorageObjectRef ref) {
          return new StorageDeleteResult(missing(ref));
        }

        private StorageObjectDescriptor missing(StorageObjectRef ref) {
          return new StorageObjectDescriptor(
              ref.layer(),
              ref.objectKey(),
              null,
              null,
              null,
              "faulty-storage",
              StorageObjectStatus.MISSING,
              ref.fileId(),
              null);
        }
      };
    }
  }

  @Test
  void capabilityEndpointReturnsMaskedDefaultStorageAdapter() throws Exception {
    mockMvc
        .perform(get("/api/storage-adapters"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[0].adapterKey").value("mock-storage"))
        .andExpect(jsonPath("$.data[0].defaultAdapter").value(true))
        .andExpect(jsonPath("$.data[0].status").value("AVAILABLE"))
        .andExpect(jsonPath("$.data[0].supportedLayers", hasItem("markdown")))
        .andExpect(jsonPath("$.data[0].maskedConfigSummary.externalNetwork").value("disabled"))
        .andExpect(jsonPath("$").value(not(containsString(System.getProperty("user.home")))))
        .andExpect(jsonPath("$").value(not(containsString("password"))));
  }

  @Test
  void mockStorageOperationPersistsDescriptorUpdatesPointerAndListsObjects() throws Exception {
    String batchId =
        createBatch(
            """
            [
              {
                "sourcePath": "Storage/BRD.pdf",
                "sourceType": "pdf",
                "status": "MARKDOWN_GENERATED",
                "confidence": 0.91,
                "reviewStatus": "REVIEW_REQUIRED",
                "pdfPath": "batch-old/BRD.pdf",
                "markdownPath": "batch-old/BRD.md"
              }
            ]
            """);
    String fileId = firstFileId(batchId);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/batches/" + batchId + "/storage-operations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "adapterKey": "mock-storage",
                          "requestedBy": "delivery-lead",
                          "mode": "mock",
                          "operations": [
                            {
                              "operationType": "STORE",
                              "layer": "markdown",
                              "objectKey": "%s/BRD.md",
                              "contentType": "text/markdown",
                              "fileId": "%s"
                            },
                            {
                              "operationType": "STORE",
                              "layer": "assets",
                              "objectKey": "%s/assets/diagram.png",
                              "contentType": "image/png"
                            }
                          ]
                        }
                        """
                            .formatted(batchId, fileId, batchId)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.adapterKey").value("mock-storage"))
            .andExpect(jsonPath("$.data.operationType").value("STORE"))
            .andExpect(jsonPath("$.data.status").value("SUCCEEDED"))
            .andExpect(jsonPath("$.data.summary.total").value(2))
            .andExpect(jsonPath("$.data.summary.stored").value(2))
            .andExpect(jsonPath("$.data.objects[0].layer").value("markdown"))
            .andExpect(jsonPath("$.data.objects[0].objectKey").value(batchId + "/BRD.md"))
            .andExpect(jsonPath("$.data.objects[0].checksum").value(containsString("sha256:")))
            .andReturn();

    String operationId = JsonPath.read(result.getResponse().getContentAsString(), "$.data.operationId");

    mockMvc
        .perform(get("/api/storage-operations/" + operationId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.operationId").value(operationId))
        .andExpect(jsonPath("$.data.objects[0].status").value("STORED"));

    mockMvc
        .perform(get("/api/files/" + fileId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.markdownPath").value(batchId + "/BRD.md"))
        .andExpect(jsonPath("$.data.status").value("MARKDOWN_GENERATED"))
        .andExpect(jsonPath("$.data.confidence").value(0.91))
        .andExpect(jsonPath("$.data.reviewStatus").value("REVIEW_REQUIRED"));

    mockMvc
        .perform(get("/api/batches/" + batchId + "/storage-objects?layer=markdown&pageSize=10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.objects[0].objectKey").value(batchId + "/BRD.md"))
        .andExpect(jsonPath("$.meta.pageSize").value(10));

    mockMvc
        .perform(get("/api/batches/" + batchId + "/storage-objects?pageSize=1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.objects[0].objectKey").value(batchId + "/BRD.md"))
        .andExpect(jsonPath("$.meta.pageSize").value(1))
        .andExpect(jsonPath("$.meta.nextPageToken").value("1"));
  }

  @Test
  void createStorageOperationRejectsUnknownAdapterUnsafeKeyAndOutOfNamespaceTarget() throws Exception {
    mockMvc
        .perform(
            post("/api/batches/batch-2026-06-20-001/storage-operations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "unknown",
                      "mode": "mock",
                      "operations": [
                        {
                          "operationType": "STORE",
                          "layer": "markdown",
                          "objectKey": "batch-2026-06-20-001/BRD.md"
                        }
                      ]
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.error.fields.adapterKey").exists());

    mockMvc
        .perform(
            post("/api/batches/batch-2026-06-20-001/storage-operations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "mock-storage",
                      "mode": "mock",
                      "operations": [
                        {
                          "operationType": "STORE",
                          "layer": "markdown",
                          "objectKey": "../raw/BRD.md"
                        }
                      ]
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields['operations[0].objectKey']").value("must be a safe relative path"));

    mockMvc
        .perform(
            post("/api/batches/batch-2026-06-20-001/storage-operations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "mock-storage",
                      "mode": "mock",
                      "operations": [
                        {
                          "operationType": "STORE",
                          "layer": "markdown",
                          "objectKey": "other-batch/BRD.md"
                        }
                      ]
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.fields['operations[0].objectKey']").value("must stay inside the batch namespace"));
  }

  @Test
  void adapterFaultFailsOperationSafelyAndLeavesFileUnchanged() throws Exception {
    String batchId =
        createBatch(
            """
            [
              {
                "sourcePath": "Storage/Fault.pdf",
                "sourceType": "pdf",
                "status": "MARKDOWN_GENERATED",
                "confidence": 0.91,
                "reviewStatus": "REVIEW_REQUIRED",
                "markdownPath": "old/Fault.md"
              }
            ]
            """);
    String fileId = firstFileId(batchId);

    mockMvc
        .perform(
            post("/api/batches/" + batchId + "/storage-operations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "adapterKey": "faulty-storage",
                      "requestedBy": "delivery-lead",
                      "mode": "mock",
                      "operations": [
                        {
                          "operationType": "STORE",
                          "layer": "markdown",
                          "objectKey": "%s/Fault.md",
                          "contentType": "text/markdown",
                          "fileId": "%s"
                        }
                      ]
                    }
                    """
                        .formatted(batchId, fileId)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.status").value("FAILED"))
        .andExpect(jsonPath("$.data.safeMessage").value(not(containsString("${STORAGE_TEST_TOKEN}"))))
        .andExpect(jsonPath("$.data.safeMessage").value(not(containsString("https://"))))
        .andExpect(jsonPath("$.data.safeMessage").value(not(containsString("secret-bucket"))))
        .andExpect(jsonPath("$.data.safeMessage").value(not(containsString("/private/runtime"))))
        .andExpect(jsonPath("$.data.objects").isEmpty());

    mockMvc
        .perform(get("/api/files/" + fileId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.markdownPath").value("old/Fault.md"))
        .andExpect(jsonPath("$.data.reviewStatus").value("REVIEW_REQUIRED"));
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
                          "name": "Storage Contract Package",
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
