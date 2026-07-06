package com.atlas.metadata.dto;

import com.atlas.metadata.enums.AuditCategory;
import com.atlas.metadata.enums.AuditResult;
import com.atlas.metadata.enums.AuditSeverity;
import java.time.OffsetDateTime;
import java.util.Map;

/** Read-only audit event response for governance users. */
public record AuditEventResponse(
    String id,
    OffsetDateTime createdAt,
    String actorUserId,
    String actorDisplay,
    String action,
    AuditCategory category,
    AuditResult result,
    AuditSeverity severity,
    String spaceId,
    String targetType,
    String targetId,
    String requestId,
    String safeSummary,
    Map<String, Object> metadata) {}
