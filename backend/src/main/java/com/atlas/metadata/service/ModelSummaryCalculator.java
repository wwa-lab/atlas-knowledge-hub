package com.atlas.metadata.service;

import com.atlas.metadata.domain.ModelRunOutput;
import com.atlas.metadata.dto.ModelUsageResponse;
import com.atlas.metadata.enums.ModelOutputKind;
import java.util.List;

/** Computes model run usage summaries from persisted output evidence. */
public class ModelSummaryCalculator {

  /** Computes prompt/completion counts plus output/failure counts. */
  public ModelUsageResponse compute(
      int promptUnits, int completionUnits, List<ModelRunOutput> outputs) {
    int failedCount =
        (int) outputs.stream().filter(output -> output.getKind() == ModelOutputKind.ERROR).count();
    return new ModelUsageResponse(promptUnits, completionUnits, outputs.size(), failedCount);
  }
}
