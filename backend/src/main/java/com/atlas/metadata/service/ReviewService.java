package com.atlas.metadata.service;

import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.ReviewRecord;
import com.atlas.metadata.dto.CreateReviewRequest;
import com.atlas.metadata.dto.ReviewResponse;
import com.atlas.metadata.dto.mapping.ReviewMapper;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.ReviewRecordRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for append-only file review records. */
@Service
public class ReviewService {

  private static final String FILE_TARGET_TYPE = "file";

  private final FileService fileService;
  private final FileItemRepository fileItemRepository;
  private final ReviewRecordRepository reviewRecordRepository;
  private final Clock clock;

  /** Creates the service. */
  @Autowired
  public ReviewService(
      FileService fileService,
      FileItemRepository fileItemRepository,
      ReviewRecordRepository reviewRecordRepository) {
    this(fileService, fileItemRepository, reviewRecordRepository, Clock.systemUTC());
  }

  ReviewService(
      FileService fileService,
      FileItemRepository fileItemRepository,
      ReviewRecordRepository reviewRecordRepository,
      Clock clock) {
    this.fileService = fileService;
    this.fileItemRepository = fileItemRepository;
    this.reviewRecordRepository = reviewRecordRepository;
    this.clock = clock;
  }

  /** Appends a review record and updates the target file review status. */
  @Transactional
  public ReviewResponse appendFileReview(String fileId, CreateReviewRequest request) {
    FileItem file = fileService.findFile(fileId);
    ReviewRecord record =
        ReviewRecord.create(
            FILE_TARGET_TYPE,
            fileId,
            request.action(),
            request.reviewer(),
            request.comment(),
            toArray(request.affectedChunks()),
            OffsetDateTime.now(clock));
    ReviewRecord saved = reviewRecordRepository.save(record);
    file.applyReviewStatus(request.action().resultingStatus());
    fileItemRepository.save(file);
    return ReviewMapper.toResponse(saved);
  }

  /** Lists review history for a file in chronological order. */
  @Transactional(readOnly = true)
  public List<ReviewResponse> listFileReviews(String fileId) {
    fileService.findFile(fileId);
    return reviewRecordRepository
        .findByTargetTypeAndTargetIdOrderByCreatedAtAsc(FILE_TARGET_TYPE, fileId)
        .stream()
        .map(ReviewMapper::toResponse)
        .toList();
  }

  private String[] toArray(List<String> affectedChunks) {
    return affectedChunks == null ? null : affectedChunks.toArray(String[]::new);
  }
}
