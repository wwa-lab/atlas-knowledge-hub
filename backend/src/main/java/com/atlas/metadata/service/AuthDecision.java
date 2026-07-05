package com.atlas.metadata.service;

import com.atlas.metadata.enums.AuthDecisionResult;
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
        "UNAUTHORIZED",
        "Authentication is required.");
  }

  public static AuthDecision forbidden() {
    return new AuthDecision(
        AuthDecisionResult.FORBIDDEN,
        HttpStatus.FORBIDDEN,
        "FORBIDDEN",
        "The current user is not allowed to perform this action.");
  }

  public static AuthDecision safeNotFound() {
    return new AuthDecision(
        AuthDecisionResult.SAFE_NOT_FOUND,
        HttpStatus.NOT_FOUND,
        "NOT_FOUND",
        "Resource not found.");
  }
}
