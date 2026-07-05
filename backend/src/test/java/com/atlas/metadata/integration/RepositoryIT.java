package com.atlas.metadata.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.SpaceStatus;
import com.atlas.metadata.repository.BatchRepository;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.ReviewRecordRepository;
import com.atlas.metadata.repository.SpaceRepository;
import com.atlas.metadata.repository.SourceChunkRepository;
import com.atlas.metadata.repository.WikiFolderRepository;
import com.atlas.metadata.repository.WikiGenerationRunRepository;
import com.atlas.metadata.repository.WikiLogEntryRepository;
import com.atlas.metadata.repository.WikiPageIssueRepository;
import com.atlas.metadata.repository.WikiPageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

/** Repository integration checks against PostgreSQL. */
class RepositoryIT extends AbstractPostgresIT {

  @Autowired private SpaceRepository spaceRepository;
  @Autowired private BatchRepository batchRepository;
  @Autowired private FileItemRepository fileItemRepository;
  @Autowired private SourceChunkRepository sourceChunkRepository;
  @Autowired private ReviewRecordRepository reviewRecordRepository;
  @Autowired private WikiPageRepository wikiPageRepository;
  @Autowired private WikiFolderRepository wikiFolderRepository;
  @Autowired private WikiGenerationRunRepository wikiGenerationRunRepository;
  @Autowired private WikiLogEntryRepository wikiLogEntryRepository;
  @Autowired private WikiPageIssueRepository wikiPageIssueRepository;

  @Test
  void repositoriesSupportPagingFiltersAndChronologicalHistory() {
    assertThat(
            spaceRepository
                .findByStatus(SpaceStatus.REVIEW_REQUIRED, PageRequest.of(0, 10))
                .getTotalElements())
        .isEqualTo(1);

    assertThat(
            batchRepository
                .findBySpaceId("ibm-i-modernization", PageRequest.of(0, 10))
                .getTotalElements())
        .isGreaterThanOrEqualTo(1);

    assertThat(
            fileItemRepository
                .findByBatchIdAndStatus(
                    "batch-2026-06-20-001", FileStatus.UNSUPPORTED, PageRequest.of(0, 10))
                .getTotalElements())
        .isEqualTo(1);

    assertThat(sourceChunkRepository.findByFileItemId("file-001"))
        .extracting("id")
        .containsExactly("chunk-file-001-p12-b02");

    assertThat(
            reviewRecordRepository.findByTargetTypeAndTargetIdOrderByCreatedAtAsc(
                "file", "file-001"))
        .extracting("action")
        .extracting(Object::toString)
        .startsWith("NEED_FIX");
  }

  @Test
  void wikiFoundationMigrationBackfillsLegacyRowsAndSupportTables() {
    var page = wikiPageRepository.findById("wiki-modernization-overview").orElseThrow();

    assertThat(page.getSlug()).isEqualTo("modernization-overview");
    assertThat(page.getPageType()).isEqualTo("SOURCE_SUMMARY");
    assertThat(page.getAliases()).isEmpty();
    assertThat(page.getSourceRefs()).extracting("type", "id").contains(
        org.assertj.core.groups.Tuple.tuple("FILE", "file-001"),
        org.assertj.core.groups.Tuple.tuple("FILE", "file-005"));
    assertThat(page.getChunkRefs()).isEmpty();
    assertThat(page.getVersion()).isEqualTo(1);
    assertThat(page.getSourceMode()).isEqualTo("PUBLISHED_FILE");
    assertThat(page.getRefreshPolicy()).isEqualTo("MANUAL");

    assertThat(wikiFolderRepository.findBySpaceIdOrderBySortOrderAscNameAsc("ibm-i-modernization"))
        .extracting("slug")
        .contains("foundation");
    assertThat(
            wikiGenerationRunRepository.findTop50BySpaceIdOrderByStartedAtDescIdAsc(
                "ibm-i-modernization"))
        .extracting("id")
        .contains("wiki-run-sample-001");
    assertThat(wikiLogEntryRepository.findTop50ByPageIdOrderByCreatedAtDescIdAsc(page.getId()))
        .extracting("eventType")
        .contains("METADATA_UPDATED");
    assertThat(wikiPageIssueRepository.findByPageIdOrderByCreatedAtDescIdAsc(page.getId()))
        .extracting("issueType")
        .contains("MISSING_SOURCE_REF");
  }
}
