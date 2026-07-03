package com.atlas.metadata.repository;

import com.atlas.metadata.domain.VectorItemResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for per-item vector run results. */
public interface VectorItemResultRepository extends JpaRepository<VectorItemResult, String> {

  /** Finds vector item results for a run in creation order. */
  List<VectorItemResult> findByRunIdOrderByCreatedAtAsc(String runId);

  /** Finds latest indexed evidence by source chunk ids. */
  List<VectorItemResult> findBySourceChunkIdIn(List<String> sourceChunkIds);
}
