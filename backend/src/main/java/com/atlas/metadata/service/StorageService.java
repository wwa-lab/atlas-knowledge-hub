package com.atlas.metadata.service;

import com.atlas.metadata.adapter.StorageAdapter;
import com.atlas.metadata.adapter.StorageCapability;
import com.atlas.metadata.adapter.StorageObjectDescriptor;
import com.atlas.metadata.adapter.StorageObjectRef;
import com.atlas.metadata.adapter.StoragePutRequest;
import com.atlas.metadata.domain.Batch;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.StorageObject;
import com.atlas.metadata.domain.StorageOperation;
import com.atlas.metadata.domain.StorageOperation.StorageSummary;
import com.atlas.metadata.dto.CreateStorageOperationRequest;
import com.atlas.metadata.dto.CreateStorageOperationRequest.StorageObjectOperationRequest;
import com.atlas.metadata.dto.StorageCapabilityResponse;
import com.atlas.metadata.dto.StorageObjectResponse;
import com.atlas.metadata.dto.StorageOperationResponse;
import com.atlas.metadata.dto.StorageOperationSummaryResponse;
import com.atlas.metadata.dto.mapping.StorageMapper;
import com.atlas.metadata.enums.StorageAdapterStatus;
import com.atlas.metadata.enums.StorageLayer;
import com.atlas.metadata.enums.StorageObjectStatus;
import com.atlas.metadata.enums.StorageOperationStatus;
import com.atlas.metadata.enums.StorageOperationType;
import com.atlas.metadata.exception.ConflictException;
import com.atlas.metadata.exception.NotFoundException;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.StorageObjectRepository;
import com.atlas.metadata.repository.StorageOperationRepository;
import com.atlas.metadata.validation.RelativePathValidator;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for storage adapter operations. */
@Service
public class StorageService {

  private static final Collection<StorageOperationStatus> ACTIVE_STATUSES =
      List.of(StorageOperationStatus.REQUESTED, StorageOperationStatus.RUNNING);
  private static final int MAX_SAFE_MESSAGE_LENGTH = 240;

  private final BatchService batchService;
  private final FileItemRepository fileItemRepository;
  private final StorageOperationRepository storageOperationRepository;
  private final StorageObjectRepository storageObjectRepository;
  private final StorageAdapterRegistry adapterRegistry;
  private final StorageSummaryCalculator summaryCalculator;
  private final RelativePathValidator relativePathValidator;
  private final Clock clock;

  /** Creates the service. */
  @Autowired
  public StorageService(
      BatchService batchService,
      FileItemRepository fileItemRepository,
      StorageOperationRepository storageOperationRepository,
      StorageObjectRepository storageObjectRepository,
      StorageAdapterRegistry adapterRegistry) {
    this(
        batchService,
        fileItemRepository,
        storageOperationRepository,
        storageObjectRepository,
        adapterRegistry,
        new StorageSummaryCalculator(),
        new RelativePathValidator(),
        Clock.systemUTC());
  }

  StorageService(
      BatchService batchService,
      FileItemRepository fileItemRepository,
      StorageOperationRepository storageOperationRepository,
      StorageObjectRepository storageObjectRepository,
      StorageAdapterRegistry adapterRegistry,
      StorageSummaryCalculator summaryCalculator,
      RelativePathValidator relativePathValidator,
      Clock clock) {
    this.batchService = batchService;
    this.fileItemRepository = fileItemRepository;
    this.storageOperationRepository = storageOperationRepository;
    this.storageObjectRepository = storageObjectRepository;
    this.adapterRegistry = adapterRegistry;
    this.summaryCalculator = summaryCalculator;
    this.relativePathValidator = relativePathValidator;
    this.clock = clock;
  }

  /** Lists storage capabilities with masked configuration. */
  @Transactional(readOnly = true)
  public List<StorageCapabilityResponse> listCapabilities() {
    return adapterRegistry.capabilities().stream().map(StorageMapper::toResponse).toList();
  }

