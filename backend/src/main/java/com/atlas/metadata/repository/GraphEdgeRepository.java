package com.atlas.metadata.repository;

import com.atlas.metadata.domain.GraphEdge;
import com.atlas.metadata.enums.GraphEdgeType;
import com.atlas.metadata.enums.ReviewStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for knowledge graph edges. */
public interface GraphEdgeRepository extends JpaRepository<GraphEdge, String> {

  List<GraphEdge> findBySpaceId(String spaceId);

  List<GraphEdge> findBySpaceIdAndReviewStatusIn(String spaceId, List<ReviewStatus> statuses);

  List<GraphEdge> findBySpaceIdAndTypeAndReviewStatusIn(
      String spaceId, GraphEdgeType type, List<ReviewStatus> statuses);

  List<GraphEdge> findBySpaceIdAndSourceNodeIdOrSpaceIdAndTargetNodeId(
      String sourceSpaceId, String sourceNodeId, String targetSpaceId, String targetNodeId);
}
