package com.atlas.metadata.dto;

import java.util.List;

/** Safe current-user profile returned by the auth boundary. */
public record AuthUserResponse(
    String id, String email, String displayName, String status, List<String> globalRoles) {}
