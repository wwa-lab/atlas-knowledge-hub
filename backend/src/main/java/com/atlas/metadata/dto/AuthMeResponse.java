package com.atlas.metadata.dto;

import java.util.List;

/** Authenticated user context exposed to the frontend. */
public record AuthMeResponse(
    AuthUserResponse user,
    String activeSpaceId,
    List<AuthMembershipResponse> memberships,
    List<String> capabilities) {}
