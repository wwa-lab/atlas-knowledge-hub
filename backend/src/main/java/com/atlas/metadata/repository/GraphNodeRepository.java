package com.atlas.metadata.repository;

import com.atlas.metadata.domain.GraphNode;
import com.atlas.metadata.enums.GraphNodeType;
import com.atlas.metadata.enums.ReviewStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for knowledge graph nodes. */
public interface GraphNodeRepository extends JpaRepository<GraphNode, String> {

  List<GraphNode> findBySpaceId(String spaceId);

  List<GraphNode> findBySpaceIdAndReviewStatusIn(String spaceId, List<ReviewStatus> statuses);

  List<GraphNode> findBySpaceIdAndTypeAndReviewStatusIn(
      String spaceId, GraphNodeType type, List<ReviewStatus> statuses);
}
