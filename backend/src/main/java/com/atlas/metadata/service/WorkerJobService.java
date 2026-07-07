package com.atlas.metadata.service;

import com.atlas.metadata.domain.DeadLetterEntry;
import com.atlas.metadata.domain.WorkerJob;
import com.atlas.metadata.domain.WorkerJobAttempt;
import com.atlas.metadata.dto.DeadLetterEntryResponse;
import com.atlas.metadata.dto.RecordWorkerFailureCommand;
import com.atlas.metadata.dto.WorkerJobResponse;
import com.atlas.metadata.dto.mapping.WorkerRecoveryMapper;
import com.atlas.metadata.enums.DeadLetterStatus;
import com.atlas.metadata.enums.WorkerJobStatus;
import com.atlas.metadata.enums.WorkerSafeErrorCategory;
import com.atlas.metadata.exception.ConflictException;
import com.atlas.metadata.exception.NotFoundException;
import com.atlas.metadata.exception.SafeErrorSanitizer;
import com.atlas.metadata.repository.DeadLetterEntryRepository;
import com.atlas.metadata.repository.WorkerJobAttemptRepository;
import com.atlas.metadata.repository.WorkerJobRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Coordinates deterministic local worker retry and dead-letter transitions. */
@Service
public class WorkerJobService {

  private final WorkerJobRepository jobRepository;
  private final WorkerJobAttemptRepository attemptRepository;
  private final DeadLetterEntryRepository deadLetterRepository;
  private final LocalRetryPolicy retryPolicy;
  private final SafeErrorSanitizer safeErrorSanitizer;
  private final Clock clock;

  /** Creates the service. */
  @Autowired
  public WorkerJobService(
      WorkerJobRepository jobRepository,
      WorkerJobAttemptRepository attemptRepository,
      DeadLetterEntryRepository deadLetterRepository,
      LocalRetryPolicy retryPolicy,
      SafeErrorSanitizer safeErrorSanitizer) {
    this(
        jobRepository,
        attemptRepository,
        deadLetterRepository,
        retryPolicy,
        safeErrorSanitizer,
        Clock.systemUTC());
  }

  public WorkerJobService(
      WorkerJobRepository jobRepository,
      WorkerJobAttemptRepository attemptRepository,
      DeadLetterEntryRepository deadLetterRepository,
      LocalRetryPolicy retryPolicy,
      SafeErrorSanitizer safeErrorSanitizer,
      Clock clock) {
    this.jobRepository = jobRepository;
    this.attemptRepository = attemptRepository;
    this.deadLetterRepository = deadLetterRepository;
    this.retryPolicy = retryPolicy;
    this.safeErrorSanitizer = safeErrorSanitizer;
    this.clock = clock;
  }

  /** Lists failed or retry-waiting worker jobs. */
  @Transactional(readOnly = true)
  public List<WorkerJobResponse> listFailedJobs() {
    List<WorkerJob> jobs =
        jobRepository.findByStatusInOrderByUpdatedAtDesc(
            List.of(WorkerJobStatus.DEAD_LETTERED, WorkerJobStatus.WAITING_RETRY, WorkerJobStatus.ACKNOWLEDGED));
    Map<String, List<WorkerJobAttempt>> attempts = attemptsByJobId(jobs);
    return jobs.stream()
        .map(job -> WorkerRecoveryMapper.toJobResponse(job, attempts.getOrDefault(job.getId(), List.of())))
        .toList();
  }

  /** Gets one worker job detail. */
  @Transactional(readOnly = true)
  public WorkerJobResponse getJob(String jobId) {
    WorkerJob job = findJob(jobId);
    return WorkerRecoveryMapper.toJobResponse(job, attempts(job.getId()));
  }

  /** Lists operator-facing dead-letter entries. */
  @Transactional(readOnly = true)
  public List<DeadLetterEntryResponse> listDeadLetters() {
    List<DeadLetterEntry> entries =
        deadLetterRepository.findByStatusInOrderByCreatedAtDesc(
            List.of(DeadLetterStatus.OPEN, DeadLetterStatus.RETRIED, DeadLetterStatus.ACKNOWLEDGED));
    return entries.stream().map(this::toDeadLetterResponse).toList();
  }

  /** Gets one dead-letter detail. */
  @Transactional(readOnly = true)
  public DeadLetterEntryResponse getDeadLetter(String entryId) {
    return toDeadLetterResponse(findDeadLetter(entryId));
  }

