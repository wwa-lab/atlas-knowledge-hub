package com.atlas.metadata.domain;

import com.atlas.metadata.enums.VectorReviewPolicy;
import com.atlas.metadata.enums.VectorRunOperation;
import com.atlas.metadata.enums.VectorRunStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Vector run evidence for index and deindex operations. */
@Entity
@Table(name = "vector_run", schema = "atlas")
public class VectorRun {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "space_id", nullable = false, columnDefinition = "text")
  private String spaceId;

  @Column(name = "batch_id", columnDefinition = "text")
  private String batchId;

  @Column(name = "adapter_key", nullable = false, columnDefinition = "text")
  private String adapterKey;

  @Column(name = "adapter_version", columnDefinition = "text")
  private String adapterVersion;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private VectorRunOperation operation;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private VectorRunStatus status;

  @Column(nullable = false, columnDefinition = "text")
  private String mode;

  @Enumerated(EnumType.STRING)
  @Column(name = "review_policy", nullable = false, columnDefinition = "text")
  private VectorReviewPolicy reviewPolicy;

  @Column(name = "dimension")
  private Integer dimension;

  @Column(name = "requested_by", columnDefinition = "text")
  private String requestedBy;

  @Column(name = "total_count", nullable = false)
  private int totalCount;

  @Column(name = "indexed_count", nullable = false)
  private int indexedCount;

  @Column(name = "deleted_count", nullable = false)
  private int deletedCount;

  @Column(name = "skipped_count", nullable = false)
  private int skippedCount;

  @Column(name = "failed_count", nullable = false)
  private int failedCount;

  @Column(name = "started_at", nullable = false)
  private OffsetDateTime startedAt;

  @Column(name = "completed_at")
  private OffsetDateTime completedAt;

  @Column(name = "safe_message", columnDefinition = "text")
  private String safeMessage;

  protected VectorRun() {}

  /** Creates a requested vector run. */
  public static VectorRun create(
      String id,
      String spaceId,
      String batchId,
      String adapterKey,
      String adapterVersion,
      VectorRunOperation operation,
      String mode,
      VectorReviewPolicy reviewPolicy,
      Integer dimension,
      String requestedBy,
      OffsetDateTime startedAt) {
    VectorRun run = new VectorRun();
    run.id = id;
    run.spaceId = spaceId;
    run.batchId = batchId;
    run.adapterKey = adapterKey;
    run.adapterVersion = adapterVersion;
    run.operation = operation;
    run.status = VectorRunStatus.REQUESTED;
    run.mode = mode;
    run.reviewPolicy = reviewPolicy;
    run.dimension = dimension;
    run.requestedBy = requestedBy;
    run.startedAt = startedAt;
    return run;
  }

  /** Marks the run as executing. */
  public void markRunning() {
    status = VectorRunStatus.RUNNING;
  }

  /** Marks the run as terminal with derived counts. */
  public void complete(
      VectorRunStatus terminalStatus,
      int totalCount,
      int indexedCount,
      int deletedCount,
      int skippedCount,
      int failedCount,
      OffsetDateTime completedAt,
      String safeMessage) {
    if (terminalStatus == VectorRunStatus.REQUESTED || terminalStatus == VectorRunStatus.RUNNING) {
      throw new IllegalArgumentException("Vector run must complete with a terminal status.");
    }
    this.status = terminalStatus;
    this.totalCount = totalCount;
    this.indexedCount = indexedCount;
    this.deletedCount = deletedCount;
    this.skippedCount = skippedCount;
    this.failedCount = failedCount;
    this.completedAt = completedAt;
    this.safeMessage = safeMessage;
  }

  public String getId() {
    return id;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public String getBatchId() {
    return batchId;
  }

  public String getAdapterKey() {
    return adapterKey;
  }

  public String getAdapterVersion() {
    return adapterVersion;
  }

  public VectorRunOperation getOperation() {
    return operation;
  }

  public VectorRunStatus getStatus() {
    return status;
  }

  public String getMode() {
    return mode;
  }

  public VectorReviewPolicy getReviewPolicy() {
    return reviewPolicy;
  }

  public Integer getDimension() {
    return dimension;
  }

  public String getRequestedBy() {
    return requestedBy;
  }

  public int getTotalCount() {
    return totalCount;
  }

  public int getIndexedCount() {
    return indexedCount;
  }

  public int getDeletedCount() {
    return deletedCount;
  }

  public int getSkippedCount() {
    return skippedCount;
  }

  public int getFailedCount() {
    return failedCount;
  }

  public OffsetDateTime getStartedAt() {
    return startedAt;
  }

  public OffsetDateTime getCompletedAt() {
    return completedAt;
  }

  public String getSafeMessage() {
    return safeMessage;
  }
}
