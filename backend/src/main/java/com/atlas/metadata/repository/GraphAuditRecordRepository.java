package com.atlas.metadata.repository;

import com.atlas.metadata.domain.GraphAuditRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for append-only graph audit records. */
public interface GraphAuditRecordRepository extends JpaRepository<GraphAuditRecord, String> {

  List<GraphAuditRecord> findByTargetTypeAndTargetIdOrderByCreatedAtAsc(String targetType, String targetId);
}
