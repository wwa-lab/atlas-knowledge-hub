package com.atlas.metadata.service;

import com.atlas.metadata.domain.AtlasUser;
import com.atlas.metadata.domain.SpaceMembership;
import com.atlas.metadata.dto.CreateSpaceMembershipRequest;
import com.atlas.metadata.dto.SpaceMembershipResponse;
import com.atlas.metadata.dto.UpdateSpaceMembershipRequest;
import com.atlas.metadata.enums.AtlasRole;
import com.atlas.metadata.enums.AuditCategory;
import com.atlas.metadata.enums.AuditResult;
import com.atlas.metadata.enums.AuditSeverity;
import com.atlas.metadata.enums.MembershipStatus;
import com.atlas.metadata.enums.UserStatus;
import com.atlas.metadata.exception.ConflictException;
import com.atlas.metadata.exception.NotFoundException;
import com.atlas.metadata.repository.AtlasUserRepository;
import com.atlas.metadata.repository.SpaceMembershipRepository;
import com.atlas.metadata.repository.SpaceRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Owner-managed space membership service with last-owner protection. */
@Service
public class SpaceMembershipService {

  private final SpaceMembershipRepository membershipRepository;
  private final AtlasUserRepository userRepository;
  private final SpaceRepository spaceRepository;
  private final AuditLogService auditLogService;

  public SpaceMembershipService(
      SpaceMembershipRepository membershipRepository,
      AtlasUserRepository userRepository,
      SpaceRepository spaceRepository) {
    this(membershipRepository, userRepository, spaceRepository, null);
  }

  @org.springframework.beans.factory.annotation.Autowired
  public SpaceMembershipService(
      SpaceMembershipRepository membershipRepository,
      AtlasUserRepository userRepository,
      SpaceRepository spaceRepository,
      AuditLogService auditLogService) {
    this.membershipRepository = membershipRepository;
    this.userRepository = userRepository;
    this.spaceRepository = spaceRepository;
    this.auditLogService = auditLogService;
  }

  /** Lists memberships for a space. */
  public List<SpaceMembershipResponse> list(String spaceId) {
    requireSpace(spaceId);
    return membershipRepository.findBySpaceIdOrderByCreatedAtAscIdAsc(spaceId).stream()
        .map(this::toResponse)
        .toList();
  }

  /** Adds or reactivates a space membership. */
  @Transactional
  public SpaceMembershipResponse create(
      String spaceId, CreateSpaceMembershipRequest request, CurrentUserContext actor) {
    requireSpace(spaceId);
    OffsetDateTime now = OffsetDateTime.now();
    AtlasUser user =
        userRepository
            .findByEmail(request.email())
            .orElseGet(
                () ->
                    userRepository.save(
                        AtlasUser.create(
                            "user-" + UUID.randomUUID(),
                            request.email(),
                            request.displayName(),
                            UserStatus.ACTIVE,
                            new String[0],
                            "mock:" + request.email(),
                            now)));
    SpaceMembership membership =
        membershipRepository
            .findBySpaceIdAndUserId(spaceId, user.getId())
            .map(existing -> reactivateOrReject(existing, request, now))
            .orElseGet(
                () ->
                    SpaceMembership.create(
                        "membership-" + UUID.randomUUID(),
                        spaceId,
                        user.getId(),
                        request.role(),
                        request.status(),
                        actor.user().getId(),
                        now));
    SpaceMembership saved = membershipRepository.save(membership);
    auditMembership(
        "MEMBERSHIP_ADDED",
        spaceId,
        actor,
        saved.getUserId(),
        AuditResult.SUCCEEDED,
        AuditSeverity.NOTICE,
        "Space membership added or reactivated.",
        Map.of("role", saved.getRole().name(), "status", saved.getStatus().name()));
    return toResponse(saved);
  }

  /** Updates a user's role/status in a space. */
  @Transactional
  public SpaceMembershipResponse update(
      String spaceId, String membershipId, UpdateSpaceMembershipRequest request, CurrentUserContext actor) {
    SpaceMembership membership =
        membershipRepository
            .findBySpaceIdAndId(spaceId, membershipId)
            .orElseThrow(() -> new NotFoundException("Space membership not found."));
    guardLastOwnerChange(membership, request.role(), request.status(), actor);
    membership.update(request.role(), request.status(), OffsetDateTime.now());
    SpaceMembership saved = membershipRepository.save(membership);
    auditMembership(
        "MEMBERSHIP_UPDATED",
        spaceId,
        actor,
        membership.getUserId(),
        AuditResult.SUCCEEDED,
        AuditSeverity.NOTICE,
        "Space membership updated.",
        Map.of("role", saved.getRole().name(), "status", saved.getStatus().name()));
    return toResponse(saved);
  }

