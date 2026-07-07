package com.atlas.metadata.exception;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.ErrorBody;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/** Creates stable safe API error envelopes. */
@Component
public class SafeErrorResponseFactory {

  private final SafeErrorSanitizer sanitizer;

  public SafeErrorResponseFactory(SafeErrorSanitizer sanitizer) {
    this.sanitizer = sanitizer;
  }

  /** Creates a safe error response entity. */
  public ResponseEntity<ApiEnvelope<Void>> response(
      HttpStatus status,
      String code,
      String message,
      Map<String, String> fields,
      String correlationId,
      Integer retryAfterSeconds,
      HttpServletRequest request) {
    return ResponseEntity.status(status)
        .body(envelope(code, message, fields, correlationId, retryAfterSeconds, request));
  }

  /** Creates a safe error envelope. */
  public ApiEnvelope<Void> envelope(
      String code,
      String message,
      Map<String, String> fields,
      String correlationId,
      Integer retryAfterSeconds,
      HttpServletRequest request) {
    return ApiEnvelope.fail(body(code, message, fields, correlationId, retryAfterSeconds, request));
  }

  /** Creates a safe error body. */
  public ErrorBody body(
      String code,
      String message,
      Map<String, String> fields,
      String correlationId,
      Integer retryAfterSeconds,
      HttpServletRequest request) {
    return new ErrorBody(
        code,
        safeMessage(code, message),
        sanitizer.sanitizeFields(fields),
        Instant.now().toEpochMilli(),
        sanitizer.sanitize(request.getRequestURI()),
        sanitizer.sanitize(correlationId),
        retryAfterSeconds);
  }

  private String safeMessage(String code, String fallback) {
    return switch (code) {
      case SafeErrorCodes.AUTHENTICATION_REQUIRED -> "Authentication is required.";
      case SafeErrorCodes.PERMISSION_DENIED -> "The current user is not allowed to perform this action.";
      case SafeErrorCodes.VALIDATION_FAILED -> "Invalid request.";
      case SafeErrorCodes.NOT_FOUND -> "Resource not found.";
      case SafeErrorCodes.CONFLICT -> safeFallback(fallback, "Request conflicts with current state.");
      case SafeErrorCodes.RATE_LIMITED -> "Too many requests. Please try again later.";
      case SafeErrorCodes.SAFE_SYSTEM_ERROR -> "Unexpected server error.";
      default -> safeFallback(fallback, "Request failed safely.");
    };
  }

  private String safeFallback(String fallback, String defaultMessage) {
    String sanitized = sanitizer.sanitize(fallback);
    return sanitized == null || sanitized.isBlank() ? defaultMessage : sanitized;
  }
}
