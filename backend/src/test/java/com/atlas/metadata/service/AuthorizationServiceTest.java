package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.domain.AtlasUser;
import com.atlas.metadata.domain.SpaceMembership;
import com.atlas.metadata.enums.AtlasCapability;
import com.atlas.metadata.enums.AtlasRole;
import com.atlas.metadata.enums.MembershipStatus;
import com.atlas.metadata.enums.UserStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AuthorizationServiceTest {

  private final AuthorizationService service = new AuthorizationService();

  @Test
  void viewerCanReadButCannotWrite() {
    CurrentUserContext viewer = context(AtlasRole.VIEWER);

    assertThat(service.can(viewer, AtlasCapability.CONTENT_READ, "space-a")).isTrue();
    assertThat(service.can(viewer, AtlasCapability.CONTENT_WRITE, "space-a")).isFalse();
  }

  @Test
  void ownerCanManageMembers() {
    CurrentUserContext owner = context(AtlasRole.SPACE_OWNER);

    assertThat(service.can(owner, AtlasCapability.MEMBER_MANAGE, "space-a")).isTrue();
  }

  @Test
  void platformAdminCanManageSettingsWithoutSpace() {
    AtlasUser user =
        AtlasUser.create(
            "platform",
            "platform@example.test",
            "Platform",
            UserStatus.ACTIVE,
            new String[] {"PLATFORM_ADMIN"},
            "mock:platform",
            OffsetDateTime.now());
    CurrentUserContext admin = new CurrentUserContext(user, List.of(), Set.of(AtlasRole.PLATFORM_ADMIN), null);

    assertThat(service.can(admin, AtlasCapability.SETTINGS_MANAGE, null)).isTrue();
  }

  private CurrentUserContext context(AtlasRole role) {
    OffsetDateTime now = OffsetDateTime.now();
    AtlasUser user =
        AtlasUser.create(
            "user-" + role.name(),
            role.name().toLowerCase() + "@example.test",
            role.name(),
            UserStatus.ACTIVE,
            new String[0],
            "mock:" + role.name(),
            now);
    SpaceMembership membership =
        SpaceMembership.create(
            "membership-" + role.name(), "space-a", user.getId(), role, MembershipStatus.ACTIVE, "owner", now);
    return new CurrentUserContext(user, List.of(membership), Set.of(), null);
  }
}
