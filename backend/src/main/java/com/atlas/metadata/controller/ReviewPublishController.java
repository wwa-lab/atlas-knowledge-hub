package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.CreateWikiPublishRequest;
import com.atlas.metadata.dto.ReviewQueuesResponse;
import com.atlas.metadata.dto.WikiPageResponse;
import com.atlas.metadata.service.ReviewPublishService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for review queues and trusted Wiki publication. */
@RestController
@RequestMapping("/api")
public class ReviewPublishController {

  private final ReviewPublishService reviewPublishService;

  /** Creates the controller. */
  public ReviewPublishController(ReviewPublishService reviewPublishService) {
    this.reviewPublishService = reviewPublishService;
  }

  /** Returns Processing Center review queues for a Knowledge Space. */
  @GetMapping("/spaces/{spaceId}/review-queues")
  public ApiEnvelope<ReviewQueuesResponse> getReviewQueues(@PathVariable String spaceId) {
    return ApiEnvelope.ok(reviewPublishService.getReviewQueues(spaceId));
  }

  /** Publishes approved Markdown metadata to a Wiki page. */
  @PostMapping("/files/{fileId}/publish")
  public ResponseEntity<ApiEnvelope<WikiPageResponse>> publishFile(
      @PathVariable String fileId, @Valid @RequestBody CreateWikiPublishRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiEnvelope.ok(reviewPublishService.publishFile(fileId, request)));
  }

  /** Lists published Wiki pages for a Knowledge Space. */
  @GetMapping("/spaces/{spaceId}/wiki-pages")
  public ApiEnvelope<List<WikiPageResponse>> listWikiPages(@PathVariable String spaceId) {
    return ApiEnvelope.ok(reviewPublishService.listPublishedWikiPages(spaceId));
  }

  /** Gets one published Wiki page. */
  @GetMapping("/wiki-pages/{wikiPageId}")
  public ApiEnvelope<WikiPageResponse> getWikiPage(@PathVariable String wikiPageId) {
    return ApiEnvelope.ok(reviewPublishService.getPublishedWikiPage(wikiPageId));
  }
}
