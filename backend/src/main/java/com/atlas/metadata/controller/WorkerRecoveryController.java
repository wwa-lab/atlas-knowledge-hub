package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.DeadLetterActionRequest;
import com.atlas.metadata.dto.DeadLetterEntryResponse;
import com.atlas.metadata.dto.WorkerJobResponse;
import com.atlas.metadata.service.WorkerJobService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for local worker retry and dead-letter inspection. */
@RestController
@RequestMapping("/api")
public class WorkerRecoveryController {

  private final WorkerJobService workerJobService;

  public WorkerRecoveryController(WorkerJobService workerJobService) {
    this.workerJobService = workerJobService;
  }

  /** Lists failed or retry-waiting worker jobs. */
  @GetMapping("/worker-jobs/failed")
  public ApiEnvelope<List<WorkerJobResponse>> listFailedJobs() {
    return ApiEnvelope.ok(workerJobService.listFailedJobs());
  }

  /** Gets one worker job detail. */
  @GetMapping("/worker-jobs/{jobId}")
  public ApiEnvelope<WorkerJobResponse> getJob(@PathVariable String jobId) {
    return ApiEnvelope.ok(workerJobService.getJob(jobId));
  }

  /** Lists dead-letter entries. */
  @GetMapping("/dead-letter-entries")
  public ApiEnvelope<List<DeadLetterEntryResponse>> listDeadLetters() {
    return ApiEnvelope.ok(workerJobService.listDeadLetters());
  }

  /** Gets one dead-letter entry detail. */
  @GetMapping("/dead-letter-entries/{entryId}")
  public ApiEnvelope<DeadLetterEntryResponse> getDeadLetter(@PathVariable String entryId) {
    return ApiEnvelope.ok(workerJobService.getDeadLetter(entryId));
  }

  /** Performs a safe local retry transition. */
  @PostMapping("/dead-letter-entries/{entryId}/retry")
  public ApiEnvelope<DeadLetterEntryResponse> retry(
      @PathVariable String entryId, @RequestBody(required = false) DeadLetterActionRequest request) {
    return ApiEnvelope.ok(workerJobService.retryDeadLetter(entryId, operator(request)));
  }

  /** Acknowledges a dead-letter entry. */
  @PostMapping("/dead-letter-entries/{entryId}/acknowledge")
  public ApiEnvelope<DeadLetterEntryResponse> acknowledge(
      @PathVariable String entryId, @RequestBody(required = false) DeadLetterActionRequest request) {
    return ApiEnvelope.ok(workerJobService.acknowledge(entryId, operator(request)));
  }

  private String operator(DeadLetterActionRequest request) {
    return request == null ? null : request.operator();
  }
}