  /** Creates and executes a storage operation for one batch. */
  @Transactional
  public StorageOperationResponse createOperation(String batchId, CreateStorageOperationRequest request) {
    Batch batch = batchService.findBatch(batchId);
    validateMode(request.mode());
    if (storageOperationRepository.existsByBatchIdAndStatusIn(batchId, ACTIVE_STATUSES)) {
      throw new ConflictException("Batch already has an active storage operation.");
    }

    StorageAdapter adapter = adapterRegistry.resolve(request.adapterKey());
    StorageCapability capability = adapter.capability();
    List<StorageObjectOperationRequest> operations = validateOperations(batch, capability, request.operations());
    Map<String, FileItem> filesById = targetFiles(batchId, operations);

    OffsetDateTime now = OffsetDateTime.now(clock);
    StorageOperation operation =
        storageOperationRepository.save(
            StorageOperation.create(
                operationId(now),
                batch.getId(),
                batch.getSpaceId(),
                capability.adapterKey(),
                capability.version(),
                operationType(operations),
                request.requestedBy(),
                effectiveMode(request.mode()),
                now));

    if (capability.status() != StorageAdapterStatus.AVAILABLE) {
      StorageSummary summary = new StorageSummary(operations.size(), 0, 0, 0, operations.size(), 0, 0);
      operation.complete(
          StorageOperationStatus.FAILED,
          summary,
          OffsetDateTime.now(clock),
          "Storage adapter is unavailable.");
      return response(storageOperationRepository.save(operation));
    }

    operation.markRunning();
    storageOperationRepository.save(operation);
    List<StorageObject> persistedObjects;
    try {
      persistedObjects = executeOperations(operation, batch, adapter, operations, filesById);
    } catch (RequestValidationException ex) {
      throw ex;
    } catch (RuntimeException ex) {
      StorageSummary summary = new StorageSummary(operations.size(), 0, 0, 0, operations.size(), 0, 0);
      operation.complete(
          StorageOperationStatus.FAILED,
          summary,
          OffsetDateTime.now(clock),
          safeAdapterFailureMessage(ex));
      return response(storageOperationRepository.save(operation));
    }
    StorageSummary summary = summaryCalculator.compute(persistedObjects, 0);
    operation.complete(
        terminalStatus(summary),
        summary,
        OffsetDateTime.now(clock),
        safeCompletionMessage(summary));
    return response(storageOperationRepository.save(operation));
  }

  /** Gets a storage operation report. */
  @Transactional(readOnly = true)
  public StorageOperationResponse getOperation(String operationId) {
    return response(findOperation(operationId));
  }

  /** Lists stored-object descriptors for one batch and optional layer. */
  @Transactional(readOnly = true)
  public Page<StorageObjectResponse> listObjects(String batchId, StorageLayer layer, Pageable pageable) {
    batchService.findBatch(batchId);
    Page<StorageObject> page =
        layer == null
            ? storageObjectRepository.findByBatchId(batchId, pageable)
            : storageObjectRepository.findByBatchIdAndLayer(batchId, layer, pageable);
    return page.map(StorageMapper::toObjectResponse);
  }

  private List<StorageObjectOperationRequest> validateOperations(
      Batch batch, StorageCapability capability, List<StorageObjectOperationRequest> operations) {
    Map<String, String> errors = new LinkedHashMap<>();
    if (operations == null || operations.isEmpty()) {
      errors.put("operations", "must include at least one storage object operation");
      throw new RequestValidationException(errors);
    }
    for (int index = 0; index < operations.size(); index++) {
      StorageObjectOperationRequest operation = operations.get(index);
      String prefix = "operations[" + index + "]";
      if (operation.operationType() == null) {
        errors.put(prefix + ".operationType", "must be STORE or DELETE");
      } else if (operation.operationType() == StorageOperationType.LIST) {
        errors.put(prefix + ".operationType", "must be STORE or DELETE");
      }
      if (operation.layer() == null) {
        errors.put(prefix + ".layer", "must be a supported storage layer");
      } else if (!capability.supportedLayers().contains(operation.layer())) {
        errors.put(prefix + ".layer", "must be supported by the resolved storage adapter");
      }
      validateObjectKey(prefix + ".objectKey", batch.getId(), operation.objectKey(), errors);
      validatePointerTarget(prefix, operation, errors);
    }
    if (!errors.isEmpty()) {
      throw new RequestValidationException(errors);
    }
    return List.copyOf(operations);
  }

  private void validateObjectKey(
      String field, String batchId, String objectKey, Map<String, String> errors) {
    if (objectKey == null || objectKey.isBlank()) {
      errors.put(field, "must be present");
      return;
    }
    if (!relativePathValidator.isValid(objectKey, null)) {
      errors.put(field, "must be a safe relative path");
      return;
    }
    if (!objectKey.startsWith(batchId + "/")) {
      errors.put(field, "must stay inside the batch namespace");
    }
  }

  private void validatePointerTarget(
      String prefix, StorageObjectOperationRequest operation, Map<String, String> errors) {
    if (operation.fileId() == null || operation.fileId().isBlank()) {
      return;
    }
    if (operation.operationType() != StorageOperationType.STORE) {
      errors.put(prefix + ".fileId", "is only supported for STORE pointer write-back");
      return;
    }
    if (operation.layer() != StorageLayer.pdf
        && operation.layer() != StorageLayer.markdown
        && operation.layer() != StorageLayer.assets) {
      errors.put(prefix + ".fileId", "requires pdf, markdown, or assets layer");
    }
  }

