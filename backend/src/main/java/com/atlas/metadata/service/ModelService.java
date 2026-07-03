package com.atlas.metadata.service;

import com.atlas.metadata.adapter.ModelCapability;
import com.atlas.metadata.adapter.ModelRequest;
import com.atlas.metadata.adapter.ModelResult;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.ModelRun;
import com.atlas.metadata.domain.ModelRunOutput;
import com.atlas.metadata.domain.ModelRunSourceReference;
import com.atlas.metadata.domain.SourceChunk;
import com.atlas.metadata.dto.CreateModelRunRequest;
import com.atlas.metadata.dto.ModelCapabilityResponse;
import com.atlas.metadata.dto.ModelRunResponse;
import com.atlas.metadata.dto.ModelSourceReferenceRequest;
import com.atlas.metadata.dto.ModelUsageResponse;
import com.atlas.metadata.dto.mapping.ModelMapper;
import com.atlas.metadata.enums.ModelAdapterStatus;
import com.atlas.metadata.enums.ModelOperation;
import com.atlas.metadata.enums.ModelOutputKind;
import com.atlas.metadata.enums.ModelRunStatus;
import com.atlas.metadata.enums.ModelSourceReferenceType;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.exception.NotFoundException;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.ModelRunOutputRepository;
import com.atlas.metadata.repository.ModelRunRepository;
import com.atlas.metadata.repository.ModelRunSourceReferenceRepository;
import com.atlas.metadata.repository.SourceChunkRepository;
import com.atlas.metadata.validation.RelativePathValidator;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for safe model adapter runs. */
@Service
public class ModelService {

  private static final int MAX_SAFE_INPUT_LENGTH = 1000;
  private static final int MAX_SAFE_MESSAGE_LENGTH = 240;
  private static final int MAX_SAFE_SUMMARY_LENGTH = 500;

  private final ModelRunRepository modelRunRepository;
  private final ModelRunOutputRepository modelRunOutputRepository;
  private final ModelRunSourceReferenceRepository sourceReferenceRepository;
  private final FileItemRepository fileItemRepository;
  private final SourceChunkRepository sourceChunkRepository;
  private final ModelAdapterRegistry adapterRegistry;
  private final ModelSummaryCalculator summaryCalculator;
  private final RelativePathValidator relativePathValidator;
  private final Clock clock;

  /** Creates the service. */
  @Autowired
  public ModelService(
      ModelRunRepository modelRunRepository,
      ModelRunOutputRepository modelRunOutputRepository,
      ModelRunSourceReferenceRepository sourceReferenceRepository,
      FileItemRepository fileItemRepository,
      SourceChunkRepository sourceChunkRepository,
      ModelAdapterRegistry adapterRegistry) {
    this(
        modelRunRepository,
        modelRunOutputRepository,
        sourceReferenceRepository,
        fileItemRepository,
        sourceChunkRepository,
        adapterRegistry,
        new ModelSummaryCalculator(),
        Clock.systemUTC());
  }

  ModelService(
      ModelRunRepository modelRunRepository,
      ModelRunOutputRepository modelRunOutputRepository,
      ModelRunSourceReferenceRepository sourceReferenceRepository,
      FileItemRepository fileItemRepository,
      SourceChunkRepository sourceChunkRepository,
      ModelAdapterRegistry adapterRegistry,
      ModelSummaryCalculator summaryCalculator,
      Clock clock) {
    this.modelRunRepository = modelRunRepository;
    this.modelRunOutputRepository = modelRunOutputRepository;
    this.sourceReferenceRepository = sourceReferenceRepository;
    this.fileItemRepository = fileItemRepository;
    this.sourceChunkRepository = sourceChunkRepository;
    this.adapterRegistry = adapterRegistry;
    this.summaryCalculator = summaryCalculator;
    this.relativePathValidator = new RelativePathValidator();
    this.clock = clock;
  }

  /** Lists model capabilities with masked configuration. */
  @Transactional(readOnly = true)
  public List<ModelCapabilityResponse> listCapabilities() {
    return adapterRegistry.capabilities().stream().map(ModelMapper::toResponse).toList();
  }

