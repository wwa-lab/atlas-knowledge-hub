package com.atlas.metadata.repository;

import com.atlas.metadata.domain.WikiPage;
import com.atlas.metadata.enums.ReviewStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository for trusted Wiki page metadata. */
public interface WikiPageRepository extends JpaRepository<WikiPage, String> {

  /** Finds published Wiki pages for a Knowledge Space. */
  List<WikiPage> findBySpaceIdAndReviewStatusOrderByTitleAsc(String spaceId, ReviewStatus reviewStatus);

  /** Finds all Wiki pages for a Knowledge Space, including review-required drafts. */
  List<WikiPage> findBySpaceIdOrderByTitleAsc(String spaceId);

  /** Finds a Wiki page by its space-scoped slug regardless of review status. */
  Optional<WikiPage> findBySpaceIdAndSlug(String spaceId, String slug);

  /** Finds a published Wiki page by its space-scoped slug. */
  Optional<WikiPage> findBySpaceIdAndSlugAndReviewStatus(
      String spaceId, String slug, ReviewStatus reviewStatus);

  /** Finds the Wiki page already published from a source file, if any. */
  @Query(
      value =
          """
          SELECT *
          FROM atlas.wiki_page
          WHERE :fileId = ANY(source_document_ids)
          ORDER BY last_updated DESC NULLS LAST, id ASC
          LIMIT 1
          """,
      nativeQuery = true)
  Optional<WikiPage> findBySourceDocumentIdsContaining(@Param("fileId") String fileId);
}