  private Map<String, FileItem> targetFiles(String batchId, List<StorageObjectOperationRequest> operations) {
    List<String> fileIds =
        operations.stream()
            .map(StorageObjectOperationRequest::fileId)
            .filter(fileId -> fileId != null && !fileId.isBlank())
            .distinct()
            .toList();
    if (fileIds.isEmpty()) {
      return Map.of();
    }
    List<FileItem> files = fileItemRepository.findByBatchIdAndIdIn(batchId, fileIds);
    if (files.size() != fileIds.size()) {
      throw new NotFoundException("File item not found.");
    }
    return files.stream().collect(Collectors.toMap(FileItem::getId, Function.identity()));
  }

  private List<StorageObject> executeOperations(
      StorageOperation operation,
      Batch batch,
      StorageAdapter adapter,
      List<StorageObjectOperationRequest> requests,
      Map<String, FileItem> filesById) {
    return requests.stream()
        .map(request -> executeOperation(operation, batch, adapter, request, filesById.get(request.fileId())))
        .toList();
  }

  private StorageObject executeOperation(
      StorageOperation operation,
      Batch batch,
      StorageAdapter adapter,
      StorageObjectOperationRequest request,
      FileItem file) {
    StorageObjectDescriptor descriptor =
        request.operationType() == StorageOperationType.STORE
            ? adapter.put(toPutRequest(operation, batch, request))
            : adapter.delete(toRef(operation, batch, request)).descriptor();
    validateDescriptor(operation, request, descriptor);
    StorageObject object = persistDescriptor(operation, batch, descriptor);
    if (descriptor.status() == StorageObjectStatus.STORED && file != null) {
      writeBackPointer(file, descriptor);
    }
    return object;
  }

  private StoragePutRequest toPutRequest(
      StorageOperation operation, Batch batch, StorageObjectOperationRequest request) {
    return new StoragePutRequest(
        operation.getId(),
        batch.getId(),
        batch.getSpaceId(),
        request.layer(),
        request.objectKey(),
        sanitize(request.contentType()),
        contentRef(request),
        request.fileId());
  }

  private StorageObjectRef toRef(
      StorageOperation operation, Batch batch, StorageObjectOperationRequest request) {
    return new StorageObjectRef(
        operation.getId(),
        batch.getId(),
        batch.getSpaceId(),
        request.layer(),
        request.objectKey(),
        request.fileId());
  }

  private String contentRef(StorageObjectOperationRequest request) {
    return request.fileId() == null || request.fileId().isBlank()
        ? request.layer().name() + ":" + request.objectKey()
        : request.fileId() + ":" + request.objectKey();
  }

  private void validateDescriptor(
      StorageOperation operation,
      StorageObjectOperationRequest request,
      StorageObjectDescriptor descriptor) {
    Map<String, String> errors = new LinkedHashMap<>();
    if (!operation.getAdapterKey().equals(descriptor.adapterKey())) {
      errors.put("adapterKey", "must match the resolved storage adapter");
    }
    if (descriptor.layer() != request.layer()) {
      errors.put("descriptor.layer", "must match the requested layer");
    }
    validateObjectKey("descriptor.objectKey", operation.getBatchId(), descriptor.objectKey(), errors);
    if (!request.objectKey().equals(descriptor.objectKey())) {
      errors.put("descriptor.objectKey", "must match the requested object key");
    }
    if (descriptor.status() == null) {
      errors.put("descriptor.status", "must be present");
    }
    if (descriptor.sizeBytes() != null && descriptor.sizeBytes() < 0) {
      errors.put("descriptor.sizeBytes", "must be non-negative");
    }
    if (descriptor.status() == StorageObjectStatus.STORED
        && (descriptor.checksum() == null || descriptor.checksum().isBlank())) {
      errors.put("descriptor.checksum", "must be present for STORED objects");
    }
    if (!errors.isEmpty()) {
      throw new RequestValidationException(errors);
    }
  }

  private StorageObject persistDescriptor(
      StorageOperation operation, Batch batch, StorageObjectDescriptor descriptor) {
    OffsetDateTime now = OffsetDateTime.now(clock);
    StorageObject object =
        StorageObject.create(
            "storage-object-" + UUID.randomUUID().toString().substring(0, 8),
            operation.getId(),
            batch.getId(),
            descriptor.fileId(),
            descriptor.layer(),
            descriptor.objectKey(),
            sanitize(descriptor.contentType()),
            descriptor.sizeBytes(),
            descriptor.checksum(),
            descriptor.adapterKey(),
            descriptor.status(),
            sanitize(descriptor.safeError()),
            now,
            descriptor.status() == StorageObjectStatus.DELETED ? now : null);
    return storageObjectRepository.save(object);
  }

