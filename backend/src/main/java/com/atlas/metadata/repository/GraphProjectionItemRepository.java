package com.atlas.metadata.repository;

import com.atlas.metadata.domain.GraphProjectionItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for per-item graph projection outcomes. */
public interface GraphProjectionItemRepository extends JpaRepository<GraphProjectionItem, String> {

  List<GraphProjectionItem> findByRunIdOrderByIdAsc(String runId);
}
