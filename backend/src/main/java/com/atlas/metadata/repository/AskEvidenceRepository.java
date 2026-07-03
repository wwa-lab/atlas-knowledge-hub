package com.atlas.metadata.repository;

import com.atlas.metadata.domain.AskEvidence;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for evidence snapshots attached to ask runs. */
public interface AskEvidenceRepository extends JpaRepository<AskEvidence, String> {

  /** Finds evidence snapshots for one ask run in insertion order. */
  List<AskEvidence> findByAskRunIdOrderByCreatedAtAsc(String askRunId);
}
