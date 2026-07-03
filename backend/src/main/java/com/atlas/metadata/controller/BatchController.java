package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.BatchResponse;
import com.atlas.metadata.dto.CreateBatchRequest;
import com.atlas.metadata.service.BatchService;
import jakarta.validation.Valid;
import java.util.List;
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

/** REST controller for batch metadata. */
@RestController
@RequestMapping("/api")
public class BatchController {

  private final BatchService batchService;

  /** Creates the controller. */
  public BatchController(BatchService batchService) {
    this.batchService = batchService;
  }

  /** Lists batches for a Knowledge Space. */
  @GetMapping("/spaces/{spaceId}/batches")
  public ApiEnvelope<List<BatchResponse>> listBatches(
      @PathVariable String spaceId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Page<BatchResponse> batches =
        batchService.listBatches(
            spaceId, PageRequests.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt")));
    return ApiEnvelope.ok(batches.getContent(), PageRequests.meta(batches));
  }

  /** Gets one batch with derived metrics. */
  @GetMapping("/batches/{batchId}")
  public ApiEnvelope<BatchResponse> getBatch(@PathVariable String batchId) {
    return ApiEnvelope.ok(batchService.getBatch(batchId));
  }

  /** Creates a batch from pre-computed inventory metadata. */
  @PostMapping("/spaces/{spaceId}/batches")
  public ResponseEntity<ApiEnvelope<BatchResponse>> createBatch(
      @PathVariable String spaceId, @Valid @RequestBody CreateBatchRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiEnvelope.ok(batchService.createBatch(spaceId, request)));
  }
}
