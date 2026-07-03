package com.atlas.metadata.service;

import com.atlas.metadata.domain.Space;
import com.atlas.metadata.dto.CreateSpaceRequest;
import com.atlas.metadata.dto.SpaceResponse;
import com.atlas.metadata.dto.mapping.SpaceMapper;
import com.atlas.metadata.enums.SpaceStatus;
import com.atlas.metadata.exception.ConflictException;
import com.atlas.metadata.exception.NotFoundException;
import com.atlas.metadata.repository.SpaceRepository;
import java.text.Normalizer;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for Knowledge Space metadata. */
@Service
public class SpaceService {

  private final SpaceRepository spaceRepository;
  private final Clock clock;

  /** Creates the service. */
  @Autowired
  public SpaceService(SpaceRepository spaceRepository) {
    this(spaceRepository, Clock.systemUTC());
  }

  SpaceService(SpaceRepository spaceRepository, Clock clock) {
    this.spaceRepository = spaceRepository;
    this.clock = clock;
  }

  /** Lists spaces, optionally filtered by status. */
  @Transactional(readOnly = true)
  public Page<SpaceResponse> listSpaces(SpaceStatus status, Pageable pageable) {
    Page<Space> spaces =
        status == null ? spaceRepository.findAll(pageable) : spaceRepository.findByStatus(status, pageable);
    return spaces.map(SpaceMapper::toResponse);
  }

  /** Gets a single space by id. */
  @Transactional(readOnly = true)
  public SpaceResponse getSpace(String spaceId) {
    return SpaceMapper.toResponse(findSpace(spaceId));
  }

  /** Creates a new Knowledge Space with server-owned defaults. */
  @Transactional
  public SpaceResponse createSpace(CreateSpaceRequest request) {
    String id = slug(request.name());
    if (spaceRepository.existsById(id)) {
      throw new ConflictException("Knowledge Space already exists.");
    }
    OffsetDateTime now = OffsetDateTime.now(clock);
    Space space =
        Space.create(
            id,
            request.name(),
            request.description(),
            request.type(),
            request.indexStrategy(),
            request.owner(),
            now);
    return SpaceMapper.toResponse(spaceRepository.save(space));
  }

  /** Returns a space or throws a user-safe not-found error. */
  @Transactional(readOnly = true)
  public Space findSpace(String spaceId) {
    return spaceRepository
        .findById(spaceId)
        .orElseThrow(() -> new NotFoundException("Knowledge Space not found."));
  }

  private String slug(String value) {
    String normalized = Normalizer.normalize(value, Normalizer.Form.NFKD);
    String slug =
        normalized
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-|-$)", "");
    return slug.isBlank() ? "space" : slug;
  }
}
