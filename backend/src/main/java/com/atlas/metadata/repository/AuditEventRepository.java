package com.atlas.metadata.repository;

import com.atlas.metadata.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Repository for append-only audit events. */
public interface AuditEventRepository
    extends JpaRepository<AuditEvent, String>, JpaSpecificationExecutor<AuditEvent> {}
