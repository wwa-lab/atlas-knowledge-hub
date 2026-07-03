package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.CreateSpaceRequest;
import com.atlas.metadata.dto.SpaceResponse;
import com.atlas.metadata.enums.SpaceStatus;
import com.atlas.metadata.service.SpaceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for Knowledge Spaces. */
@RestController
@RequestMapping("/api/spaces")
public class SpaceController {

  private final SpaceService spaceService;

  /** Creates the controller. */
  public SpaceController(SpaceService spaceService) {
    this.spaceService = spaceService;
  }

  /** Lists Knowledge Spaces. */
  @GetMapping
  public ApiEnvelope<List<SpaceResponse>> listSpaces(
      @RequestParam(required = false) SpaceStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Page<SpaceResponse> spaces =
        spaceService.listSpaces(
            status, PageRequests.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
    return ApiEnvelope.ok(spaces.getContent(), PageRequests.meta(spaces));
  }

  /** Returns detail for one Knowledge Space. */
  @GetMapping("/{spaceId}")
  public ApiEnvelope<SpaceResponse> getSpace(@PathVariable String spaceId) {
    return ApiEnvelope.ok(spaceService.getSpace(spaceId));
  }

  /** Creates a Knowledge Space. */
  @PostMapping
  public ResponseEntity<ApiEnvelope<SpaceResponse>> createSpace(
      @Valid @RequestBody CreateSpaceRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiEnvelope.ok(spaceService.createSpace(request)));
  }
}
