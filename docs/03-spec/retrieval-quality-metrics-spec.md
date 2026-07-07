# Specification: retrieval-quality-metrics

Status: Accepted by goal preauthorization
Last updated: 2026-07-07

## Behavior Source Of Truth

This spec defines local deterministic quality metrics for Atlas retrieval surfaces. It does not define production retrieval governance, answer approval, provider analytics, billing, A/B evaluation, or raw diagnostic capture.

## Functional Requirements

### FR-RETRIEVAL-QUALITY-METRICS-001: Run metric calculation

For a stored Ask run, Atlas must derive a safe quality object from `AskRun` and `AskEvidence` records. The calculation must be deterministic and must not require provider, vector, analytics, or cloud calls.

Maps to: REQ-RETRIEVAL-QUALITY-METRICS-001, AC-RETRIEVAL-QUALITY-METRICS-001.

### FR-RETRIEVAL-QUALITY-METRICS-002: Metric categories

The run metric object must include:

- evidence coverage: evidence count, cited evidence count, coverage ratio, missing evidence flag.
- citation health: citation count, healthy citation count, uncited evidence count, unhealthy citation flag.
- confidence: average evidence confidence, answer confidence, confidence band.
- review eligibility: boolean, status, and reasons.
- no-evidence refusal: boolean for run-level refusal behavior.
- safe diagnostics: bounded, non-sensitive reason codes.

Maps to: REQ-RETRIEVAL-QUALITY-METRICS-002, AC-RETRIEVAL-QUALITY-METRICS-002.

### FR-RETRIEVAL-QUALITY-METRICS-003: Aggregate metrics

For a Knowledge Space, Atlas must aggregate Ask runs into safe counts and ratios:

- total runs.
- succeeded runs.
- no-evidence refusals.
- failed or partial runs.
- review-eligible runs.
- average evidence coverage.
- average citation health.
- low confidence runs.

The aggregate response must not include raw questions, answers, prompts, provider payloads, or raw source text.

Maps to: REQ-RETRIEVAL-QUALITY-METRICS-001, REQ-RETRIEVAL-QUALITY-METRICS-003, AC-RETRIEVAL-QUALITY-METRICS-003.

### FR-RETRIEVAL-QUALITY-METRICS-004: Review eligibility rules

A run is review-eligible only when all of the following are true:

- run status is `SUCCEEDED`.
- evidence count is greater than zero.
- citation health is not unhealthy.
- average evidence confidence is at least `0.700`.
- answer confidence is absent or at least `0.700`.
- all evidence records are `APPROVED`.

Otherwise the response must report `reviewEligible=false` and include safe reason codes such as `NO_EVIDENCE`, `LOW_CONFIDENCE`, `REVIEW_REQUIRED_EVIDENCE`, `FAILED_RUN`, or `UNHEALTHY_CITATIONS`.

Maps to: REQ-RETRIEVAL-QUALITY-METRICS-004, AC-RETRIEVAL-QUALITY-METRICS-004.

### FR-RETRIEVAL-QUALITY-METRICS-005: API surface

Atlas must expose:

- `GET /api/ask-runs/{runId}/quality-metrics`
- `GET /api/spaces/{spaceId}/retrieval-quality-metrics`

Both endpoints must use `ApiEnvelope` and existing safe error handling.

Maps to: REQ-RETRIEVAL-QUALITY-METRICS-005, AC-RETRIEVAL-QUALITY-METRICS-005.

### FR-RETRIEVAL-QUALITY-METRICS-006: Frontend display

The Vue frontend must display safe quality metrics in the existing API-backed Trusted Ask surface. The display may use compact quality chips and a diagnostic section. It must not show raw source content, raw prompt text, raw provider response, internal endpoints, private paths, stack traces, or secrets.

Maps to: REQ-RETRIEVAL-QUALITY-METRICS-006, AC-RETRIEVAL-QUALITY-METRICS-006.

### FR-RETRIEVAL-QUALITY-METRICS-007: Non-regression

Existing Ask, Graph, Wiki, review/publish, auth, rate-limit, safe-error, and adapter behaviors must continue to pass current tests. This slice may add DTO fields or endpoints but must not change provider/model adapter call semantics.

Maps to: REQ-RETRIEVAL-QUALITY-METRICS-007, AC-RETRIEVAL-QUALITY-METRICS-007.

### FR-RETRIEVAL-QUALITY-METRICS-008: Closeout evidence

Traceability, roadmap, task completion, verification evidence, closeout gate result, commit hash, and push target must be recorded.

Maps to: REQ-RETRIEVAL-QUALITY-METRICS-008, AC-RETRIEVAL-QUALITY-METRICS-008.

## State Rules

| Condition | Review eligibility | Safe reasons |
|---|---|---|
| `SUCCEEDED` with approved evidence and healthy citations | true | none |
| `NO_EVIDENCE` | false | `NO_EVIDENCE` |
| `FAILED` or `PARTIAL_FAILED` | false | `FAILED_RUN` |
| Any `REVIEW_REQUIRED` or `REJECTED` evidence | false | `REVIEW_REQUIRED_EVIDENCE` |
| Average confidence below threshold | false | `LOW_CONFIDENCE` |
| Evidence exists but none has source trace | false | `UNHEALTHY_CITATIONS` |

## Acceptance Matrix

| Acceptance | Covered by |
|---|---|
| AC-RETRIEVAL-QUALITY-METRICS-001 | Unit tests for calculator determinism. |
| AC-RETRIEVAL-QUALITY-METRICS-002 | DTO/API contract tests and frontend tests. |
| AC-RETRIEVAL-QUALITY-METRICS-003 | Safe DTO tests and diff scans. |
| AC-RETRIEVAL-QUALITY-METRICS-004 | Unit tests for eligibility filtering. |
| AC-RETRIEVAL-QUALITY-METRICS-005 | API contract tests. |
| AC-RETRIEVAL-QUALITY-METRICS-006 | Frontend component/unit and E2E tests. |
| AC-RETRIEVAL-QUALITY-METRICS-007 | `mvn verify`, frontend typecheck/test/build. |
| AC-RETRIEVAL-QUALITY-METRICS-008 | Traceability and closeout gate. |

## Constraints

- No external network calls or new dependencies.
- No production governance claim.
- No raw source content, prompt, provider payload, stack trace, secret, private path, or internal endpoint in API, UI, fixtures, logs, or docs.
- Preserve adapter boundaries and existing security semantics.
