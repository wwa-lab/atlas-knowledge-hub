package com.atlas.metadata.service;

import com.atlas.metadata.domain.AskEvidence;
import com.atlas.metadata.domain.AskRun;
import com.atlas.metadata.dto.RetrievalQualityMetricsSummaryResponse;
import com.atlas.metadata.dto.RetrievalRunQualityMetricsResponse;
import com.atlas.metadata.exception.NotFoundException;
import com.atlas.metadata.repository.AskEvidenceRepository;
import com.atlas.metadata.repository.AskRunRepository;
import com.atlas.metadata.repository.SpaceRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read-only service for deterministic retrieval quality metrics. */
@Service
public class RetrievalQualityMetricsService {

  private final SpaceRepository spaceRepository;
  private final AskRunRepository askRunRepository;
  private final AskEvidenceRepository askEvidenceRepository;
  private final RetrievalQualityMetricsCalculator calculator;
  private final Clock clock;

  /** Creates the service. */
  @Autowired
  public RetrievalQualityMetricsService(
      SpaceRepository spaceRepository,
      AskRunRepository askRunRepository,
      AskEvidenceRepository askEvidenceRepository) {
    this(
        spaceRepository,
        askRunRepository,
        askEvidenceRepository,
        new RetrievalQualityMetricsCalculator(),
        Clock.systemUTC());
  }

  RetrievalQualityMetricsService(
      SpaceRepository spaceRepository,
      AskRunRepository askRunRepository,
      AskEvidenceRepository askEvidenceRepository,
      RetrievalQualityMetricsCalculator calculator,
      Clock clock) {
    this.spaceRepository = spaceRepository;
    this.askRunRepository = askRunRepository;
    this.askEvidenceRepository = askEvidenceRepository;
    this.calculator = calculator;
    this.clock = clock;
  }

  /** Returns safe metrics for one Ask run. */
  @Transactional(readOnly = true)
  public RetrievalRunQualityMetricsResponse getRunMetrics(String runId) {
    AskRun run =
        askRunRepository
            .findById(runId)
            .orElseThrow(() -> new NotFoundException("Ask run was not found."));
    return calculator.compute(run, evidence(run.getId()));
  }

  /** Returns safe aggregate metrics for a Knowledge Space. */
  @Transactional(readOnly = true)
  public RetrievalQualityMetricsSummaryResponse getSpaceMetrics(String spaceId) {
    if (!spaceRepository.existsById(spaceId)) {
      throw new NotFoundException("Knowledge Space was not found.");
    }
    List<RetrievalRunQualityMetricsResponse> runMetrics =
        askRunRepository.findBySpaceId(spaceId).stream()
            .map(run -> calculator.compute(run, evidence(run.getId())))
            .toList();
    return calculator.summarize(spaceId, runMetrics, OffsetDateTime.now(clock));
  }

  private List<AskEvidence> evidence(String runId) {
    return askEvidenceRepository.findByAskRunIdOrderByCreatedAtAsc(runId);
  }
}
