package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.dto.BatchMetricsResponse;
import com.atlas.metadata.dto.BatchResponse;
import com.atlas.metadata.dto.CreateBatchRequest;
import com.atlas.metadata.dto.CreateParserRunRequest;
import com.atlas.metadata.dto.ParserRunResponse;
import com.atlas.metadata.dto.ParserRunSummaryResponse;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ParserRunStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceKind;
import com.atlas.metadata.enums.SourceType;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.SourceChunkRepository;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

/** Unit tests for upload ingestion orchestration. */
@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {

  @TempDir private Path tempDir;

  @Mock private BatchService batchService;
  @Mock private ParserService parserService;
  @Mock private FileItemRepository fileItemRepository;
  @Mock private SourceChunkRepository sourceChunkRepository;

  private IngestionService service;

  @BeforeEach
  void setUp() {
    service =
        new IngestionService(
            new LocalArtifactStorageService(tempDir),
            batchService,
            parserService,
            fileItemRepository,
            sourceChunkRepository);
  }

  @Test
  void ingestPdfCreatesBatchMetadataAndRunsLocalPdfParser() throws Exception {
    BatchResponse batch =
        new BatchResponse(
            "batch-1",
            "space-1",
            "Uploaded documents",
            SourceKind.folder,
            "user",
            OffsetDateTime.parse("2026-07-05T00:00:00Z"),
            new BatchMetricsResponse(1, 1, 0, 1, 0, 0));
    ParserRunResponse parserRun =
        new ParserRunResponse(
            "parse-run-1",
            "batch-1",
            "local-pdf-text",
            ParserRunStatus.SUCCEEDED,
            "done",
            new ParserRunSummaryResponse(1, 1, 0, 0, 0, 0, 0),
            List.of(),
            List.of(),
            OffsetDateTime.parse("2026-07-05T00:00:00Z"),
            OffsetDateTime.parse("2026-07-05T00:00:01Z"));
    FileItem savedFile =
        FileItem.create(
            "file-1",
            "batch-1",
            "source.pdf",
            SourceType.pdf,
            FileStatus.PDF_CONVERTED,
            BigDecimal.ONE,
            ReviewStatus.REVIEW_REQUIRED,
            OffsetDateTime.parse("2026-07-05T00:00:00Z"));
    savedFile.setArtifacts("uploads/space-1/session/source.pdf", null, null, null);
    when(batchService.createBatch(eq("space-1"), any(CreateBatchRequest.class))).thenReturn(batch);
    when(parserService.createRun(eq("batch-1"), any(CreateParserRunRequest.class))).thenReturn(parserRun);
    when(fileItemRepository.findByBatchId("batch-1")).thenReturn(List.of(savedFile));
    when(sourceChunkRepository.findByFileItemIdIn(List.of("file-1"))).thenReturn(List.of());

    var response =
        service.ingest(
            "space-1",
            List.of(new MockMultipartFile("files", "source.pdf", "application/pdf", "%PDF-1.4".getBytes())),
            "user");

    assertThat(response.batch().id()).isEqualTo("batch-1");
    assertThat(response.parserRun().adapterKey()).isEqualTo("local-pdf-text");
    assertThat(response.files()).singleElement().satisfies(file -> {
      assertThat(file.sourcePath()).isEqualTo("source.pdf");
      assertThat(file.status()).isEqualTo(FileStatus.PDF_CONVERTED);
    });

    ArgumentCaptor<CreateBatchRequest> batchRequest = ArgumentCaptor.forClass(CreateBatchRequest.class);
    verify(batchService).createBatch(eq("space-1"), batchRequest.capture());
    assertThat(batchRequest.getValue().sourceKind()).isEqualTo(SourceKind.folder);
    assertThat(batchRequest.getValue().files()).singleElement().satisfies(file -> {
      assertThat(file.sourcePath()).isEqualTo("source.pdf");
      assertThat(file.status()).isEqualTo(FileStatus.PDF_CONVERTED);
      assertThat(file.pdfPath()).startsWith("uploads/space-1/");
      assertThat(file.reviewStatus()).isEqualTo(ReviewStatus.REVIEW_REQUIRED);
    });
    assertThat(Files.exists(tempDir.resolve(batchRequest.getValue().files().getFirst().pdfPath()))).isTrue();

    ArgumentCaptor<CreateParserRunRequest> parserRequest = ArgumentCaptor.forClass(CreateParserRunRequest.class);
    verify(parserService).createRun(eq("batch-1"), parserRequest.capture());
    assertThat(parserRequest.getValue().adapterKey()).isEqualTo("local-pdf-text");
  }
}
