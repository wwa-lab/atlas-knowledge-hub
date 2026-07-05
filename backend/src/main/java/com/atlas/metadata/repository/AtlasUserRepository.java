package com.atlas.metadata.repository;

import com.atlas.metadata.domain.AtlasUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for safe Atlas users. */
public interface AtlasUserRepository extends JpaRepository<AtlasUser, String> {

  /** Finds a user by email. */
  Optional<AtlasUser> findByEmail(String email);
}
