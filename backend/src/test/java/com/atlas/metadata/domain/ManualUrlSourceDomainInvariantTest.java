package com.atlas.metadata.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.enums.ManualUrlEligibilityStatus;
import com.atlas.metadata.enums.ManualUrlFetchIntent;
import com.atlas.metadata.enums.ManualUrlFetchPolicy;
import com.atlas.metadata.enums.ManualUrlIngestStatus;
import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

/** Domain invariant coverage for metadata-only manual URL sources. */
class ManualUrlSourceDomainInvariantTest {

  @Test
  void createDefaultsToMetadataOnlyReviewRequiredAndTraceable() {
    ManualUrlSource source =
        ManualUrlSource.create(
            "url-src-test",
            "space-test",
            "hash",
            "https://example.com/reference/page",
            "example.com",
            "Reference",
            "Sample-safe URL.",
            ManualUrlFetchIntent.FETCH_LATER,
            "Manual URL metadata: https://example.com/reference/page",
            "batch-test",
            "file-test",
            "frontend-user",
            new BigDecimal("0.300"),
            OffsetDateTime.parse("2026-07-07T00:00:00Z"));

    assertThat(source.getFetchPolicy()).isEqualTo(ManualUrlFetchPolicy.NO_FETCH_METADATA_ONLY);
    assertThat(source.getIngestStatus()).isEqualTo(ManualUrlIngestStatus.REVIEW_REQUIRED);
    assertThat(source.getReviewStatus()).isEqualTo(ReviewStatus.REVIEW_REQUIRED);
    assertThat(source.getEligibilityStatus()).isEqualTo(ManualUrlEligibilityStatus.REVIEW_REQUIRED_ONLY);
    assertThat(source.getSourceTrace()).contains(source.getDisplayUrl());
  }
}
