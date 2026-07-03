package com.atlas.metadata.domain;

import com.atlas.metadata.enums.StorageLayer;
import com.atlas.metadata.enums.StorageObjectStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Stored-object descriptor evidence without raw object bytes or storage internals. */
@Entity
@Table(name = "storage_object", schema = "atlas")
public class StorageObject {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "operation_id", nullable = false, columnDefinition = "text")
  private String operationId;

  @Column(name = "batch_id", nullable = false, columnDefinition = "text")
  private String batchId;

  @Column(name = "file_item_id", columnDefinition = "text")
  private String fileItemId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private StorageLayer layer;

  @Column(name = "object_key", nullable = false, columnDefinition = "text")
  private String objectKey;

  @Column(name = "content_type", columnDefinition = "text")
  private String contentType;

  @Column(name = "size_bytes")
  private Long sizeBytes;

  @Column(columnDefinition = "text")
  private String checksum;

  @Column(name = "adapter_key", nullable = false, columnDefinition = "text")
  private String adapterKey;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private StorageObjectStatus status;

  @Column(name = "safe_error", columnDefinition = "text")
  private String safeError;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  protected StorageObject() {}

  /** Creates a storage object descriptor row. */
  public static StorageObject create(
      String id,
      String operationId,
      String batchId,
      String fileItemId,
      StorageLayer layer,
      String objectKey,
      String contentType,
      Long sizeBytes,
      String checksum,
      String adapterKey,
      StorageObjectStatus status,
      String safeError,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt) {
    StorageObject object = new StorageObject();
    object.id = id;
    object.operationId = operationId;
    object.batchId = batchId;
    object.fileItemId = fileItemId;
    object.layer = layer;
    object.objectKey = objectKey;
    object.contentType = contentType;
    object.sizeBytes = sizeBytes;
    object.checksum = checksum;
    object.adapterKey = adapterKey;
    object.status = status;
    object.safeError = safeError;
    object.createdAt = createdAt;
    object.updatedAt = updatedAt;
    return object;
  }

  public String getId() {
    return id;
  }

  public String getOperationId() {
    return operationId;
  }

  public String getBatchId() {
    return batchId;
  }

  public String getFileItemId() {
    return fileItemId;
  }

  public StorageLayer getLayer() {
    return layer;
  }

  public String objectKey() {
    return objectKey;
  }

  public String getContentType() {
    return contentType;
  }

  public Long getSizeBytes() {
    return sizeBytes;
  }

  public String getChecksum() {
    return checksum;
  }

  public String getAdapterKey() {
    return adapterKey;
  }

  public StorageObjectStatus getStatus() {
    return status;
  }

  public String getSafeError() {
    return safeError;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
}