  private void writeBackPointer(FileItem file, StorageObjectDescriptor descriptor) {
    String pdfPath = file.getPdfPath();
    String markdownPath = file.getMarkdownPath();
    String assetsPath = file.getAssetsPath();
    if (descriptor.layer() == StorageLayer.pdf) {
      pdfPath = descriptor.objectKey();
    } else if (descriptor.layer() == StorageLayer.markdown) {
      markdownPath = descriptor.objectKey();
    } else if (descriptor.layer() == StorageLayer.assets) {
      assetsPath = descriptor.objectKey();
    }
    file.setArtifacts(pdfPath, markdownPath, assetsPath, file.getErrorMessage());
    fileItemRepository.save(file);
  }

  private StorageOperationResponse response(StorageOperation operation) {
    List<StorageObject> objects =
        storageObjectRepository.findByOperationIdOrderByCreatedAtAsc(operation.getId());
    StorageOperationSummaryResponse summary =
        operation.getCompletedAt() == null
            ? toSummary(summaryCalculator.compute(objects, 0))
            : StorageMapper.toSummary(operation);
    return StorageMapper.toResponse(operation, summary, objects);
  }

  private StorageOperation findOperation(String operationId) {
    return storageOperationRepository
        .findById(operationId)
        .orElseThrow(() -> new NotFoundException("Storage operation not found."));
  }

  private StorageOperationType operationType(List<StorageObjectOperationRequest> operations) {
    boolean hasStore =
        operations.stream().anyMatch(operation -> operation.operationType() == StorageOperationType.STORE);
    return hasStore ? StorageOperationType.STORE : StorageOperationType.DELETE;
  }

  private StorageOperationStatus terminalStatus(StorageSummary summary) {
    int succeeded = summary.stored() + summary.deleted();
    if (summary.total() == 0 || succeeded == 0) {
      return StorageOperationStatus.FAILED;
    }
    if (summary.failed() > 0 || summary.skipped() > 0 || summary.missing() > 0) {
      return StorageOperationStatus.PARTIAL_FAILED;
    }
    return StorageOperationStatus.SUCCEEDED;
  }

  private StorageOperationSummaryResponse toSummary(StorageSummary summary) {
    return new StorageOperationSummaryResponse(
        summary.total(),
        summary.stored(),
        summary.deleted(),
        summary.missing(),
        summary.failed(),
        summary.skipped(),
        summary.totalBytes());
  }

  private String safeCompletionMessage(StorageSummary summary) {
    if (summary.failed() > 0 || summary.skipped() > 0 || summary.missing() > 0) {
      return "Storage operation completed with one or more object outcomes requiring review.";
    }
    return "Storage operation completed.";
  }

  private void validateMode(String mode) {
    if (mode == null || mode.isBlank() || "mock".equals(mode) || "configured".equals(mode)) {
      return;
    }
    throw new RequestValidationException(Map.of("mode", "must be mock or configured"));
  }

  private String effectiveMode(String mode) {
    return mode == null || mode.isBlank() ? "mock" : mode;
  }

  private String sanitize(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String sanitized =
        value.replaceAll("(?i)(password|token|api[_-]?key|secret|credential)\\s*[:=]\\s*\\S+", "$1=[masked]")
            .replaceAll("(?i)arn:aws:[^\\s]+", "[storage-resource]")
            .replaceAll("(?i)(bucket|endpoint|region)\\s*[:=]\\s*\\S+", "$1=[masked]")
            .replaceAll("(?i)\\b[a-z][a-z0-9+.-]*://[^\\s]+", "[endpoint]")
            .replaceAll("[A-Za-z]:[/\\\\][^\\s]+", "[path]")
            .replaceAll("(/[^\\s]+){2,}", "[path]")
            .replaceAll("\\bat\\s+[\\w.$]+\\([^)]*\\)", "[stack]")
            .replaceAll(
                "(?i)\\b[a-z0-9][a-z0-9-]*(?:\\.[a-z0-9][a-z0-9-]*)+(?::\\d+)?\\b",
                "[host]");
    return sanitized.length() <= MAX_SAFE_MESSAGE_LENGTH
        ? sanitized
        : sanitized.substring(0, MAX_SAFE_MESSAGE_LENGTH);
  }

  private String safeAdapterFailureMessage(RuntimeException ex) {
    String safeMessage = sanitize(ex.getMessage());
    return safeMessage == null ? "Storage adapter failed." : safeMessage;
  }

  private String operationId(OffsetDateTime now) {
    return "storage-op-"
        + now.format(DateTimeFormatter.ISO_LOCAL_DATE)
        + "-"
        + UUID.randomUUID().toString().substring(0, 8);
  }
}
