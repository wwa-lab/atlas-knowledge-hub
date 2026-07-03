package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.FileItemResponse;
import com.atlas.metadata.dto.SourceChunkResponse;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.service.FileService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for file items and source chunks. */
@RestController
@RequestMapping("/api")
public class FileController {

  private final FileService fileService;

  /** Creates the controller. */
  public FileController(FileService fileService) {
    this.fileService = fileService;
  }

  /** Lists files for a batch, optionally filtered by status. */
  @GetMapping("/batches/{batchId}/files")
  public ApiEnvelope<List<FileItemResponse>> listFiles(
      @PathVariable String batchId,
      @RequestParam(required = false) FileStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Page<FileItemResponse> files =
        fileService.listFiles(
            batchId, status, PageRequests.of(page, size, Sort.by(Sort.Direction.ASC, "id")));
    return ApiEnvelope.ok(files.getContent(), PageRequests.meta(files));
  }

  /** Gets one file item. */
  @GetMapping("/files/{fileId}")
  public ApiEnvelope<FileItemResponse> getFile(@PathVariable String fileId) {
    return ApiEnvelope.ok(fileService.getFile(fileId));
  }

  /** Lists source chunks for one file item. */
  @GetMapping("/files/{fileId}/chunks")
  public ApiEnvelope<List<SourceChunkResponse>> listChunks(@PathVariable String fileId) {
    return ApiEnvelope.ok(fileService.listChunks(fileId));
  }
}
