package com.atlas.metadata.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.atlas.metadata.controller.ReviewPublishController;
import com.atlas.metadata.config.AtlasAuthInterceptor;
import com.atlas.metadata.config.AtlasAuthWebConfig;
import com.atlas.metadata.config.LocalRateLimitInterceptor;
import com.atlas.metadata.dto.CreateWikiPublishRequest;
import com.atlas.metadata.dto.ReviewQueueItemResponse;
import com.atlas.metadata.dto.ReviewQueueItemResponse.ReviewQueueType;
import com.atlas.metadata.dto.ReviewQueueRepresentativeResponse;
import com.atlas.metadata.dto.ReviewQueuesResponse;
import com.atlas.metadata.dto.WikiFolderResponse;
import com.atlas.metadata.dto.WikiGenerationRunResponse;
import com.atlas.metadata.dto.WikiLogEntryResponse;
import com.atlas.metadata.dto.WikiPageResponse;
import com.atlas.metadata.dto.WikiPageIssueResponse;
import com.atlas.metadata.dto.WikiReferenceResponse;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.exception.ConflictException;
import com.atlas.metadata.exception.SafeErrorResponseFactory;
import com.atlas.metadata.exception.SafeErrorSanitizer;
import com.atlas.metadata.service.ReviewPublishService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** API contract tests for review queues and Wiki publication. */
@WebMvcTest(
    controllers = ReviewPublishController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = {AtlasAuthInterceptor.class, AtlasAuthWebConfig.class, LocalRateLimitInterceptor.class}))
