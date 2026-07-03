package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.ModelOutputKind;
import com.atlas.metadata.enums.ModelAdapterStatus;
import com.atlas.metadata.enums.ModelOperation;
import com.atlas.metadata.enums.ModelType;
import com.atlas.metadata.enums.ReviewStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Configured provider adapter for opt-in DeepSeek chat execution. */
@Component
public class ConfiguredModelAdapter implements ModelAdapter {

  private static final String ADAPTER_KEY = "deepseek";
  private static final String DEFAULT_ENDPOINT = "https://api.deepseek.com";
  private static final String DEFAULT_MODEL = "deepseek-chat";
  private static final int CONTEXT_LIMIT = 8192;
  private static final int MAX_SAFE_SUMMARY_LENGTH = 500;
  private static final int REQUEST_TIMEOUT_SECONDS = 45;

  private final ModelEnvironment environment;
  private final DeepSeekChatClient client;

  /** Creates the configured adapter placeholder. */
  public ConfiguredModelAdapter() {
    this(System.getenv(), new HttpDeepSeekChatClient());
  }

  /** Visible for contract tests that verify raw configuration never leaks. */
  public ConfiguredModelAdapter(String configuredEndpoint) {
    this(
        Map.of(
            "ATLAS_MODEL_PROVIDER",
            "deepseek",
            "ATLAS_MODEL_ENDPOINT",
            configuredEndpoint == null ? "" : configuredEndpoint),
        request -> {
          throw new IllegalStateException("Provider execution is disabled for this test adapter.");
        });
  }

  /** Visible for contract tests with a fake provider client. */
  public ConfiguredModelAdapter(Map<String, String> environment, DeepSeekChatClient client) {
    this.environment = ModelEnvironment.from(environment);
    this.client = client;
  }

  @Override
  public List<ModelCapability> capabilities() {
    return List.of(
        new ModelCapability(
            ADAPTER_KEY,
            environment.modelName(),
            "DeepSeek Chat",
            "deepseek",
            ModelType.CHAT,
            List.of(ModelOperation.CHAT),
            true,
            environment.available() ? ModelAdapterStatus.AVAILABLE : ModelAdapterStatus.MISCONFIGURED,
            CONTEXT_LIMIT,
            Map.of(
                "provider",
                environment.providerConfigured() ? "deepseek" : "missing",
                "credential",
                environment.apiKeyConfigured() ? "configured" : "missing",
                "endpoint",
                environment.endpointConfigured() ? "configured" : "missing",
                "externalNetwork",
                environment.available() ? "enabled" : "disabled")));
  }

  @Override
  public ModelResult execute(ModelRequest request) {
    if (!environment.available()) {
      throw new IllegalStateException("DeepSeek model adapter is not configured.");
    }
    if (request.operationType() != ModelOperation.CHAT) {
      throw new IllegalStateException("DeepSeek model adapter only supports chat.");
    }
    if (!"configured".equals(request.mode())) {
      throw new IllegalStateException("DeepSeek model adapter requires configured mode.");
    }
    try {
      DeepSeekChatResponse response = client.complete(DeepSeekChatRequest.from(environment, request));
      String safeSummary = sanitize(response.answer(), MAX_SAFE_SUMMARY_LENGTH);
      if (safeSummary == null || safeSummary.isBlank()) {
        throw new IllegalStateException("Provider returned an empty chat response.");
      }
      return new ModelResult(
          ADAPTER_KEY,
          request.modelKey(),
          "Provider chat operation completed.",
          new ModelResult.ModelUsage(response.promptUnits(), response.completionUnits()),
          List.of(
              new ModelResult.ModelOutput(
                  request.runId() + "-output-001",
                  ModelOutputKind.TEXT_SUMMARY,
                  "generated/model/" + request.runId() + "-deepseek-chat.json",
                  safeSummary,
                  List.of(),
                  null,
                  null,
                  new BigDecimal("0.720"),
                  ReviewStatus.REVIEW_REQUIRED,
                  null)));
    } catch (IOException ex) {
      throw new IllegalStateException("Provider network call failed safely.");
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Provider network call timed out or was interrupted.");
    }
  }

  private static String sanitize(String value, int maxLength) {
    if (value == null) {
      return null;
    }
    String safe =
        value
            .replaceAll("(?i)(password|token|api[_-]?key|bearer)\\s*[:=]\\s*\\S+", "$1=[masked]")
            .replaceAll("(?i)\\b[a-z][a-z0-9+.-]*://[^\\s]+", "[endpoint]")
            .replaceAll("[A-Za-z]:[/\\\\][^\\s]+", "[path]")
            .replaceAll("(/[^\\s]+){2,}", "[path]");
    if (safe.length() <= maxLength) {
      return safe;
    }
    return safe.substring(0, maxLength);
  }

  /** Provider client boundary for DeepSeek chat completion calls. */
  public interface DeepSeekChatClient {
    DeepSeekChatResponse complete(DeepSeekChatRequest request) throws IOException, InterruptedException;
  }

