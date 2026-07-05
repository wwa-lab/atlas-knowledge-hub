package com.atlas.metadata.service;

import com.atlas.metadata.adapter.ModelConfigurationProvider;
import com.atlas.metadata.dto.ModelConfigurationResponse;
import com.atlas.metadata.dto.SaveModelConfigurationRequest;
import com.atlas.metadata.exception.RequestValidationException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Service;

/** Runtime model configuration store for the user-runnable v1 closed loop. */
@Service
public class ModelRuntimeConfigurationService implements ModelConfigurationProvider {

  private static final String DEEPSEEK = "deepseek";
  private static final String DEFAULT_ENDPOINT = "";
  private static final String DEFAULT_MODEL = "deepseek-chat";

  private final Map<String, String> processEnvironment;
  private final AtomicReference<RuntimeModelConfiguration> runtimeConfiguration = new AtomicReference<>();

  /** Creates a runtime configuration service with process environment fallback. */
  public ModelRuntimeConfigurationService() {
    this(System.getenv());
  }

  /** Visible for unit tests. */
  ModelRuntimeConfigurationService(Map<String, String> processEnvironment) {
    this.processEnvironment = processEnvironment == null ? Map.of() : Map.copyOf(processEnvironment);
  }

  /** Reads masked runtime chat configuration state. */
  public ModelConfigurationResponse readChatConfiguration() {
    RuntimeModelConfiguration runtime = runtimeConfiguration.get();
    Map<String, String> effective = effectiveEnvironment();
    boolean hasRuntime = runtime != null;
    boolean hasCredential = hasValue(effective.get("ATLAS_MODEL_API_KEY"));
    boolean hasEndpoint = hasValue(effective.get("ATLAS_MODEL_ENDPOINT"));
    String credentialStatus =
        hasRuntime && hasCredential ? "CONFIGURED" : hasCredential ? "ENV_CONFIGURED" : "MISSING";
    String endpointStatus = hasEndpoint ? "CONFIGURED" : "MISSING";
    String mode = hasRuntime ? "runtime" : hasCredential ? "environment" : "missing";
    return new ModelConfigurationResponse(
        DEEPSEEK,
        DEEPSEEK,
        value(effective, "ATLAS_MODEL_NAME", DEFAULT_MODEL),
        credentialStatus,
        endpointStatus,
        mode,
        Map.of(
            "provider", DEEPSEEK,
            "credential", credentialStatus.toLowerCase().replace('_', '-'),
            "endpoint", endpointStatus.toLowerCase(),
            "externalNetwork", hasCredential && hasEndpoint ? "enabled" : "disabled"));
  }

  /** Saves runtime chat configuration and returns masked state. */
  public ModelConfigurationResponse saveChatConfiguration(SaveModelConfigurationRequest request) {
    Map<String, String> errors = new LinkedHashMap<>();
    String provider = normalizeProvider(request == null ? null : request.provider());
    if (provider.isBlank()) {
      provider = DEEPSEEK;
    }
    if (!DEEPSEEK.equals(provider)) {
      errors.put("provider", "must be deepseek for the v1 runtime configuration endpoint");
    }
    String endpoint = normalizeEndpoint(request == null ? null : request.endpoint(), errors);
    String modelName = value(request == null ? null : request.modelName(), DEFAULT_MODEL);
    String apiKey = request == null ? "" : trim(request.apiKey());
    if (apiKey.isBlank()) {
      apiKey = trim(effectiveEnvironment().get("ATLAS_MODEL_API_KEY"));
    }
    if (apiKey.isBlank()) {
      errors.put("apiKey", "must be provided; use DELETE to clear configuration");
    }
    if (!errors.isEmpty()) {
      throw new RequestValidationException(errors);
    }
    runtimeConfiguration.set(new RuntimeModelConfiguration(provider, endpoint, apiKey, modelName));
    return readChatConfiguration();
  }

  /** Clears runtime configuration and returns masked fallback state. */
  public ModelConfigurationResponse clearChatConfiguration() {
    runtimeConfiguration.set(null);
    return readChatConfiguration();
  }

  @Override
  public Map<String, String> effectiveEnvironment() {
    RuntimeModelConfiguration runtime = runtimeConfiguration.get();
    if (runtime != null) {
      return Map.of(
          "ATLAS_MODEL_PROVIDER", runtime.provider(),
          "ATLAS_MODEL_ENDPOINT", runtime.endpoint(),
          "ATLAS_MODEL_API_KEY", runtime.apiKey(),
          "ATLAS_MODEL_NAME", runtime.modelName());
    }
    return processEnvironment;
  }

  private String normalizeEndpoint(String endpoint, Map<String, String> errors) {
    String normalized = trim(endpoint);
    if (normalized.isBlank()) {
      return DEFAULT_ENDPOINT;
    }
    try {
      URI uri = new URI(normalized);
      String scheme = uri.getScheme();
      if (!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme)) {
        errors.put("endpoint", "must use http or https");
      }
      if (uri.getHost() == null || uri.getHost().isBlank()) {
        errors.put("endpoint", "must include a host");
      }
      if (uri.getUserInfo() != null) {
        errors.put("endpoint", "must not include credentials");
      }
    } catch (URISyntaxException ex) {
      errors.put("endpoint", "must be a valid URI");
    }
    return normalized.replaceAll("/+$", "");
  }

  private static String normalizeProvider(String provider) {
    return trim(provider).toLowerCase().replace('_', '-');
  }

  private static String value(Map<String, String> values, String key, String defaultValue) {
    return value(values == null ? null : values.get(key), defaultValue);
  }

  private static String value(String value, String defaultValue) {
    String trimmed = trim(value);
    return trimmed.isBlank() ? defaultValue : trimmed;
  }

  private static boolean hasValue(String value) {
    return value != null && !value.isBlank();
  }

  private static String trim(String value) {
    return value == null ? "" : value.trim();
  }

  private record RuntimeModelConfiguration(String provider, String endpoint, String apiKey, String modelName) {}
}
