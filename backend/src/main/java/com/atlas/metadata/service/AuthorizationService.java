package com.atlas.metadata.service;

import com.atlas.metadata.enums.AtlasCapability;
import com.atlas.metadata.enums.AtlasRole;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Service;

/** Role-to-capability authorization matrix for Atlas space RBAC. */
@Service
public class AuthorizationService {

  private static final EnumMap<AtlasRole, Set<AtlasCapability>> ROLE_CAPABILITIES =
      buildRoleCapabilities();

  /** Evaluates a requirement for the current user. */
  public AuthDecision evaluate(CurrentUserContext context, AuthRequirement requirement) {
    if (context == null) {
      return AuthDecision.unauthenticated();
    }
    if (can(context, requirement.capability(), requirement.spaceId())) {
      return AuthDecision.allowed();
    }
    if (requirement.spaceId() != null
        && requirement.safeNotFoundOnNoMembership()
        && !context.hasMembershipIn(requirement.spaceId())
        && !context.globalRoles().contains(AtlasRole.PLATFORM_ADMIN)) {
      return AuthDecision.safeNotFound();
    }
    return AuthDecision.forbidden();
  }

  /** Returns true when a context grants a capability. */
  public boolean can(CurrentUserContext context, AtlasCapability capability, String spaceId) {
    if (capability == AtlasCapability.AUTHENTICATED) {
      return true;
    }
    if (context.globalRoles().contains(AtlasRole.PLATFORM_ADMIN)) {
      return true;
    }
    if (spaceId == null) {
      return (capability == AtlasCapability.SETTINGS_MANAGE
              || capability == AtlasCapability.SPACE_MANAGE)
          && ownsAnySpace(context);
    }
    return context.rolesForSpace(spaceId).stream()
        .map(ROLE_CAPABILITIES::get)
        .anyMatch(capabilities -> capabilities != null && capabilities.contains(capability));
  }

  private boolean ownsAnySpace(CurrentUserContext context) {
    return context.memberships().stream().anyMatch(membership -> membership.getRole() == AtlasRole.SPACE_OWNER);
  }

  private static EnumMap<AtlasRole, Set<AtlasCapability>> buildRoleCapabilities() {
    EnumMap<AtlasRole, Set<AtlasCapability>> matrix = new EnumMap<>(AtlasRole.class);
    matrix.put(AtlasRole.VIEWER, EnumSet.of(AtlasCapability.SPACE_READ, AtlasCapability.CONTENT_READ));
    matrix.put(
        AtlasRole.EDITOR,
        EnumSet.of(AtlasCapability.SPACE_READ, AtlasCapability.CONTENT_READ, AtlasCapability.CONTENT_WRITE));
    matrix.put(
        AtlasRole.KNOWLEDGE_MANAGER,
        EnumSet.of(
            AtlasCapability.SPACE_READ,
            AtlasCapability.CONTENT_READ,
            AtlasCapability.CONTENT_WRITE,
            AtlasCapability.KNOWLEDGE_OPERATE,
            AtlasCapability.GOVERNANCE_READ));
    matrix.put(
        AtlasRole.SPACE_OWNER,
        EnumSet.of(
            AtlasCapability.SPACE_READ,
            AtlasCapability.SPACE_MANAGE,
            AtlasCapability.CONTENT_READ,
            AtlasCapability.CONTENT_WRITE,
            AtlasCapability.KNOWLEDGE_OPERATE,
            AtlasCapability.GOVERNANCE_READ,
            AtlasCapability.MEMBER_MANAGE,
            AtlasCapability.SETTINGS_MANAGE));
    matrix.put(
        AtlasRole.AUDITOR,
        EnumSet.of(
            AtlasCapability.SPACE_READ, AtlasCapability.CONTENT_READ, AtlasCapability.GOVERNANCE_READ));
    matrix.put(AtlasRole.PLATFORM_ADMIN, EnumSet.allOf(AtlasCapability.class));
    return matrix;
  }
}
