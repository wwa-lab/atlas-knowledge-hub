package com.atlas.metadata.repository;

import com.atlas.metadata.domain.ModelRunOutput;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for safe model output descriptors. */
public interface ModelRunOutputRepository extends JpaRepository<ModelRunOutput, String> {

  List<ModelRunOutput> findByRunIdOrderByIdAsc(String runId);
}
