package com.atlas.metadata.dto.mapping;

import com.atlas.metadata.domain.AuditEvent;
import com.atlas.metadata.dto.AuditEventResponse;

/** Maps audit domain records to read-only API responses. */
public final class AuditMapper {

  private AuditMapper() {}

  public static AuditEventResponse toResponse(AuditEvent event) {
    return new AuditEventResponse(
        event.getId(),
        event.getCreatedAt(),
        event.getActorUserId(),
        event.getActorDisplay(),
        event.getAction(),
        event.getCategory(),
        event.getResult(),
        event.getSeverity(),
        event.getSpaceId(),
        event.getTargetType(),
        event.getTargetId(),
        event.getRequestId(),
        event.getSafeSummary(),
        event.getMetadata());
  }
}
