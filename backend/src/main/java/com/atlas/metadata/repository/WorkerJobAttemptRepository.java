package com.atlas.metadata.repository;

import com.atlas.metadata.domain.WorkerJobAttempt;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for local worker job attempts. */
public interface WorkerJobAttemptRepository extends JpaRepository<WorkerJobAttempt, String> {

  List<WorkerJobAttempt> findByWorkerJobIdOrderByAttemptNumberAsc(String workerJobId);
}
