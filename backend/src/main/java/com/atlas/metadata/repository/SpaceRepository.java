package com.atlas.metadata.repository;

import com.atlas.metadata.domain.Space;
import com.atlas.metadata.enums.SpaceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for Knowledge Space metadata. */
public interface SpaceRepository extends JpaRepository<Space, String> {

  /** Finds spaces by status for card filtering. */
  Page<Space> findByStatus(SpaceStatus status, Pageable pageable);
}
