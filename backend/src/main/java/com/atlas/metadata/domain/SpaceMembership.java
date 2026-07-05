package com.atlas.metadata.domain;

import com.atlas.metadata.enums.AtlasRole;
import com.atlas.metadata.enums.MembershipStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Space-scoped Atlas membership and role assignment. */
@Entity
@Table(name = "space_membership", schema = "atlas")
public class SpaceMembership {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "space_id", nullable = false, columnDefinition = "text")
  private String spaceId;

  @Column(name = "user_id", nullable = false, columnDefinition = "text")
  private String userId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private AtlasRole role;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private MembershipStatus status;

  @Column(name = "invited_by_user_id", columnDefinition = "text")
  private String invitedByUserId;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  protected SpaceMembership() {}

  /** Creates a space membership. */
  public static SpaceMembership create(
      String id,
      String spaceId,
      String userId,
      AtlasRole role,
      MembershipStatus status,
      String invitedByUserId,
      OffsetDateTime now) {
    SpaceMembership membership = new SpaceMembership();
    membership.id = id;
    membership.spaceId = spaceId;
    membership.userId = userId;
    membership.role = role;
    membership.status = status;
    membership.invitedByUserId = invitedByUserId;
    membership.createdAt = now;
    membership.updatedAt = now;
    return membership;
  }

  /** Applies a role/status update. */
  public void update(AtlasRole role, MembershipStatus status, OffsetDateTime now) {
    this.role = role;
    this.status = status;
    this.updatedAt = now;
  }

  public String getId() {
    return id;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public String getUserId() {
    return userId;
  }

  public AtlasRole getRole() {
    return role;
  }

  public MembershipStatus getStatus() {
    return status;
  }

  public String getInvitedByUserId() {
    return invitedByUserId;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
}