  /** Creates and executes a model run through the selected adapter. */
  @Transactional
  public ModelRunResponse createRun(CreateModelRunRequest request) {
    validateRequestShape(request);
    ModelAdapterRegistry.ResolvedModelAdapter resolved =
        adapterRegistry.resolve(request.adapterKey(), request.modelKey(), request.operationType());
    ModelCapability capability = resolved.capability();
    List<PreparedSourceReference> sourceReferences = prepareSourceReferences(request.sourceReferences());
    OffsetDateTime now = OffsetDateTime.now(clock);
    ModelRun run =
        modelRunRepository.save(
            ModelRun.create(
                runId(now),
                capability.adapterKey(),
                capability.modelKey(),
                capability.modelType(),
                request.operationType(),
                effectiveMode(request.mode()),
                request.purpose().trim(),
                blankToNull(request.requestedBy()),
                blankToNull(request.inputReference()),
                safeInputSummary(request.safeMockInput(), request.inputReference()),
                now));
    persistSourceReferences(run, sourceReferences);

    if (capability.status() != ModelAdapterStatus.AVAILABLE) {
      run.complete(ModelRunStatus.FAILED, 0, 0, OffsetDateTime.now(clock), "Model adapter is unavailable.");
      return response(modelRunRepository.save(run));
    }

    run.markRunning();
    modelRunRepository.save(run);
    ModelResult adapterResult;
    try {
      adapterResult =
          resolved.adapter().execute(toAdapterRequest(run, request, sourceReferences));
    } catch (RuntimeException ex) {
      run.complete(ModelRunStatus.FAILED, 0, 0, OffsetDateTime.now(clock), safeAdapterFailureMessage(ex));
      return response(modelRunRepository.save(run));
    }

    List<ModelRunOutput> outputs = persistOutputs(run, adapterResult);
    ModelResult.ModelUsage usage = adapterResult.usage() == null ? new ModelResult.ModelUsage(0, 0) : adapterResult.usage();
    run.complete(
        terminalStatus(outputs),
        usage.promptUnits(),
        usage.completionUnits(),
        OffsetDateTime.now(clock),
        sanitize(adapterResult.safeMessage(), MAX_SAFE_MESSAGE_LENGTH));
    return response(modelRunRepository.save(run));
  }

  /** Returns a previously recorded model run. */
  @Transactional(readOnly = true)
  public ModelRunResponse getRun(String runId) {
    return response(findRun(runId));
  }

  private void validateRequestShape(CreateModelRunRequest request) {
    Map<String, String> errors = new LinkedHashMap<>();
    if (request == null) {
      throw new RequestValidationException(Map.of("body", "must be provided"));
    }
    if (request.operationType() == null) {
      errors.put("operationType", "must be provided");
    }
    if (!isAllowedMode(request.mode())) {
      errors.put("mode", "must be mock or configured");
    }
    if (isBlank(request.purpose())) {
      errors.put("purpose", "must be provided");
    }
    if (isBlank(request.safeMockInput()) && isBlank(request.inputReference())) {
      errors.put("inputReference", "must be provided when safeMockInput is blank");
    }
    if (!isBlank(request.inputReference()) && !isSafeProductReference(request.inputReference())) {
      errors.put("inputReference", "must be a safe product reference");
    }
    if (!isBlank(request.safeMockInput()) && !isSafeText(request.safeMockInput(), MAX_SAFE_INPUT_LENGTH)) {
      errors.put("safeMockInput", "must be bounded safe mock text without secrets, endpoints, or private paths");
    }
    if (!errors.isEmpty()) {
      throw new RequestValidationException(errors);
    }
  }

