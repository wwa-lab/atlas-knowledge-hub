package com.atlas.metadata.repository;

import com.atlas.metadata.domain.GraphProjectionRun;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for graph projection run evidence. */
public interface GraphProjectionRunRepository extends JpaRepository<GraphProjectionRun, String> {}
