package com.atlas.metadata.repository;

import com.atlas.metadata.domain.WorkerJob;
import com.atlas.metadata.enums.WorkerJobStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for local worker job reliability records. */
public interface WorkerJobRepository extends JpaRepository<WorkerJob, String> {

  List<WorkerJob> findByStatusInOrderByUpdatedAtDesc(Collection<WorkerJobStatus> statuses);
}
