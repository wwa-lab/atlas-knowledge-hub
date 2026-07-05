package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.AuthMeResponse;
import com.atlas.metadata.service.CurrentUserContext;
import com.atlas.metadata.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Current user context API. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final CurrentUserService currentUserService;

  public AuthController(CurrentUserService currentUserService) {
    this.currentUserService = currentUserService;
  }

  @GetMapping("/me")
  public ApiEnvelope<AuthMeResponse> me(HttpServletRequest request) {
    CurrentUserContext context =
        (CurrentUserContext) request.getAttribute(CurrentUserService.CURRENT_USER_ATTRIBUTE);
    return ApiEnvelope.ok(currentUserService.me(context));
  }
}
