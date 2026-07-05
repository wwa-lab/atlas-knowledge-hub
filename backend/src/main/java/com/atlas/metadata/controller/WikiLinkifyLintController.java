package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.CreateWikiLinkifyLintRunRequest;
import com.atlas.metadata.dto.WikiLinkifyLintRunResponse;
import com.atlas.metadata.service.WikiLinkifyLintService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for deterministic Wiki linkify/lint runs. */
@RestController
@RequestMapping("/api")
public class WikiLinkifyLintController {

  private final WikiLinkifyLintService wikiLinkifyLintService;

  /** Creates the controller. */
  public WikiLinkifyLintController(WikiLinkifyLintService wikiLinkifyLintService) {
    this.wikiLinkifyLintService = wikiLinkifyLintService;
  }

  /** Starts a deterministic Wiki linkify/lint run. */
  @PostMapping("/spaces/{spaceId}/wiki-linkify-lint-runs")
  public ResponseEntity<ApiEnvelope<WikiLinkifyLintRunResponse>> startLinkifyLintRun(
      @PathVariable String spaceId, @RequestBody(required = false) CreateWikiLinkifyLintRunRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiEnvelope.ok(wikiLinkifyLintService.startLinkifyLintRun(spaceId, request)));
  }
}
