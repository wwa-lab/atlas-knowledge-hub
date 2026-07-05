package com.atlas.metadata.adapter;

import com.atlas.metadata.adapter.runtime.RuntimeAdapterConfiguration;
import com.atlas.metadata.adapter.runtime.RuntimeExecutionResult;
import com.atlas.metadata.adapter.runtime.RuntimeExecutor;
import com.atlas.metadata.adapter.runtime.RuntimeInvocation;
import com.atlas.metadata.adapter.runtime.RuntimeOutputSanitizer;
import com.atlas.metadata.enums.ConverterAdapterStatus;
import com.atlas.metadata.enums.FileStatus;
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

/** Optional real trinity-office boundary, enabled only through configured runtime mode. */
@Component
public class TrinityOfficeConverterAdapter implements ConverterAdapter {

  private static final String CONFIGURED = "configured";
  private static final String MISSING = "missing";
  private static final String DISABLED = "disabled";

  private final RuntimeAdapterConfiguration configuration;
  private final RuntimeExecutor runtimeExecutor;
  private final ObjectMapper objectMapper;
  private final RelativePathValidator relativePathValidator = new RelativePathValidator();

  /** Creates the Spring-configured runtime adapter. */
  @Autowired
  public TrinityOfficeConverterAdapter(
      RuntimeExecutor runtimeExecutor,
      ObjectMapper objectMapper,
      @Value("${atlas.runtime.trinity-office.enabled:false}") boolean enabled,
      @Value("${atlas.runtime.trinity-office.command:}") String command) {
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
  public TrinityOfficeConverterAdapter(String commandStatus) {
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
  public TrinityOfficeConverterAdapter(
      RuntimeAdapterConfiguration configuration,
      RuntimeExecutor runtimeExecutor,
      ObjectMapper objectMapper) {
    this.configuration = configuration;
    this.runtimeExecutor = runtimeExecutor;
    this.objectMapper = objectMapper;
  }

  @Override
  public ConverterCapability capability() {
    return new ConverterCapability(
        MockTrinityOfficeConverterAdapter.ADAPTER_KEY,
        "Trinity Office Converter",
        CONFIGURED,
        "pdf",
        List.of(SourceType.pptx, SourceType.docx, SourceType.xlsx, SourceType.pdf),
        false,
        status(),
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
  public ConverterResult convert(ConverterRequest request) {
    if (status() != ConverterAdapterStatus.AVAILABLE) {
      return failureResult(request, "Converter runtime is " + commandState() + ".");
    }
    RuntimeExecutionResult execution = runtimeExecutor.execute(invocation(request));
    if (execution.timedOut() || execution.exitCode() != 0) {
      String message =
          execution.timedOut()
              ? "Converter runtime timed out. " + diagnostic(execution)
              : "Converter runtime failed. " + diagnostic(execution);
      return failureResult(request, message);
    }
    try {
      return fromRuntimeJson(request, execution.stdout());
    } catch (RuntimeException ex) {
      return failureResult(request, "Converter runtime returned invalid output. " + ex.getMessage());
    }
  }

  private ConverterAdapterStatus status() {
    if (!configuration.enabled()) {
      return ConverterAdapterStatus.DISABLED;
    }
    return configuration.commandConfigured()
        ? ConverterAdapterStatus.AVAILABLE
        : ConverterAdapterStatus.MISCONFIGURED;
  }

  private String commandState() {
    if (!configuration.enabled()) {
      return DISABLED;
    }
    return configuration.commandConfigured() ? CONFIGURED : MISSING;
  }

  private RuntimeInvocation invocation(ConverterRequest request) {
    return new RuntimeInvocation(
        configuration.command(),
        List.of("--manifest-stdin"),
        manifest(request),
        configuration.timeout(),
        configuration.capturedOutputLimit());
  }

  private String manifest(ConverterRequest request) {
    ObjectNode root = objectMapper.createObjectNode();
    root.put("runId", request.runId());
    root.put("batchId", request.batchId());
    root.put("artifactRoot", request.artifactRoot());
    ArrayNode files = root.putArray("files");
    for (ConverterRequest.ConverterFile file : request.files()) {
      ObjectNode node = files.addObject();
      node.put("fileId", file.fileId());
      node.put("sourcePath", file.sourcePath());
      node.put("sourceType", file.sourceType().name());
      node.put("currentStatus", file.currentStatus().name());
    }
    return root.toString();
  }

  private ConverterResult fromRuntimeJson(ConverterRequest request, String stdout) {
    JsonNode root = readTree(stdout);
    Map<String, JsonNode> byFileId = new LinkedHashMap<>();
    JsonNode files = root.path("files");
    if (files.isArray()) {
      files.forEach(node -> byFileId.put(text(node, "fileId"), node));
    }
    Map<String, ConverterRequest.ConverterFile> requested =
        request.files().stream()
            .collect(Collectors.toMap(ConverterRequest.ConverterFile::fileId, Function.identity()));
    List<ConverterResult.ConverterFileResult> results =
        requested.values().stream()
            .map(file -> fromRuntimeFile(file, byFileId.get(file.fileId())))
            .toList();
    return new ConverterResult(
        MockTrinityOfficeConverterAdapter.ADAPTER_KEY,
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

  private ConverterResult.ConverterFileResult fromRuntimeFile(
      ConverterRequest.ConverterFile file, JsonNode node) {
    if (node == null || node.isMissingNode()) {
      if (file.sourceType() == SourceType.pdf) {
        return new ConverterResult.ConverterFileResult(
            file.fileId(), FileStatus.PDF_CONVERTED, file.sourcePath(), BigDecimal.ONE, null);
      }
      return failure(file, "Runtime output missing file result.");
    }
    FileStatus status = fileStatus(text(node, "status"), failureStatus(file));
    String pdfPath = text(node, "pdfPath");
    BigDecimal confidence = decimal(node.get("confidence"));
    String safeError = sanitize(text(node, "safeError"));
    if (!validConfidence(confidence)) {
      return failure(file, "Runtime output confidence rejected.");
    }
    if (status == FileStatus.PDF_CONVERTED && !relativePathValidator.isValid(pdfPath, null)) {
      return failure(file, "Runtime output path rejected.");
    }
    return new ConverterResult.ConverterFileResult(file.fileId(), status, pdfPath, confidence, safeError);
  }

  private ConverterResult failureResult(ConverterRequest request, String rawMessage) {
    String safeMessage = sanitize(rawMessage);
    List<ConverterResult.ConverterFileResult> results =
        request.files().stream().map(file -> failure(file, safeMessage)).toList();
    return new ConverterResult(MockTrinityOfficeConverterAdapter.ADAPTER_KEY, results, safeMessage);
  }

  private ConverterResult.ConverterFileResult failure(ConverterRequest.ConverterFile file, String safeError) {
    return new ConverterResult.ConverterFileResult(
        file.fileId(), failureStatus(file), null, BigDecimal.ZERO, sanitize(safeError));
  }

  private FileStatus failureStatus(ConverterRequest.ConverterFile file) {
    return switch (file.sourceType()) {
      case image -> FileStatus.OCR_REQUIRED;
      case unsupported -> FileStatus.UNSUPPORTED;
      default -> FileStatus.PDF_CONVERT_FAILED;
    };
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

  private BigDecimal decimal(JsonNode node) {
    if (node == null || node.isNull() || !node.isNumber()) {
      return null;
    }
    return node.decimalValue();
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