  /** Records a worker failure and returns a dead-letter entry only for terminal failures. */
  @Transactional
  public DeadLetterEntry recordFailure(String jobId, RecordWorkerFailureCommand command) {
    WorkerJob job = findJob(jobId);
    OffsetDateTime now = OffsetDateTime.now(clock);
    int nextAttemptNumber = job.getAttemptCount() + 1;
    String safeCode = safeText(command.safeErrorCode());
    WorkerSafeErrorCategory category =
        command.safeErrorCategory() == null ? WorkerSafeErrorCategory.SAFE_SYSTEM : command.safeErrorCategory();
    String safeMessage = safeText(command.safeErrorMessage());
    Map<String, String> requestedSourceTrace = command.sourceTrace();
    Map<String, String> sourceTrace =
        sanitizeMap(
            requestedSourceTrace == null || requestedSourceTrace.isEmpty()
                ? job.getSourceTrace()
                : requestedSourceTrace);
    boolean terminal = !command.retryable() || !retryPolicy.hasRetryRemaining(nextAttemptNumber);
    WorkerJobAttempt attempt =
        WorkerJobAttempt.failed(
            id("worker-attempt"),
            job,
            nextAttemptNumber,
            command.retryable(),
            safeCode,
            category,
            safeMessage,
            sourceTrace,
            now,
            terminal);
    attemptRepository.save(attempt);

    if (!terminal) {
      int delaySeconds = retryPolicy.delaySeconds(nextAttemptNumber);
      job.markWaitingRetry(
          nextAttemptNumber,
          delaySeconds,
          now.plusSeconds(delaySeconds),
          safeCode,
          category,
          safeMessage);
      jobRepository.save(job);
      return null;
    }

    job.markDeadLettered(nextAttemptNumber, safeCode, category, safeMessage, now);
    jobRepository.save(job);
    return deadLetterRepository
        .findByWorkerJobId(job.getId())
        .orElseGet(
            () ->
                deadLetterRepository.save(
                    DeadLetterEntry.createOpen(
                        id("dead-letter"),
                        job,
                        nextAttemptNumber + "/" + job.getMaxAttempts() + " attempts failed",
                        safeCode,
                        category,
                        safeMessage,
                        now)));
  }

  /** Marks an open dead-letter entry as locally retried. */
  @Transactional
  public DeadLetterEntryResponse retryDeadLetter(String entryId, String operator) {
    DeadLetterEntry entry = findDeadLetter(entryId);
    if (entry.getStatus() == DeadLetterStatus.ACKNOWLEDGED) {
      throw new ConflictException("Dead-letter entry is already acknowledged.");
    }
    WorkerJob job = findJob(entry.getWorkerJobId());
    OffsetDateTime now = OffsetDateTime.now(clock);
    int delaySeconds = retryPolicy.delaySeconds(Math.max(1, job.getAttemptCount()));
    job.markWaitingRetry(
        job.getAttemptCount(),
        delaySeconds,
        now.plusSeconds(delaySeconds),
        job.getSafeErrorCode(),
        job.getSafeErrorCategory(),
        job.getSafeErrorMessage());
    entry.markRetried(safeOperator(operator), now);
    jobRepository.save(job);
    deadLetterRepository.save(entry);
    return toDeadLetterResponse(entry);
  }

  /** Acknowledges an open or already acknowledged dead-letter entry. */
  @Transactional
  public DeadLetterEntryResponse acknowledge(String entryId, String operator) {
    DeadLetterEntry entry = findDeadLetter(entryId);
    WorkerJob job = findJob(entry.getWorkerJobId());
    OffsetDateTime now = OffsetDateTime.now(clock);
    entry.acknowledge(safeOperator(operator), now);
    job.acknowledge(now);
    jobRepository.save(job);
    deadLetterRepository.save(entry);
    return toDeadLetterResponse(entry);
  }

  private DeadLetterEntryResponse toDeadLetterResponse(DeadLetterEntry entry) {
    WorkerJob job = findJob(entry.getWorkerJobId());
    return WorkerRecoveryMapper.toDeadLetterResponse(entry, job, attempts(job.getId()));
  }

  private WorkerJob findJob(String jobId) {
    return jobRepository.findById(jobId).orElseThrow(() -> new NotFoundException("Worker job not found."));
  }

  private DeadLetterEntry findDeadLetter(String entryId) {
    return deadLetterRepository
        .findById(entryId)
        .orElseThrow(() -> new NotFoundException("Dead-letter entry not found."));
  }

  private List<WorkerJobAttempt> attempts(String jobId) {
    return attemptRepository.findByWorkerJobIdOrderByAttemptNumberAsc(jobId);
  }

  private Map<String, List<WorkerJobAttempt>> attemptsByJobId(List<WorkerJob> jobs) {
    return jobs.stream()
        .collect(Collectors.toUnmodifiableMap(WorkerJob::getId, job -> attempts(job.getId())));
  }

  private String id(String prefix) {
    return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
  }

  private String safeText(String value) {
    return safeErrorSanitizer.sanitize(value == null || value.isBlank() ? "Safe system failure." : value);
  }

  private String safeOperator(String operator) {
    return safeErrorSanitizer.sanitize(operator == null || operator.isBlank() ? "local-operator" : operator);
  }

  private Map<String, String> sanitizeMap(Map<String, String> values) {
    if (values == null || values.isEmpty()) {
      return Map.of();
    }
    return values.entrySet().stream()
        .collect(Collectors.toUnmodifiableMap(entry -> safeText(entry.getKey()), entry -> safeText(entry.getValue())));
  }
}
