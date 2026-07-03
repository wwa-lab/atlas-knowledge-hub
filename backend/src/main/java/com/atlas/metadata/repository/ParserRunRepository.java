package com.atlas.metadata.repository;

import com.atlas.metadata.domain.ParserRun;
import com.atlas.metadata.enums.ParserRunStatus;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for parser run evidence. */
public interface ParserRunRepository extends JpaRepository<ParserRun, String> {

  /** Returns true if a batch already has a non-terminal parser run. */
  boolean existsByBatchIdAndStatusIn(String batchId, Collection<ParserRunStatus> statuses);
}
