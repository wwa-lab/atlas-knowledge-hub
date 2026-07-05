package com.atlas.metadata.repository;

import com.atlas.metadata.domain.WikiFolder;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for Wiki folder metadata. */
public interface WikiFolderRepository extends JpaRepository<WikiFolder, String> {

  /** Finds folders under one Knowledge Space in deterministic display order. */
  List<WikiFolder> findBySpaceIdOrderBySortOrderAscNameAsc(String spaceId);
}
