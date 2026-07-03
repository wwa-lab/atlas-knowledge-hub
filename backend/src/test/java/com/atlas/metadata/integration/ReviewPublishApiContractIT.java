package com.atlas.metadata.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atlas.metadata.controller.ReviewPublishController;
import com.atlas.metadata.dto.CreateWikiPublishRequest;
import com.atlas.metadata.dto.ReviewQueueItemResponse;
import com.atlas.metadata.dto.ReviewQueueItemResponse.ReviewQueueType;
import com.atlas.metadata.dto.ReviewQueueRepresentativeResponse;
import com.atlas.metadata.dto.ReviewQueuesResponse;
import com.atlas.metadata.dto.WikiPageResponse;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.exception.ConflictException;
import com.atlas.metadata.service.ReviewPublishService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** API contract tests for review queues and Wiki publication. */
@WebMvcTest(ReviewPublishController.class)
class ReviewPublishApiContractIT {

  @Autowired private MockMvc mockMvc;
  @MockBean private ReviewPublishService reviewPublishService;

  @Test
  void reviewQueuesReturnBlockedAndReadyCategories() throws Exception {
    when(reviewPublishService.getReviewQueues("ibm-i-modernization"))
        .thenReturn(
            new ReviewQueuesResponse(
                "ibm-i-modernization",
                List.of(
                    queue(ReviewQueueType.PARSER_FAILURE, 1, true),
                    queue(ReviewQueueType.OCR_REQUIRED, 1, true),
                    queue(ReviewQueueType.LOW_CONFIDENCE, 1, true),
                    queue(ReviewQueueType.MISSING_SOURCE_TRACE, 1, true),
                    queue(ReviewQueueType.READY_TO_PUBLISH, 1, false))));

    mockMvc
        .perform(get("/api/spaces/ibm-i-modernization/review-queues"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.spaceId").value("ibm-i-modernization"))
        .andExpect(jsonPath("$.data.queues[*].type", hasItem("PARSER_FAILURE")))
        .andExpect(jsonPath("$.data.queues[*].type", hasItem("OCR_REQUIRED")))
        .andExpect(jsonPath("$.data.queues[*].type", hasItem("LOW_CONFIDENCE")))
        .andExpect(jsonPath("$.data.queues[*].type", hasItem("MISSING_SOURCE_TRACE")))
        .andExpect(jsonPath("$.data.queues[*].type", hasItem("READY_TO_PUBLISH")))
        .andExpect(jsonPath("$.data.queues[0].representativeItems[0].fileId").value("file-parser-failed"))
        .andExpect(jsonPath("$.data.queues[0].representativeItems[0].hasSourceTrace").value(false));
  }

  @Test
  void publishApprovedFileReturnsPublishedWikiMetadata() throws Exception {
    when(reviewPublishService.publishFile(eq("file-001"), any(CreateWikiPublishRequest.class)))
        .thenReturn(
            new WikiPageResponse(
                "wiki-file-001",
                "ibm-i-modernization",
                "BRD Generated Flow",
                "generated/md/BRD.md",
                List.of("file-001"),
                new BigDecimal("0.820"),
                ReviewStatus.PUBLISHED,
                "sme-team",
                OffsetDateTime.parse("2026-07-03T12:00:00Z")));

    mockMvc
        .perform(
            post("/api/files/file-001/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "title": "BRD Generated Flow",
                      "owner": "sme-team"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value("wiki-file-001"))
        .andExpect(jsonPath("$.data.reviewStatus").value("PUBLISHED"))
        .andExpect(jsonPath("$.data.markdownPath").value("generated/md/BRD.md"))
        .andExpect(jsonPath("$.data.sourceDocumentIds[0]").value("file-001"))
        .andExpect(jsonPath("$.data.confidence").value(0.820));
  }

  @Test
  void publishRejectsUnapprovedOrMissingTraceWithoutSecretLeakage() throws Exception {
    when(reviewPublishService.publishFile(eq("file-005"), any(CreateWikiPublishRequest.class)))
        .thenThrow(new ConflictException("Publish blocked: file requires SME approval."));
    when(reviewPublishService.publishFile(eq("file-002"), any(CreateWikiPublishRequest.class)))
        .thenThrow(new ConflictException("Publish blocked: source trace is required."));

    mockMvc
        .perform(
            post("/api/files/file-005/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "title": "Inventory",
                      "owner": "sme-team"
                    }
                    """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("CONFLICT"))
        .andExpect(jsonPath("$").value(not(containsString("Exception"))))
        .andExpect(jsonPath("$").value(not(containsString(System.getProperty("user.home")))))
        .andExpect(jsonPath("$").value(not(containsString("password"))));

    mockMvc
        .perform(
            post("/api/files/file-002/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "title": "Current State",
                      "owner": "sme-team"
                    }
                    """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error.message").value("Publish blocked: source trace is required."));
  }

  @Test
  void publishRejectsInvalidRequestBodyBeforeServiceCall() throws Exception {
    mockMvc
        .perform(
            post("/api/files/file-001/publish")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.error.fields.title").exists())
        .andExpect(jsonPath("$.error.fields.owner").exists());

    verifyNoInteractions(reviewPublishService);
  }

  private ReviewQueueItemResponse queue(ReviewQueueType type, long count, boolean publishBlocked) {
    return new ReviewQueueItemResponse(
        type,
        count,
        publishBlocked,
        List.of(
            new ReviewQueueRepresentativeResponse(
                "file-parser-failed",
                FileStatus.PDF_CONVERT_FAILED,
                ReviewStatus.REVIEW_REQUIRED,
                new BigDecimal("0.100"),
                false)));
  }
}
