package com.atlas.metadata.domain;

import com.atlas.metadata.enums.SourceKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Batch metadata entity. */
@Entity
@Table(name = "batch", schema = "atlas")
public class Batch {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "space_id", nullable = false, columnDefinition = "text")
  private String spaceId;

  @Column(nullable = false, columnDefinition = "text")
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_kind", nullable = false, columnDefinition = "text")
  private SourceKind sourceKind;

  @Column(columnDefinition = "text")
  private String owner;

  @Column(name = "uploaded_at", nullable = false)
  private OffsetDateTime uploadedAt;

  protected Batch() {}

  /** Creates a batch from pre-computed inventory metadata. */
  public static Batch create(
      String id,
      String spaceId,
      String name,
      SourceKind sourceKind,
      String owner,
      OffsetDateTime uploadedAt) {
    Batch batch = new Batch();
    batch.id = id;
    batch.spaceId = spaceId;
    batch.name = name;
    batch.sourceKind = sourceKind;
    batch.owner = owner;
    batch.uploadedAt = uploadedAt;
    return batch;
  }

  public String getId() {
    return id;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public String getName() {
    return name;
  }

  public SourceKind getSourceKind() {
    return sourceKind;
  }

  public String getOwner() {
    return owner;
  }

  public OffsetDateTime getUploadedAt() {
    return uploadedAt;
  }
}
