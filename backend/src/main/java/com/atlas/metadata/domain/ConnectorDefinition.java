package com.atlas.metadata.domain;

import com.atlas.metadata.enums.ConnectorConfigurationState;
import com.atlas.metadata.enums.ConnectorDefinitionStatus;
import com.atlas.metadata.enums.ConnectorReviewPolicy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Connector definition metadata with safe configuration state. */
@Entity
@Table(name = "connector_definition", schema = "atlas")
public class ConnectorDefinition {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "connector_key", nullable = false, columnDefinition = "text")
  private String connectorKey;

  @Column(nullable = false, columnDefinition = "text")
  private String name;

  @Column(name = "connector_type", nullable = false, columnDefinition = "text")
  private String connectorType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private ConnectorDefinitionStatus status;

  @Column(nullable = false, columnDefinition = "text")
  private String version;

  @Column(name = "capability_summary", nullable = false, columnDefinition = "text")
  private String capabilitySummary;

  @Enumerated(EnumType.STRING)
  @Column(name = "configuration_state", nullable = false, columnDefinition = "text")
  private ConnectorConfigurationState configurationState;

  @Enumerated(EnumType.STRING)
  @Column(name = "review_policy", nullable = false, columnDefinition = "text")
  private ConnectorReviewPolicy reviewPolicy;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  protected ConnectorDefinition() {}

  public String getId() {
    return id;
  }

  public String getConnectorKey() {
    return connectorKey;
  }

  public String getName() {
    return name;
  }

  public String getConnectorType() {
    return connectorType;
  }

  public ConnectorDefinitionStatus getStatus() {
    return status;
  }

  public String getVersion() {
    return version;
  }

  public String getCapabilitySummary() {
    return capabilitySummary;
  }

  public ConnectorConfigurationState getConfigurationState() {
    return configurationState;
  }

  public ConnectorReviewPolicy getReviewPolicy() {
    return reviewPolicy;
  }
}
