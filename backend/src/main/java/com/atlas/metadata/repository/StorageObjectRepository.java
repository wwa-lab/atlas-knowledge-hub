package com.atlas.metadata.repository;

import com.atlas.metadata.domain.StorageObject;
import com.atlas.metadata.enums.StorageLayer;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for stored-object descriptors. */
public interface StorageObjectRepository extends JpaRepository<StorageObject, String> {

  /** Finds descriptors for one storage operation in creation order. */
  List<StorageObject> findByOperationIdOrderByCreatedAtAsc(String operationId);

  /** Finds paged descriptors for a batch. */
  Page<StorageObject> findByBatchId(String batchId, Pageable pageable);

  /** Finds paged descriptors for a batch and layer. */
  Page<StorageObject> findByBatchIdAndLayer(String batchId, StorageLayer layer, Pageable pageable);
}
