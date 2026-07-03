package com.atlas.metadata.domain;

import com.atlas.metadata.enums.ModelOperation;
import com.atlas.metadata.enums.ModelRunStatus;
import com.atlas.metadata.enums.ModelType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/** Append-only model run evidence with safe summaries only. */
@Entity
@Table(name = "model_run", schema = "atlas")
public class ModelRun {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "adapter_key", nullable = false, columnDefinition = "text")
  private String adapterKey;

  @Column(name = "model_key", nullable = false, columnDefinition = "text")
  private String modelKey;

  @Enumerated(EnumType.STRING)
  @Column(name = "model_type", nullable = false, columnDefinition = "text")
  private ModelType modelType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private ModelOperation operation;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "text")
  private ModelRunStatus status;

  @Column(nullable = false, columnDefinition = "text")
  private String mode;

  @Column(nullable = false, columnDefinition = "text")
  private String purpose;

  @Column(name = "requested_by", columnDefinition = "text")
  private String requestedBy;

  @Column(name = "input_reference", columnDefinition = "text")
  private String inputReference;

  @Column(name = "safe_input_summary", columnDefinition = "text")
  private String safeInputSummary;

  @Column(name = "prompt_units", nullable = false)
  private int promptUnits;

  @Column(name = "completion_units", nullable = false)
  private int completionUnits;

  @Column(name = "started_at", nullable = false)
  private OffsetDateTime startedAt;

  @Column(name = "completed_at")
  private OffsetDateTime completedAt;

  @Column(name = "safe_message", columnDefinition = "text")
  private String safeMessage;

  protected ModelRun() {}

  /** Creates a requested model run. */
  public static ModelRun create(
      String id,
      String adapterKey,
      String modelKey,
      ModelType modelType,
      ModelOperation operation,
      String mode,
      String purpose,
      String requestedBy,
      String inputReference,
      String safeInputSummary,
      OffsetDateTime startedAt) {
    ModelRun run = new ModelRun();
    run.id = id;
    run.adapterKey = adapterKey;
    run.modelKey = modelKey;
    run.modelType = modelType;
    run.operation = operation;
    run.status = ModelRunStatus.REQUESTED;
    run.mode = mode;
    run.purpose = purpose;
    run.requestedBy = requestedBy;
    run.inputReference = inputReference;
    run.safeInputSummary = safeInputSummary;
    run.startedAt = startedAt;
    return run;
  }

  /** Marks the run as executing. */
  public void markRunning() {
    status = ModelRunStatus.RUNNING;
  }

  /** Marks the run as terminal while recording bounded usage counts. */
  public void complete(
      ModelRunStatus terminalStatus,
      int promptUnits,
      int completionUnits,
      OffsetDateTime completedAt,
      String safeMessage) {
    if (terminalStatus == ModelRunStatus.REQUESTED || terminalStatus == ModelRunStatus.RUNNING) {
      throw new IllegalArgumentException("Model run must complete with a terminal status.");
    }
    if (promptUnits < 0 || completionUnits < 0) {
      throw new IllegalArgumentException("Model usage counts must be non-negative.");
    }
    this.status = terminalStatus;
    this.promptUnits = promptUnits;
    this.completionUnits = completionUnits;
    this.completedAt = completedAt;
    this.safeMessage = safeMessage;
  }

  public String getId() {
    return id;
  }

  public String getAdapterKey() {
    return adapterKey;
  }

  public String getModelKey() {
    return modelKey;
  }

  public ModelType getModelType() {
    return modelType;
  }

  public ModelOperation getOperation() {
    return operation;
  }

  public ModelRunStatus getStatus() {
    return status;
  }

  public String getMode() {
    return mode;
  }

  public String getPurpose() {
    return purpose;
  }

  public String getRequestedBy() {
    return requestedBy;
  }

  public String getInputReference() {
    return inputReference;
  }

  public String getSafeInputSummary() {
    return safeInputSummary;
  }

  public int getPromptUnits() {
    return promptUnits;
  }

  public int getCompletionUnits() {
    return completionUnits;
  }

  public OffsetDateTime getStartedAt() {
    return startedAt;
  }

  public OffsetDateTime getCompletedAt() {
    return completedAt;
  }

  public String getSafeMessage() {
    return safeMessage;
  }
}
