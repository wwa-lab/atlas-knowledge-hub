package com.atlas.metadata.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Session-level container for a Trusted Ask conversation in one Knowledge Space. */
@Entity
@Table(name = "ask_session", schema = "atlas")
public class AskSession {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "space_id", nullable = false, columnDefinition = "text")
  private String spaceId;

  @Column(nullable = false, columnDefinition = "text")
  private String title;

  @Column(name = "created_by", nullable = false, columnDefinition = "text")
  private String createdBy;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  protected AskSession() {}

  /** Creates a session snapshot for Trusted Ask history. */
  public static AskSession create(
      String id, String spaceId, String title, String createdBy, OffsetDateTime createdAt) {
    AskSession session = new AskSession();
    session.id = id;
    session.spaceId = spaceId;
    session.title = title;
    session.createdBy = createdBy;
    session.createdAt = createdAt;
    session.updatedAt = createdAt;
    return session;
  }

  public void touch(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getId() {
    return id;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public String getTitle() {
    return title;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
}
