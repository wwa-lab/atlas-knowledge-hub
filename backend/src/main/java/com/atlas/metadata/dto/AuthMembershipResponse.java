package com.atlas.metadata.dto;

/** Current user's membership summary for one Knowledge Space. */
public record AuthMembershipResponse(String id, String spaceId, String spaceName, String role, String status) {}
