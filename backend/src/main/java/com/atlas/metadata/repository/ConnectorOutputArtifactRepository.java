package com.atlas.metadata.repository;

import com.atlas.metadata.domain.ConnectorOutputArtifact;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for connector output artifacts. */
public interface ConnectorOutputArtifactRepository
    extends JpaRepository<ConnectorOutputArtifact, String> {

  /** Lists artifacts for connector sync items. */
  List<ConnectorOutputArtifact> findByItemIdIn(Collection<String> itemIds);
}
