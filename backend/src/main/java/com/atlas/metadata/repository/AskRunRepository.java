package com.atlas.metadata.repository;

import com.atlas.metadata.domain.AskRun;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for trusted ask runs. */
public interface AskRunRepository extends JpaRepository<AskRun, String> {

  /** Finds ask runs belonging to one Knowledge Space. */
  List<AskRun> findBySpaceId(String spaceId);

  /** Finds ask runs for one Trusted Ask session in conversation order. */
  List<AskRun> findBySessionIdOrderByCreatedAtAsc(String sessionId);

  /** Counts ask runs for one Trusted Ask session. */
  long countBySessionId(String sessionId);
}