  /** Updates a user's role/status in a space. */
  @Transactional
  public SpaceMembershipResponse update(
      String spaceId, String membershipId, UpdateSpaceMembershipRequest request) {
    return update(spaceId, membershipId, request, null);
  }

  /** Marks a user as removed from a space. */
  @Transactional
  public void remove(String spaceId, String membershipId, CurrentUserContext actor) {
    SpaceMembership membership =
        membershipRepository
            .findBySpaceIdAndId(spaceId, membershipId)
            .orElseThrow(() -> new NotFoundException("Space membership not found."));
    guardLastOwnerChange(membership, membership.getRole(), MembershipStatus.REMOVED, actor);
    membership.update(membership.getRole(), MembershipStatus.REMOVED, OffsetDateTime.now());
    membershipRepository.save(membership);
    auditMembership(
        "MEMBERSHIP_REMOVED",
        spaceId,
        actor,
        membership.getUserId(),
        AuditResult.SUCCEEDED,
        AuditSeverity.WARNING,
        "Space membership removed.",
        Map.of("role", membership.getRole().name(), "status", membership.getStatus().name()));
  }

  /** Marks a user as removed from a space. */
  @Transactional
  public void remove(String spaceId, String membershipId) {
    remove(spaceId, membershipId, null);
  }

  private SpaceMembership reactivateOrReject(
      SpaceMembership existing, CreateSpaceMembershipRequest request, OffsetDateTime now) {
    if (existing.getStatus() != MembershipStatus.REMOVED) {
      throw new ConflictException("User already has a space membership.");
    }
    existing.update(request.role(), request.status(), now);
    return existing;
  }

  private void guardLastOwnerChange(
      SpaceMembership membership,
      AtlasRole nextRole,
      MembershipStatus nextStatus,
      CurrentUserContext actor) {
    boolean removesActiveOwner =
        membership.getRole() == AtlasRole.SPACE_OWNER
            && membership.getStatus() == MembershipStatus.ACTIVE
            && (nextRole != AtlasRole.SPACE_OWNER || nextStatus != MembershipStatus.ACTIVE);
    if (!removesActiveOwner) {
      return;
    }
    long activeOwners =
        membershipRepository.countBySpaceIdAndRoleAndStatus(
            membership.getSpaceId(), AtlasRole.SPACE_OWNER, MembershipStatus.ACTIVE);
    if (activeOwners <= 1) {
      auditMembership(
          "MEMBERSHIP_LAST_OWNER_GUARD",
          membership.getSpaceId(),
          actor,
          membership.getUserId(),
          AuditResult.CONFLICT,
          AuditSeverity.WARNING,
          "Last active space owner protection blocked membership change.",
          Map.of("nextRole", nextRole.name(), "nextStatus", nextStatus.name()));
      throw new ConflictException("At least one active space owner is required.");
    }
  }

  private void auditMembership(
      String action,
      String spaceId,
      CurrentUserContext actor,
      String targetUserId,
      AuditResult result,
      AuditSeverity severity,
      String summary,
      Map<String, Object> metadata) {
    if (auditLogService == null) {
      return;
    }
    auditLogService.recordIfEnabled(
        new AuditLogService.CreateAuditEventCommand(
            actor == null ? null : actor.user().getId(),
            actor == null ? "system" : actor.user().getDisplayName(),
            action,
            AuditCategory.MEMBERSHIP,
            result,
            severity,
            spaceId,
            "space_membership",
            targetUserId,
            null,
            summary,
            metadata));
  }

  private void requireSpace(String spaceId) {
    if (!spaceRepository.existsById(spaceId)) {
      throw new NotFoundException("Knowledge Space not found.");
    }
  }

  private SpaceMembershipResponse toResponse(SpaceMembership membership) {
    AtlasUser user =
        userRepository
            .findById(membership.getUserId())
            .orElseThrow(() -> new NotFoundException("Atlas user not found."));
    return new SpaceMembershipResponse(
        membership.getId(),
        membership.getSpaceId(),
        user.getId(),
        user.getEmail(),
        user.getDisplayName(),
        membership.getRole().name(),
        membership.getStatus().name(),
        membership.getCreatedAt(),
        membership.getUpdatedAt());
  }
}
