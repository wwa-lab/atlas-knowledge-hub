package com.atlas.metadata.repository;

import com.atlas.metadata.domain.ModelRunSourceReference;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for model source trace references. */
public interface ModelRunSourceReferenceRepository
    extends JpaRepository<ModelRunSourceReference, String> {

  List<ModelRunSourceReference> findByRunIdOrderByIdAsc(String runId);
}
