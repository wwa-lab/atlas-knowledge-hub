package com.atlas.metadata.repository;

import com.atlas.metadata.domain.SpaceMembership;
import com.atlas.metadata.enums.AtlasRole;
import com.atlas.metadata.enums.MembershipStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for space memberships. */
public interface SpaceMembershipRepository extends JpaRepository<SpaceMembership, String> {

  /** Finds memberships for one user. */
  List<SpaceMembership> findByUserId(String userId);

  /** Finds memberships in one space. */
  List<SpaceMembership> findBySpaceIdOrderByCreatedAtAscIdAsc(String spaceId);

  /** Finds one user's membership in a space. */
  Optional<SpaceMembership> findBySpaceIdAndUserId(String spaceId, String userId);

  /** Finds one membership by id inside a space. */
  Optional<SpaceMembership> findBySpaceIdAndId(String spaceId, String id);

  /** Counts active memberships for a role in one space. */
  long countBySpaceIdAndRoleAndStatus(String spaceId, AtlasRole role, MembershipStatus status);
}