@Import({SafeErrorResponseFactory.class, SafeErrorSanitizer.class})
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
        .thenReturn(wikiPage("wiki-file-001", "ibm-i-modernization", "BRD Generated Flow", "brd"));

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
        .andExpect(jsonPath("$.data.slug").value("brd"))
        .andExpect(jsonPath("$.data.pageType").value("SOURCE_SUMMARY"))
        .andExpect(jsonPath("$.data.markdownPath").value("generated/md/BRD.md"))
        .andExpect(jsonPath("$.data.sourceDocumentIds[0]").value("file-001"))
        .andExpect(jsonPath("$.data.aliases").isArray())
        .andExpect(jsonPath("$.data.sourceRefs[0].type").value("FILE"))
        .andExpect(jsonPath("$.data.chunkRefs[0].type").value("SOURCE_CHUNK"))
        .andExpect(jsonPath("$.data.inLinks").isArray())
        .andExpect(jsonPath("$.data.outLinks").isArray())
        .andExpect(jsonPath("$.data.version").value(1))
        .andExpect(jsonPath("$.data.sourceMode").value("PUBLISHED_FILE"))
        .andExpect(jsonPath("$.data.refreshPolicy").value("MANUAL"))
        .andExpect(jsonPath("$.data.confidence").value(0.820));
  }

  @Test
  void wikiFoundationReadEndpointsReturnSafeMetadata() throws Exception {
    when(reviewPublishService.getPublishedWikiPageBySlug("ibm-i-modernization", "brd"))
        .thenReturn(wikiPage("wiki-file-001", "ibm-i-modernization", "BRD Generated Flow", "brd"));
    when(reviewPublishService.listWikiFolders("ibm-i-modernization"))
        .thenReturn(
            List.of(
                new WikiFolderResponse(
                    "wiki-folder-foundation",
                    "ibm-i-modernization",
                    null,
                    "foundation",
                    "Foundation",
                    "Sample-safe Wiki foundation pages",
                    10)));
    when(reviewPublishService.listWikiGenerationRuns("ibm-i-modernization"))
        .thenReturn(
            List.of(
                new WikiGenerationRunResponse(
                    "wiki-run-sample-001",
                    "ibm-i-modernization",
                    "wiki-file-001",
                    "SUCCEEDED",
                    "deterministic",
                    "PUBLISHED_FILE",
                    "MANUAL",
                    "system-sample",
                    List.of(new WikiReferenceResponse("FILE", "file-001", "file-001", null)),
                    List.of(),
                    List.of("wiki-file-001"),
                    List.of(),
                    "Sample-safe metadata refresh recorded.",
                    null,
                    OffsetDateTime.parse("2026-07-03T12:00:00Z"),
                    OffsetDateTime.parse("2026-07-03T12:00:03Z"))));
    when(reviewPublishService.listWikiPageLogs("wiki-file-001"))
        .thenReturn(
            List.of(
                new WikiLogEntryResponse(
                    "wiki-log-001",
                    "ibm-i-modernization",
                    "wiki-file-001",
                    null,
                    "PUBLISHED",
                    "sme-team",
                    "Published safe Wiki metadata.",
                    Map.of("sourceMode", "PUBLISHED_FILE"),
                    OffsetDateTime.parse("2026-07-03T12:00:00Z"))));
    when(reviewPublishService.listWikiPageIssues("wiki-file-001"))
        .thenReturn(
            List.of(
                new WikiPageIssueResponse(
                    "wiki-issue-001",
                    "ibm-i-modernization",
                    "wiki-file-001",
                    "MISSING_SOURCE_REF",
                    "LOW",
                    "OPEN",
                    List.of(new WikiReferenceResponse("WIKI_PAGE", "wiki-file-001", "page", null)),
                    "Sample-safe issue placeholder for future lint workflows.",
                    OffsetDateTime.parse("2026-07-03T12:00:00Z"),
                    null)));

    mockMvc
        .perform(get("/api/spaces/ibm-i-modernization/wiki-pages/by-slug/brd"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value("wiki-file-001"))
        .andExpect(jsonPath("$.data.slug").value("brd"));

    mockMvc
        .perform(get("/api/spaces/ibm-i-modernization/wiki-folders"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].slug").value("foundation"));

    mockMvc
        .perform(get("/api/spaces/ibm-i-modernization/wiki-generation-runs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].status").value("SUCCEEDED"))
        .andExpect(jsonPath("$.data[0].safeSummary").value("Sample-safe metadata refresh recorded."));

    mockMvc
        .perform(get("/api/wiki-pages/wiki-file-001/logs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].eventType").value("PUBLISHED"))
        .andExpect(jsonPath("$").value(not(containsString(System.getProperty("user.home")))));

    mockMvc
        .perform(get("/api/wiki-pages/wiki-file-001/issues"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].issueType").value("MISSING_SOURCE_REF"))
        .andExpect(jsonPath("$").value(not(containsString("password"))));
  }

  @Test
  void wikiSpaceIssuesReturnSafeLintWarnings() throws Exception {
    when(reviewPublishService.listWikiSpaceIssues("ibm-i-modernization", "OPEN", "BROKEN_LINK"))
        .thenReturn(
            List.of(
                new WikiPageIssueResponse(
                    "wiki-issue-broken-link-001",
                    "ibm-i-modernization",
                    "wiki-file-001",
                    "BROKEN_LINK",
                    "MEDIUM",
                    "OPEN",
                    List.of(new WikiReferenceResponse("WIKI_PAGE", "wiki-file-001", "p0-wiki", null)),
                    "Wiki link target does not exist in this Knowledge Space.",
                    OffsetDateTime.parse("2026-07-05T00:00:00Z"),
                    null)));

    mockMvc
        .perform(get("/api/spaces/ibm-i-modernization/wiki-page-issues?status=OPEN&issueType=BROKEN_LINK"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].issueType").value("BROKEN_LINK"))
        .andExpect(jsonPath("$.data[0].severity").value("MEDIUM"))
        .andExpect(jsonPath("$").value(not(containsString(System.getProperty("user.home")))))
        .andExpect(jsonPath("$").value(not(containsString("password"))));
  }

  @Test
  void wikiPagesCanExplicitlyIncludeGeneratedDrafts() throws Exception {
    when(reviewPublishService.listWikiPages("ibm-i-modernization", true))
        .thenReturn(
            List.of(
                new WikiPageResponse(
                    "wiki-auto-file-001",
                    "ibm-i-modernization",
                    null,
                    "Generated Topic",
                    "generated-topic",
                    "TOPIC",
                    "generated/wiki/ibm-i-modernization/generated-topic.md",
                    List.of("file-001"),
                    List.of(),
                    List.of(new WikiReferenceResponse("FILE", "file-001", "file-001", null)),
                    List.of(new WikiReferenceResponse("SOURCE_CHUNK", "chunk-file-001", "source chunk", "page 1")),
                    List.of(),
                    List.of(),
                    1,
                    "AUTO_GENERATED",
                    "ON_SOURCE_CHANGE",
                    new BigDecimal("0.910"),
                    ReviewStatus.REVIEW_REQUIRED,
                    "knowledge-manager",
                    OffsetDateTime.parse("2026-07-05T00:00:00Z"))));

    mockMvc
        .perform(get("/api/spaces/ibm-i-modernization/wiki-pages?includeDrafts=true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].reviewStatus").value("REVIEW_REQUIRED"))
        .andExpect(jsonPath("$.data[0].sourceMode").value("AUTO_GENERATED"))
        .andExpect(jsonPath("$.data[0].refreshPolicy").value("ON_SOURCE_CHANGE"));

    verify(reviewPublishService).listWikiPages("ibm-i-modernization", true);
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
        .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
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

  private WikiPageResponse wikiPage(String id, String spaceId, String title, String slug) {
    return new WikiPageResponse(
        id,
        spaceId,
        null,
        title,
        slug,
        "SOURCE_SUMMARY",
        "generated/md/BRD.md",
        List.of("file-001"),
        List.of(),
        List.of(new WikiReferenceResponse("FILE", "file-001", "file-001", "generated/md/BRD.md")),
        List.of(new WikiReferenceResponse("SOURCE_CHUNK", "chunk-file-001-p12-b02", "source chunk", "page 12")),
        List.of(),
        List.of(),
        1,
        "PUBLISHED_FILE",
        "MANUAL",
        new BigDecimal("0.820"),
        ReviewStatus.PUBLISHED,
        "sme-team",
        OffsetDateTime.parse("2026-07-03T12:00:00Z"));
  }
}
