package com.atlas.metadata.controller;

import com.atlas.metadata.dto.ApiEnvelope;
import com.atlas.metadata.dto.CreateReviewRequest;
import com.atlas.metadata.dto.ReviewResponse;
import com.atlas.metadata.service.ReviewService;
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

/** REST controller for append-only review history. */
@RestController
@RequestMapping("/api/files/{fileId}/reviews")
public class ReviewController {

  private final ReviewService reviewService;

  /** Creates the controller. */
  public ReviewController(ReviewService reviewService) {
    this.reviewService = reviewService;
  }

  /** Appends a review record and updates the file review status. */
  @PostMapping
  public ResponseEntity<ApiEnvelope<ReviewResponse>> appendReview(
      @PathVariable String fileId, @Valid @RequestBody CreateReviewRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiEnvelope.ok(reviewService.appendFileReview(fileId, request)));
  }

  /** Lists chronological review history for a file. */
  @GetMapping
  public ApiEnvelope<List<ReviewResponse>> listReviews(@PathVariable String fileId) {
    return ApiEnvelope.ok(reviewService.listFileReviews(fileId));
  }
}
