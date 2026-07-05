package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.IngestionResponse;
import com.atlas.metadata.service.IngestionService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** REST controller for real document ingestion. */
@RestController
@RequestMapping("/api")
public class IngestionController {

  private final IngestionService ingestionService;

  /** Creates the controller. */
  public IngestionController(IngestionService ingestionService) {
    this.ingestionService = ingestionService;
  }

  /** Uploads PDFs or ZIP archives containing PDFs into a Knowledge Space. */
  @PostMapping(
      value = "/spaces/{spaceId}/ingestions",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiEnvelope<IngestionResponse>> ingest(
      @PathVariable String spaceId,
      @RequestPart("files") List<MultipartFile> files,
      @RequestParam(defaultValue = "frontend-user") String owner) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiEnvelope.ok(ingestionService.ingest(spaceId, files, owner)));
  }
}
