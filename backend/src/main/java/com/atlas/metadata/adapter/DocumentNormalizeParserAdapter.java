package com.atlas.metadata.adapter;

import com.atlas.metadata.adapter.runtime.RuntimeAdapterConfiguration;
import com.atlas.metadata.adapter.runtime.RuntimeExecutionResult;
import com.atlas.metadata.adapter.runtime.RuntimeExecutor;
import com.atlas.metadata.adapter.runtime.RuntimeInvocation;
import com.atlas.metadata.adapter.runtime.RuntimeOutputSanitizer;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ParserAdapterStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceType;
import com.atlas.metadata.validation.RelativePathValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Optional real document-normalize boundary, enabled only through configured runtime mode. */
@Component
public class DocumentNormalizeParserAdapter implements ParserAdapter {

  private static final String CONFIGURED = "configured";
  private static final String MISSING = "missing";
  private static final String DISABLED = "disabled";

  private final RuntimeAdapterConfiguration configuration;
  private final RuntimeExecutor runtimeExecutor;
  private final ObjectMapper objectMapper;
  private final RelativePathValidator relativePathValidator = new RelativePathValidator();

  /** Creates the Spring-configured runtime adapter. */
  @Autowired
  public DocumentNormalizeParserAdapter(
      RuntimeExecutor runtimeExecutor,
      ObjectMapper objectMapper,
      @Value("${atlas.runtime.document-normalize.enabled:false}") boolean enabled,
      @Value("${atlas.runtime.document-normalize.command:}") String command) {
    this(
        new RuntimeAdapterConfiguration(
            enabled,
            command,
            RuntimeAdapterConfiguration.DEFAULT_TIMEOUT,
            RuntimeAdapterConfiguration.DEFAULT_CAPTURED_OUTPUT_LIMIT),
        runtimeExecutor,
        objectMapper);
  }

  /** Visible for older contract tests that verify raw configuration never leaks. */
  public DocumentNormalizeParserAdapter(String commandStatus) {
    this(
        new RuntimeAdapterConfiguration(
            true,
            CONFIGURED.equals(commandStatus) ? "configured-command" : "",
            Duration.ofSeconds(120),
            RuntimeAdapterConfiguration.DEFAULT_CAPTURED_OUTPUT_LIMIT),
        invocation -> new RuntimeExecutionResult(1, "", "Runtime execution is disabled for this test adapter.", false),
        new ObjectMapper());
  }

  /** Visible for unit tests with fake executors. */
  public DocumentNormalizeParserAdapter(
      RuntimeAdapterConfiguration configuration,
      RuntimeExecutor runtimeExecutor,
      ObjectMapper objectMapper) {
    this.configuration = configuration;
    this.runtimeExecutor = runtimeExecutor;
    this.objectMapper = objectMapper;
  }

  @Override
  public ParserCapability capability() {
    return new ParserCapability(
        MockDocumentNormalizeParserAdapter.ADAPTER_KEY,
        "Document Normalize Parser",
        CONFIGURED,
        List.of(SourceType.pdf),
        List.of("markdown", "assets"),
        false,
        status(),
        new BigDecimal("0.800"),
        Map.of(
            "command",
            commandState(),
            "timeout",
            CONFIGURED,
            "capturedOutput",
            CONFIGURED,
            "externalNetwork",
            DISABLED));
  }

  @Override
  public ParserResult parse(ParserRequest request) {
    if (status() != ParserAdapterStatus.AVAILABLE) {
      return failureResult(request, "Parser runtime is " + commandState() + ".");
    }
    RuntimeExecutionResult execution = runtimeExecutor.execute(invocation(request));
    if (execution.timedOut() || execution.exitCode() != 0) {
      String message =
          execution.timedOut()
              ? "Parser runtime timed out. " + diagnostic(execution)
              : "Parser runtime failed. " + diagnostic(execution);
      return failureResult(request, message);
    }
    try {
      return fromRuntimeJson(request, execution.stdout());
    } catch (RuntimeException ex) {
      return failureResult(request, "Parser runtime returned invalid output. " + ex.getMessage());
    }
  }

  private ParserAdapterStatus status() {
    if (!configuration.enabled()) {
      return ParserAdapterStatus.DISABLED;
    }
    return configuration.commandConfigured()
        ? ParserAdapterStatus.AVAILABLE
        : ParserAdapterStatus.MISCONFIGURED;
  }

  private String commandState() {
    if (!configuration.enabled()) {
      return DISABLED;
    }
    return configuration.commandConfigured() ? CONFIGURED : MISSING;
  }

  private RuntimeInvocation invocation(ParserRequest request) {
    return new RuntimeInvocation(
        configuration.command(),
        List.of("--manifest-stdin"),
        manifest(request),
        configuration.timeout(),
        configuration.capturedOutputLimit());
  }

  private String manifest(ParserRequest request) {
    ObjectNode root = objectMapper.createObjectNode();
    root.put("runId", request.runId());
    root.put("batchId", request.batchId());
    root.put("markdownRoot", request.markdownRoot());
    root.put("assetsRoot", request.assetsRoot());
    root.put("lowConfidenceThreshold", request.lowConfidenceThreshold());
    ArrayNode files = root.putArray("files");
    for (ParserRequest.ParserFile file : request.files()) {
      ObjectNode node = files.addObject();
      node.put("fileId", file.fileId());
      node.put("sourcePath", file.sourcePath());
      node.put("sourceType", file.sourceType().name());
      node.put("currentStatus", file.currentStatus().name());
      node.put("pdfPath", file.pdfPath());
    }
    return root.toString();
  }

