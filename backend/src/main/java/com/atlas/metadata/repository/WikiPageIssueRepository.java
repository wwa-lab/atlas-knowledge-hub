package com.atlas.metadata.repository;

import com.atlas.metadata.domain.WikiPageIssue;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for safe Wiki page issue metadata. */
public interface WikiPageIssueRepository extends JpaRepository<WikiPageIssue, String> {

  /** Finds issues for one Wiki page in deterministic order. */
  List<WikiPageIssue> findByPageIdOrderByCreatedAtDescIdAsc(String pageId);

  /** Finds issues for one Knowledge Space in deterministic order. */
  List<WikiPageIssue> findBySpaceIdOrderByCreatedAtDescIdAsc(String spaceId);

  /** Finds issues for one Knowledge Space and status in deterministic order. */
  List<WikiPageIssue> findBySpaceIdAndStatusOrderByCreatedAtDescIdAsc(String spaceId, String status);

  /** Finds issues for one Knowledge Space, issue type, and status in deterministic order. */
  List<WikiPageIssue> findBySpaceIdAndIssueTypeAndStatusOrderByCreatedAtDescIdAsc(
      String spaceId, String issueType, String status);
}
