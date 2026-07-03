package com.atlas.metadata.service;

import com.atlas.metadata.adapter.ConverterAdapter;
import com.atlas.metadata.adapter.ConverterCapability;
import com.atlas.metadata.adapter.ConverterRequest;
import com.atlas.metadata.adapter.ConverterResult;
import com.atlas.metadata.domain.ConversionFileResult;
import com.atlas.metadata.domain.ConversionRun;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.dto.ConversionRunResponse;
import com.atlas.metadata.dto.ConversionRunSummaryResponse;
import com.atlas.metadata.dto.ConverterCapabilityResponse;
import com.atlas.metadata.dto.CreateConversionRunRequest;
import com.atlas.metadata.dto.mapping.ConverterMapper;
import com.atlas.metadata.enums.ConversionRunStatus;
import com.atlas.metadata.enums.ConverterAdapterStatus;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.exception.ConflictException;
import com.atlas.metadata.exception.NotFoundException;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.repository.ConversionFileResultRepository;
import com.atlas.metadata.repository.ConversionRunRepository;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.validation.RelativePathValidator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for converter adapter runs. */
@Service
public class ConversionService {

  private static final Collection<ConversionRunStatus> ACTIVE_STATUSES =
      List.of(ConversionRunStatus.REQUESTED, ConversionRunStatus.RUNNING);
  private static final List<FileStatus> CONVERSION_RESULT_STATUSES =
      List.of(
          FileStatus.PDF_CONVERTED,
          FileStatus.PDF_CONVERT_FAILED,
          FileStatus.OCR_REQUIRED,
          FileStatus.FAILED,
          FileStatus.UNSUPPORTED);
  private static final int MAX_SAFE_ERROR_LENGTH = 240;

  private final BatchService batchService;
  private final FileItemRepository fileItemRepository;
  private final ConversionRunRepository conversionRunRepository;
  private final ConversionFileResultRepository conversionFileResultRepository;
  private final ConverterAdapterRegistry adapterRegistry;
  private final ConversionSummaryCalculator summaryCalculator;
  private final RelativePathValidator relativePathValidator;
  private final Clock clock;

  /** Creates the service. */
  @Autowired
  public ConversionService(
      BatchService batchService,
      FileItemRepository fileItemRepository,
      ConversionRunRepository conversionRunRepository,
      ConversionFileResultRepository conversionFileResultRepository,
      ConverterAdapterRegistry adapterRegistry) {
    this(
        batchService,
        fileItemRepository,
        conversionRunRepository,
        conversionFileResultRepository,
        adapterRegistry,
        new ConversionSummaryCalculator(),
        new RelativePathValidator(),
        Clock.systemUTC());
  }

  ConversionService(
      BatchService batchService,
      FileItemRepository fileItemRepository,
      ConversionRunRepository conversionRunRepository,
      ConversionFileResultRepository conversionFileResultRepository,
      ConverterAdapterRegistry adapterRegistry,
      ConversionSummaryCalculator summaryCalculator,
      RelativePathValidator relativePathValidator,
      Clock clock) {
    this.batchService = batchService;
    this.fileItemRepository = fileItemRepository;
    this.conversionRunRepository = conversionRunRepository;
    this.conversionFileResultRepository = conversionFileResultRepository;
    this.adapterRegistry = adapterRegistry;
    this.summaryCalculator = summaryCalculator;
    this.relativePathValidator = relativePathValidator;
    this.clock = clock;
  }

  /** Lists converter capabilities with masked configuration. */
  @Transactional(readOnly = true)
  public List<ConverterCapabilityResponse> listCapabilities() {
    return adapterRegistry.capabilities().stream().map(ConverterMapper::toResponse).toList();
  }

