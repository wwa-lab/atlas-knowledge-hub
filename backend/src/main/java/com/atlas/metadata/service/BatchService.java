package com.atlas.metadata.service;

import com.atlas.metadata.domain.Batch;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.dto.BatchMetricsResponse;
import com.atlas.metadata.dto.BatchResponse;
import com.atlas.metadata.dto.CreateBatchRequest;
import com.atlas.metadata.dto.CreateBatchRequest.InventoryFileRequest;
import com.atlas.metadata.dto.CreateBatchRequest.SourceChunkRequest;
import com.atlas.metadata.dto.mapping.BatchMapper;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.exception.NotFoundException;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.repository.BatchRepository;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.SourceChunkRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for batch metadata. */
@Service
public class BatchService {

  private final BatchRepository batchRepository;
  private final FileItemRepository fileItemRepository;
  private final SourceChunkRepository sourceChunkRepository;
  private final SpaceService spaceService;
  private final MetricsCalculator metricsCalculator;
  private final Clock clock;

  /** Creates the service. */
  @Autowired
  public BatchService(
      BatchRepository batchRepository,
      FileItemRepository fileItemRepository,
      SourceChunkRepository sourceChunkRepository,
      SpaceService spaceService) {
    this(
        batchRepository,
        fileItemRepository,
        sourceChunkRepository,
        spaceService,
        new MetricsCalculator(),
        Clock.systemUTC());
  }

  BatchService(
      BatchRepository batchRepository,
      FileItemRepository fileItemRepository,
      SourceChunkRepository sourceChunkRepository,
      SpaceService spaceService,
      MetricsCalculator metricsCalculator,
      Clock clock) {
    this.batchRepository = batchRepository;
    this.fileItemRepository = fileItemRepository;
    this.sourceChunkRepository = sourceChunkRepository;
    this.spaceService = spaceService;
    this.metricsCalculator = metricsCalculator;
    this.clock = clock;
  }

  /** Lists batches for a space with derived metrics. */
  @Transactional(readOnly = true)
  public Page<BatchResponse> listBatches(String spaceId, Pageable pageable) {
    spaceService.findSpace(spaceId);
    return batchRepository
        .findBySpaceId(spaceId, pageable)
        .map(batch -> BatchMapper.toResponse(batch, metrics(batch.getId())));
  }

  /** Gets a batch by id with derived metrics. */
  @Transactional(readOnly = true)
  public BatchResponse getBatch(String batchId) {
    Batch batch = findBatch(batchId);
    return BatchMapper.toResponse(batch, metrics(batch.getId()));
  }

  /** Creates a batch and file/chunk metadata without touching bytes or engines. */
  @Transactional
  public BatchResponse createBatch(String spaceId, CreateBatchRequest request) {
    spaceService.findSpace(spaceId);
    validateReviewDefaults(request.files());
    OffsetDateTime now = OffsetDateTime.now(clock);
    String batchId =
        "batch-"
            + now.format(DateTimeFormatter.ISO_LOCAL_DATE)
            + "-"
            + UUID.randomUUID().toString().substring(0, 8);
    Batch batch =
        batchRepository.save(
            Batch.create(batchId, spaceId, request.name(), request.sourceKind(), request.owner(), now));

    int index = 0;
    for (InventoryFileRequest file : request.files()) {
      String fileId = "file-" + UUID.randomUUID().toString().substring(0, 8);
      FileItem item =
          FileItem.create(
              fileId,
              batch.getId(),
              file.sourcePath(),
              file.sourceType(),
              file.status(),
              scale(file.confidence()),
              file.reviewStatus(),
              now);
      item.setArtifacts(file.pdfPath(), file.markdownPath(), file.assetsPath(), file.errorMessage());
      fileItemRepository.save(item);
      saveChunks(file.chunks(), item, index);
      index++;
    }
    return BatchMapper.toResponse(batch, metrics(batch.getId()));
  }

  /** Finds a batch entity or throws a user-safe not-found error. */
  @Transactional(readOnly = true)
  public Batch findBatch(String batchId) {
    return batchRepository
        .findById(batchId)
        .orElseThrow(() -> new NotFoundException("Batch not found."));
  }

  private void saveChunks(List<SourceChunkRequest> chunks, FileItem item, int fileIndex) {
    if (chunks == null) {
      return;
    }
    int chunkIndex = 0;
    for (SourceChunkRequest chunk : chunks) {
      String chunkId =
          "chunk-" + item.getId() + "-" + fileIndex + "-" + chunkIndex + "-"
              + UUID.randomUUID().toString().substring(0, 6);
      sourceChunkRepository.save(
          SourceChunk.create(
              chunkId,
              item.getId(),
              chunk.sourceFile(),
              chunk.page(),
              chunk.section(),
              scale(chunk.confidence()),
              chunk.reviewStatus()));
      chunkIndex++;
    }
  }

  private void validateReviewDefaults(List<InventoryFileRequest> files) {
    for (int index = 0; index < files.size(); index++) {
      ReviewStatus status = files.get(index).reviewStatus();
      if (status == ReviewStatus.APPROVED || status == ReviewStatus.PUBLISHED) {
        throw new RequestValidationException(
            Map.of("files[" + index + "].reviewStatus", "must be REVIEW_REQUIRED on create"));
      }
    }
  }

  private BatchMetricsResponse metrics(String batchId) {
    return metricsCalculator.compute(fileItemRepository.findByBatchId(batchId));
  }

  private BigDecimal scale(BigDecimal value) {
    return value == null ? null : value.setScale(3, RoundingMode.HALF_UP);
  }
}
