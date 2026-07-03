package com.atlas.metadata.service;

import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.dto.BatchMetricsResponse;
import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewStatus;
import java.util.Collection;

/** Derives batch metrics from file-item rows. */
public class MetricsCalculator {

  /** Computes metrics without storing a separate copy. */
  public BatchMetricsResponse compute(Collection<FileItem> items) {
    long total = items.size();
    long pdfConverted = count(items, FileStatus.PDF_CONVERTED);
    long markdownGenerated = count(items, FileStatus.MARKDOWN_GENERATED);
    long reviewRequired =
        items.stream().filter(item -> item.getReviewStatus() == ReviewStatus.REVIEW_REQUIRED).count();
    long failed =
        items.stream()
            .filter(
                item ->
                    item.getStatus() == FileStatus.FAILED
                        || item.getStatus() == FileStatus.PDF_CONVERT_FAILED)
            .count();
    long unsupported = count(items, FileStatus.UNSUPPORTED);
    return new BatchMetricsResponse(
        total, pdfConverted, markdownGenerated, reviewRequired, failed, unsupported);
  }

  private long count(Collection<FileItem> items, FileStatus status) {
    return items.stream().filter(item -> item.getStatus() == status).count();
  }
}