  /** Creates and executes a conversion run for one batch. */
  @Transactional
  public ConversionRunResponse createRun(String batchId, CreateConversionRunRequest request) {
    batchService.findBatch(batchId);
    validateMode(request.mode());
    if (conversionRunRepository.existsByBatchIdAndStatusIn(batchId, ACTIVE_STATUSES)) {
      throw new ConflictException("Batch already has an active conversion run.");
    }

    ConverterAdapter adapter = adapterRegistry.resolve(request.adapterKey());
    ConverterCapability capability = adapter.capability();
    OffsetDateTime now = OffsetDateTime.now(clock);
    ConversionRun run =
        conversionRunRepository.save(
            ConversionRun.create(
                runId(now),
                batchId,
                capability.adapterKey(),
                capability.version(),
                request.requestedBy(),
                now));
    List<FileItem> files = targetFiles(batchId, request.fileIds());
    if (capability.status() != ConverterAdapterStatus.AVAILABLE) {
      run.complete(ConversionRunStatus.FAILED, OffsetDateTime.now(clock), "Converter adapter is unavailable.");
      return response(conversionRunRepository.save(run));
    }

    run.markRunning();
    conversionRunRepository.save(run);
    ConverterResult adapterResult;
    try {
      adapterResult = adapter.convert(toAdapterRequest(run, files));
    } catch (RuntimeException ex) {
      run.complete(
          ConversionRunStatus.FAILED,
          OffsetDateTime.now(clock),
          safeAdapterFailureMessage(ex));
      return response(conversionRunRepository.save(run));
    }
    List<ConversionFileResult> persistedResults = persistResults(run, adapterResult, files);
    ConversionRunSummaryResponse summary = summaryCalculator.compute(persistedResults);
    run.complete(terminalStatus(summary), OffsetDateTime.now(clock), sanitize(adapterResult.safeMessage()));
    return response(conversionRunRepository.save(run));
  }

  /** Gets a conversion run report. */
  @Transactional(readOnly = true)
  public ConversionRunResponse getRun(String runId) {
    return response(findRun(runId));
  }

  private List<FileItem> targetFiles(String batchId, List<String> fileIds) {
    List<FileItem> files =
        fileIds == null || fileIds.isEmpty()
            ? fileItemRepository.findByBatchId(batchId)
            : fileItemRepository.findByBatchIdAndIdIn(batchId, fileIds);
    if (files.isEmpty()) {
      throw new RequestValidationException(Map.of("fileIds", "must reference files in the batch"));
    }
    if (fileIds != null && !fileIds.isEmpty() && files.size() != fileIds.size()) {
      throw new NotFoundException("File item not found.");
    }
    return files;
  }

  private ConverterRequest toAdapterRequest(ConversionRun run, List<FileItem> files) {
    List<ConverterRequest.ConverterFile> adapterFiles =
        files.stream()
            .map(
                file ->
                    new ConverterRequest.ConverterFile(
                        file.getId(), file.getSourcePath(), file.getSourceType(), file.getStatus()))
            .toList();
    return new ConverterRequest(run.getId(), run.getBatchId(), adapterFiles, "generated/pdf");
  }

  private List<ConversionFileResult> persistResults(
      ConversionRun run, ConverterResult adapterResult, List<FileItem> files) {
    Map<String, FileItem> filesById =
        files.stream().collect(Collectors.toMap(FileItem::getId, Function.identity(), (left, right) -> left));
    Map<String, String> validationErrors = new LinkedHashMap<>();
    List<ConversionFileResult> results =
        adapterResult.files().stream()
            .map(result -> toEntity(run, adapterResult.adapterKey(), result, filesById, validationErrors))
            .toList();
    if (!validationErrors.isEmpty()) {
      throw new RequestValidationException(validationErrors);
    }
    return results.stream().map(conversionFileResultRepository::save).toList();
  }

