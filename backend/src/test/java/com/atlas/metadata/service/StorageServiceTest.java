package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlas.metadata.adapter.StorageAdapter;
import com.atlas.metadata.adapter.StorageCapability;
import com.atlas.metadata.adapter.StorageDeleteResult;
import com.atlas.metadata.adapter.StorageListRequest;
import com.atlas.metadata.adapter.StorageListResult;
import com.atlas.metadata.adapter.StorageObjectDescriptor;
import com.atlas.metadata.adapter.StorageObjectRef;
import com.atlas.metadata.adapter.StoragePutRequest;
import com.atlas.metadata.domain.Batch;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.StorageObject;
import com.atlas.metadata.dto.CreateStorageOperationRequest;
import com.atlas.metadata.dto.CreateStorageOperationRequest.StorageObjectOperationRequest;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceKind;
import com.atlas.metadata.enums.SourceType;
import com.atlas.metadata.enums.StorageAdapterStatus;
import com.atlas.metadata.enums.StorageLayer;
import com.atlas.metadata.enums.StorageObjectStatus;
import com.atlas.metadata.enums.StorageOperationType;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.StorageObjectRepository;
import com.atlas.metadata.repository.StorageOperationRepository;
import com.atlas.metadata.validation.RelativePathValidator;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** Unit tests for storage service validation, execution, and pointer write-back behavior. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StorageServiceTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-07-03T00:00:00Z"), ZoneOffset.UTC);

  @Mock private BatchService batchService;
  @Mock private FileItemRepository fileItemRepository;
  @Mock private StorageOperationRepository storageOperationRepository;
  @Mock private StorageObjectRepository storageObjectRepository;

  private final Map<String, FileItem> filesById = new LinkedHashMap<>();
  private final List<StorageObject> savedObjects = new ArrayList<>();

  @BeforeEach
  void setUp() {
    when(batchService.findBatch("batch"))
        .thenReturn(
            Batch.create(
                "batch",
                "space",
                "Storage Service Test",
                SourceKind.folder,
                "delivery-lead",
                OffsetDateTime.now(CLOCK)));
    when(storageOperationRepository.existsByBatchIdAndStatusIn(any(), any())).thenReturn(false);
    when(storageOperationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(fileItemRepository.save(any(FileItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(fileItemRepository.findByBatchIdAndIdIn(any(), any()))
        .thenAnswer(
            invocation -> {
              List<String> ids = invocation.getArgument(1);
              return ids.stream().map(filesById::get).filter(file -> file != null).toList();
            });
    when(storageObjectRepository.save(any(StorageObject.class)))
        .thenAnswer(
            invocation -> {
              StorageObject object = invocation.getArgument(0);
              savedObjects.add(object);
              return object;
            });
    when(storageObjectRepository.findByOperationIdOrderByCreatedAtAsc(any()))
        .thenAnswer(invocation -> savedObjects);
  }

  @Test
  void createStoreOperationPersistsDescriptorAndPreservesFileReviewState() {
    StorageService service = service(new CapturingStorageAdapter());
    FileItem file = file("file-001");
    filesById.put(file.getId(), file);

    var response =
        service.createOperation(
            "batch",
            request(
                operation(
                    StorageOperationType.STORE,
                    StorageLayer.markdown,
                    "batch/BRD.md",
                    "text/markdown",
                    "file-001")));

    assertThat(response.status().name()).isEqualTo("SUCCEEDED");
    assertThat(response.summary().total()).isEqualTo(1);
    assertThat(response.summary().stored()).isEqualTo(1);
    assertThat(response.summary().totalBytes()).isPositive();
    assertThat(response.objects()).singleElement().satisfies(object -> {
      assertThat(object.layer()).isEqualTo(StorageLayer.markdown);
      assertThat(object.objectKey()).isEqualTo("batch/BRD.md");
      assertThat(object.status()).isEqualTo(StorageObjectStatus.STORED);
      assertThat(object.fileId()).isEqualTo("file-001");
    });
    assertThat(file.getMarkdownPath()).isEqualTo("batch/BRD.md");
    assertThat(file.getStatus()).isEqualTo(FileStatus.MARKDOWN_GENERATED);
    assertThat(file.getConfidence()).isEqualByComparingTo("0.910");
    assertThat(file.getReviewStatus()).isEqualTo(ReviewStatus.REVIEW_REQUIRED);
  }

  @Test
  void rejectsUnsafeKeyBeforeOperationOrObjectPersistence() {
    StorageService service = service(new CapturingStorageAdapter());

    assertThatThrownBy(
            () ->
                service.createOperation(
                    "batch",
                    request(
                        operation(
                            StorageOperationType.STORE,
                            StorageLayer.markdown,
                            "../raw/BRD.md",
                            "text/markdown",
                            null))))
        .isInstanceOf(RequestValidationException.class);

    verify(storageOperationRepository, never()).save(any());
    verify(storageObjectRepository, never()).save(any());
  }

  @Test
  void rejectsOutOfNamespaceKeyBeforeAdapterExecution() {
    CapturingStorageAdapter adapter = new CapturingStorageAdapter();
    StorageService service = service(adapter);

    assertThatThrownBy(
            () ->
                service.createOperation(
                    "batch",
                    request(
                        operation(
                            StorageOperationType.STORE,
                            StorageLayer.markdown,
                            "other-batch/BRD.md",
                            "text/markdown",
                            null))))
        .isInstanceOf(RequestValidationException.class);

    assertThat(adapter.putCalled).isFalse();
    verify(storageOperationRepository, never()).save(any());
  }

  @Test
  void rejectsInvalidStoredDescriptorWithoutFileWriteBack() {
    StorageAdapter adapter =
        new CapturingStorageAdapter() {
          @Override
          public StorageObjectDescriptor put(StoragePutRequest request) {
            putCalled = true;
            return new StorageObjectDescriptor(
                request.layer(),
                request.objectKey(),
                request.contentType(),
                -1L,
                "",
                ADAPTER_KEY,
                StorageObjectStatus.STORED,
                request.fileId(),
                null);
          }
        };
    StorageService service = service(adapter);
    FileItem file = file("file-001");
    filesById.put(file.getId(), file);

    assertThatThrownBy(
            () ->
                service.createOperation(
                    "batch",
                    request(
                        operation(
                            StorageOperationType.STORE,
                            StorageLayer.markdown,
                            "batch/BRD.md",
                            "text/markdown",
                            "file-001"))))
        .isInstanceOf(RequestValidationException.class)
        .satisfies(
            error ->
                assertThat(((RequestValidationException) error).getFields())
                    .containsEntry("descriptor.sizeBytes", "must be non-negative")
                    .containsEntry("descriptor.checksum", "must be present for STORED objects"));

    verify(fileItemRepository, never()).save(file);
    assertThat(savedObjects).isEmpty();
  }

  @Test
  void adapterFaultMasksSecretsEndpointsHostsAndPrivatePathsAndLeavesFileUnchanged() {
    StorageAdapter adapter =
        new CapturingStorageAdapter() {
          @Override
          public StorageObjectDescriptor put(StoragePutRequest request) {
            throw new IllegalStateException(
                "token=${STORAGE_TEST_TOKEN} endpoint=https://storage.internal.local bucket=secret-bucket at /private/runtime/storage");
          }
        };
    StorageService service = service(adapter);
    FileItem file = file("file-001");
    filesById.put(file.getId(), file);

    var response =
        service.createOperation(
            "batch",
            request(
                operation(
                    StorageOperationType.STORE,
                    StorageLayer.markdown,
                    "batch/BRD.md",
                    "text/markdown",
                    "file-001")));

    assertThat(response.status().name()).isEqualTo("FAILED");
    assertThat(response.safeMessage())
        .doesNotContain("${STORAGE_TEST_TOKEN}", "STORAGE_TEST_TOKEN", "https://", "secret-bucket", "/private/runtime")
        .contains("token", "[masked]", "endpoint", "bucket");
    assertThat(response.objects()).isEmpty();
    assertThat(file.getMarkdownPath()).isNull();
  }

  private StorageService service(StorageAdapter adapter) {
    return new StorageService(
        batchService,
        fileItemRepository,
        storageOperationRepository,
        storageObjectRepository,
        new StorageAdapterRegistry(List.of(adapter)),
        new StorageSummaryCalculator(),
        new RelativePathValidator(),
        CLOCK);
  }

  private CreateStorageOperationRequest request(StorageObjectOperationRequest... operations) {
    return new CreateStorageOperationRequest("mock-storage", "delivery-lead", "mock", List.of(operations));
  }

  private StorageObjectOperationRequest operation(
      StorageOperationType operationType,
      StorageLayer layer,
      String objectKey,
      String contentType,
      String fileId) {
    return new StorageObjectOperationRequest(operationType, layer, objectKey, contentType, fileId);
  }

  private FileItem file(String id) {
    return FileItem.create(
        id,
        "batch",
        "Discovery/" + id + ".pdf",
        SourceType.pdf,
        FileStatus.MARKDOWN_GENERATED,
        new BigDecimal("0.910"),
        ReviewStatus.REVIEW_REQUIRED,
        OffsetDateTime.now(CLOCK));
  }

  private static class CapturingStorageAdapter implements StorageAdapter {

    protected static final String ADAPTER_KEY = "mock-storage";
    protected boolean putCalled;

    @Override
    public StorageCapability capability() {
      return new StorageCapability(
          ADAPTER_KEY,
          "Mock Storage",
          "test",
          List.of(StorageLayer.values()),
          true,
          StorageAdapterStatus.AVAILABLE,
          Map.of("externalNetwork", "disabled"));
    }

    @Override
    public StorageObjectDescriptor put(StoragePutRequest request) {
      putCalled = true;
      return new StorageObjectDescriptor(
          request.layer(),
          request.objectKey(),
          request.contentType(),
          256L,
          "sha256:test",
          ADAPTER_KEY,
          StorageObjectStatus.STORED,
          request.fileId(),
          null);
    }

    @Override
    public StorageObjectDescriptor get(StorageObjectRef ref) {
      return new StorageObjectDescriptor(
          ref.layer(), ref.objectKey(), null, null, null, ADAPTER_KEY, StorageObjectStatus.MISSING, ref.fileId(), null);
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
      return new StorageDeleteResult(
          new StorageObjectDescriptor(
              ref.layer(), ref.objectKey(), null, null, null, ADAPTER_KEY, StorageObjectStatus.DELETED, ref.fileId(), null));
    }
  }
}
