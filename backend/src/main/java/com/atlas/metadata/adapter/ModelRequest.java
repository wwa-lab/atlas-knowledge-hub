package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.ModelOperation;
import com.atlas.metadata.enums.ModelSourceReferenceType;
import com.atlas.metadata.enums.ReviewStatus;
import java.math.BigDecimal;
import java.util.List;

/** Safe model operation request passed to adapter implementations. */
public record ModelRequest(
    String runId,
    ModelOperation operationType,
    String modelKey,
    String mode,
    String purpose,
    String inputReference,
    String safeMockInput,
    List<ModelSourceReference> sourceReferences) {

  /** Source evidence reference without copied source text. */
  public record ModelSourceReference(
      ModelSourceReferenceType refType,
      String refId,
      String label,
      BigDecimal confidence,
      ReviewStatus reviewStatus) {}
}
