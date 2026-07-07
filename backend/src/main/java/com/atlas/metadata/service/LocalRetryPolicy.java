package com.atlas.metadata.service;

import org.springframework.stereotype.Component;

/** Deterministic local retry policy for worker retry/dead-letter v0. */
@Component
public class LocalRetryPolicy {

  private static final int MAX_ATTEMPTS = 3;
  private static final int[] DELAYS = {30, 120};

  public int maxAttempts() {
    return MAX_ATTEMPTS;
  }

  /** Returns retry delay seconds for a failed attempt number. */
  public int delaySeconds(int attemptNumber) {
    int index = Math.max(0, Math.min(attemptNumber - 1, DELAYS.length - 1));
    return DELAYS[index];
  }

  /** Returns whether another retry is available after this attempt number. */
  public boolean hasRetryRemaining(int attemptNumber) {
    return attemptNumber < MAX_ATTEMPTS;
  }
}
