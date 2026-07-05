package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.CreateSpaceMembershipRequest;
import com.atlas.metadata.dto.SpaceMembershipResponse;
import com.atlas.metadata.dto.UpdateSpaceMembershipRequest;
import com.atlas.metadata.service.CurrentUserContext;
import com.atlas.metadata.service.CurrentUserService;
import com.atlas.metadata.service.SpaceMembershipService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Space owner membership management API. */
@RestController
@RequestMapping("/api/spaces/{spaceId}/members")
public class SpaceMembershipController {

  private final SpaceMembershipService membershipService;

  public SpaceMembershipController(SpaceMembershipService membershipService) {
    this.membershipService = membershipService;
  }

  @GetMapping
  public ApiEnvelope<List<SpaceMembershipResponse>> list(@PathVariable String spaceId) {
    return ApiEnvelope.ok(membershipService.list(spaceId));
  }

  @PostMapping
  public ResponseEntity<ApiEnvelope<SpaceMembershipResponse>> create(
      @PathVariable String spaceId,
      @Valid @RequestBody CreateSpaceMembershipRequest request,
      HttpServletRequest httpRequest) {
    CurrentUserContext actor =
        (CurrentUserContext) httpRequest.getAttribute(CurrentUserService.CURRENT_USER_ATTRIBUTE);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiEnvelope.ok(membershipService.create(spaceId, request, actor)));
  }

  @PutMapping("/{userId}")
  public ApiEnvelope<SpaceMembershipResponse> update(
      @PathVariable String spaceId,
      @PathVariable String userId,
      @Valid @RequestBody UpdateSpaceMembershipRequest request) {
    return ApiEnvelope.ok(membershipService.update(spaceId, userId, request));
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<ApiEnvelope<Void>> remove(
      @PathVariable String spaceId, @PathVariable String userId) {
    membershipService.remove(spaceId, userId);
    return ResponseEntity.ok(ApiEnvelope.ok(null));
  }
}
