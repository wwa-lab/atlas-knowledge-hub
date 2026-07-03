package com.atlas.metadata.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlas.metadata.enums.FileStatus;
import com.atlas.metadata.enums.ReviewAction;
import com.atlas.metadata.enums.ReviewStatus;
import com.atlas.metadata.validation.RelativePathValidator;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/** Unit checks for enum and validation invariants. */
class DomainInvariantTest {

  @Test
  void fileStatusSetMatchesBatchProcessingContract() {
    assertThat(Arrays.stream(FileStatus.values()).map(Enum::name))
        .containsExactly(
            "NEW",
            "UPLOADED",
            "PDF_CONVERTED",
            "PDF_CONVERT_FAILED",
            "MARKDOWN_GENERATED",
            "OCR_REQUIRED",
            "LOW_CONFIDENCE",
            "REVIEW_REQUIRED",
            "APPROVED",
            "PUBLISHED",
            "FAILED",
            "UNSUPPORTED");
  }

  @Test
  void reviewStatusFollowsReqProd030NotFrontendRejectedType() {
    assertThat(Arrays.stream(ReviewStatus.values()).map(Enum::name))
        .containsExactly("REVIEW_REQUIRED", "APPROVED", "NEED_FIX", "OCR_REQUIRED", "PUBLISHED");
  }

  @Test
  void reviewActionsNeverMapToPublished() {
    assertThat(ReviewAction.APPROVE.resultingStatus()).isEqualTo(ReviewStatus.APPROVED);
    assertThat(ReviewAction.NEED_FIX.resultingStatus()).isEqualTo(ReviewStatus.NEED_FIX);
    assertThat(ReviewAction.OCR_REQUIRED.resultingStatus()).isEqualTo(ReviewStatus.OCR_REQUIRED);
  }

  @Test
  void relativePathValidatorRejectsAbsoluteAndTraversalPaths() {
    RelativePathValidator validator = new RelativePathValidator();

    assertThat(validator.isValid("Discovery/BRD/BRD.docx", null)).isTrue();
    assertThat(validator.isValid("../secret.docx", null)).isFalse();
    assertThat(validator.isValid("/private/secret.docx", null)).isFalse();
    assertThat(validator.isValid("C:/private/secret.docx", null)).isFalse();
    assertThat(validator.isValid("file:///private/secret.docx", null)).isFalse();
    assertThat(validator.isValid("Discovery\\BRD.docx", null)).isFalse();
  }
}
