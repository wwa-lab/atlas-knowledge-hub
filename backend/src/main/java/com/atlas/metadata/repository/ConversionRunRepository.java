package com.atlas.metadata.repository;

import com.atlas.metadata.domain.ConversionRun;
import com.atlas.metadata.enums.ConversionRunStatus;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for conversion run evidence. */
public interface ConversionRunRepository extends JpaRepository<ConversionRun, String> {

  /** Returns true if a batch already has a non-terminal conversion run. */
  boolean existsByBatchIdAndStatusIn(String batchId, Collection<ConversionRunStatus> statuses);
}
