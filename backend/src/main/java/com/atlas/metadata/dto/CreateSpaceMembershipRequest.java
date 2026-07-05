package com.atlas.metadata.dto;

import com.atlas.metadata.enums.AtlasRole;
import com.atlas.metadata.enums.MembershipStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Request to add or invite a user into a Knowledge Space. */
public record CreateSpaceMembershipRequest(
    @NotBlank @Email String email,
    @NotBlank String displayName,
    @NotNull AtlasRole role,
    @NotNull MembershipStatus status) {}
