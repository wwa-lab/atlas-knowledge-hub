package com.atlas.metadata.repository;

import com.atlas.metadata.domain.ParserFileResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for per-file parser results. */
public interface ParserFileResultRepository extends JpaRepository<ParserFileResult, String> {

  /** Finds results for a parser run in creation order. */
  List<ParserFileResult> findByRunIdOrderByCreatedAtAsc(String runId);
}