  private List<PreparedSourceReference> prepareSourceReferences(
      List<ModelSourceReferenceRequest> references) {
    List<PreparedSourceReference> prepared = new ArrayList<>();
    Map<String, String> errors = new LinkedHashMap<>();
    if (references == null) {
      return prepared;
    }
    HashSet<String> seen = new HashSet<>();
    int index = 0;
    for (ModelSourceReferenceRequest reference : references) {
      String prefix = "sourceReferences[" + index + "]";
      if (reference == null || reference.refType() == null || isBlank(reference.refId())) {
        errors.put(prefix, "must include refType and refId");
      } else if (!isSafeProductReference(reference.refId())) {
        errors.put(prefix + ".refId", "must be a safe product reference id");
      } else if (!seen.add(reference.refType() + ":" + reference.refId())) {
        errors.put(prefix + ".refId", "must be unique within the run");
      } else {
        prepared.add(resolveReference(reference, prefix, errors));
      }
      index++;
    }
    if (!errors.isEmpty()) {
      throw new RequestValidationException(errors);
    }
    return prepared;
  }

  private PreparedSourceReference resolveReference(
      ModelSourceReferenceRequest reference, String prefix, Map<String, String> errors) {
    String label = sanitize(blankToNull(reference.label()), MAX_SAFE_MESSAGE_LENGTH);
    if (reference.refType() == ModelSourceReferenceType.SOURCE_CHUNK) {
      SourceChunk chunk = sourceChunkRepository.findById(reference.refId()).orElse(null);
      if (chunk == null) {
        errors.put(prefix + ".refId", "must reference an existing source chunk");
        return PreparedSourceReference.empty(reference, label);
      }
      return new PreparedSourceReference(
          reference.refType(),
          reference.refId(),
          label == null ? chunk.getSourceFile() : label,
          chunk.getConfidence(),
          chunk.getReviewStatus());
    }
    if (reference.refType() == ModelSourceReferenceType.FILE_ITEM) {
      FileItem file = fileItemRepository.findById(reference.refId()).orElse(null);
      if (file == null) {
        errors.put(prefix + ".refId", "must reference an existing file item");
        return PreparedSourceReference.empty(reference, label);
      }
      return new PreparedSourceReference(
          reference.refType(),
          reference.refId(),
          label == null ? file.getSourcePath() : label,
          file.getConfidence(),
          file.getReviewStatus());
    }
    return new PreparedSourceReference(
        reference.refType(), reference.refId(), label, null, ReviewStatus.REVIEW_REQUIRED);
  }

  private void persistSourceReferences(ModelRun run, List<PreparedSourceReference> references) {
    int index = 1;
    for (PreparedSourceReference reference : references) {
      sourceReferenceRepository.save(
          ModelRunSourceReference.create(
              run.getId() + "-source-" + String.format("%03d", index++),
              run.getId(),
              reference.refType(),
              reference.refId(),
              reference.label(),
              scale(reference.confidence()),
              reference.reviewStatus()));
    }
  }

  private ModelRequest toAdapterRequest(
      ModelRun run, CreateModelRunRequest request, List<PreparedSourceReference> sourceReferences) {
    return new ModelRequest(
        run.getId(),
        run.getOperation(),
        run.getModelKey(),
        run.getMode(),
        run.getPurpose(),
        run.getInputReference(),
        sanitize(blankToNull(request.safeMockInput()), MAX_SAFE_INPUT_LENGTH),
        sourceReferences.stream()
            .map(
                reference ->
                    new ModelRequest.ModelSourceReference(
                        reference.refType(),
                        reference.refId(),
                        reference.label(),
                        scale(reference.confidence()),
                        reference.reviewStatus()))
            .toList());
  }

