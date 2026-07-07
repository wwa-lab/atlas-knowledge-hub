package com.atlas.metadata.dto;

import com.atlas.metadata.enums.WorkerSafeErrorCategory;
import java.util.Map;

/** Command used by local worker integrations to record a safe failure. */
public record RecordWorkerFailureCommand(
    boolean retryable,
    String safeErrorCode,
    WorkerSafeErrorCategory safeErrorCategory,
    String safeErrorMessage,
    Map<String, String> sourceTrace) {}
