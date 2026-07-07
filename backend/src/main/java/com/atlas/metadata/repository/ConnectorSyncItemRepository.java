package com.atlas.metadata.repository;

import com.atlas.metadata.domain.ConnectorSyncItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for connector sync items. */
public interface ConnectorSyncItemRepository extends JpaRepository<ConnectorSyncItem, String> {

  /** Lists items for a run in stable id order. */
  List<ConnectorSyncItem> findByRunIdOrderByIdAsc(String runId);
}
