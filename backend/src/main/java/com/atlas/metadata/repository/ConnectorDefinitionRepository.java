package com.atlas.metadata.repository;

import com.atlas.metadata.domain.ConnectorDefinition;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for connector definition metadata. */
public interface ConnectorDefinitionRepository extends JpaRepository<ConnectorDefinition, String> {

  /** Finds a connector definition by stable connector key. */
  Optional<ConnectorDefinition> findByConnectorKey(String connectorKey);
}
