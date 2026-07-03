package com.atlas.metadata.service;

import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.dto.FileItemResponse;
import com.atlas.metadata.dto.SourceChunkResponse;
import com.atlas.metadata.dto.mapping.FileItemMapper;
import com.atlas.metadata.dto.mapping.SourceChunkMapper;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.exception.NotFoundException;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.SourceChunkRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for file-item and source-chunk metadata. */
@Service
public class FileService {

  private final FileItemRepository fileItemRepository;
  private final SourceChunkRepository sourceChunkRepository;
  private final BatchService batchService;

  /** Creates the service. */
  public FileService(
      FileItemRepository fileItemRepository,
      SourceChunkRepository sourceChunkRepository,
      BatchService batchService) {
    this.fileItemRepository = fileItemRepository;
    this.sourceChunkRepository = sourceChunkRepository;
    this.batchService = batchService;
  }

  /** Lists file items for a batch, optionally filtered by file status. */
  @Transactional(readOnly = true)
  public Page<FileItemResponse> listFiles(String batchId, FileStatus status, Pageable pageable) {
    batchService.findBatch(batchId);
    Page<FileItem> files =
        status == null
            ? fileItemRepository.findByBatchId(batchId, pageable)
            : fileItemRepository.findByBatchIdAndStatus(batchId, status, pageable);
    return files.map(FileItemMapper::toResponse);
  }

  /** Gets a file item by id. */
  @Transactional(readOnly = true)
  public FileItemResponse getFile(String fileId) {
    return FileItemMapper.toResponse(findFile(fileId));
  }

  /** Lists source chunks for a file item. */
  @Transactional(readOnly = true)
  public List<SourceChunkResponse> listChunks(String fileId) {
    findFile(fileId);
    return sourceChunkRepository.findByFileItemId(fileId).stream()
        .map(SourceChunkMapper::toResponse)
        .toList();
  }

  /** Finds a file item or throws a user-safe not-found error. */
  @Transactional(readOnly = true)
  public FileItem findFile(String fileId) {
    return fileItemRepository
        .findById(fileId)
        .orElseThrow(() -> new NotFoundException("File item not found."));
  }
}