  private List<ModelRunOutput> persistOutputs(ModelRun run, ModelResult result) {
    Map<String, String> errors = new LinkedHashMap<>();
    if (result == null) {
      throw new RequestValidationException(Map.of("adapterResult", "must be provided by the model adapter"));
    }
    if (!run.getAdapterKey().equals(result.adapterKey())) {
      errors.put("adapterKey", "must match the resolved model adapter");
    }
    if (!run.getModelKey().equals(result.modelKey())) {
      errors.put("modelKey", "must match the resolved model");
    }
    ModelResult.ModelUsage usage = result.usage() == null ? new ModelResult.ModelUsage(0, 0) : result.usage();
    if (usage.promptUnits() < 0 || usage.completionUnits() < 0) {
      errors.put("usage", "must use non-negative counts");
    }
    List<ModelResult.ModelOutput> outputs = result.outputs() == null ? List.of() : result.outputs();
    HashSet<String> ids = new HashSet<>();
    List<ModelRunOutput> prepared = new ArrayList<>();
    int index = 0;
    for (ModelResult.ModelOutput output : outputs) {
      prepared.add(toOutput(run, output, ids, index++, errors));
    }
    if (!errors.isEmpty()) {
      throw new RequestValidationException(errors);
    }
    return prepared.stream().map(modelRunOutputRepository::save).toList();
  }

  private ModelRunOutput toOutput(
      ModelRun run,
      ModelResult.ModelOutput output,
      HashSet<String> ids,
      int index,
      Map<String, String> errors) {
    String prefix = "outputs[" + index + "]";
    if (output == null) {
      errors.put(prefix, "must be a safe output descriptor");
      return null;
    }
    if (isBlank(output.outputId()) || !isSafeProductReference(output.outputId())) {
      errors.put(prefix + ".outputId", "must be a safe product reference id");
    } else if (!ids.add(output.outputId())) {
      errors.put(prefix + ".outputId", "must be unique");
    }
    if (output.kind() == null) {
      errors.put(prefix + ".kind", "must be provided");
    }
    if (!isBlank(output.outputReference()) && !relativePathValidator.isValid(output.outputReference(), null)) {
      errors.put(prefix + ".outputReference", "must be a safe relative output reference");
    }
    if (output.reviewStatus() != null && output.reviewStatus() != ReviewStatus.REVIEW_REQUIRED) {
      errors.put(prefix + ".reviewStatus", "must remain REVIEW_REQUIRED");
    }
    if (!isValidConfidence(output.confidence())) {
      errors.put(prefix + ".confidence", "must be between 0 and 1");
    }
    if (output.kind() == ModelOutputKind.EMBEDDING_METADATA) {
      if (output.embeddingDimension() == null || output.embeddingDimension() <= 0) {
        errors.put(prefix + ".embeddingDimension", "must be positive for embedding metadata");
      }
      if (output.embeddingItemCount() == null || output.embeddingItemCount() < 0) {
        errors.put(prefix + ".embeddingItemCount", "must be non-negative for embedding metadata");
      }
    }
    List<String> rankedIds = output.rankedItemIds() == null ? List.of() : output.rankedItemIds();
    for (String itemId : rankedIds) {
      if (!isSafeProductReference(itemId)) {
        errors.put(prefix + ".rankedItemIds", "must contain safe product reference ids");
      }
    }
    return ModelRunOutput.create(
        output.outputId(),
        run.getId(),
        output.kind(),
        blankToNull(output.outputReference()),
        sanitize(blankToNull(output.safeSummary()), MAX_SAFE_SUMMARY_LENGTH),
        rankedIds.toArray(String[]::new),
        output.embeddingDimension(),
        output.embeddingItemCount(),
        scale(output.confidence()),
        output.reviewStatus() == null ? ReviewStatus.REVIEW_REQUIRED : output.reviewStatus(),
        sanitize(blankToNull(output.safeError()), MAX_SAFE_MESSAGE_LENGTH),
        OffsetDateTime.now(clock));
  }

  private ModelRunStatus terminalStatus(List<ModelRunOutput> outputs) {
    if (outputs == null || outputs.isEmpty()) {
      return ModelRunStatus.FAILED;
    }
    long failed = outputs.stream().filter(output -> output.getKind() == ModelOutputKind.ERROR).count();
    if (failed == outputs.size()) {
      return ModelRunStatus.FAILED;
    }
    if (failed > 0) {
      return ModelRunStatus.PARTIAL_FAILED;
    }
    return ModelRunStatus.SUCCEEDED;
  }

