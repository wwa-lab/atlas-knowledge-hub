package com.atlas.metadata.repository;

import com.atlas.metadata.domain.ConnectorSyncJob;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for connector sync jobs. */
public interface ConnectorSyncJobRepository extends JpaRepository<ConnectorSyncJob, String> {}