  private ParserResult fromRuntimeJson(ParserRequest request, String stdout) {
    JsonNode root = readTree(stdout);
    Map<String, JsonNode> byFileId = new LinkedHashMap<>();
    JsonNode files = root.path("files");
    if (files.isArray()) {
      files.forEach(node -> byFileId.put(text(node, "fileId"), node));
    }
    Map<String, ParserRequest.ParserFile> requested =
        request.files().stream()
            .collect(Collectors.toMap(ParserRequest.ParserFile::fileId, Function.identity()));
    List<ParserResult.ParserFileResult> results =
        requested.values().stream()
            .map(file -> fromRuntimeFile(request, file, byFileId.get(file.fileId())))
            .toList();
    return new ParserResult(
        MockDocumentNormalizeParserAdapter.ADAPTER_KEY,
        results,
        sanitize(text(root, "safeMessage")));
  }

  private JsonNode readTree(String stdout) {
    try {
      return objectMapper.readTree(stdout == null || stdout.isBlank() ? "{}" : stdout);
    } catch (Exception ex) {
      throw new IllegalStateException("Runtime JSON could not be parsed.");
    }
  }

  private ParserResult.ParserFileResult fromRuntimeFile(
      ParserRequest request, ParserRequest.ParserFile file, JsonNode node) {
    if (node == null || node.isMissingNode()) {
      return failure(file, "Runtime output missing file result.");
    }
    FileStatus status = fileStatus(text(node, "status"), FileStatus.FAILED);
    BigDecimal confidence = decimal(node.get("confidence"));
    String markdownPath = text(node, "markdownPath");
    String assetsPath = text(node, "assetsPath");
    String safeError = sanitize(text(node, "safeError"));
    if (!validConfidence(confidence)) {
      return failure(file, "Runtime output confidence rejected.");
    }
    if (status == FileStatus.MARKDOWN_GENERATED
        && confidence != null
        && confidence.compareTo(request.lowConfidenceThreshold()) < 0) {
      status = FileStatus.LOW_CONFIDENCE;
    }
    if ((status == FileStatus.MARKDOWN_GENERATED || status == FileStatus.LOW_CONFIDENCE)
        && (!relativePathValidator.isValid(markdownPath, null)
            || !relativePathValidator.isValid(assetsPath, null))) {
      return failure(file, "Runtime output path rejected.");
    }
    return new ParserResult.ParserFileResult(
        file.fileId(),
        status,
        markdownPath,
        assetsPath,
        confidence,
        safeError,
        chunks(node.path("chunks")));
  }

  private List<ParserResult.ParserChunkResult> chunks(JsonNode chunks) {
    if (!chunks.isArray()) {
      return List.of();
    }
    return java.util.stream.StreamSupport.stream(chunks.spliterator(), false)
        .map(this::chunk)
        .toList();
  }

  private ParserResult.ParserChunkResult chunk(JsonNode node) {
    return new ParserResult.ParserChunkResult(
        text(node, "chunkId"),
        integer(node.get("page")),
        sanitize(text(node, "section")),
        decimal(node.get("confidence")),
        reviewStatus(text(node, "reviewStatus")));
  }

  private ParserResult failureResult(ParserRequest request, String rawMessage) {
    String safeMessage = sanitize(rawMessage);
    List<ParserResult.ParserFileResult> results =
        request.files().stream().map(file -> failure(file, safeMessage)).toList();
    return new ParserResult(MockDocumentNormalizeParserAdapter.ADAPTER_KEY, results, safeMessage);
  }

  private ParserResult.ParserFileResult failure(ParserRequest.ParserFile file, String safeError) {
    return new ParserResult.ParserFileResult(
        file.fileId(), FileStatus.FAILED, null, null, BigDecimal.ZERO, sanitize(safeError), List.of());
  }

  private FileStatus fileStatus(String value, FileStatus fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    try {
      return FileStatus.valueOf(value);
    } catch (IllegalArgumentException ex) {
      return fallback;
    }
  }

  private ReviewStatus reviewStatus(String value) {
    if (value == null || value.isBlank()) {
      return ReviewStatus.REVIEW_REQUIRED;
    }
    try {
      return ReviewStatus.valueOf(value);
    } catch (IllegalArgumentException ex) {
      return ReviewStatus.REVIEW_REQUIRED;
    }
  }

  private BigDecimal decimal(JsonNode node) {
    if (node == null || node.isNull() || !node.isNumber()) {
      return null;
    }
    return node.decimalValue();
  }

  private Integer integer(JsonNode node) {
    return node == null || node.isNull() || !node.canConvertToInt() ? null : node.intValue();
  }

  private boolean validConfidence(BigDecimal confidence) {
    return confidence == null
        || (confidence.compareTo(BigDecimal.ZERO) >= 0 && confidence.compareTo(BigDecimal.ONE) <= 0);
  }

  private String text(JsonNode node, String field) {
    JsonNode value = node == null ? null : node.get(field);
    return value == null || value.isNull() ? null : value.asText();
  }

  private String diagnostic(RuntimeExecutionResult execution) {
    String safe = sanitize(execution.diagnosticOutput());
    return safe == null ? "" : safe;
  }

  private String sanitize(String value) {
    return RuntimeOutputSanitizer.sanitize(value, configuration.capturedOutputLimit());
  }
}
