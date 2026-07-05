package com.atlas.metadata.dto;

import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/** Published Wiki page metadata response. */
public record WikiPageResponse(
    String id,
    String spaceId,
    String folderId,
    String title,
    String slug,
    String pageType,
    String markdownPath,
    List<String> sourceDocumentIds,
    List<String> aliases,
    List<WikiReferenceResponse> sourceRefs,
    List<WikiReferenceResponse> chunkRefs,
    List<String> inLinks,
    List<String> outLinks,
    Integer version,
    String sourceMode,
    String refreshPolicy,
    BigDecimal confidence,
    ReviewStatus reviewStatus,
    String owner,
    OffsetDateTime lastUpdated) {}
