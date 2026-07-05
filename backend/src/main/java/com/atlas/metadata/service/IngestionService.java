package com.atlas.metadata.service;

import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.dto.BatchResponse;
import com.atlas.metadata.dto.CreateBatchRequest;
import com.atlas.metadata.dto.CreateBatchRequest.InventoryFileRequest;
import com.atlas.metadata.dto.CreateParserRunRequest;
import com.atlas.metadata.dto.IngestionResponse;
import com.atlas.metadata.dto.ParserRunResponse;
import com.atlas.metadata.dto.mapping.FileItemMapper;
import com.atlas.metadata.dto.mapping.SourceChunkMapper;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceKind;
import com.atlas.metadata.enums.SourceType;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.SourceChunkRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** Orchestrates upload storage, batch metadata creation, and local PDF parsing. */
@Service
public class IngestionService {

  private static final String LOCAL_PDF_TEXT_ADAPTER = "local-pdf-text";

  private final LocalArtifactStorageService artifactStorage;
  private final BatchService batchService;
  private final ParserService parserService;
  private final FileItemRepository fileItemRepository;
  private final SourceChunkRepository sourceChunkRepository;

  /** Creates the ingestion service. */
  public IngestionService(
      LocalArtifactStorageService artifactStorage,
      BatchService batchService,
      ParserService parserService,
      FileItemRepository fileItemRepository,
      SourceChunkRepository sourceChunkRepository) {
    this.artifactStorage = artifactStorage;
    this.batchService = batchService;
    this.parserService = parserService;
    this.fileItemRepository = fileItemRepository;
    this.sourceChunkRepository = sourceChunkRepository;
  }

  /** Ingests user-uploaded PDF files or ZIP archives containing PDF files. */
  public IngestionResponse ingest(String spaceId, List<MultipartFile> uploads, String owner) {
    List<StoredPdf> storedPdfs = storeUploads(spaceId, uploads);
    BatchResponse batch =
        batchService.createBatch(
            spaceId,
            new CreateBatchRequest(
                "Uploaded documents",
                storedPdfs.stream().anyMatch(StoredPdf::fromZip) ? SourceKind.zip : SourceKind.folder,
                safeOwner(owner),
                storedPdfs.stream().map(this::toBatchFile).toList()));
    ParserRunResponse parserRun =
        parserService.createRun(
            batch.id(),
            new CreateParserRunRequest(LOCAL_PDF_TEXT_ADAPTER, null, safeOwner(owner), "configured"));
    List<FileItem> fileItems = fileItemRepository.findByBatchId(batch.id());
    List<String> fileIds = fileItems.stream().map(FileItem::getId).toList();
    List<SourceChunk> chunks = fileIds.isEmpty() ? List.of() : sourceChunkRepository.findByFileItemIdIn(fileIds);
    return new IngestionResponse(
        batch,
        fileItems.stream().map(FileItemMapper::toResponse).toList(),
        chunks.stream().map(SourceChunkMapper::toResponse).toList(),
        parserRun,
        "Uploaded documents were stored and queued for local PDF parsing.");
  }

  private List<StoredPdf> storeUploads(String spaceId, List<MultipartFile> uploads) {
    if (uploads == null || uploads.isEmpty()) {
      throw new RequestValidationException(Map.of("files", "must include at least one PDF or ZIP file"));
    }
    Map<String, String> errors = new LinkedHashMap<>();
    List<StoredPdf> storedPdfs = new ArrayList<>();
    for (int index = 0; index < uploads.size(); index++) {
      MultipartFile upload = uploads.get(index);
      if (upload == null || upload.isEmpty()) {
        errors.put("files[" + index + "]", "must not be empty");
        continue;
      }
      String filename = upload.getOriginalFilename();
      if (isPdf(filename, upload.getContentType())) {
        storedPdfs.add(storePdf(spaceId, filename, upload, false));
      } else if (isZip(filename, upload.getContentType())) {
        storedPdfs.addAll(storeZipPdfs(spaceId, upload, index));
      } else {
        errors.put("files[" + index + "]", "must be a PDF or ZIP containing PDF files");
      }
    }
    if (!errors.isEmpty()) {
      throw new RequestValidationException(errors);
    }
    if (storedPdfs.isEmpty()) {
      throw new RequestValidationException(Map.of("files", "must include at least one PDF file"));
    }
    return storedPdfs;
  }

  private StoredPdf storePdf(String spaceId, String filename, MultipartFile upload, boolean fromZip) {
    try {
      var stored = artifactStorage.storeUploadedPdf(spaceId, filename, upload.getInputStream());
      return new StoredPdf(stored.fileName(), stored.relativePath(), fromZip);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to read uploaded PDF safely.");
    }
  }

  private List<StoredPdf> storeZipPdfs(String spaceId, MultipartFile upload, int uploadIndex) {
    List<StoredPdf> stored = new ArrayList<>();
    try (ZipInputStream zip = new ZipInputStream(upload.getInputStream())) {
      ZipEntry entry;
      while ((entry = zip.getNextEntry()) != null) {
        if (entry.isDirectory()) {
          continue;
        }
        String entryName = safeZipEntryName(entry.getName(), uploadIndex);
        if (!isPdf(entryName, null)) {
          continue;
        }
        var artifact = artifactStorage.storeUploadedPdf(spaceId, entryName, zip);
        stored.add(new StoredPdf(entryName, artifact.relativePath(), true));
      }
    } catch (IOException ex) {
      throw new RequestValidationException(Map.of("files[" + uploadIndex + "]", "must be a readable ZIP archive"));
    }
    return stored;
  }

  private String safeZipEntryName(String entryName, int uploadIndex) {
    String normalized = entryName == null ? "" : entryName.replace('\\', '/');
    try {
      Path path = Path.of(normalized).normalize();
      if (path.isAbsolute() || path.startsWith("..")) {
        throw new RequestValidationException(
            Map.of("files[" + uploadIndex + "]", "ZIP entries must not contain traversal paths"));
      }
      for (Path part : path) {
        if ("..".equals(part.toString())) {
          throw new RequestValidationException(
              Map.of("files[" + uploadIndex + "]", "ZIP entries must not contain traversal paths"));
        }
      }
      return path.toString().replace('\\', '/');
    } catch (RuntimeException ex) {
      if (ex instanceof RequestValidationException validationException) {
        throw validationException;
      }
      throw new RequestValidationException(
          Map.of("files[" + uploadIndex + "]", "ZIP entries must use safe relative paths"));
    }
  }

  private InventoryFileRequest toBatchFile(StoredPdf pdf) {
    return new InventoryFileRequest(
        pdf.sourcePath(),
        SourceType.pdf,
        FileStatus.PDF_CONVERTED,
        BigDecimal.ONE,
        ReviewStatus.REVIEW_REQUIRED,
        pdf.pdfPath(),
        null,
        null,
        null,
        List.of());
  }

  private boolean isPdf(String filename, String contentType) {
    return lower(filename).endsWith(".pdf") || "application/pdf".equalsIgnoreCase(contentType);
  }

  private boolean isZip(String filename, String contentType) {
    String lowered = lower(filename);
    return lowered.endsWith(".zip")
        || "application/zip".equalsIgnoreCase(contentType)
        || "application/x-zip-compressed".equalsIgnoreCase(contentType);
  }

  private String safeOwner(String owner) {
    return owner == null || owner.isBlank() ? "frontend-user" : owner.trim();
  }

  private String lower(String value) {
    return value == null ? "" : value.toLowerCase();
  }

  private record StoredPdf(String sourcePath, String pdfPath, boolean fromZip) {}
}
