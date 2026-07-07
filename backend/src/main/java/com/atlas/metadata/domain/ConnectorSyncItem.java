package com.atlas.metadata.domain;

import com.atlas.metadata.enums.ConnectorItemStatus;
import com.atlas.metadata.enums.ConnectorSafeErrorCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Per-source item evidence produced by connector sync. */
@Entity
@Table(name = "connector_sync_item", schema = "atlas")
public class ConnectorSyncItem {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "run_id", nullable = false, columnDefinition = "text")
  private String runId;

  @Column(name = "external_id", nullable = false, columnDefinition = "text")
  private String externalId;

  @Column(nullable = false, columnDefinition = "text")
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(name = "item_status", nullable = false, columnDefinition = "text")
  private ConnectorItemStatus itemStatus;

  @Column(name = "source_reference", nullable = false, columnDefinition = "text")
  private String sourceReference;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "source_trace", nullable = false, columnDefinition = "jsonb")
  private Map<String, String> sourceTrace;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(nullable = false, columnDefinition = "jsonb")
  private Map<String, String> provenance;

  @Column(precision = 4, scale = 3)
  private BigDecimal confidence;

  @Column(name = "review_eligible", nullable = false)
  private boolean reviewEligible;

  @Enumerated(EnumType.STRING)
  @Column(name = "safe_error_category", nullable = false, columnDefinition = "text")
  private ConnectorSafeErrorCategory safeErrorCategory;

  @Column(name = "safe_error_message", columnDefinition = "text")
  private String safeErrorMessage;

  @Column(name = "discovered_at", nullable = false)
  private OffsetDateTime discoveredAt;

  protected ConnectorSyncItem() {}

  /** Creates a connector sync item evidence row. */
  public static ConnectorSyncItem create(
      String id,
      String runId,
      String externalId,
      String title,
      ConnectorItemStatus itemStatus,
      String sourceReference,
      Map<String, String> sourceTrace,
      Map<String, String> provenance,
      BigDecimal confidence,
      boolean reviewEligible,
      ConnectorSafeErrorCategory safeErrorCategory,
      String safeErrorMessage,
      OffsetDateTime discoveredAt) {
    ConnectorSyncItem item = new ConnectorSyncItem();
    item.id = id;
    item.runId = runId;
    item.externalId = externalId;
    item.title = title;
    item.itemStatus = itemStatus;
    item.sourceReference = sourceReference;
    item.sourceTrace = Map.copyOf(sourceTrace);
    item.provenance = Map.copyOf(provenance);
    item.confidence = confidence;
    item.reviewEligible = reviewEligible;
    item.safeErrorCategory = safeErrorCategory;
    item.safeErrorMessage = safeErrorMessage;
    item.discoveredAt = discoveredAt;
    return item;
  }

  public String getId() {
    return id;
  }

  public String getRunId() {
    return runId;
  }

  public String getExternalId() {
    return externalId;
  }

  public String getTitle() {
    return title;
  }

  public ConnectorItemStatus getItemStatus() {
    return itemStatus;
  }

  public String getSourceReference() {
    return sourceReference;
  }

  public Map<String, String> getSourceTrace() {
    return sourceTrace;
  }

  public Map<String, String> getProvenance() {
    return provenance;
  }

  public BigDecimal getConfidence() {
    return confidence;
  }

  public boolean isReviewEligible() {
    return reviewEligible;
  }

  public ConnectorSafeErrorCategory getSafeErrorCategory() {
    return safeErrorCategory;
  }

  public String getSafeErrorMessage() {
    return safeErrorMessage;
  }

  public OffsetDateTime getDiscoveredAt() {
    return discoveredAt;
  }
}
