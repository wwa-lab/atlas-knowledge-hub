package com.atlas.metadata.repository;

import com.atlas.metadata.domain.WikiGenerationRun;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for safe Wiki generation run metadata. */
public interface WikiGenerationRunRepository extends JpaRepository<WikiGenerationRun, String> {

  /** Finds recent generation runs under one Knowledge Space. */
  List<WikiGenerationRun> findTop50BySpaceIdOrderByStartedAtDescIdAsc(String spaceId);
}
