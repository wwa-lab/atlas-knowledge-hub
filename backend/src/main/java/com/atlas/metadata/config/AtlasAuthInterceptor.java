package com.atlas.metadata.config;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.ErrorBody;
import com.atlas.metadata.service.AuthDecision;
import com.atlas.metadata.service.AuthRequirement;
import com.atlas.metadata.service.AuditLogService;
import com.atlas.metadata.service.AuthorizationPathPolicy;
import com.atlas.metadata.service.AuthorizationService;
import com.atlas.metadata.service.CurrentUserContext;
import com.atlas.metadata.service.CurrentUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Backend auth/RBAC boundary for Atlas API endpoints. */
@Component
public class AtlasAuthInterceptor implements HandlerInterceptor {

  private final CurrentUserService currentUserService;
  private final AuthorizationPathPolicy pathPolicy;
  private final AuthorizationService authorizationService;
  private final AuditLogService auditLogService;
  private final ObjectMapper objectMapper;

  public AtlasAuthInterceptor(
      CurrentUserService currentUserService,
      AuthorizationPathPolicy pathPolicy,
      AuthorizationService authorizationService,
      AuditLogService auditLogService,
      ObjectMapper objectMapper) {
    this.currentUserService = currentUserService;
    this.pathPolicy = pathPolicy;
    this.authorizationService = authorizationService;
    this.auditLogService = auditLogService;
    this.objectMapper = objectMapper;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws IOException {
    if ("OPTIONS".equals(request.getMethod())) {
      return true;
    }
    Optional<AuthRequirement> requirement = pathPolicy.requirementFor(request);
    if (requirement.isEmpty()) {
      return true;
    }
    Optional<CurrentUserContext> context = currentUserService.resolve(request);
    if (context.isEmpty()) {
      AuthDecision decision = AuthDecision.unauthenticated();
      auditDenied(null, requirement.get(), decision, request);
      write(response, request, decision);
      return false;
    }
    if (currentUserService.isDisabled(context.get())) {
      AuthDecision decision = AuthDecision.forbidden();
      auditDenied(context.get(), requirement.get(), decision, request);
      write(response, request, decision);
      return false;
    }
    AuthDecision decision = authorizationService.evaluate(context.get(), requirement.get());
    if (!decision.result().name().equals("ALLOWED")) {
      auditDenied(context.get(), requirement.get(), decision, request);
      write(response, request, decision);
      return false;
    }
    return true;
  }

  private void auditDenied(
      CurrentUserContext context,
      AuthRequirement requirement,
      AuthDecision decision,
      HttpServletRequest request) {
    try {
      auditLogService.recordAuthorizationDenied(context, requirement, decision, request);
    } catch (RuntimeException ignored) {
      // Authorization responses must remain user-safe even if audit persistence is unavailable.
    }
  }

  private void write(HttpServletResponse response, HttpServletRequest request, AuthDecision decision)
      throws IOException {
    response.setStatus(decision.status().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    ErrorBody error =
        new ErrorBody(
            decision.code(), decision.message(), null, Instant.now().toEpochMilli(), request.getRequestURI());
    objectMapper.writeValue(response.getOutputStream(), ApiEnvelope.fail(error));
  }
}
