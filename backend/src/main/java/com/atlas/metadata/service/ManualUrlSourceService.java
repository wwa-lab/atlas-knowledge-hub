package com.atlas.metadata.service;

import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.domain.ManualUrlSource;
import com.atlas.metadata.dto.CreateBatchRequest;
import com.atlas.metadata.dto.CreateBatchRequest.InventoryFileRequest;
import com.atlas.metadata.dto.CreateBatchRequest.SourceChunkRequest;
import com.atlas.metadata.dto.CreateManualUrlSourceRequest;
import com.atlas.metadata.dto.ManualUrlSourceResponse;
import com.atlas.metadata.dto.mapping.ManualUrlSourceMapper;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ManualUrlFetchIntent;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.enums.SourceKind;
import com.atlas.metadata.enums.SourceType;
import com.atlas.metadata.exception.NotFoundException;
import com.atlas.metadata.exception.RequestValidationException;
import com.atlas.metadata.repository.FileItemRepository;
import com.atlas.metadata.repository.ManualUrlSourceRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application service for metadata-only manual URL sources. */
@Service
public class ManualUrlSourceService {

  private static final BigDecimal DEFAULT_CONFIDENCE =
      new BigDecimal("0.300").setScale(3, RoundingMode.HALF_UP);
  private static final int MAX_PATH_LENGTH = 160;
  private static final int MAX_USER_LENGTH = 80;
  private static final int MAX_TITLE_LENGTH = 120;
  private static final int MAX_DESCRIPTION_LENGTH = 500;

  private final ManualUrlSourceRepository manualUrlSourceRepository;
  private final BatchService batchService;
  private final FileItemRepository fileItemRepository;
  private final SpaceService spaceService;
  private final Clock clock;

  /** Creates the service. */
  @Autowired
  public ManualUrlSourceService(
      ManualUrlSourceRepository manualUrlSourceRepository,
      BatchService batchService,
      FileItemRepository fileItemRepository,
      SpaceService spaceService) {
    this(
        manualUrlSourceRepository,
        batchService,
        fileItemRepository,
        spaceService,
        Clock.systemUTC());
  }

  ManualUrlSourceService(
      ManualUrlSourceRepository manualUrlSourceRepository,
      BatchService batchService,
      FileItemRepository fileItemRepository,
      SpaceService spaceService,
      Clock clock) {
    this.manualUrlSourceRepository = manualUrlSourceRepository;
    this.batchService = batchService;
    this.fileItemRepository = fileItemRepository;
    this.spaceService = spaceService;
    this.clock = clock;
  }

  /** Registers a metadata-only manual URL source. */
  @Transactional
  public ManualUrlSourceResponse create(String spaceId, CreateManualUrlSourceRequest request) {
    spaceService.findSpace(spaceId);
    SafeUrl safeUrl = validateUrl(request == null ? null : request.url());
    String urlHash = hash(safeUrl.displayUrl());
    return manualUrlSourceRepository
        .findBySpaceIdAndUrlHash(spaceId, urlHash)
        .map(ManualUrlSourceMapper::toResponse)
        .orElseGet(() -> createNewSource(spaceId, request, safeUrl, urlHash));
  }

  /** Lists manual URL sources for a Knowledge Space. */
  @Transactional(readOnly = true)
  public List<ManualUrlSourceResponse> list(String spaceId) {
    spaceService.findSpace(spaceId);
    return manualUrlSourceRepository.findBySpaceIdOrderByCreatedAtDesc(spaceId).stream()
        .map(ManualUrlSourceMapper::toResponse)
        .toList();
  }

  /** Gets one manual URL source by id. */
  @Transactional(readOnly = true)
  public ManualUrlSourceResponse get(String sourceId) {
    return ManualUrlSourceMapper.toResponse(findSource(sourceId));
  }

  private ManualUrlSourceResponse createNewSource(
      String spaceId, CreateManualUrlSourceRequest request, SafeUrl safeUrl, String urlHash) {
    ManualUrlFetchIntent fetchIntent =
        request.fetchIntent() == null ? ManualUrlFetchIntent.METADATA_ONLY : request.fetchIntent();
    String title = sanitizeText("title", request.title(), MAX_TITLE_LENGTH);
    String description = sanitizeText("description", request.description(), MAX_DESCRIPTION_LENGTH);
    String createdBy = sanitizeUser(request.createdBy());
    String sourceTrace = "Manual URL metadata: " + safeUrl.displayUrl();
    var batch =
        batchService.createBatch(
            spaceId,
            new CreateBatchRequest(
                "Manual URL: " + safeUrl.host(),
                SourceKind.url,
                createdBy,
                List.of(
                    new InventoryFileRequest(
                        safeUrl.displayUrl(),
                        SourceType.url,
                        FileStatus.REVIEW_REQUIRED,
                        DEFAULT_CONFIDENCE,
                        ReviewStatus.REVIEW_REQUIRED,
                        null,
                        null,
                        null,
                        "Metadata-only URL source; no remote content fetched.",
                        List.of(
                            new SourceChunkRequest(
                                safeUrl.displayUrl(),
                                null,
                                "Manual URL metadata",
                                DEFAULT_CONFIDENCE,
                                ReviewStatus.REVIEW_REQUIRED))))));
    FileItem fileItem =
        fileItemRepository.findByBatchId(batch.id()).stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Manual URL batch did not create file metadata."));
    OffsetDateTime now = OffsetDateTime.now(clock);
    ManualUrlSource source =
        ManualUrlSource.create(
            "url-src-" + UUID.randomUUID().toString().substring(0, 8),
            spaceId,
            urlHash,
            safeUrl.displayUrl(),
            safeUrl.host(),
            title,
            description,
            fetchIntent,
            sourceTrace,
            batch.id(),
            fileItem.getId(),
            createdBy,
            DEFAULT_CONFIDENCE,
            now);
    return ManualUrlSourceMapper.toResponse(manualUrlSourceRepository.save(source));
  }

