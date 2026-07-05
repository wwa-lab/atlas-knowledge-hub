package com.atlas.metadata.service;

import com.atlas.metadata.enums.AtlasCapability;

/** Authorization requirement derived from the requested API path. */
public record AuthRequirement(AtlasCapability capability, String spaceId, boolean safeNotFoundOnNoMembership) {

  public static AuthRequirement authenticated() {
    return new AuthRequirement(AtlasCapability.AUTHENTICATED, null, false);
  }

  public static AuthRequirement platform(AtlasCapability capability) {
    return new AuthRequirement(capability, null, false);
  }

  public static AuthRequirement space(
      AtlasCapability capability, String spaceId, boolean safeNotFoundOnNoMembership) {
    return new AuthRequirement(capability, spaceId, safeNotFoundOnNoMembership);
  }
}
