package com.atlas.metadata.service;

import com.atlas.metadata.domain.AuditEvent;
import com.atlas.metadata.dto.AuditEventResponse;
import com.atlas.metadata.dto.mapping.AuditMapper;
import com.atlas.metadata.enums.AuditCategory;
import com.atlas.metadata.enums.AuditResult;
import com.atlas.metadata.enums.AuditSeverity;
import com.atlas.metadata.enums.AuthDecisionResult;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.repository.AuditEventRepository;
import com.atlas.metadata.repository.SpaceRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Append-only audit service that persists only user-safe metadata. */
@Service
public class AuditLogService {

  private static final int MAX_SIZE = 100;
  private static final int DEFAULT_SIZE = 25;
  private static final int MAX_TEXT = 160;
  private static final int MAX_SUMMARY = 240;
  private static final Set<String> ALLOWED_METADATA_KEYS =
      Set.of(
          "adapterKey",
          "capability",
          "count",
          "createdCount",
          "decisionCode",
          "evidenceCount",
          "httpMethod",
          "mode",
          "nextRole",
          "nextStatus",
          "pageType",
          "reasonCode",
          "reviewAction",
          "reviewPolicy",
          "reviewStatus",
          "role",
          "skippedCount",
          "sourceChunkCount",
          "status");
  private static final Pattern UNSAFE_KEY =
      Pattern.compile("(?i)(password|secret|token|credential|api[_-]?key|authorization|bearer)");
  private static final Pattern UNSAFE_VALUE =
      Pattern.compile(
          "(?i)(password|secret|token|api[_-]?key|bearer)\\s*[:=]|https?://|jdbc:|(^|\\s)(/"
              + "Users/|/home/|/var/|/etc/|[A-Za-z]:\\\\|\\\\\\\\)|stacktrace|exception:");

  private final AuditEventRepository auditEventRepository;
  private final SpaceRepository spaceRepository;
  private final Clock clock;

  @Autowired
  public AuditLogService(AuditEventRepository auditEventRepository, SpaceRepository spaceRepository) {
    this(auditEventRepository, spaceRepository, Clock.systemUTC());
  }

  AuditLogService(AuditEventRepository auditEventRepository, SpaceRepository spaceRepository, Clock clock) {
    this.auditEventRepository = auditEventRepository;
    this.spaceRepository = spaceRepository;
    this.clock = clock;
  }

  /** Creates one sanitized audit event. */
  @Transactional
  public AuditEvent record(CreateAuditEventCommand command) {
    if (command.spaceId() != null
        && !command.spaceId().isBlank()
        && !spaceRepository.existsById(command.spaceId())) {
      throw new IllegalArgumentException("Audit space must exist.");
    }
    OffsetDateTime now = OffsetDateTime.now(clock);
    AuditEvent event =
        AuditEvent.create(
            "audit-" + UUID.randomUUID(),
            now,
            blankToNull(safeText(command.actorUserId(), MAX_TEXT)),
            safeText(firstNonBlank(command.actorDisplay(), command.actorUserId(), "anonymous"), MAX_TEXT),
            safeText(firstNonBlank(command.action(), "AUDIT_EVENT"), MAX_TEXT),
            command.category() == null ? AuditCategory.SETTINGS : command.category(),
            command.result() == null ? AuditResult.SUCCEEDED : command.result(),
            command.severity() == null ? AuditSeverity.INFO : command.severity(),
            blankToNull(safeText(command.spaceId(), MAX_TEXT)),
            safeText(firstNonBlank(command.targetType(), "unknown"), MAX_TEXT),
            safeText(firstNonBlank(command.targetId(), "unknown"), MAX_TEXT),
            blankToNull(safeText(command.requestId(), MAX_TEXT)),
            safeSummary(command.safeSummary()),
            safeMetadata(command.metadata()));
    return auditEventRepository.save(event);
  }

  /** Records a denied auth decision without letting audit failures mask the original response. */
  @Transactional
  public void recordAuthorizationDenied(
      CurrentUserContext context,
      AuthRequirement requirement,
      AuthDecision decision,
      HttpServletRequest request) {
    if (requirement.spaceId() == null || requirement.spaceId().isBlank()) {
      return;
    }
    AuditResult result =
        decision.result() == AuthDecisionResult.SAFE_NOT_FOUND ? AuditResult.SAFE_NOT_FOUND : AuditResult.DENIED;
    record(
        new CreateAuditEventCommand(
            context == null ? null : context.user().getId(),
            context == null ? "anonymous" : context.user().getDisplayName(),
            "AUTH_" + requirement.capability().name() + "_DENIED",
            AuditCategory.AUTH,
            result,
            result == AuditResult.DENIED ? AuditSeverity.SECURITY : AuditSeverity.NOTICE,
            requirement.spaceId(),
            "api_route",
            request.getRequestURI(),
            requestId(request),
            "Access denied for required capability " + requirement.capability().name() + ".",
            Map.of(
                "capability", requirement.capability().name(),
                "decisionCode", decision.code(),
                "httpMethod", request.getMethod())));
  }

