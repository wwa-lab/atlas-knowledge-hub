# Design: retrieval-quality-metrics

Status: Accepted by goal preauthorization
Last updated: 2026-07-07

Trace IDs: REQ-RETRIEVAL-QUALITY-METRICS-001, REQ-RETRIEVAL-QUALITY-METRICS-002, REQ-RETRIEVAL-QUALITY-METRICS-003, REQ-RETRIEVAL-QUALITY-METRICS-004, REQ-RETRIEVAL-QUALITY-METRICS-005, REQ-RETRIEVAL-QUALITY-METRICS-006, REQ-RETRIEVAL-QUALITY-METRICS-007, REQ-RETRIEVAL-QUALITY-METRICS-008.

## Backend Design

- Add `RetrievalQualityMetricsCalculator` as a deterministic pure calculator.
- Add `RetrievalQualityMetricsService` to validate run/space existence and compose calculator inputs.
- Add `RetrievalQualityMetricsController` with read-only endpoints.
- Add DTO records for evidence coverage, citation health, confidence, review eligibility, run metrics, and summary metrics.
- Extend repositories only with read methods needed to list Ask runs and evidence.

## Metric Rules

- Evidence coverage ratio: `citedEvidenceCount / evidenceCount`, or `0.000` when evidence count is zero.
- Cited evidence: evidence with a source chunk id and at least one safe source label among source file, page, or section.
- Citation health ratio: `healthyCitationCount / evidenceCount`, or `0.000` when evidence count is zero.
- Confidence band:
  - `HIGH`: average evidence confidence and answer confidence are at least `0.850`.
  - `MEDIUM`: both known confidence values are at least `0.700`.
  - `LOW`: any known confidence value is below `0.700`.
  - `UNKNOWN`: no confidence values exist.
- Review eligibility follows FR-RETRIEVAL-QUALITY-METRICS-004.

## Frontend Design

- Add TypeScript metric types.
- Add API client functions for run and space quality metrics.
- In API-backed Trusted Ask, request metrics after a successful Ask run response when a run id is present.
- Render compact chips: evidence coverage, citation health, confidence band, review eligibility, no-evidence refusal.
- Render safe unavailable state if metrics cannot be loaded.

## Test Design

- Backend unit tests cover calculator determinism, eligibility filtering, confidence bands, no-evidence refusal, and safe DTO omission of raw fields.
- Backend integration/API contract tests cover both endpoints and `NOT_FOUND`.
- Frontend tests cover type mapping, rendering of quality chips, safe unavailable state, and no raw diagnostics.
- E2E covers API-backed Trusted Ask quality signal display with mocks.

## Accessibility And UX

Quality chips use text labels and stable data attributes. They are supplemental to existing answer/citation content and do not block the existing flow.

## Risks

- Metrics may be mistaken for production-grade governance. Mitigation: conservative naming and docs say these are local quality signals.
- Existing Ask evidence lacks explicit citation objects. Mitigation: use safe evidence trace metadata as citation health proxy.
