package com.atlas.metadata.repository;

import com.atlas.metadata.domain.ModelRun;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for model run evidence. */
public interface ModelRunRepository extends JpaRepository<ModelRun, String> {}
