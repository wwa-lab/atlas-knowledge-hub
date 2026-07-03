package com.atlas.metadata.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.SpaceStatus;
import com.atlas.metadata.repository.BatchRepository;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.ReviewRecordRepository;
import com.atlas.metadata.repository.SpaceRepository;
import com.atlas.metadata.repository.SourceChunkRepository;
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
}
