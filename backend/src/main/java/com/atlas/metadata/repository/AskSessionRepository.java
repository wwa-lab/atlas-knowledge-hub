package com.atlas.metadata.repository;

import com.atlas.metadata.domain.AskSession;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for Trusted Ask sessions. */
public interface AskSessionRepository extends JpaRepository<AskSession, String> {

  /** Finds recent sessions for one Knowledge Space. */
  List<AskSession> findTop20BySpaceIdOrderByUpdatedAtDesc(String spaceId);
}
