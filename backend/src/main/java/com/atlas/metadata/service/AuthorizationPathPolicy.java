package com.atlas.metadata.service;

import com.atlas.metadata.domain.Batch;
import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.enums.AtlasCapability;
import com.atlas.metadata.repository.AskRunRepository;
import com.atlas.metadata.repository.BatchRepository;
import com.atlas.metadata.repository.ConversionRunRepository;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.GraphProjectionRunRepository;
import com.atlas.metadata.repository.ParserRunRepository;
import com.atlas.metadata.repository.StorageOperationRepository;
import com.atlas.metadata.repository.VectorRunRepository;
import com.atlas.metadata.repository.WikiGenerationRunRepository;
import com.atlas.metadata.repository.WikiPageRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** Maps API paths to RBAC requirements without coupling controllers to auth details. */
@Service
public class AuthorizationPathPolicy {

  private final BatchRepository batchRepository;
  private final FileItemRepository fileItemRepository;
  private final WikiPageRepository wikiPageRepository;
  private final AskRunRepository askRunRepository;
  private final GraphProjectionRunRepository graphProjectionRunRepository;
  private final VectorRunRepository vectorRunRepository;
  private final ConversionRunRepository conversionRunRepository;
  private final ParserRunRepository parserRunRepository;
  private final StorageOperationRepository storageOperationRepository;
  private final WikiGenerationRunRepository wikiGenerationRunRepository;

  public AuthorizationPathPolicy(
      BatchRepository batchRepository,
      FileItemRepository fileItemRepository,
      WikiPageRepository wikiPageRepository,
      AskRunRepository askRunRepository,
      GraphProjectionRunRepository graphProjectionRunRepository,
      VectorRunRepository vectorRunRepository,
      ConversionRunRepository conversionRunRepository,
      ParserRunRepository parserRunRepository,
      StorageOperationRepository storageOperationRepository,
      WikiGenerationRunRepository wikiGenerationRunRepository) {
    this.batchRepository = batchRepository;
    this.fileItemRepository = fileItemRepository;
    this.wikiPageRepository = wikiPageRepository;
    this.askRunRepository = askRunRepository;
    this.graphProjectionRunRepository = graphProjectionRunRepository;
    this.vectorRunRepository = vectorRunRepository;
    this.conversionRunRepository = conversionRunRepository;
    this.parserRunRepository = parserRunRepository;
    this.storageOperationRepository = storageOperationRepository;
    this.wikiGenerationRunRepository = wikiGenerationRunRepository;
  }

  /** Derives the least-powerful requirement that can serve the request. */
  public Optional<AuthRequirement> requirementFor(HttpServletRequest request) {
    String path = request.getRequestURI();
    if (!path.startsWith("/api/") && !path.equals("/api")) {
      return Optional.empty();
    }
    List<String> segments = segments(path);
    if (segments.isEmpty()) {
      return Optional.of(AuthRequirement.authenticated());
    }
    String method = request.getMethod();
    String first = segments.get(0);
    if ("auth".equals(first)) {
      return Optional.of(AuthRequirement.authenticated());
    }
    if (isAdapterOrSettingsPath(segments)) {
      return Optional.of(AuthRequirement.platform(AtlasCapability.SETTINGS_MANAGE));
    }
    if ("spaces".equals(first)) {
      return Optional.of(spaceRequirement(method, segments));
    }
    if ("batches".equals(first) && segments.size() >= 2) {
      return batchSpaceId(segments.get(1))
          .map(spaceId -> AuthRequirement.space(batchCapability(method, segments), spaceId, true))
          .or(() -> Optional.of(AuthRequirement.space(AtlasCapability.CONTENT_READ, "__missing__", true)));
    }
    if ("files".equals(first) && segments.size() >= 2) {
      return fileSpaceId(segments.get(1))
          .map(spaceId -> AuthRequirement.space(fileCapability(method, segments), spaceId, true))
          .or(() -> Optional.of(AuthRequirement.space(AtlasCapability.CONTENT_READ, "__missing__", true)));
    }
    if ("wiki-pages".equals(first) && segments.size() >= 2) {
      return wikiPageRepository
          .findById(segments.get(1))
          .map(page -> AuthRequirement.space(AtlasCapability.CONTENT_READ, page.getSpaceId(), true))
          .or(() -> Optional.of(AuthRequirement.space(AtlasCapability.CONTENT_READ, "__missing__", true)));
    }
    if ("ask-runs".equals(first) && segments.size() >= 2) {
      return askRunRepository
          .findById(segments.get(1))
          .map(run -> AuthRequirement.space(AtlasCapability.CONTENT_READ, run.getSpaceId(), true))
          .or(() -> Optional.of(AuthRequirement.space(AtlasCapability.CONTENT_READ, "__missing__", true)));
    }
    if ("graph".equals(first) && segments.size() >= 3 && "projection-runs".equals(segments.get(1))) {
      return graphProjectionRunRepository
          .findById(segments.get(2))
          .map(run -> AuthRequirement.space(AtlasCapability.CONTENT_READ, run.getSpaceId(), true))
          .or(() -> Optional.of(AuthRequirement.space(AtlasCapability.CONTENT_READ, "__missing__", true)));
    }
    if ("vector-runs".equals(first) && segments.size() >= 2) {
      return vectorRunRepository
          .findById(segments.get(1))
          .map(run -> AuthRequirement.space(AtlasCapability.CONTENT_READ, run.getSpaceId(), true))
          .or(() -> Optional.of(AuthRequirement.space(AtlasCapability.CONTENT_READ, "__missing__", true)));
    }
    if ("conversion-runs".equals(first) && segments.size() >= 2) {
      return conversionRunRepository.findById(segments.get(1)).flatMap(run -> batchReadRequirement(run.getBatchId()));
    }
    if ("parser-runs".equals(first) && segments.size() >= 2) {
      return parserRunRepository.findById(segments.get(1)).flatMap(run -> batchReadRequirement(run.getBatchId()));
    }
    if ("storage-operations".equals(first) && segments.size() >= 2) {
      return storageOperationRepository
          .findById(segments.get(1))
          .flatMap(run -> batchReadRequirement(run.getBatchId()));
    }
    return Optional.of(AuthRequirement.authenticated());
  }

