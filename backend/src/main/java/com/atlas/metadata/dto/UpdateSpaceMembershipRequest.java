package com.atlas.metadata.dto;

import com.atlas.metadata.enums.AtlasRole;
import com.atlas.metadata.enums.MembershipStatus;
import jakarta.validation.constraints.NotNull;

/** Request to update a user's space role or membership status. */
public record UpdateSpaceMembershipRequest(@NotNull AtlasRole role, @NotNull MembershipStatus status) {}
