package com.atlas.metadata.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Safe page issue metadata reserved for future Wiki lint and freshness workflows. */
@Entity
@Table(name = "wiki_page_issue", schema = "atlas")
public class WikiPageIssue {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "space_id", nullable = false, columnDefinition = "text")
  private String spaceId;

  @Column(name = "page_id", nullable = false, columnDefinition = "text")
  private String pageId;

  @Column(name = "issue_type", nullable = false, columnDefinition = "text")
  private String issueType;

  @Column(nullable = false, columnDefinition = "text")
  private String severity;

  @Column(nullable = false, columnDefinition = "text")
  private String status;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "evidence_refs", nullable = false, columnDefinition = "jsonb")
  private List<WikiReference> evidenceRefs;

  @Column(columnDefinition = "text")
  private String message;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "resolved_at")
  private OffsetDateTime resolvedAt;

  protected WikiPageIssue() {}

  /** Creates an open safe Wiki issue. */
  public static WikiPageIssue open(
      String id,
      String spaceId,
      String pageId,
      String issueType,
      String severity,
      List<WikiReference> evidenceRefs,
      String message,
      OffsetDateTime createdAt) {
    WikiPageIssue issue = new WikiPageIssue();
    issue.id = id;
    issue.spaceId = spaceId;
    issue.pageId = pageId;
    issue.issueType = issueType;
    issue.severity = severity;
    issue.status = "OPEN";
    issue.evidenceRefs = evidenceRefs == null ? List.of() : List.copyOf(evidenceRefs);
    issue.message = message;
    issue.createdAt = createdAt;
    issue.resolvedAt = null;
    return issue;
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

  public String getIssueType() {
    return issueType;
  }

  public String getSeverity() {
    return severity;
  }

  public String getStatus() {
    return status;
  }

  public List<WikiReference> getEvidenceRefs() {
    return evidenceRefs == null ? List.of() : List.copyOf(evidenceRefs);
  }

  public String getMessage() {
    return message;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getResolvedAt() {
    return resolvedAt;
  }
}