  private ModelRunResponse response(ModelRun run) {
    List<ModelRunOutput> outputs = modelRunOutputRepository.findByRunIdOrderByIdAsc(run.getId());
    List<ModelRunSourceReference> references =
        sourceReferenceRepository.findByRunIdOrderByIdAsc(run.getId());
    ModelUsageResponse usage =
        summaryCalculator.compute(run.getPromptUnits(), run.getCompletionUnits(), outputs);
    return ModelMapper.toResponse(run, usage, outputs, references);
  }

  private ModelRun findRun(String runId) {
    return modelRunRepository.findById(runId).orElseThrow(() -> new NotFoundException("Model run not found."));
  }

  private String safeInputSummary(String safeMockInput, String inputReference) {
    if (!isBlank(inputReference)) {
      return "Input reference: " + sanitize(inputReference, MAX_SAFE_MESSAGE_LENGTH);
    }
    return sanitize(safeMockInput, MAX_SAFE_MESSAGE_LENGTH);
  }

  private String safeAdapterFailureMessage(RuntimeException ex) {
    return sanitize("Model adapter failed: " + ex.getMessage(), MAX_SAFE_MESSAGE_LENGTH);
  }

  private String sanitize(String value, int maxLength) {
    if (value == null) {
      return null;
    }
    String sanitized =
        value
            .replaceAll("(?i)(password|token|api[_-]?key)\\s*[:=]\\s*\\S+", "$1=[masked]")
            .replaceAll("(?i)\\b[a-z][a-z0-9+.-]*://[^\\s]+", "[endpoint]")
            .replaceAll("[A-Za-z]:[/\\\\][^\\s]+", "[path]")
            .replaceAll("(/[^\\s]+){2,}", "[path]")
            .replaceAll("\\bat\\s+[\\w.$]+\\([^)]*\\)", "[stack]")
            .replaceAll("(?i)\\b[a-z0-9][a-z0-9-]*(?:\\.[a-z0-9][a-z0-9-]*)+(?::\\d+)?\\b", "[host]");
    if (sanitized.length() <= maxLength) {
      return sanitized;
    }
    return sanitized.substring(0, maxLength);
  }

  private boolean isSafeText(String value, int maxLength) {
    if (isBlank(value) || value.length() > maxLength) {
      return false;
    }
    return sanitize(value, maxLength).equals(value);
  }

  private boolean isSafeProductReference(String value) {
    if (isBlank(value)) {
      return false;
    }
    return !value.startsWith("/")
        && !value.startsWith("\\")
        && !value.startsWith("//")
        && !value.contains("\\")
        && !value.contains("..")
        && !value.matches("^[A-Za-z]:.*")
        && !value.matches("(?i)^[a-z][a-z0-9+.-]*://.*")
        && value.length() <= 240;
  }

  private boolean isAllowedMode(String value) {
    return "mock".equals(value) || "configured".equals(value);
  }

  private String effectiveMode(String value) {
    return value == null ? "mock" : value;
  }

  private boolean isValidConfidence(BigDecimal value) {
    return value == null
        || (value.compareTo(BigDecimal.ZERO) >= 0 && value.compareTo(BigDecimal.ONE) <= 0);
  }

  private BigDecimal scale(BigDecimal value) {
    return value == null ? null : value.setScale(3, java.math.RoundingMode.HALF_UP);
  }

  private String runId(OffsetDateTime now) {
    return "model-run-"
        + DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(now)
        + "-"
        + UUID.randomUUID().toString().substring(0, 8);
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private String blankToNull(String value) {
    return isBlank(value) ? null : value.trim();
  }

  private record PreparedSourceReference(
      ModelSourceReferenceType refType,
      String refId,
      String label,
      BigDecimal confidence,
      ReviewStatus reviewStatus) {

    static PreparedSourceReference empty(ModelSourceReferenceRequest request, String label) {
      return new PreparedSourceReference(
          request.refType(), request.refId(), label, null, ReviewStatus.REVIEW_REQUIRED);
    }
  }
}
