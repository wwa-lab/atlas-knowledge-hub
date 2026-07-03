package com.atlas.metadata.repository;

import com.atlas.metadata.domain.ConversionFileResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for per-file conversion results. */
public interface ConversionFileResultRepository extends JpaRepository<ConversionFileResult, String> {

  /** Finds results for a conversion run in creation order. */
  List<ConversionFileResult> findByRunIdOrderByCreatedAtAsc(String runId);
}
