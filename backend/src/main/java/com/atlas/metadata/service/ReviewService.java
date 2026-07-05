package com.atlas.metadata.service;

import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.ReviewRecord;
import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.dto.CreateReviewRequest;
import com.atlas.metadata.dto.ReviewResponse;
import com.atlas.metadata.dto.mapping.ReviewMapper;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.exception.ConflictException;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.ReviewRecordRepository;
import com.atlas.metadata.repository.SourceChunkRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
  private final SourceChunkRepository sourceChunkRepository;
  private final Clock clock;

  /** Creates the service. */
  @Autowired
  public ReviewService(
      FileService fileService,
      FileItemRepository fileItemRepository,
      ReviewRecordRepository reviewRecordRepository,
      SourceChunkRepository sourceChunkRepository) {
    this(fileService, fileItemRepository, reviewRecordRepository, sourceChunkRepository, Clock.systemUTC());
  }

  ReviewService(
      FileService fileService,
      FileItemRepository fileItemRepository,
      ReviewRecordRepository reviewRecordRepository,
      SourceChunkRepository sourceChunkRepository,
      Clock clock) {
    this.fileService = fileService;
    this.fileItemRepository = fileItemRepository;
    this.reviewRecordRepository = reviewRecordRepository;
    this.sourceChunkRepository = sourceChunkRepository;
    this.clock = clock;
  }

  /** Appends a review record and updates the target file review status. */
  @Transactional
  public ReviewResponse appendFileReview(String fileId, CreateReviewRequest request) {
    FileItem file = fileService.findFile(fileId);
    ReviewStatus resultingStatus = request.action().resultingStatus();
    List<SourceChunk> affectedChunks = resolveAffectedChunks(fileId, request.affectedChunks());
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
    file.applyReviewStatus(resultingStatus);
    fileItemRepository.save(file);
    affectedChunks.forEach(chunk -> chunk.applyReviewStatus(resultingStatus));
    if (!affectedChunks.isEmpty()) {
      sourceChunkRepository.saveAll(affectedChunks);
    }
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

  private List<SourceChunk> resolveAffectedChunks(String fileId, List<String> affectedChunkIds) {
    if (affectedChunkIds == null || affectedChunkIds.isEmpty()) {
      return sourceChunkRepository.findByFileItemId(fileId);
    }
    List<String> requestedIds = new LinkedHashSet<>(affectedChunkIds).stream().toList();
    List<SourceChunk> chunks = sourceChunkRepository.findAllById(requestedIds);
    Set<String> foundIds = chunks.stream().map(SourceChunk::getId).collect(java.util.stream.Collectors.toSet());
    if (foundIds.size() != requestedIds.size()) {
      throw new ConflictException("All affected chunks must exist before file review can be recorded.");
    }
    boolean includesForeignChunk = chunks.stream().anyMatch(chunk -> !fileId.equals(chunk.getFileItemId()));
    if (includesForeignChunk) {
      throw new ConflictException("All affected chunks must belong to the reviewed file.");
    }
    return chunks;
  }
}
