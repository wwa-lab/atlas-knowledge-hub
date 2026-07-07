package com.atlas.metadata.exception;

import com.atlas.metadata.dto.ApiEnvelope;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Maps application exceptions to the Atlas API envelope. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private final SafeErrorResponseFactory safeErrors;

  public GlobalExceptionHandler(SafeErrorResponseFactory safeErrors) {
    this.safeErrors = safeErrors;
  }

  /** Handles Bean Validation errors on request bodies. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiEnvelope<Void>> onBodyValidation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, String> fields = new LinkedHashMap<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      fields.putIfAbsent(error.getField(), error.getDefaultMessage());
    }
    return validation(fields, request);
  }

  /** Handles validation errors raised inside services. */
  @ExceptionHandler(RequestValidationException.class)
  public ResponseEntity<ApiEnvelope<Void>> onRequestValidation(
      RequestValidationException ex, HttpServletRequest request) {
    return validation(ex.getFields(), request);
  }

  /** Handles invalid query/path parameter values. */
  @ExceptionHandler({MethodArgumentTypeMismatchException.class, ConstraintViolationException.class})
  public ResponseEntity<ApiEnvelope<Void>> onParameterValidation(
      Exception ex, HttpServletRequest request) {
    return validation(Map.of("request", "must match the documented API contract"), request);
  }

  /** Handles malformed JSON and invalid enum values. */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiEnvelope<Void>> onUnreadableBody(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    return validation(Map.of("body", "must match the documented API contract"), request);
  }

  /** Handles unknown resource ids. */
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiEnvelope<Void>> onNotFound(
      NotFoundException ex, HttpServletRequest request) {
    return error(HttpStatus.NOT_FOUND, SafeErrorCodes.NOT_FOUND, ex.getMessage(), null, null, request);
  }

  /** Handles duplicate or conflicting creates. */
  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ApiEnvelope<Void>> onConflict(
      ConflictException ex, HttpServletRequest request) {
    return error(HttpStatus.CONFLICT, SafeErrorCodes.CONFLICT, ex.getMessage(), null, null, request);
  }

  /** Handles unexpected server faults with a generic user-safe message. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiEnvelope<Void>> onUnexpected(Exception ex, HttpServletRequest request) {
    String correlationId = UUID.randomUUID().toString();
    LOG.error(
        "Unhandled metadata API error correlationId={} path={}",
        correlationId,
        request.getRequestURI(),
        ex);
    return error(
        HttpStatus.INTERNAL_SERVER_ERROR,
        SafeErrorCodes.SAFE_SYSTEM_ERROR,
        "Unexpected server error.",
        null,
        correlationId,
        request);
  }

  private ResponseEntity<ApiEnvelope<Void>> validation(
      Map<String, String> fields, HttpServletRequest request) {
    return error(
        HttpStatus.BAD_REQUEST,
        SafeErrorCodes.VALIDATION_FAILED,
        "Invalid request.",
        fields,
        null,
        request);
  }

  private ResponseEntity<ApiEnvelope<Void>> error(
      HttpStatus status,
      String code,
      String message,
      Map<String, String> fields,
      String correlationId,
      HttpServletRequest request) {
    return safeErrors.response(status, code, message, fields, correlationId, null, request);
  }
}
