package com.atlas.metadata.exception;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.ErrorBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
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
    return error(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), null, request);
  }

  /** Handles duplicate or conflicting creates. */
  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ApiEnvelope<Void>> onConflict(
      ConflictException ex, HttpServletRequest request) {
    return error(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), null, request);
  }

  /** Handles unexpected server faults with a generic user-safe message. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiEnvelope<Void>> onUnexpected(Exception ex, HttpServletRequest request) {
    return error(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "INTERNAL_ERROR",
        "Unexpected server error.",
        null,
        request);
  }

  private ResponseEntity<ApiEnvelope<Void>> validation(
      Map<String, String> fields, HttpServletRequest request) {
    return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Invalid request.", fields, request);
  }

  private ResponseEntity<ApiEnvelope<Void>> error(
      HttpStatus status,
      String code,
      String message,
      Map<String, String> fields,
      HttpServletRequest request) {
    ErrorBody body =
        new ErrorBody(code, message, fields, Instant.now().toEpochMilli(), request.getRequestURI());
    return ResponseEntity.status(status).body(ApiEnvelope.fail(body));
  }
}
