package com.atlas.metadata.repository;

import com.atlas.metadata.domain.ReviewRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for append-only review records. */
public interface ReviewRecordRepository extends JpaRepository<ReviewRecord, Long> {

  /** Finds review history in chronological order. */
  List<ReviewRecord> findByTargetTypeAndTargetIdOrderByCreatedAtAsc(
      String targetType, String targetId);
}