  /** Safe request shape passed from the adapter to its provider client. */
  public record DeepSeekChatRequest(
      String endpoint,
      String apiKey,
      String modelName,
      String systemPrompt,
      String userPrompt) {

    static DeepSeekChatRequest from(ModelEnvironment environment, ModelRequest request) {
      return new DeepSeekChatRequest(
          environment.endpoint(),
          environment.apiKey(),
          environment.modelName(),
          "You are Atlas Knowledge Hub. Answer only from the provided Atlas evidence references. "
              + "Do not invent source details. Keep the answer concise.",
          userPrompt(request));
    }

    private static String userPrompt(ModelRequest request) {
      StringBuilder prompt = new StringBuilder();
      prompt.append("Purpose: ").append(nullToEmpty(request.purpose())).append('\n');
      prompt.append("Input reference: ").append(nullToEmpty(request.inputReference())).append('\n');
      prompt.append("Instruction: ").append(nullToEmpty(request.safeMockInput())).append('\n');
      prompt.append("Evidence references:\n");
      List<ModelRequest.ModelSourceReference> references =
          request.sourceReferences() == null ? List.of() : request.sourceReferences();
      for (ModelRequest.ModelSourceReference reference : references) {
        prompt
            .append("- ")
            .append(reference.refType())
            .append(" ")
            .append(reference.refId())
            .append(" label=")
            .append(nullToEmpty(reference.label()))
            .append(" reviewStatus=")
            .append(reference.reviewStatus())
            .append(" confidence=")
            .append(reference.confidence())
            .append('\n');
      }
      return prompt.toString();
    }

    private static String nullToEmpty(String value) {
      return value == null ? "" : value;
    }
  }

  /** Sanitized provider response used by the adapter. */
  public record DeepSeekChatResponse(String answer, int promptUnits, int completionUnits) {}

  private record ModelEnvironment(
      String provider, String endpoint, String apiKey, String modelName) {

    static ModelEnvironment from(Map<String, String> values) {
      String provider = value(values, "ATLAS_MODEL_PROVIDER", "");
      String endpoint = value(values, "ATLAS_MODEL_ENDPOINT", DEFAULT_ENDPOINT);
      String apiKey = value(values, "ATLAS_MODEL_API_KEY", "");
      String modelName = value(values, "ATLAS_MODEL_NAME", DEFAULT_MODEL);
      return new ModelEnvironment(provider, trimTrailingSlash(endpoint), apiKey, modelName);
    }

    boolean available() {
      return providerConfigured() && endpointConfigured() && apiKeyConfigured();
    }

    boolean providerConfigured() {
      return "deepseek".equalsIgnoreCase(provider);
    }

    boolean endpointConfigured() {
      return endpoint != null && !endpoint.isBlank();
    }

    boolean apiKeyConfigured() {
      return apiKey != null && !apiKey.isBlank();
    }

    private static String value(Map<String, String> values, String key, String defaultValue) {
      String value = values == null ? null : values.get(key);
      return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private static String trimTrailingSlash(String value) {
      String trimmed = value == null ? "" : value.trim();
      return trimmed.replaceAll("/+$", "");
    }
  }

  private static class HttpDeepSeekChatClient implements DeepSeekChatClient {

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public DeepSeekChatResponse complete(DeepSeekChatRequest request)
        throws IOException, InterruptedException {
      String requestBody = requestBody(request);
      HttpRequest httpRequest =
          HttpRequest.newBuilder()
              .uri(URI.create(request.endpoint() + "/chat/completions"))
              .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
              .header("Content-Type", "application/json")
              .header("Authorization", "Bearer " + request.apiKey())
              .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .build();
      HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 429) {
        throw new IllegalStateException("Provider rate limit reached.");
      }
      if (response.statusCode() >= 500) {
        throw new IllegalStateException("Provider service is unavailable.");
      }
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new IllegalStateException("Provider rejected the chat request.");
      }
      return responseBody(response.body());
    }

    private String requestBody(DeepSeekChatRequest request) throws IOException {
      ObjectNode body = objectMapper.createObjectNode();
      body.put("model", request.modelName());
      body.put("stream", false);
      body.put("temperature", 0.1);
      ArrayNode messages = body.putArray("messages");
      messages.addObject().put("role", "system").put("content", request.systemPrompt());
      messages.addObject().put("role", "user").put("content", request.userPrompt());
      return objectMapper.writeValueAsString(body);
    }

    private DeepSeekChatResponse responseBody(String body) throws IOException {
      JsonNode root = objectMapper.readTree(body);
      String answer = root.path("choices").path(0).path("message").path("content").asText("");
      if (answer.isBlank()) {
        throw new IllegalStateException("Provider returned malformed chat response.");
      }
      int promptUnits = root.path("usage").path("prompt_tokens").asInt(0);
      int completionUnits = root.path("usage").path("completion_tokens").asInt(0);
      return new DeepSeekChatResponse(answer, Math.max(promptUnits, 0), Math.max(completionUnits, 0));
    }
  }
}
