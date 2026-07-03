package com.atlas.metadata.repository;

import com.atlas.metadata.domain.Batch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for batch metadata. */
public interface BatchRepository extends JpaRepository<Batch, String> {

  /** Finds batches belonging to a Knowledge Space. */
  Page<Batch> findBySpaceId(String spaceId, Pageable pageable);
}
