package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ManualUrlEligibilityStatus;
import com.atlas.metadata.enums.ManualUrlFetchIntent;
import com.atlas.metadata.enums.ManualUrlFetchPolicy;
import com.atlas.metadata.enums.ManualUrlIngestStatus;
import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Safe API response for a metadata-only manual URL source. */
public record ManualUrlSourceResponse(
    String id,
    String spaceId,
    String displayUrl,
    String host,
    String title,
    String description,
    ManualUrlFetchIntent fetchIntent,
    ManualUrlFetchPolicy fetchPolicy,
    ManualUrlIngestStatus ingestStatus,
    ReviewStatus reviewStatus,
    ManualUrlEligibilityStatus eligibilityStatus,
    BigDecimal confidence,
    String sourceTrace,
    String batchId,
    String fileItemId,
    String createdBy,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {}
