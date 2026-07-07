package com.atlas.metadata.repository;

import com.atlas.metadata.domain.ManualUrlSource;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for metadata-only manual URL sources. */
public interface ManualUrlSourceRepository extends JpaRepository<ManualUrlSource, String> {

  /** Finds all manual URL sources for a Knowledge Space. */
  List<ManualUrlSource> findBySpaceIdOrderByCreatedAtDesc(String spaceId);

  /** Finds a URL source by space and sanitized URL hash. */
  Optional<ManualUrlSource> findBySpaceIdAndUrlHash(String spaceId, String urlHash);
}
