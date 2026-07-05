package com.atlas.metadata.dto;

import java.time.OffsetDateTime;

/** Space membership response for owner-managed membership lists. */
public record SpaceMembershipResponse(
    String id,
    String spaceId,
    String userId,
    String email,
    String displayName,
    String role,
    String status,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {}
