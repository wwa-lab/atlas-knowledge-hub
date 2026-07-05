package com.atlas.metadata.service;

import com.atlas.metadata.domain.AtlasUser;
import com.atlas.metadata.domain.SpaceMembership;
import com.atlas.metadata.enums.AtlasRole;
import java.util.List;
import java.util.Set;

/** Resolved user context for one request. */
public record CurrentUserContext(
    AtlasUser user,
    List<SpaceMembership> memberships,
    Set<AtlasRole> globalRoles,
    AtlasRole roleOverride) {

  /** Returns active roles for a specific space, including local mock overrides. */
  public Set<AtlasRole> rolesForSpace(String spaceId) {
    if (roleOverride != null) {
      return Set.of(roleOverride);
    }
    return memberships.stream()
        .filter(membership -> membership.getSpaceId().equals(spaceId))
        .map(SpaceMembership::getRole)
        .collect(java.util.stream.Collectors.toUnmodifiableSet());
  }

  /** Returns true when the user has at least one active membership in the space. */
  public boolean hasMembershipIn(String spaceId) {
    return !rolesForSpace(spaceId).isEmpty();
  }
}
