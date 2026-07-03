package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlas.metadata.adapter.ParserAdapter;
import com.atlas.metadata.adapter.ParserCapability;
import com.atlas.metadata.adapter.ParserRequest;
import com.atlas.metadata.adapter.ParserResult;
import com.atlas.metadata.domain.Batch;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.ParserFileResult;
import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.dto.CreateParserRunRequest;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ParserAdapterStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceKind;
import com.atlas.metadata.enums.SourceType;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.ParserFileResultRepository;
import com.atlas.metadata.repository.ParserRunRepository;
import com.atlas.metadata.repository.SourceChunkRepository;
import com.atlas.metadata.validation.RelativePathValidator;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/** Unit tests for parser service eligibility, validation, and write-back behavior. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ParserServiceTest {

  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-07-03T00:00:00Z"), ZoneOffset.UTC);

  @Mock private BatchService batchService;
  @Mock private FileItemRepository fileItemRepository;
  @Mock private ParserRunRepository parserRunRepository;
  @Mock private ParserFileResultRepository parserFileResultRepository;
  @Mock private SourceChunkRepository sourceChunkRepository;

  private final Map<String, FileItem> filesById = new LinkedHashMap<>();
  private final List<ParserFileResult> savedResults = new ArrayList<>();
  private final List<SourceChunk> savedChunks = new ArrayList<>();

  @BeforeEach
  void setUp() {
    when(batchService.findBatch("batch"))
        .thenReturn(
            Batch.create(
                "batch",
                "space",
                "Parser Service Test",
                SourceKind.folder,
                "delivery-lead",
                OffsetDateTime.now(CLOCK)));
    when(parserRunRepository.existsByBatchIdAndStatusIn(any(), any())).thenReturn(false);
    when(parserRunRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(fileItemRepository.save(any(FileItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(parserFileResultRepository.save(any(ParserFileResult.class)))
        .thenAnswer(
            invocation -> {
              ParserFileResult result = invocation.getArgument(0);
              savedResults.add(result);
              return result;
            });
    when(parserFileResultRepository.findByRunIdOrderByCreatedAtAsc(any())).thenAnswer(invocation -> savedResults);
    when(sourceChunkRepository.save(any(SourceChunk.class)))
        .thenAnswer(
            invocation -> {
              SourceChunk chunk = invocation.getArgument(0);
              savedChunks.add(chunk);
              return chunk;
            });
    when(sourceChunkRepository.findByFileItemIdIn(any())).thenAnswer(invocation -> savedChunks);
    when(fileItemRepository.findAllById(any())).thenAnswer(invocation -> new ArrayList<>(filesById.values()));
  }

  @Test
  void createRunSendsOnlyEligibleFilesAndPersistsMetadataAndChunks() {
    CapturingParserAdapter adapter =
        new CapturingParserAdapter(
            file ->
                new ParserResult.ParserFileResult(
                    file.fileId(),
                    FileStatus.MARKDOWN_GENERATED,
                    "generated/markdown/BRD.md",
                    "generated/assets/BRD",
                    new BigDecimal("0.910"),
                    null,
                    List.of(
                        new ParserResult.ParserChunkResult(
                            "chunk-" + file.fileId() + "-001",
                            1,
                            "Overview",
                            new BigDecimal("0.910"),
                            null))));
    ParserService service = service(adapter);
    FileItem eligible = file("eligible", FileStatus.PDF_CONVERTED, "generated/pdf/BRD.pdf");
    FileItem ineligible = file("ineligible", FileStatus.UPLOADED, "generated/pdf/not-ready.pdf");
    filesById.put(eligible.getId(), eligible);
    filesById.put(ineligible.getId(), ineligible);
    when(fileItemRepository.findByBatchId("batch")).thenReturn(List.of(eligible, ineligible));

    var response =
        service.createRun(
            "batch",
            new CreateParserRunRequest("document-normalize", null, "delivery-lead", "mock"));

    assertThat(adapter.lastRequest.files()).extracting(ParserRequest.ParserFile::fileId)
        .containsExactly("eligible");
    assertThat(response.summary().total()).isEqualTo(2);
    assertThat(response.summary().markdownGenerated()).isEqualTo(1);
    assertThat(response.summary().skipped()).isEqualTo(1);
    assertThat(response.results()).hasSize(2);
    assertThat(response.results())
        .filteredOn(result -> result.fileId().equals("ineligible"))
        .singleElement()
        .satisfies(
            result -> {
              assertThat(result.skipped()).isTrue();
              assertThat(result.status()).isEqualTo(FileStatus.UPLOADED);
              assertThat(result.safeError()).contains("Skipped");
            });
    assertThat(eligible.getStatus()).isEqualTo(FileStatus.MARKDOWN_GENERATED);
    assertThat(eligible.getMarkdownPath()).isEqualTo("generated/markdown/BRD.md");
    assertThat(eligible.getReviewStatus()).isEqualTo(ReviewStatus.REVIEW_REQUIRED);
    assertThat(ineligible.getStatus()).isEqualTo(FileStatus.UPLOADED);
    assertThat(savedChunks).hasSize(1);
  }

  @Test
  void rejectsUnsafeParserResultBeforeFileWriteBack() {
    CapturingParserAdapter adapter =
        new CapturingParserAdapter(
            file ->
                new ParserResult.ParserFileResult(
                    file.fileId(),
                    FileStatus.MARKDOWN_GENERATED,
                    "/private/BRD.md",
                    "generated/assets/BRD",
                    new BigDecimal("0.910"),
                    null,
                    List.of()));
    ParserService service = service(adapter);
    FileItem eligible = file("eligible", FileStatus.PDF_CONVERTED, "generated/pdf/BRD.pdf");
    filesById.put(eligible.getId(), eligible);
    when(fileItemRepository.findByBatchId("batch")).thenReturn(List.of(eligible));

    assertThatThrownBy(
            () ->
                service.createRun(
                    "batch",
                    new CreateParserRunRequest("document-normalize", null, "delivery-lead", "mock")))
        .isInstanceOf(RequestValidationException.class);

    verify(fileItemRepository, never()).save(eligible);
    assertThat(savedResults).isEmpty();
  }

  @Test
  void rejectsAdapterResultWithMismatchedAdapterKey() {
    ParserAdapter adapter =
        new CapturingParserAdapter(
            file ->
                new ParserResult.ParserFileResult(
                    file.fileId(),
                    FileStatus.MARKDOWN_GENERATED,
                    "generated/markdown/BRD.md",
                    "generated/assets/BRD",
                    new BigDecimal("0.910"),
                    null,
                    List.of())) {
          @Override
          public ParserResult parse(ParserRequest request) {
            lastRequest = request;
            return new ParserResult(
                "different-parser", request.files().stream().map(resultFactory::create).toList(), "Done.");
          }
        };
    ParserService service = service(adapter);
    FileItem eligible = file("eligible", FileStatus.PDF_CONVERTED, "generated/pdf/BRD.pdf");
    filesById.put(eligible.getId(), eligible);
    when(fileItemRepository.findByBatchId("batch")).thenReturn(List.of(eligible));

    assertThatThrownBy(
            () ->
                service.createRun(
                    "batch",
                    new CreateParserRunRequest("document-normalize", null, "delivery-lead", "mock")))
        .isInstanceOf(RequestValidationException.class)
        .satisfies(
            error ->
                assertThat(((RequestValidationException) error).getFields())
                    .containsEntry("adapterKey", "must match the resolved parser adapter"));

    verify(fileItemRepository, never()).save(eligible);
    assertThat(savedResults).isEmpty();
  }

  @Test
  void adapterFaultMasksSecretsUrlsHostnamesAndPrivatePaths() {
    ParserAdapter adapter =
        new CapturingParserAdapter(file -> null) {
          @Override
          public ParserResult parse(ParserRequest request) {
            throw new IllegalStateException(
                "token=${PARSER_TEST_TOKEN} at https://parser.internal.local:9443/run host parser.corp.example /private/runtime/file");
          }
        };
    ParserService service = service(adapter);
    FileItem eligible = file("eligible", FileStatus.PDF_CONVERTED, "generated/pdf/BRD.pdf");
    filesById.put(eligible.getId(), eligible);
    when(fileItemRepository.findByBatchId("batch")).thenReturn(List.of(eligible));

    var response =
        service.createRun(
            "batch",
            new CreateParserRunRequest("document-normalize", null, "delivery-lead", "mock"));

    assertThat(response.status().name()).isEqualTo("FAILED");
    assertThat(response.safeMessage())
        .doesNotContain(
            "${PARSER_TEST_TOKEN}",
            "PARSER_TEST_TOKEN",
            "https://",
            "parser.internal.local",
            "parser.corp.example",
            "/private/runtime")
        .contains("token", "[masked]", "[endpoint]", "[host]", "[path]");
    assertThat(eligible.getStatus()).isEqualTo(FileStatus.PDF_CONVERTED);
  }

  private ParserService service(ParserAdapter adapter) {
    return new ParserService(
        batchService,
        fileItemRepository,
        parserRunRepository,
        parserFileResultRepository,
        sourceChunkRepository,
        new ParserAdapterRegistry(List.of(adapter)),
        new ParserSummaryCalculator(),
        new RelativePathValidator(),
        CLOCK);
  }

  private FileItem file(String id, FileStatus status, String pdfPath) {
    FileItem file =
        FileItem.create(
            id,
            "batch",
            "Discovery/" + id + ".pdf",
            SourceType.pdf,
            status,
            BigDecimal.ONE,
            ReviewStatus.REVIEW_REQUIRED,
            OffsetDateTime.now(CLOCK));
    file.setArtifacts(pdfPath, null, null, null);
    return file;
  }

  private interface ResultFactory {
    ParserResult.ParserFileResult create(ParserRequest.ParserFile file);
  }

  private static class CapturingParserAdapter implements ParserAdapter {

    protected final ResultFactory resultFactory;
    protected ParserRequest lastRequest;

    CapturingParserAdapter(ResultFactory resultFactory) {
      this.resultFactory = resultFactory;
    }

    @Override
    public ParserCapability capability() {
      return new ParserCapability(
          "document-normalize",
          "Document Normalize Parser",
          "test",
          List.of(SourceType.pdf),
          List.of("markdown", "assets"),
          true,
          ParserAdapterStatus.AVAILABLE,
          new BigDecimal("0.800"),
          Map.of("command", "configured", "externalNetwork", "disabled"));
    }

    @Override
    public ParserResult parse(ParserRequest request) {
      lastRequest = request;
      return new ParserResult(
          "document-normalize", request.files().stream().map(resultFactory::create).toList(), "Done.");
    }
  }
}
