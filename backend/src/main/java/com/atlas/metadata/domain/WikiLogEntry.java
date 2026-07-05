package com.atlas.metadata.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Safe lifecycle metadata for Wiki pages and generation runs. */
@Entity
@Table(name = "wiki_log_entry", schema = "atlas")
public class WikiLogEntry {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "space_id", nullable = false, columnDefinition = "text")
  private String spaceId;

  @Column(name = "page_id", columnDefinition = "text")
  private String pageId;

  @Column(name = "run_id", columnDefinition = "text")
  private String runId;

  @Column(name = "event_type", nullable = false, columnDefinition = "text")
  private String eventType;

  @Column(columnDefinition = "text")
  private String actor;

  @Column(columnDefinition = "text")
  private String message;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> metadata;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected WikiLogEntry() {}

  /** Creates a safe Wiki lifecycle log entry. */
  public static WikiLogEntry create(
      String id,
      String spaceId,
      String pageId,
      String runId,
      String eventType,
      String actor,
      String message,
      Map<String, Object> metadata,
      OffsetDateTime createdAt) {
    WikiLogEntry entry = new WikiLogEntry();
    entry.id = id;
    entry.spaceId = spaceId;
    entry.pageId = pageId;
    entry.runId = runId;
    entry.eventType = eventType;
    entry.actor = actor;
    entry.message = message;
    entry.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    entry.createdAt = createdAt;
    return entry;
  }

  public String getId() {
    return id;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public String getPageId() {
    return pageId;
  }

  public String getRunId() {
    return runId;
  }

  public String getEventType() {
    return eventType;
  }

  public String getActor() {
    return actor;
  }

  public String getMessage() {
    return message;
  }

  public Map<String, Object> getMetadata() {
    return metadata == null ? Map.of() : Map.copyOf(metadata);
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
