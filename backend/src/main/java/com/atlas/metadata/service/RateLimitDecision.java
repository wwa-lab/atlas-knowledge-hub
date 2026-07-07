package com.atlas.metadata.service;

/** Local deterministic rate-limit decision. */
public record RateLimitDecision(boolean allowed, int limit, int retryAfterSeconds) {

  public static RateLimitDecision allowed(int limit) {
    return new RateLimitDecision(true, limit, 0);
  }

  public static RateLimitDecision denied(int limit, int retryAfterSeconds) {
    return new RateLimitDecision(false, limit, retryAfterSeconds);
  }
}
