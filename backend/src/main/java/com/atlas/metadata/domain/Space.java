package com.atlas.metadata.domain;

import com.atlas.metadata.enums.IndexStrategy;
import com.atlas.metadata.enums.SpaceStatus;
import com.atlas.metadata.enums.SpaceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Knowledge Space metadata entity. */
@Entity
@Table(name = "space", schema = "atlas")
public class Space {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(nullable = false, columnDefinition = "text")
  private String name;

  @Column(columnDefinition = "text")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private SpaceType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "index_strategy", nullable = false, columnDefinition = "text")
  private IndexStrategy indexStrategy;

  @Column(columnDefinition = "text")
  private String owner;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private SpaceStatus status;

  @Column(name = "document_count", nullable = false)
  private int documentCount;

  @Column(name = "wiki_page_count", nullable = false)
  private int wikiPageCount;

  @Column(name = "review_count", nullable = false)
  private int reviewCount;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  protected Space() {}

  /** Creates a new space with server-owned defaults. */
  public static Space create(
      String id,
      String name,
      String description,
      SpaceType type,
      IndexStrategy indexStrategy,
      String owner,
      OffsetDateTime now) {
    Space space = new Space();
    space.id = id;
    space.name = name;
    space.description = description;
    space.type = type;
    space.indexStrategy = indexStrategy;
    space.owner = owner;
    space.status = SpaceStatus.HEALTHY;
    space.createdAt = now;
    space.updatedAt = now;
    return space;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public SpaceType getType() {
    return type;
  }

  public IndexStrategy getIndexStrategy() {
    return indexStrategy;
  }

  public String getOwner() {
    return owner;
  }

  public SpaceStatus getStatus() {
    return status;
  }

  public int getDocumentCount() {
    return documentCount;
  }

  public int getWikiPageCount() {
    return wikiPageCount;
  }

  public int getReviewCount() {
    return reviewCount;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
}
