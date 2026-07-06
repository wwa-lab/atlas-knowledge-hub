package com.atlas.metadata.domain;

import com.atlas.metadata.enums.AuditCategory;
import com.atlas.metadata.enums.AuditResult;
import com.atlas.metadata.enums.AuditSeverity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Append-only, user-safe audit event for governance review. */
@Entity
@Table(name = "audit_event", schema = "atlas")
public class AuditEvent {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "actor_user_id", columnDefinition = "text")
  private String actorUserId;

  @Column(name = "actor_display", nullable = false, columnDefinition = "text")
  private String actorDisplay;

  @Column(nullable = false, columnDefinition = "text")
  private String action;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private AuditCategory category;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private AuditResult result;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private AuditSeverity severity;

  @Column(name = "space_id", columnDefinition = "text")
  private String spaceId;

  @Column(name = "target_type", nullable = false, columnDefinition = "text")
  private String targetType;

  @Column(name = "target_id", nullable = false, columnDefinition = "text")
  private String targetId;

  @Column(name = "request_id", columnDefinition = "text")
  private String requestId;

  @Column(name = "safe_summary", nullable = false, columnDefinition = "text")
  private String safeSummary;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> metadata;

  protected AuditEvent() {}

  /** Creates a sanitized append-only audit event. */
  public static AuditEvent create(
      String id,
      OffsetDateTime createdAt,
      String actorUserId,
      String actorDisplay,
      String action,
      AuditCategory category,
      AuditResult result,
      AuditSeverity severity,
      String spaceId,
      String targetType,
      String targetId,
      String requestId,
      String safeSummary,
      Map<String, Object> metadata) {
    AuditEvent event = new AuditEvent();
    event.id = id;
    event.createdAt = createdAt;
    event.actorUserId = actorUserId;
    event.actorDisplay = actorDisplay;
    event.action = action;
    event.category = category;
    event.result = result;
    event.severity = severity;
    event.spaceId = spaceId;
    event.targetType = targetType;
    event.targetId = targetId;
    event.requestId = requestId;
    event.safeSummary = safeSummary;
    event.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    return event;
  }

  public String getId() {
    return id;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public String getActorUserId() {
    return actorUserId;
  }

  public String getActorDisplay() {
    return actorDisplay;
  }

  public String getAction() {
    return action;
  }

  public AuditCategory getCategory() {
    return category;
  }

  public AuditResult getResult() {
    return result;
  }

  public AuditSeverity getSeverity() {
    return severity;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public String getTargetType() {
    return targetType;
  }

  public String getTargetId() {
    return targetId;
  }

  public String getRequestId() {
    return requestId;
  }

  public String getSafeSummary() {
    return safeSummary;
  }

  public Map<String, Object> getMetadata() {
    return metadata == null ? Map.of() : Map.copyOf(metadata);
  }
}
