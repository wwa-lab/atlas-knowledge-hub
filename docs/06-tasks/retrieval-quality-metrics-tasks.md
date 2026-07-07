# Tasks: retrieval-quality-metrics

Status: Complete and verified
Last updated: 2026-07-07

## Workstreams

- SDD and execution manifest.
- Backend deterministic metric calculation and API contract.
- Frontend safe quality signal display.
- Tests, traceability, roadmap, closeout, commit, and push.

## Task Details

### T-RETRIEVAL-QUALITY-METRICS-001: Confirm SDD and execution manifest
- Requirements: REQ-RETRIEVAL-QUALITY-METRICS-008
- Spec: FR-RETRIEVAL-QUALITY-METRICS-008
- Owner: docs
- Priority: Must
- Verification: `npm run agent:check-sdd -- --slice retrieval-quality-metrics --require-api-guide --report docs/00-context/retrieval-quality-metrics-sdd-completion-report.md`

### T-RETRIEVAL-QUALITY-METRICS-002: Implement backend DTOs and calculator
- Requirements: REQ-RETRIEVAL-QUALITY-METRICS-001, REQ-RETRIEVAL-QUALITY-METRICS-002, REQ-RETRIEVAL-QUALITY-METRICS-004
- Spec: FR-RETRIEVAL-QUALITY-METRICS-001, FR-RETRIEVAL-QUALITY-METRICS-002, FR-RETRIEVAL-QUALITY-METRICS-004
- Owner: backend
- Priority: Must
- Verification: backend unit tests for deterministic calculation, confidence bands, no-evidence refusal, citation health, and conservative review eligibility.

### T-RETRIEVAL-QUALITY-METRICS-003: Add repository reads and service orchestration
- Requirements: REQ-RETRIEVAL-QUALITY-METRICS-001, REQ-RETRIEVAL-QUALITY-METRICS-003, REQ-RETRIEVAL-QUALITY-METRICS-004
- Spec: FR-RETRIEVAL-QUALITY-METRICS-001, FR-RETRIEVAL-QUALITY-METRICS-003, FR-RETRIEVAL-QUALITY-METRICS-004
- Owner: backend
- Priority: Must
- Verification: service tests prove run and space metrics use persisted metadata and exclude raw content fields.

### T-RETRIEVAL-QUALITY-METRICS-004: Add read-only metrics controller
- Requirements: REQ-RETRIEVAL-QUALITY-METRICS-005, REQ-RETRIEVAL-QUALITY-METRICS-007
- Spec: FR-RETRIEVAL-QUALITY-METRICS-005, FR-RETRIEVAL-QUALITY-METRICS-007
- Owner: backend
- Priority: Must
- Verification: API contract tests for run metrics, space metrics, no-evidence behavior, low-confidence behavior, and safe `NOT_FOUND`.

### T-RETRIEVAL-QUALITY-METRICS-005: Add frontend types, API client calls, and UI display
- Requirements: REQ-RETRIEVAL-QUALITY-METRICS-002, REQ-RETRIEVAL-QUALITY-METRICS-006, REQ-RETRIEVAL-QUALITY-METRICS-007
- Spec: FR-RETRIEVAL-QUALITY-METRICS-002, FR-RETRIEVAL-QUALITY-METRICS-006, FR-RETRIEVAL-QUALITY-METRICS-007
- Owner: frontend
- Priority: Must
- Verification: `cd frontend && npm run typecheck && npm run test && npm run build`

### T-RETRIEVAL-QUALITY-METRICS-006: Add E2E coverage for safe quality signals
- Requirements: REQ-RETRIEVAL-QUALITY-METRICS-006, REQ-RETRIEVAL-QUALITY-METRICS-007
- Spec: FR-RETRIEVAL-QUALITY-METRICS-006, FR-RETRIEVAL-QUALITY-METRICS-007
- Owner: QA
- Priority: Must
- Verification: E2E covers API-backed Trusted Ask quality signals with mock-safe data.

### T-RETRIEVAL-QUALITY-METRICS-007: Run backend, frontend, scan, and closeout gates
- Requirements: REQ-RETRIEVAL-QUALITY-METRICS-007, REQ-RETRIEVAL-QUALITY-METRICS-008
- Spec: FR-RETRIEVAL-QUALITY-METRICS-007, FR-RETRIEVAL-QUALITY-METRICS-008
- Owner: docs
- Priority: Must
- Verification: `cd backend && mvn verify`; `cd frontend && npm run typecheck`; `cd frontend && npm run test`; `cd frontend && npm run build`; `git diff --check`; focused secret/private-path/real-data scan; focused network/dependency scan; `npm run agent:closeout`.

### T-RETRIEVAL-QUALITY-METRICS-008: Update traceability, roadmaps, commit, and push
- Requirements: REQ-RETRIEVAL-QUALITY-METRICS-008
- Spec: FR-RETRIEVAL-QUALITY-METRICS-008
- Owner: docs
- Priority: Must
- Verification: traceability and roadmaps show completed implementation evidence; commit message is `feat: add retrieval quality metrics`; push target is `origin develop-leo`.

## Dependency Plan

T-RETRIEVAL-QUALITY-METRICS-001 -> T-RETRIEVAL-QUALITY-METRICS-002 -> T-RETRIEVAL-QUALITY-METRICS-003 -> T-RETRIEVAL-QUALITY-METRICS-004 -> T-RETRIEVAL-QUALITY-METRICS-005 -> T-RETRIEVAL-QUALITY-METRICS-006 -> T-RETRIEVAL-QUALITY-METRICS-007 -> T-RETRIEVAL-QUALITY-METRICS-008.

## Risks

- Metrics are local deterministic quality signals, not production retrieval governance.
- Existing Ask evidence is used as citation proxy because explicit ask-session-citations is out of scope.

## Open Questions

None blocking under the attached goal's preauthorization boundary.

## Completion Evidence

- T-RETRIEVAL-QUALITY-METRICS-001: SDD artifact set, execution manifest, and SDD completion report were created; SDD gate passed.
- T-RETRIEVAL-QUALITY-METRICS-002: Backend DTOs and `RetrievalQualityMetricsCalculator` compute evidence coverage, citation health, confidence band, no-evidence refusal, and conservative review eligibility.
- T-RETRIEVAL-QUALITY-METRICS-003: `RetrievalQualityMetricsService` reads persisted Ask run/evidence metadata and excludes raw question, answer, source content, prompt, and provider payload from metrics DTOs.
- T-RETRIEVAL-QUALITY-METRICS-004: `RetrievalQualityMetricsController` exposes read-only run and space metrics endpoints with safe Atlas envelopes.
- T-RETRIEVAL-QUALITY-METRICS-005: Frontend TypeScript, API client, Trusted Ask UI, and safe quality chips were added.
- T-RETRIEVAL-QUALITY-METRICS-006: E2E mock and API-backed Trusted Ask test coverage verify visible quality signals.
- T-RETRIEVAL-QUALITY-METRICS-007: Backend, frontend, E2E, SDD, diff, secret/private-path, network/dependency, and closeout gates were run.
- T-RETRIEVAL-QUALITY-METRICS-008: Traceability, slice roadmap, repo status roadmap, commit, and push evidence are tracked during closeout.
