package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.CreateStorageOperationRequest;
import com.atlas.metadata.dto.PageMeta;
import com.atlas.metadata.dto.StorageCapabilityResponse;
import com.atlas.metadata.dto.StorageObjectListResponse;
import com.atlas.metadata.dto.StorageObjectResponse;
import com.atlas.metadata.dto.StorageOperationResponse;
import com.atlas.metadata.enums.StorageLayer;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.service.StorageService;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for storage adapter capabilities and storage operations. */
@RestController
@RequestMapping("/api")
public class StorageController {

  private final StorageService storageService;

  /** Creates the controller. */
  public StorageController(StorageService storageService) {
    this.storageService = storageService;
  }

  /** Lists storage adapter capabilities with masked configuration. */
  @GetMapping("/storage-adapters")
  public ApiEnvelope<List<StorageCapabilityResponse>> listStorageAdapters() {
    return ApiEnvelope.ok(storageService.listCapabilities());
  }

  /** Creates and executes a storage operation for a batch. */
  @PostMapping("/batches/{batchId}/storage-operations")
  public ResponseEntity<ApiEnvelope<StorageOperationResponse>> createStorageOperation(
      @PathVariable String batchId, @RequestBody CreateStorageOperationRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiEnvelope.ok(storageService.createOperation(batchId, request)));
  }

  /** Gets a storage operation report. */
  @GetMapping("/storage-operations/{operationId}")
  public ApiEnvelope<StorageOperationResponse> getStorageOperation(@PathVariable String operationId) {
    return ApiEnvelope.ok(storageService.getOperation(operationId));
  }

  /** Lists stored-object descriptors for a batch. */
  @GetMapping("/batches/{batchId}/storage-objects")
  public ApiEnvelope<StorageObjectListResponse> listStorageObjects(
      @PathVariable String batchId,
      @RequestParam(required = false) StorageLayer layer,
      @RequestParam(defaultValue = "50") int pageSize,
      @RequestParam(required = false) String pageToken) {
    int page = pageFromToken(pageToken);
    Page<StorageObjectResponse> objects =
        storageService.listObjects(
            batchId,
            layer,
            PageRequests.of(page, pageSize, Sort.by(Sort.Direction.ASC, "createdAt")));
    String nextPageToken = objects.hasNext() ? Integer.toString(objects.getNumber() + 1) : null;
    return ApiEnvelope.ok(
        new StorageObjectListResponse(objects.getContent()),
        PageMeta.continuation(objects.getSize(), nextPageToken));
  }

  private int pageFromToken(String pageToken) {
    if (pageToken == null || pageToken.isBlank()) {
      return 0;
    }
    try {
      int page = Integer.parseInt(pageToken);
      if (page < 0) {
        throw new NumberFormatException("negative page token");
      }
      return page;
    } catch (NumberFormatException ex) {
      throw new RequestValidationException(Map.of("pageToken", "must be a non-negative page number token"));
    }
  }
}