  private ManualUrlSource findSource(String sourceId) {
    return manualUrlSourceRepository
        .findById(sourceId)
        .orElseThrow(() -> new NotFoundException("Manual URL source not found."));
  }

  private SafeUrl validateUrl(String rawUrl) {
    Map<String, String> fields = new LinkedHashMap<>();
    if (rawUrl == null || rawUrl.isBlank()) {
      fields.put("url", "must be a public HTTPS URL");
      throw new RequestValidationException(fields);
    }
    URI uri;
    try {
      uri = new URI(rawUrl.trim());
    } catch (URISyntaxException ex) {
      fields.put("url", "must be a public HTTPS URL");
      throw new RequestValidationException(fields);
    }
    String scheme = lower(uri.getScheme());
    String host = lower(uri.getHost());
    if (!"https".equals(scheme)) {
      fields.put("url", "must use https");
    }
    if (uri.getUserInfo() != null) {
      fields.put("url", "must not include credentials");
    }
    if (uri.getRawQuery() != null || uri.getRawFragment() != null) {
      fields.put("url", "must not include query or fragment");
    }
    if (host == null || host.isBlank() || isPrivateHost(host)) {
      fields.put("url", "must use a public host");
    }
    String path = uri.getRawPath() == null ? "" : uri.getRawPath();
    if (path.length() > MAX_PATH_LENGTH || containsSecretIndicator(path)) {
      fields.put("url", "must not include sensitive path details");
    }
    if (!fields.isEmpty()) {
      throw new RequestValidationException(fields);
    }
    String displayUrl = buildDisplayUrl(uri, host, path);
    return new SafeUrl(displayUrl, host);
  }

  private String buildDisplayUrl(URI uri, String host, String path) {
    StringBuilder display = new StringBuilder("https://").append(host);
    if (uri.getPort() > 0 && uri.getPort() != 443) {
      display.append(":").append(uri.getPort());
    }
    if (path != null && !path.isBlank()) {
      display.append(path);
    }
    return display.toString();
  }

  private boolean isPrivateHost(String host) {
    if (host.equals("localhost")
        || host.endsWith(".local")
        || host.endsWith(".internal")
        || host.endsWith(".corp")) {
      return true;
    }
    if (host.startsWith("[") || host.contains(":")) {
      return host.equals("::1") || host.startsWith("fc") || host.startsWith("fd") || host.startsWith("fe80");
    }
    String[] parts = host.split("\\.");
    if (parts.length != 4) {
      return false;
    }
    try {
      int first = Integer.parseInt(parts[0]);
      int second = Integer.parseInt(parts[1]);
      return first == 10
          || first == 127
          || (first == 172 && second >= 16 && second <= 31)
          || (first == 192 && second == 168)
          || (first == 169 && second == 254);
    } catch (NumberFormatException ex) {
      return false;
    }
  }

  private String sanitizeText(String field, String value, int maxLength) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String sanitized = value.replaceAll("[\\p{Cntrl}&&[^\n\t]]", " ").trim();
    if (containsSecretIndicator(sanitized)) {
      throw new RequestValidationException(Map.of(field, "must not include sensitive values"));
    }
    return sanitized.length() > maxLength ? sanitized.substring(0, maxLength) : sanitized;
  }

  private String sanitizeUser(String value) {
    String sanitized = sanitizeText("createdBy", value, MAX_USER_LENGTH);
    return sanitized == null ? "frontend-user" : sanitized;
  }

  private boolean containsSecretIndicator(String value) {
    String lower = value.toLowerCase(Locale.ROOT);
    return lower.contains("password")
        || lower.contains("passwd")
        || lower.contains("token")
        || lower.contains("secret")
        || lower.contains("cookie")
        || lower.contains("apikey")
        || lower.contains("api_key");
  }

  private String hash(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 is required for URL hash generation.", ex);
    }
  }

  private String lower(String value) {
    return value == null ? null : value.toLowerCase(Locale.ROOT);
  }

  record SafeUrl(String displayUrl, String host) {}
}
