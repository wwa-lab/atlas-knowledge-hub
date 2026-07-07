package com.atlas.metadata.repository;

import com.atlas.metadata.domain.DeadLetterEntry;
import com.atlas.metadata.enums.DeadLetterStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for local dead-letter entries. */
public interface DeadLetterEntryRepository extends JpaRepository<DeadLetterEntry, String> {

  Optional<DeadLetterEntry> findByWorkerJobId(String workerJobId);

  List<DeadLetterEntry> findByStatusInOrderByCreatedAtDesc(List<DeadLetterStatus> statuses);
}
