package com.atlas.metadata.repository;

import com.atlas.metadata.domain.WikiLogEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for safe Wiki lifecycle logs. */
public interface WikiLogEntryRepository extends JpaRepository<WikiLogEntry, String> {

  /** Finds recent safe log entries for one Wiki page. */
  List<WikiLogEntry> findTop50ByPageIdOrderByCreatedAtDescIdAsc(String pageId);
}
