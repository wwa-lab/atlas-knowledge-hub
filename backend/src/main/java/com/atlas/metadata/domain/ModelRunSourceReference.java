package com.atlas.metadata.domain;

import com.atlas.metadata.enums.ModelSourceReferenceType;
import com.atlas.metadata.enums.ReviewStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** Source trace evidence attached to a model run. */
@Entity
@Table(name = "model_run_source_reference", schema = "atlas")
public class ModelRunSourceReference {

  @Id
  @Column(columnDefinition = "text")
  private String id;

  @Column(name = "run_id", nullable = false, columnDefinition = "text")
  private String runId;

  @Enumerated(EnumType.STRING)
  @Column(name = "ref_type", nullable = false, columnDefinition = "text")
  private ModelSourceReferenceType refType;

  @Column(name = "ref_id", nullable = false, columnDefinition = "text")
  private String refId;

  @Column(columnDefinition = "text")
  private String label;

  @Column(precision = 4, scale = 3)
  private BigDecimal confidence;

  @Enumerated(EnumType.STRING)
  @Column(name = "review_status", nullable = false, columnDefinition = "text")
  private ReviewStatus reviewStatus;

  protected ModelRunSourceReference() {}

  /** Creates source trace evidence. */
  public static ModelRunSourceReference create(
      String id,
      String runId,
      ModelSourceReferenceType refType,
      String refId,
      String label,
      BigDecimal confidence,
      ReviewStatus reviewStatus) {
    ModelRunSourceReference reference = new ModelRunSourceReference();
    reference.id = id;
    reference.runId = runId;
    reference.refType = refType;
    reference.refId = refId;
    reference.label = label;
    reference.confidence = confidence;
    reference.reviewStatus = reviewStatus == null ? ReviewStatus.REVIEW_REQUIRED : reviewStatus;
    return reference;
  }

  public String getId() {
    return id;
  }

  public String getRunId() {
    return runId;
  }

  public ModelSourceReferenceType getRefType() {
    return refType;
  }

  public String getRefId() {
    return refId;
  }

  public String getLabel() {
    return label;
  }

  public BigDecimal getConfidence() {
    return confidence;
  }

  public ReviewStatus getReviewStatus() {
    return reviewStatus;
  }
}
