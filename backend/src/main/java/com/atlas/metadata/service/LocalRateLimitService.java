package com.atlas.metadata.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Deterministic local fixed-window rate limiter for mock-safe API protection. */
@Service
public class LocalRateLimitService {

  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
  private final int defaultLimit;
  private final long defaultWindowMillis;

  public LocalRateLimitService(
      @Value("${atlas.rate-limit.default-limit:1000}") int defaultLimit,
      @Value("${atlas.rate-limit.window-millis:60000}") long defaultWindowMillis) {
    this.defaultLimit = Math.max(1, defaultLimit);
    this.defaultWindowMillis = Math.max(1000, defaultWindowMillis);
  }

  /** Evaluates one caller against its local bucket. */
  public RateLimitDecision evaluate(
      String testKey,
      String user,
      String remoteAddr,
      String requestedLimit,
      String requestedWindowMillis) {
    int limit = positiveInt(requestedLimit).orElse(defaultLimit);
    long windowMillis = positiveLong(requestedWindowMillis).orElse(defaultWindowMillis);
    String key = callerKey(testKey, user, remoteAddr);
    long now = System.currentTimeMillis();
    AtomicBoolean allowed = new AtomicBoolean(true);
    Bucket bucket =
        buckets.compute(
            key,
            (ignored, current) -> {
              if (current == null || now >= current.windowStartMillis() + windowMillis) {
                return new Bucket(now, 1);
              }
              if (current.used() >= limit) {
                allowed.set(false);
                return current;
              }
              return new Bucket(current.windowStartMillis(), current.used() + 1);
            });
    return allowed.get()
        ? RateLimitDecision.allowed(limit)
        : RateLimitDecision.denied(
            limit, retryAfterSeconds(now, bucket.windowStartMillis(), windowMillis));
  }

  /** Clears local buckets for deterministic tests. */
  public void reset() {
    buckets.clear();
  }

  private int retryAfterSeconds(long now, long windowStartMillis, long windowMillis) {
    long remainingMillis = Math.max(1000, windowStartMillis + windowMillis - now);
    return (int) Math.ceil(remainingMillis / 1000.0);
  }

  private String callerKey(String testKey, String user, String remoteAddr) {
    if (testKey != null && !testKey.isBlank()) {
      return "test:" + testKey.trim();
    }
    if (user != null && !user.isBlank()) {
      return "user:" + user.trim();
    }
    return "remote:" + Optional.ofNullable(remoteAddr).orElse("unknown");
  }

  private Optional<Integer> positiveInt(String value) {
    try {
      int parsed = Integer.parseInt(value == null ? "" : value.trim());
      return parsed > 0 ? Optional.of(parsed) : Optional.empty();
    } catch (NumberFormatException ignored) {
      return Optional.empty();
    }
  }

  private Optional<Long> positiveLong(String value) {
    try {
      long parsed = Long.parseLong(value == null ? "" : value.trim());
      return parsed > 0 ? Optional.of(parsed) : Optional.empty();
    } catch (NumberFormatException ignored) {
      return Optional.empty();
    }
  }

  private record Bucket(long windowStartMillis, int used) {}
}
