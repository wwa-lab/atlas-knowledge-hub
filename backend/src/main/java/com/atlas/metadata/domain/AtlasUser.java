package com.atlas.metadata.domain;

import com.atlas.metadata.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Safe Atlas user profile used for auth/RBAC decisions. */
@Entity
@Table(name = "atlas_user", schema = "atlas")
public class AtlasUser {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(nullable = false, columnDefinition = "text")
  private String email;

  @Column(name = "display_name", nullable = false, columnDefinition = "text")
  private String displayName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private UserStatus status;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "global_roles", nullable = false, columnDefinition = "text[]")
  private String[] globalRoles;

  @Column(name = "auth_subject", columnDefinition = "text")
  private String authSubject;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  protected AtlasUser() {}

  /** Creates a sample-safe Atlas user. */
  public static AtlasUser create(
      String id,
      String email,
      String displayName,
      UserStatus status,
      String[] globalRoles,
      String authSubject,
      OffsetDateTime now) {
    AtlasUser user = new AtlasUser();
    user.id = id;
    user.email = email;
    user.displayName = displayName;
    user.status = status;
    user.globalRoles = globalRoles == null ? new String[0] : globalRoles.clone();
    user.authSubject = authSubject;
    user.createdAt = now;
    user.updatedAt = now;
    return user;
  }

  public String getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getDisplayName() {
    return displayName;
  }

  public UserStatus getStatus() {
    return status;
  }

  public String[] getGlobalRoles() {
    return globalRoles == null ? new String[0] : globalRoles.clone();
  }

  public String getAuthSubject() {
    return authSubject;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
}
