package com.atlas.metadata.domain;

import com.atlas.metadata.enums.StorageOperationStatus;
import com.atlas.metadata.enums.StorageOperationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Append-oriented storage operation evidence for one batch. */
@Entity
@Table(name = "storage_operation", schema = "atlas")
public class StorageOperation {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "batch_id", nullable = false, columnDefinition = "text")
  private String batchId;

  @Column(name = "workspace_id", columnDefinition = "text")
  private String workspaceId;

  @Column(name = "adapter_key", nullable = false, columnDefinition = "text")
  private String adapterKey;

  @Column(name = "adapter_version", columnDefinition = "text")
  private String adapterVersion;

  @Enumerated(EnumType.STRING)
  @Column(name = "operation_type", nullable = false, columnDefinition = "text")
  private StorageOperationType operationType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private StorageOperationStatus status;

  @Column(name = "requested_by", columnDefinition = "text")
  private String requestedBy;

  @Column(nullable = false, columnDefinition = "text")
  private String mode;

  @Column(name = "total_count", nullable = false)
  private int totalCount;

  @Column(name = "stored_count", nullable = false)
  private int storedCount;

  @Column(name = "deleted_count", nullable = false)
  private int deletedCount;

  @Column(name = "missing_count", nullable = false)
  private int missingCount;

  @Column(name = "failed_count", nullable = false)
  private int failedCount;

  @Column(name = "skipped_count", nullable = false)
  private int skippedCount;

  @Column(name = "total_bytes", nullable = false)
  private long totalBytes;

  @Column(name = "started_at", nullable = false)
  private OffsetDateTime startedAt;

  @Column(name = "completed_at")
  private OffsetDateTime completedAt;

  @Column(name = "safe_message", columnDefinition = "text")
  private String safeMessage;

  protected StorageOperation() {}

  /** Creates a requested storage operation. */
  public static StorageOperation create(
      String id,
      String batchId,
      String workspaceId,
      String adapterKey,
      String adapterVersion,
      StorageOperationType operationType,
      String requestedBy,
      String mode,
      OffsetDateTime startedAt) {
    StorageOperation operation = new StorageOperation();
    operation.id = id;
    operation.batchId = batchId;
    operation.workspaceId = workspaceId;
    operation.adapterKey = adapterKey;
    operation.adapterVersion = adapterVersion;
    operation.operationType = operationType;
    operation.status = StorageOperationStatus.REQUESTED;
    operation.requestedBy = requestedBy;
    operation.mode = mode;
    operation.startedAt = startedAt;
    return operation;
  }

  /** Marks the operation as executing. */
  public void markRunning() {
    status = StorageOperationStatus.RUNNING;
  }

  /** Completes the operation with derived summary counts and a user-safe message. */
  public void complete(
      StorageOperationStatus terminalStatus,
      StorageSummary summary,
      OffsetDateTime completedAt,
      String safeMessage) {
    if (terminalStatus == StorageOperationStatus.REQUESTED
        || terminalStatus == StorageOperationStatus.RUNNING) {
      throw new IllegalArgumentException("Storage operation must complete with a terminal status.");
    }
    this.status = terminalStatus;
    this.totalCount = summary.total();
    this.storedCount = summary.stored();
    this.deletedCount = summary.deleted();
    this.missingCount = summary.missing();
    this.failedCount = summary.failed();
    this.skippedCount = summary.skipped();
    this.totalBytes = summary.totalBytes();
    this.completedAt = completedAt;
    this.safeMessage = safeMessage;
  }

  public String getId() {
    return id;
  }

  public String getBatchId() {
    return batchId;
  }

  public String getWorkspaceId() {
    return workspaceId;
  }

  public String getAdapterKey() {
    return adapterKey;
  }

  public String getAdapterVersion() {
    return adapterVersion;
  }

  public StorageOperationType getOperationType() {
    return operationType;
  }

  public StorageOperationStatus getStatus() {
    return status;
  }

  public String getRequestedBy() {
    return requestedBy;
  }

  public String getMode() {
    return mode;
  }

  public int getTotalCount() {
    return totalCount;
  }

  public int getStoredCount() {
    return storedCount;
  }

  public int getDeletedCount() {
    return deletedCount;
  }

  public int getMissingCount() {
    return missingCount;
  }

  public int getFailedCount() {
    return failedCount;
  }

  public int getSkippedCount() {
    return skippedCount;
  }

  public long getTotalBytes() {
    return totalBytes;
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

  /** Summary counts persisted on the operation record. */
  public record StorageSummary(
      int total, int stored, int deleted, int missing, int failed, int skipped, long totalBytes) {}
}
