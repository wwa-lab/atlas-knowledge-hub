package com.atlas.metadata.service;

import com.atlas.metadata.enums.AuthDecisionResult;
import com.atlas.metadata.exception.SafeErrorCodes;
import org.springframework.http.HttpStatus;

/** User-safe authorization decision emitted by the auth boundary. */
public record AuthDecision(AuthDecisionResult result, HttpStatus status, String code, String message) {

  public static AuthDecision allowed() {
    return new AuthDecision(AuthDecisionResult.ALLOWED, HttpStatus.OK, "OK", "Allowed.");
  }

  public static AuthDecision unauthenticated() {
    return new AuthDecision(
        AuthDecisionResult.UNAUTHENTICATED,
        HttpStatus.UNAUTHORIZED,
        SafeErrorCodes.AUTHENTICATION_REQUIRED,
        "Authentication is required.");
  }

  public static AuthDecision forbidden() {
    return new AuthDecision(
        AuthDecisionResult.FORBIDDEN,
        HttpStatus.FORBIDDEN,
        SafeErrorCodes.PERMISSION_DENIED,
        "The current user is not allowed to perform this action.");
  }

  public static AuthDecision safeNotFound() {
    return new AuthDecision(
        AuthDecisionResult.SAFE_NOT_FOUND,
        HttpStatus.NOT_FOUND,
        SafeErrorCodes.NOT_FOUND,
        "Resource not found.");
  }
}
