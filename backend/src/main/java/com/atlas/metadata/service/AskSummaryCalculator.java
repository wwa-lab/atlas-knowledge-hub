package com.atlas.metadata.service;

import com.atlas.metadata.domain.AskEvidence;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Component;

/** Deterministic confidence summary for trusted ask responses. */
@Component
public class AskSummaryCalculator {

  /** Uses model confidence first and falls back to average evidence score. */
  public BigDecimal answerConfidence(BigDecimal modelConfidence, List<AskEvidence> evidence) {
    if (modelConfidence != null) {
      return modelConfidence.setScale(3, RoundingMode.HALF_UP);
    }
    List<BigDecimal> scores =
        evidence == null
            ? List.of()
            : evidence.stream().map(AskEvidence::getScore).filter(score -> score != null).toList();
    if (scores.isEmpty()) {
      return null;
    }
    BigDecimal total = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    return total.divide(BigDecimal.valueOf(scores.size()), 3, RoundingMode.HALF_UP);
  }
}
