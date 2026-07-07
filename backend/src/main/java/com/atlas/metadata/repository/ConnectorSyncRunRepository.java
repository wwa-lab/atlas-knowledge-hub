package com.atlas.metadata.repository;

import com.atlas.metadata.domain.ConnectorSyncRun;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for connector sync runs. */
public interface ConnectorSyncRunRepository extends JpaRepository<ConnectorSyncRun, String> {}
