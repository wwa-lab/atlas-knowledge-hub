package com.atlas.metadata.service;

import com.atlas.metadata.domain.AtlasUser;
import com.atlas.metadata.domain.SpaceMembership;
import com.atlas.metadata.dto.AuthMeResponse;
import com.atlas.metadata.dto.AuthMembershipResponse;
import com.atlas.metadata.dto.AuthUserResponse;
import com.atlas.metadata.enums.AtlasCapability;
import com.atlas.metadata.enums.AtlasRole;
import com.atlas.metadata.enums.MembershipStatus;
import com.atlas.metadata.enums.UserStatus;
import com.atlas.metadata.repository.AtlasUserRepository;
import com.atlas.metadata.repository.SpaceMembershipRepository;
import com.atlas.metadata.repository.SpaceRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/** Resolves mock-local Atlas users behind a replaceable auth-provider boundary. */
@Service
public class CurrentUserService {

  public static final String CURRENT_USER_ATTRIBUTE = "atlas.currentUser";
  private static final String USER_HEADER = "X-Atlas-User";
  private static final String ROLE_HEADER = "X-Atlas-Role";
  private static final Set<String> MISSING_MARKERS = Set.of("__missing__", "__anonymous__");

  private final AtlasUserRepository userRepository;
  private final SpaceMembershipRepository membershipRepository;
  private final SpaceRepository spaceRepository;
  private final AuthorizationService authorizationService;

  public CurrentUserService(
      AtlasUserRepository userRepository,
      SpaceMembershipRepository membershipRepository,
      SpaceRepository spaceRepository,
      AuthorizationService authorizationService) {
    this.userRepository = userRepository;
    this.membershipRepository = membershipRepository;
    this.spaceRepository = spaceRepository;
    this.authorizationService = authorizationService;
  }

  /** Resolves and stores the current user context on the request. */
  public Optional<CurrentUserContext> resolve(HttpServletRequest request) {
    String userId = firstHeaderValue(request, USER_HEADER).orElse("");
    if (MISSING_MARKERS.contains(userId.trim())) {
      return Optional.empty();
    }
    if (userId.isBlank()) {
      return Optional.empty();
    }
    Optional<AtlasUser> user = userRepository.findById(userId);
    if (user.isEmpty()) {
      return Optional.empty();
    }
    CurrentUserContext context =
        new CurrentUserContext(
            user.get(),
            activeMembershipsFor(user.get()),
            parseGlobalRoles(user.get()),
            firstHeaderValue(request, ROLE_HEADER).flatMap(AtlasRole::fromHeader).orElse(null));
    request.setAttribute(CURRENT_USER_ATTRIBUTE, context);
    return Optional.of(context);
  }

  /** Returns true if the resolved user is disabled. */
  public boolean isDisabled(CurrentUserContext context) {
    return context.user().getStatus() == UserStatus.DISABLED;
  }

  /** Builds the /api/auth/me response. */
  public AuthMeResponse me(CurrentUserContext context) {
    List<AuthMembershipResponse> memberships =
        context.memberships().stream()
            .map(
                membership -> {
                  String spaceName =
                      spaceRepository
                          .findById(membership.getSpaceId())
                          .map(space -> space.getName())
                          .orElse(membership.getSpaceId());
                  return new AuthMembershipResponse(
                      membership.getId(),
                      membership.getSpaceId(),
                      spaceName,
                      membership.getRole().name(),
                      membership.getStatus().name());
                })
            .toList();
    String activeSpaceId = memberships.isEmpty() ? null : memberships.get(0).spaceId();
    List<String> capabilities =
        Arrays.stream(AtlasCapability.values())
            .filter(capability -> authorizationService.can(context, capability, activeSpaceId))
            .map(Enum::name)
            .sorted()
            .toList();
    return new AuthMeResponse(
        new AuthUserResponse(
            context.user().getId(),
            context.user().getEmail(),
            context.user().getDisplayName(),
            context.user().getStatus().name(),
            context.globalRoles().stream().map(Enum::name).sorted().toList()),
        activeSpaceId,
        memberships,
        capabilities);
  }

  private List<SpaceMembership> activeMembershipsFor(AtlasUser user) {
    return membershipRepository.findByUserId(user.getId()).stream()
        .filter(membership -> membership.getStatus() == MembershipStatus.ACTIVE)
        .toList();
  }

  private Set<AtlasRole> parseGlobalRoles(AtlasUser user) {
    return Arrays.stream(user.getGlobalRoles())
        .filter(Objects::nonNull)
        .map(AtlasRole::fromHeader)
        .flatMap(Optional::stream)
        .collect(Collectors.toUnmodifiableSet());
  }

  private Optional<String> firstHeaderValue(HttpServletRequest request, String headerName) {
    Enumeration<String> values = request.getHeaders(headerName);
    if (values == null) {
      return Optional.empty();
    }
    List<String> allValues = Collections.list(values);
    if (allValues.stream().anyMatch(value -> MISSING_MARKERS.contains(value.trim()))) {
      return Optional.of("__missing__");
    }
    return allValues.stream().filter(value -> value != null && !value.isBlank()).findFirst();
  }
}
