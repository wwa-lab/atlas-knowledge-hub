# Data Model: retrieval-quality-metrics

Status: Accepted by goal preauthorization
Last updated: 2026-07-07

Trace IDs: REQ-RETRIEVAL-QUALITY-METRICS-001, REQ-RETRIEVAL-QUALITY-METRICS-002, REQ-RETRIEVAL-QUALITY-METRICS-003, REQ-RETRIEVAL-QUALITY-METRICS-004, REQ-RETRIEVAL-QUALITY-METRICS-005, REQ-RETRIEVAL-QUALITY-METRICS-006, REQ-RETRIEVAL-QUALITY-METRICS-007, REQ-RETRIEVAL-QUALITY-METRICS-008.

## Persistence

No new persistence table is required for this slice. Metrics are derived from:

- `AskRun`: run id, space id, status, review policy, answer confidence, answer review status, safe message, timestamps.
- `AskEvidence`: run id, source chunk id, file item id, source file label, page, section, review status, confidence, vector item key, score, created timestamp.

## DTO Model

### RetrievalRunQualityMetricsResponse

| Field | Type | Notes |
|---|---|---|
| `runId` | string | Ask run id. |
| `spaceId` | string | Knowledge Space id. |
| `status` | string | Existing Ask status. |
| `evidenceCoverage` | object | Counts and ratio. |
| `citationHealth` | object | Citation counts and health flag. |
| `confidence` | object | Evidence average, answer confidence, band. |
| `reviewEligibility` | object | Boolean, status, safe reason codes. |
| `noEvidenceRefusal` | boolean | True for no-evidence refusal behavior. |
| `safeDiagnostics` | string[] | Bounded non-sensitive reason codes. |

### RetrievalQualityMetricsSummaryResponse

| Field | Type | Notes |
|---|---|---|
| `spaceId` | string | Knowledge Space id. |
| `totalRuns` | number | Total Ask runs in space. |
| `succeededRuns` | number | Run status `SUCCEEDED`. |
| `noEvidenceRefusals` | number | Run status `NO_EVIDENCE`. |
| `failedRuns` | number | `FAILED` plus `PARTIAL_FAILED`. |
| `reviewEligibleRuns` | number | Conservative eligible count. |
| `lowConfidenceRuns` | number | Confidence band low. |
| `averageEvidenceCoverage` | number | Rounded deterministic ratio. |
| `averageCitationHealth` | number | Rounded deterministic ratio. |
| `generatedAt` | string | Server time for response creation, not a provider timestamp. |

## Safe Enums

- Confidence band: `HIGH`, `MEDIUM`, `LOW`, `UNKNOWN`.
- Review eligibility status: `ELIGIBLE`, `NEEDS_REVIEW`, `NOT_ELIGIBLE`.
- Safe reason codes: `NO_EVIDENCE`, `LOW_CONFIDENCE`, `REVIEW_REQUIRED_EVIDENCE`, `FAILED_RUN`, `UNHEALTHY_CITATIONS`, `MISSING_SOURCE_TRACE`.

## Data Safety

DTOs must not expose raw question text, answer text, source document body, prompt, provider payload, private path, stack trace, secret, or internal endpoint.
