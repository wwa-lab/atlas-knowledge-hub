package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.domain.DeadLetterEntry;
import com.atlas.metadata.domain.WorkerJob;
import com.atlas.metadata.dto.RecordWorkerFailureCommand;
import com.atlas.metadata.enums.DeadLetterStatus;
import com.atlas.metadata.enums.WorkerJobStatus;
import com.atlas.metadata.enums.WorkerJobType;
import com.atlas.metadata.enums.WorkerSafeErrorCategory;
import com.atlas.metadata.exception.SafeErrorCodes;
import com.atlas.metadata.exception.SafeErrorSanitizer;
import com.atlas.metadata.repository.DeadLetterEntryRepository;
import com.atlas.metadata.repository.WorkerJobAttemptRepository;
import com.atlas.metadata.repository.WorkerJobRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Unit coverage for deterministic local worker retry and dead-letter transitions. */
@ExtendWith(MockitoExtension.class)
class WorkerJobServiceTest {

  private static final Clock FIXED_CLOCK =
      Clock.fixed(Instant.parse("2026-07-07T00:00:00Z"), ZoneOffset.UTC);

  @Mock private WorkerJobRepository jobRepository;
  @Mock private WorkerJobAttemptRepository attemptRepository;
  @Mock private DeadLetterEntryRepository deadLetterRepository;

  @Test
  void retryableFailureBeforeExhaustionSchedulesDeterministicRetryAndSanitizesError() {
    WorkerJobService service = service();
    WorkerJob job = createJob();
    when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
    when(jobRepository.save(any(WorkerJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(attemptRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.recordFailure(
        job.getId(),
        new RecordWorkerFailureCommand(
            true,
            SafeErrorCodes.SAFE_SYSTEM_ERROR,
            WorkerSafeErrorCategory.SOURCE_UNREADABLE,
            "api" + "Key=hidden /" + "Users/private/source.md RuntimeException",
            Map.of("sourceFile", "samples/input/demo.pdf")));

    assertThat(job.getStatus()).isEqualTo(WorkerJobStatus.WAITING_RETRY);
    assertThat(job.getAttemptCount()).isEqualTo(1);
    assertThat(job.getRetryDelaySeconds()).isEqualTo(30);
    assertThat(job.getNextRetryAt()).isNotNull();
    assertThat(job.getSafeErrorMessage()).doesNotContain("hidden", "/Users", "RuntimeException");
  }

  @Test
  void exhaustedRetryableFailureCreatesOneDeadLetterEntryWithSourceTrace() {
    WorkerJobService service = service();
    WorkerJob job = createJob();
    job.markWaitingRetry(1, 30, FIXED_CLOCK.instant().atOffset(ZoneOffset.UTC), "SAFE_SYSTEM_ERROR", WorkerSafeErrorCategory.SAFE_SYSTEM, "first");
    job.markWaitingRetry(2, 120, FIXED_CLOCK.instant().atOffset(ZoneOffset.UTC), "SAFE_SYSTEM_ERROR", WorkerSafeErrorCategory.SAFE_SYSTEM, "second");
    when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
    when(jobRepository.save(any(WorkerJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(attemptRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(deadLetterRepository.findByWorkerJobId(job.getId())).thenReturn(Optional.empty());
    when(deadLetterRepository.save(any(DeadLetterEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

    DeadLetterEntry entry =
        service.recordFailure(
            job.getId(),
            new RecordWorkerFailureCommand(
                true,
                SafeErrorCodes.SAFE_SYSTEM_ERROR,
                WorkerSafeErrorCategory.SAFE_SYSTEM,
                "third terminal failure",
                Map.of("sourceFile", "samples/input/demo.pdf")));

    assertThat(job.getStatus()).isEqualTo(WorkerJobStatus.DEAD_LETTERED);
    assertThat(entry.getStatus()).isEqualTo(DeadLetterStatus.OPEN);
    assertThat(entry.getAttemptSummary()).isEqualTo("3/3 attempts failed");
    assertThat(entry.getSourceTrace()).containsEntry("sourceFile", "samples/input/demo.pdf");
  }

  @Test
  void acknowledgeIsIdempotentForDeadLetterEntry() {
    WorkerJobService service = service();
    WorkerJob job = createJob();
    DeadLetterEntry entry =
        DeadLetterEntry.createOpen(
            "dead-letter-1",
            job,
            "1/3 attempts failed",
            SafeErrorCodes.SAFE_SYSTEM_ERROR,
            WorkerSafeErrorCategory.SAFE_SYSTEM,
            "safe failure",
            FIXED_CLOCK.instant().atOffset(ZoneOffset.UTC));
    when(deadLetterRepository.findById(entry.getId())).thenReturn(Optional.of(entry));
    when(jobRepository.findById(job.getId())).thenReturn(Optional.of(job));
    when(jobRepository.save(any(WorkerJob.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(deadLetterRepository.save(any(DeadLetterEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

    service.acknowledge(entry.getId(), "operator-one");
    service.acknowledge(entry.getId(), "operator-one");

    assertThat(entry.getStatus()).isEqualTo(DeadLetterStatus.ACKNOWLEDGED);
    assertThat(job.getStatus()).isEqualTo(WorkerJobStatus.ACKNOWLEDGED);
  }

  private WorkerJobService service() {
    return new WorkerJobService(
        jobRepository,
        attemptRepository,
        deadLetterRepository,
        new LocalRetryPolicy(),
        new SafeErrorSanitizer(),
        FIXED_CLOCK);
  }

  private WorkerJob createJob() {
    return WorkerJob.create(
        "worker-job-1",
        WorkerJobType.CONNECTOR_SYNC,
        "connector-sync-run",
        "connector-run-1",
        Map.of("sourceFile", "samples/input/demo.pdf"),
        false,
        FIXED_CLOCK.instant().atOffset(ZoneOffset.UTC));
  }
}
