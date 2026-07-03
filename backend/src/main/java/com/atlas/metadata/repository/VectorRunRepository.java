package com.atlas.metadata.repository;

import com.atlas.metadata.domain.VectorRun;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for vector run evidence. */
public interface VectorRunRepository extends JpaRepository<VectorRun, String> {

  /** Finds vector runs for a space. */
  List<VectorRun> findBySpaceId(String spaceId);
}