  private AuthRequirement spaceRequirement(String method, List<String> segments) {
    if (segments.size() == 1) {
      if ("POST".equals(method)) {
        return AuthRequirement.platform(AtlasCapability.SPACE_MANAGE);
      }
      return AuthRequirement.authenticated();
    }
    String spaceId = segments.get(1);
    if (segments.size() >= 3 && "members".equals(segments.get(2))) {
      return AuthRequirement.space(AtlasCapability.MEMBER_MANAGE, spaceId, false);
    }
    if ("GET".equals(method)) {
      if (segments.size() >= 3 && "audit-events".equals(segments.get(2))) {
        return AuthRequirement.space(AtlasCapability.GOVERNANCE_READ, spaceId, false);
      }
      if (segments.size() >= 3 && "wiki-page-issues".equals(segments.get(2))) {
        return AuthRequirement.space(AtlasCapability.GOVERNANCE_READ, spaceId, false);
      }
      return AuthRequirement.space(AtlasCapability.CONTENT_READ, spaceId, false);
    }
    if (segments.size() >= 3
        && ("graph".equals(segments.get(2))
            || "ask".equals(segments.get(2))
            || "vector-runs".equals(segments.get(2))
            || "vector-query".equals(segments.get(2))
            || "wiki-ingest-runs".equals(segments.get(2))
            || "wiki-linkify-lint-runs".equals(segments.get(2))
            || "downstream-refresh".equals(segments.get(2)))) {
      return AuthRequirement.space(AtlasCapability.KNOWLEDGE_OPERATE, spaceId, false);
    }
    return AuthRequirement.space(AtlasCapability.CONTENT_WRITE, spaceId, false);
  }

  private Optional<AuthRequirement> batchReadRequirement(String batchId) {
    return batchSpaceId(batchId)
        .map(spaceId -> AuthRequirement.space(AtlasCapability.CONTENT_READ, spaceId, true))
        .or(() -> Optional.of(AuthRequirement.space(AtlasCapability.CONTENT_READ, "__missing__", true)));
  }

  private AtlasCapability batchCapability(String method, List<String> segments) {
    if ("GET".equals(method)) {
      return AtlasCapability.CONTENT_READ;
    }
    if (segments.size() >= 3
        && ("conversion-runs".equals(segments.get(2))
            || "parser-runs".equals(segments.get(2))
            || "storage-operations".equals(segments.get(2)))) {
      return AtlasCapability.CONTENT_WRITE;
    }
    return AtlasCapability.CONTENT_WRITE;
  }

  private AtlasCapability fileCapability(String method, List<String> segments) {
    if ("GET".equals(method)) {
      return AtlasCapability.CONTENT_READ;
    }
    return AtlasCapability.KNOWLEDGE_OPERATE;
  }

  private Optional<String> fileSpaceId(String fileId) {
    return fileItemRepository.findById(fileId).map(FileItem::getBatchId).flatMap(this::batchSpaceId);
  }

  private Optional<String> batchSpaceId(String batchId) {
    return batchRepository.findById(batchId).map(Batch::getSpaceId);
  }

  private boolean isAdapterOrSettingsPath(List<String> segments) {
    String first = segments.get(0);
    return first.endsWith("-adapters")
        || "model-configurations".equals(first)
        || "model-runs".equals(first);
  }

  private List<String> segments(String path) {
    String withoutApi = path.equals("/api") ? "" : path.substring("/api/".length());
    return Arrays.stream(withoutApi.split("/")).filter(segment -> !segment.isBlank()).toList();
  }
}
