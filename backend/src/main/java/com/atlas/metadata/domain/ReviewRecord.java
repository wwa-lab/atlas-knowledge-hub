package com.atlas.metadata.domain;

import com.atlas.metadata.enums.ReviewAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Append-only review event for file or chunk targets. */
@Entity
@Table(name = "review_record", schema = "atlas")
public class ReviewRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "target_type", nullable = false, columnDefinition = "text")
  private String targetType;

  @Column(name = "target_id", nullable = false, columnDefinition = "text")
  private String targetId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private ReviewAction action;

  @Column(nullable = false, columnDefinition = "text")
  private String reviewer;

  @Column(columnDefinition = "text")
  private String comment;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "affected_chunks", columnDefinition = "text[]")
  private String[] affectedChunks;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected ReviewRecord() {}

  /** Creates an append-only review record. */
  public static ReviewRecord create(
      String targetType,
      String targetId,
      ReviewAction action,
      String reviewer,
      String comment,
      String[] affectedChunks,
      OffsetDateTime createdAt) {
    ReviewRecord record = new ReviewRecord();
    record.targetType = targetType;
    record.targetId = targetId;
    record.action = action;
    record.reviewer = reviewer;
    record.comment = comment;
    record.affectedChunks = affectedChunks;
    record.createdAt = createdAt;
    return record;
  }

  public Long getId() {
    return id;
  }

  public String getTargetType() {
    return targetType;
  }

  public String getTargetId() {
    return targetId;
  }

  public ReviewAction getAction() {
    return action;
  }

  public String getReviewer() {
    return reviewer;
  }

  public String getComment() {
    return comment;
  }

  public String[] getAffectedChunks() {
    return affectedChunks;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
