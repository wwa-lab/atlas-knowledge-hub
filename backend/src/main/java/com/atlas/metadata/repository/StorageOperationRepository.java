package com.atlas.metadata.repository;

import com.atlas.metadata.domain.StorageOperation;
import com.atlas.metadata.enums.StorageOperationStatus;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for storage operation evidence. */
public interface StorageOperationRepository extends JpaRepository<StorageOperation, String> {

  /** Returns true if a batch already has a non-terminal storage operation. */
  boolean existsByBatchIdAndStatusIn(String batchId, Collection<StorageOperationStatus> statuses);
}
