package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.ReviewRecord;
import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.dto.CreateReviewRequest;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewAction;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceType;
import com.atlas.metadata.exception.ConflictException;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.ReviewRecordRepository;
import com.atlas.metadata.repository.SourceChunkRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for append-only review records and chunk review propagation. */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-07-05T00:00:00Z"), ZoneOffset.UTC);

  @Mock private FileService fileService;
  @Mock private FileItemRepository fileItemRepository;
  @Mock private ReviewRecordRepository reviewRecordRepository;
  @Mock private SourceChunkRepository sourceChunkRepository;

  private ReviewService service;

  @BeforeEach
  void setUp() {
    service =
        new ReviewService(
            fileService, fileItemRepository, reviewRecordRepository, sourceChunkRepository, CLOCK);
  }

  @Test
  void appendFileReviewUpdatesOnlySelectedChunksForTheFile() {
    FileItem file = file("file-1");
    SourceChunk selected = chunk("chunk-1", "file-1");
    when(fileService.findFile("file-1")).thenReturn(file);
    when(sourceChunkRepository.findAllById(List.of("chunk-1"))).thenReturn(List.of(selected));
    when(reviewRecordRepository.save(any(ReviewRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

    service.appendFileReview(
        "file-1", new CreateReviewRequest(ReviewAction.APPROVE, "sme", "Looks good", List.of("chunk-1")));

    assertThat(file.getReviewStatus()).isEqualTo(ReviewStatus.APPROVED);
    assertThat(selected.getReviewStatus()).isEqualTo(ReviewStatus.APPROVED);
    verify(sourceChunkRepository).saveAll(List.of(selected));
  }

  @Test
  void appendFileReviewUpdatesAllFileChunksWhenRequestOmitsAffectedChunks() {
    FileItem file = file("file-1");
    SourceChunk first = chunk("chunk-1", "file-1");
    SourceChunk second = chunk("chunk-2", "file-1");
    when(fileService.findFile("file-1")).thenReturn(file);
    when(sourceChunkRepository.findByFileItemId("file-1")).thenReturn(List.of(first, second));
    when(reviewRecordRepository.save(any(ReviewRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

    service.appendFileReview(
        "file-1", new CreateReviewRequest(ReviewAction.NEED_FIX, "sme", "Missing source trace", null));

    assertThat(file.getReviewStatus()).isEqualTo(ReviewStatus.NEED_FIX);
    assertThat(first.getReviewStatus()).isEqualTo(ReviewStatus.NEED_FIX);
    assertThat(second.getReviewStatus()).isEqualTo(ReviewStatus.NEED_FIX);
    verify(sourceChunkRepository).saveAll(List.of(first, second));
  }

  @Test
  void appendFileReviewRejectsAffectedChunksFromAnotherFile() {
    FileItem file = file("file-1");
    SourceChunk foreign = chunk("chunk-foreign", "file-2");
    when(fileService.findFile("file-1")).thenReturn(file);
    when(sourceChunkRepository.findAllById(List.of("chunk-foreign"))).thenReturn(List.of(foreign));

    assertThatThrownBy(
            () ->
                service.appendFileReview(
                    "file-1",
                    new CreateReviewRequest(
                        ReviewAction.APPROVE, "sme", "Wrong chunk", List.of("chunk-foreign"))))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("affected chunks");

    verify(reviewRecordRepository, never()).save(any(ReviewRecord.class));
    verify(fileItemRepository, never()).save(any(FileItem.class));
    verify(sourceChunkRepository, never()).saveAll(any());
  }

  private FileItem file(String id) {
    return FileItem.create(
        id,
        "batch",
        "uploads/" + id + ".pdf",
        SourceType.pdf,
        FileStatus.MARKDOWN_GENERATED,
        new BigDecimal("0.920"),
        ReviewStatus.REVIEW_REQUIRED,
        OffsetDateTime.now(CLOCK));
  }

  private SourceChunk chunk(String id, String fileId) {
    return SourceChunk.create(
        id, fileId, fileId + ".pdf", 1, "Overview", new BigDecimal("0.900"), ReviewStatus.REVIEW_REQUIRED);
  }
}