  private ConversionFileResult toEntity(
      ConversionRun run,
      String adapterKey,
      ConverterResult.ConverterFileResult result,
      Map<String, FileItem> filesById,
      Map<String, String> validationErrors) {
    FileItem file = filesById.get(result.fileId());
    if (file == null) {
      validationErrors.put("results." + result.fileId(), "must reference a target file");
      return null;
    }
    if (!CONVERSION_RESULT_STATUSES.contains(result.status())) {
      validationErrors.put("results." + result.fileId() + ".status", "must be a converter result status");
    }
    if (!relativePathValidator.isValid(result.pdfPath(), null)) {
      validationErrors.put("results." + result.fileId() + ".pdfPath", "must be a safe relative path");
    }
    String safeError = sanitize(result.safeError());
    BigDecimal confidence = scale(result.confidence());
    if (confidence != null
        && (confidence.compareTo(BigDecimal.ZERO) < 0 || confidence.compareTo(BigDecimal.ONE) > 0)) {
      validationErrors.put("results." + result.fileId() + ".confidence", "must be between 0 and 1");
    }
    file.applyConversionResult(result.status(), confidence, result.pdfPath(), safeError);
    fileItemRepository.save(file);
    return ConversionFileResult.create(
        "conv-result-" + UUID.randomUUID().toString().substring(0, 8),
        run.getId(),
        file.getId(),
        file.getSourcePath(),
        file.getSourceType(),
        result.status(),
        result.pdfPath(),
        confidence,
        adapterKey,
        safeError,
        OffsetDateTime.now(clock));
  }

  private ConversionRunResponse response(ConversionRun run) {
    List<ConversionFileResult> results =
        conversionFileResultRepository.findByRunIdOrderByCreatedAtAsc(run.getId());
    Map<String, FileItem> filesById =
        fileItemRepository.findAllById(results.stream().map(ConversionFileResult::getFileItemId).toList())
            .stream()
            .collect(Collectors.toMap(FileItem::getId, Function.identity()));
    return ConverterMapper.toResponse(run, summaryCalculator.compute(results), results, filesById);
  }

  private ConversionRun findRun(String runId) {
    return conversionRunRepository
        .findById(runId)
        .orElseThrow(() -> new NotFoundException("Conversion run not found."));
  }

  private ConversionRunStatus terminalStatus(ConversionRunSummaryResponse summary) {
    if (summary.total() == 0 || summary.pdfConverted() == 0) {
      return ConversionRunStatus.FAILED;
    }
    if (summary.pdfConvertFailed() > 0 || summary.ocrRequired() > 0 || summary.unsupported() > 0) {
      return ConversionRunStatus.PARTIAL_FAILED;
    }
    return ConversionRunStatus.SUCCEEDED;
  }

  private void validateMode(String mode) {
    if (mode == null || mode.isBlank() || "mock".equals(mode) || "configured".equals(mode)) {
      return;
    }
    throw new RequestValidationException(Map.of("mode", "must be mock or configured"));
  }

  private String sanitize(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String sanitized =
        value.replaceAll("(?i)(password|token|api[_-]?key)\\s*[:=]\\s*\\S+", "$1=[masked]")
            .replaceAll("[A-Za-z]:[/\\\\][^\\s]+", "[path]")
            .replaceAll("(/[^\\s]+){2,}", "[path]")
            .replaceAll("\\bat\\s+[\\w.$]+\\([^)]*\\)", "[stack]");
    return sanitized.length() <= MAX_SAFE_ERROR_LENGTH
        ? sanitized
        : sanitized.substring(0, MAX_SAFE_ERROR_LENGTH);
  }

  private String safeAdapterFailureMessage(RuntimeException ex) {
    String safeMessage = sanitize(ex.getMessage());
    return safeMessage == null ? "Converter adapter failed." : safeMessage;
  }

  private BigDecimal scale(BigDecimal value) {
    return value == null ? null : value.setScale(3, RoundingMode.HALF_UP);
  }

  private String runId(OffsetDateTime now) {
    return "conv-run-"
        + now.format(DateTimeFormatter.ISO_LOCAL_DATE)
        + "-"
        + UUID.randomUUID().toString().substring(0, 8);
  }
}
