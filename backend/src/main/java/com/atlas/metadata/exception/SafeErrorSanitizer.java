package com.atlas.metadata.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/** Redacts sensitive implementation details from API error responses. */
@Component
public class SafeErrorSanitizer {

  private static final int MAX_VALUE_LENGTH = 240;
  private static final Pattern CREDENTIAL_ASSIGNMENT =
      Pattern.compile("(?i)(password|token|secret|api[_-]?key)\\s*[:=]\\s*[^\\s,;]+");
  private static final Pattern URL = Pattern.compile("https?://[^\\s,;\\]\")]+");
  private static final Pattern POSIX_PRIVATE_PATH =
      Pattern.compile("(?i)/(Users|home|var|private|etc|opt)/[^\\s,;\\]\")]+");
  private static final Pattern WINDOWS_PRIVATE_PATH =
      Pattern.compile("(?i)[a-z]:\\\\[^\\s,;\\]\")]+");
  private static final Pattern STACK_TRACE_LINE =
      Pattern.compile("(?m)\\bat\\s+[a-zA-Z0-9_.$]+\\([^)]*\\)");
  private static final Pattern EXCEPTION_CLASS =
      Pattern.compile("\\b[a-zA-Z0-9_.]+(?:Exception|Error)\\b");
  private static final Pattern SOURCE_BLOCK = Pattern.compile("(?s)```.*?```");

  /** Sanitizes a potentially unsafe string. */
  public String sanitize(String value) {
    if (value == null) {
      return null;
    }
    String sanitized = value;
    sanitized = SOURCE_BLOCK.matcher(sanitized).replaceAll("[redacted-source]");
    sanitized = CREDENTIAL_ASSIGNMENT.matcher(sanitized).replaceAll("$1=[redacted]");
    sanitized = URL.matcher(sanitized).replaceAll("[redacted-endpoint]");
    sanitized = POSIX_PRIVATE_PATH.matcher(sanitized).replaceAll("[redacted-path]");
    sanitized = WINDOWS_PRIVATE_PATH.matcher(sanitized).replaceAll("[redacted-path]");
    sanitized = STACK_TRACE_LINE.matcher(sanitized).replaceAll("[redacted-stack]");
    sanitized = EXCEPTION_CLASS.matcher(sanitized).replaceAll("[redacted-exception]");
    sanitized = sanitized.replaceAll("[\\r\\n\\t]+", " ").trim();
    if (sanitized.length() > MAX_VALUE_LENGTH) {
      return sanitized.substring(0, MAX_VALUE_LENGTH) + "...";
    }
    return sanitized;
  }

  /** Sanitizes field-level validation messages. */
  public Map<String, String> sanitizeFields(Map<String, String> fields) {
    if (fields == null || fields.isEmpty()) {
      return null;
    }
    Map<String, String> sanitized = new LinkedHashMap<>();
    for (Map.Entry<String, String> entry : fields.entrySet()) {
      sanitized.put(sanitize(entry.getKey()), sanitize(entry.getValue()));
    }
    return Map.copyOf(sanitized);
  }
}
