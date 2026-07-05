package com.atlas.metadata.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Minimal read model for Wiki folder metadata. */
@Entity
@Table(name = "wiki_folder", schema = "atlas")
public class WikiFolder {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "space_id", nullable = false, columnDefinition = "text")
  private String spaceId;

  @Column(name = "parent_folder_id", columnDefinition = "text")
  private String parentFolderId;

  @Column(nullable = false, columnDefinition = "text")
  private String slug;

  @Column(nullable = false, columnDefinition = "text")
  private String name;

  @Column(columnDefinition = "text")
  private String description;

  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  protected WikiFolder() {}

  public String getId() {
    return id;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public String getParentFolderId() {
    return parentFolderId;
  }

  public String getSlug() {
    return slug;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Integer getSortOrder() {
    return sortOrder == null ? 0 : sortOrder;
  }
}
