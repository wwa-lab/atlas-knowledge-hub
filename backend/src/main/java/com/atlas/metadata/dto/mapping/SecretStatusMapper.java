package com.atlas.metadata.dto.mapping;

import com.atlas.metadata.dto.SecretReferenceResponse;
import com.atlas.metadata.dto.SecretStatusResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Builds typed status-only secret metadata from existing masked summaries. */
public final class SecretStatusMapper {

  private static final Set<String> SECRET_STATUS_KEYS =
      Set.of(
          "credential",
          "credentials",
          "endpoint",
          "command",
          "bucket",
          "region",
          "collection",
          "timeout",
          "capturedOutput");

  private SecretStatusMapper() {}

  /** Converts safe summary entries into typed secret status responses. */
  public static List<SecretStatusResponse> fromSummary(
      String provider, String scope, Map<String, String> summary, String defaultSource) {
    if (summary == null || summary.isEmpty()) {
      return List.of();
    }
    return summary.entrySet().stream()
        .filter(entry -> SECRET_STATUS_KEYS.contains(entry.getKey()))
        .map(entry -> fromStatus(provider, scope, entry.getKey(), entry.getValue(), defaultSource))
        .toList();
  }

  /** Creates one typed status response. */
  public static SecretStatusResponse fromStatus(
      String provider, String scope, String key, String rawStatus, String defaultSource) {
    String normalized = normalize(rawStatus);
    String status =
        switch (normalized) {
          case "configured" -> "CONFIGURED";
          case "env-configured" -> "ENV_CONFIGURED";
          case "disabled" -> "DISABLED";
          case "not-required", "mock" -> "NOT_REQUIRED";
          default -> "MISSING";
        };
    String source =
        switch (status) {
          case "ENV_CONFIGURED" -> "environment";
          case "NOT_REQUIRED" -> "mock".equals(normalized) ? "mock" : "none";
          case "MISSING" -> "none";
          default -> defaultSource == null || defaultSource.isBlank() ? "adapter" : defaultSource;
        };
    boolean replaceable = isReplaceable(key);
    boolean removable = replaceable && ("CONFIGURED".equals(status) || "ENV_CONFIGURED".equals(status));
    return new SecretStatusResponse(
        new SecretReferenceResponse(provider, scope, key, displayName(key)),
        status,
        source,
        maskedLabel(status),
        replaceable,
        removable);
  }

  private static boolean isReplaceable(String key) {
    return "credential".equals(key)
        || "credentials".equals(key)
        || "endpoint".equals(key)
        || "command".equals(key)
        || "bucket".equals(key)
        || "region".equals(key)
        || "collection".equals(key);
  }

  private static String normalize(String status) {
    return status == null ? "" : status.trim().toLowerCase().replace('_', '-');
  }

  private static String displayName(String key) {
    return switch (key) {
      case "credential", "credentials" -> "Credential";
      case "endpoint" -> "Endpoint";
      case "command" -> "Runtime command";
      case "bucket" -> "Bucket";
      case "region" -> "Region";
      case "collection" -> "Collection";
      case "timeout" -> "Timeout";
      case "capturedOutput" -> "Captured output";
      default -> key;
    };
  }

  private static String maskedLabel(String status) {
    return switch (status) {
      case "CONFIGURED" -> "Configured";
      case "ENV_CONFIGURED" -> "Configured from environment";
      case "DISABLED" -> "Disabled";
      case "NOT_REQUIRED" -> "Not required";
      default -> "Missing";
    };
  }
}