  /** Lists audit events for one Knowledge Space using bounded offset pagination. */
  @Transactional(readOnly = true)
  public Page<AuditEventResponse> list(
      String spaceId,
      AuditCategory category,
      AuditResult result,
      AuditSeverity severity,
      String action,
      String actorUserId,
      String targetType,
      String targetId,
      OffsetDateTime createdFrom,
      OffsetDateTime createdTo,
      int page,
      int size) {
    requireSpace(spaceId);
    validateTimeRange(createdFrom, createdTo);
    PageRequest pageable =
        PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_SIZE), Sort.by("createdAt").descending());
    return auditEventRepository
        .findAll(
            scopedSpecification(
                spaceId,
                category,
                result,
                severity,
                action,
                actorUserId,
                targetType,
                targetId,
                createdFrom,
                createdTo),
            pageable)
        .map(AuditMapper::toResponse);
  }

  /** Lists audit events for one target in a Knowledge Space. */
  @Transactional(readOnly = true)
  public Page<AuditEventResponse> listForTarget(
      String spaceId,
      String targetType,
      String targetId,
      OffsetDateTime createdFrom,
      OffsetDateTime createdTo,
      int page,
      int size) {
    return list(
        spaceId,
        null,
        null,
        null,
        null,
        null,
        targetType,
        targetId,
        createdFrom,
        createdTo,
        page,
        size);
  }

  /** Records an optional knowledge/governance event when the service is available. */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void recordIfEnabled(CreateAuditEventCommand command) {
    record(command);
  }

  public int defaultPageSize() {
    return DEFAULT_SIZE;
  }

  private Specification<AuditEvent> scopedSpecification(
      String spaceId,
      AuditCategory category,
      AuditResult result,
      AuditSeverity severity,
      String action,
      String actorUserId,
      String targetType,
      String targetId,
      OffsetDateTime createdFrom,
      OffsetDateTime createdTo) {
    return (root, query, cb) -> {
      java.util.List<Predicate> predicates = new java.util.ArrayList<>();
      predicates.add(cb.equal(root.get("spaceId"), spaceId));
      if (category != null) {
        predicates.add(cb.equal(root.get("category"), category));
      }
      if (result != null) {
        predicates.add(cb.equal(root.get("result"), result));
      }
      if (severity != null) {
        predicates.add(cb.equal(root.get("severity"), severity));
      }
      if (action != null && !action.isBlank()) {
        predicates.add(cb.equal(root.get("action"), action.trim()));
      }
      if (actorUserId != null && !actorUserId.isBlank()) {
        predicates.add(cb.equal(root.get("actorUserId"), actorUserId.trim()));
      }
      if (targetType != null && !targetType.isBlank()) {
        predicates.add(cb.equal(root.get("targetType"), targetType.trim()));
      }
      if (targetId != null && !targetId.isBlank()) {
        predicates.add(cb.equal(root.get("targetId"), targetId.trim()));
      }
      if (createdFrom != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
      }
      if (createdTo != null) {
        predicates.add(cb.lessThan(root.get("createdAt"), createdTo));
      }
      return cb.and(predicates.toArray(Predicate[]::new));
    };
  }

  private void validateTimeRange(OffsetDateTime createdFrom, OffsetDateTime createdTo) {
    if (createdFrom != null && createdTo != null && !createdFrom.isBefore(createdTo)) {
      throw new RequestValidationException(Map.of("createdFrom", "must be before createdTo"));
    }
  }

  private void requireSpace(String spaceId) {
    if (!spaceRepository.existsById(spaceId)) {
      throw new com.atlas.metadata.exception.NotFoundException("Knowledge Space not found.");
    }
  }

  private Map<String, Object> safeMetadata(Map<String, Object> metadata) {
    if (metadata == null || metadata.isEmpty()) {
      return Map.of();
    }
    Map<String, Object> safe = new LinkedHashMap<>();
    metadata.forEach(
        (key, value) -> {
          if (key == null
              || key.isBlank()
              || !ALLOWED_METADATA_KEYS.contains(key)
              || UNSAFE_KEY.matcher(key).find()
              || value == null) {
            return;
          }
          Object safeValue = safeMetadataValue(value);
          if (safeValue != null) {
            safe.put(safeText(key, 80), safeValue);
          }
        });
    return Map.copyOf(safe);
  }

  private Object safeMetadataValue(Object value) {
    if (value instanceof Number || value instanceof Boolean) {
      return value;
    }
    if (value instanceof Enum<?> enumValue) {
      return enumValue.name();
    }
    String text = String.valueOf(value);
    if (UNSAFE_VALUE.matcher(text).find()) {
      return null;
    }
    return safeText(text, MAX_TEXT);
  }

  private String safeSummary(String summary) {
    String candidate = firstNonBlank(summary, "Audit event recorded.");
    if (UNSAFE_VALUE.matcher(candidate).find()) {
      return "Audit event recorded with safe metadata.";
    }
    return safeText(candidate, MAX_SUMMARY);
  }

  private String safeText(String value, int max) {
    String text = firstNonBlank(value, "unknown").trim();
    return text.length() <= max ? text : text.substring(0, max);
  }

  private String firstNonBlank(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return "unknown";
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() || "unknown".equals(value) ? null : value;
  }

  private String requestId(HttpServletRequest request) {
    String header = request.getHeader("X-Request-Id");
    return header == null || header.isBlank() ? UUID.randomUUID().toString() : header;
  }

  /** Command for creating one sanitized audit event. */
  public record CreateAuditEventCommand(
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
}
