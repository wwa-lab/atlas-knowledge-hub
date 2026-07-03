package com.atlas.metadata.repository;

import com.atlas.metadata.domain.SourceChunk;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for source chunk trace records. */
public interface SourceChunkRepository extends JpaRepository<SourceChunk, String> {

  /** Finds chunks for a file item. */
  List<SourceChunk> findByFileItemId(String fileItemId);
}
