package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.AuditEventResponse;
import com.atlas.metadata.dto.PageMeta;
import com.atlas.metadata.enums.AuditCategory;
import com.atlas.metadata.enums.AuditResult;
import com.atlas.metadata.enums.AuditSeverity;
import com.atlas.metadata.service.AuditLogService;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Read-only governance API for safe audit events. */
@RestController
@RequestMapping("/api/spaces/{spaceId}/audit-events")
public class AuditLogController {

  private final AuditLogService auditLogService;

  public AuditLogController(AuditLogService auditLogService) {
    this.auditLogService = auditLogService;
  }

  @GetMapping
  public ApiEnvelope<List<AuditEventResponse>> list(
      @PathVariable String spaceId,
      @RequestParam(required = false) AuditCategory category,
      @RequestParam(required = false) AuditResult result,
      @RequestParam(required = false) AuditSeverity severity,
      @RequestParam(required = false) String action,
      @RequestParam(required = false) String actorUserId,
      @RequestParam(required = false) String targetType,
      @RequestParam(required = false) String targetId,
      @RequestParam(required = false) OffsetDateTime createdFrom,
      @RequestParam(required = false) OffsetDateTime createdTo,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(required = false) Integer size) {
    Page<AuditEventResponse> events =
        auditLogService.list(
            spaceId,
            category,
            result,
            severity,
            action,
            actorUserId,
            targetType,
            targetId,
            createdFrom,
            createdTo,
            page,
            size == null ? auditLogService.defaultPageSize() : size);
    return ApiEnvelope.ok(events.getContent(), new PageMeta(events.getNumber(), events.getSize(), events.getTotalElements()));
  }

  @GetMapping("/targets/{targetType}/{targetId}")
  public ApiEnvelope<List<AuditEventResponse>> listForTarget(
      @PathVariable String spaceId,
      @PathVariable String targetType,
      @PathVariable String targetId,
      @RequestParam(required = false) OffsetDateTime createdFrom,
      @RequestParam(required = false) OffsetDateTime createdTo,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(required = false) Integer size) {
    Page<AuditEventResponse> events =
        auditLogService.listForTarget(
            spaceId,
            targetType,
            targetId,
            createdFrom,
            createdTo,
            page,
            size == null ? auditLogService.defaultPageSize() : size);
    return ApiEnvelope.ok(events.getContent(), new PageMeta(events.getNumber(), events.getSize(), events.getTotalElements()));
  }
}
