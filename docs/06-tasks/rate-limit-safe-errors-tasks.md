# Tasks: rate-limit-safe-errors

Status: Complete and verified
Last updated: 2026-07-07

## Workstreams

- Backend safe error contract and redaction.
- Backend deterministic local rate limiting.
- Frontend safe error classification and state rendering.
- Verification, traceability, roadmap, closeout, commit, and push.

## Task Details

### T-RATE-LIMIT-SAFE-ERRORS-001: Confirm SDD and execution manifest
- Requirements: REQ-RATE-LIMIT-SAFE-ERRORS-011
- Spec: FR-RATE-LIMIT-SAFE-ERRORS-011
- Owner: docs
- Priority: Must
- Verification: `npm run agent:check-sdd -- --slice rate-limit-safe-errors --require-api-guide --report docs/00-context/rate-limit-safe-errors-sdd-completion-report.md`

### T-RATE-LIMIT-SAFE-ERRORS-002: Implement safe error body, factory, and sanitizer
- Requirements: REQ-RATE-LIMIT-SAFE-ERRORS-001, REQ-RATE-LIMIT-SAFE-ERRORS-006, REQ-RATE-LIMIT-SAFE-ERRORS-007
- Spec: FR-RATE-LIMIT-SAFE-ERRORS-001, FR-RATE-LIMIT-SAFE-ERRORS-006, FR-RATE-LIMIT-SAFE-ERRORS-007
- Owner: backend
- Priority: Must
- Verification: backend unit tests for redaction and safe body fields.

### T-RATE-LIMIT-SAFE-ERRORS-003: Map validation, auth, not found, conflict, and unexpected errors
- Requirements: REQ-RATE-LIMIT-SAFE-ERRORS-002, REQ-RATE-LIMIT-SAFE-ERRORS-003, REQ-RATE-LIMIT-SAFE-ERRORS-004, REQ-RATE-LIMIT-SAFE-ERRORS-006, REQ-RATE-LIMIT-SAFE-ERRORS-012
- Spec: FR-RATE-LIMIT-SAFE-ERRORS-002, FR-RATE-LIMIT-SAFE-ERRORS-003, FR-RATE-LIMIT-SAFE-ERRORS-004, FR-RATE-LIMIT-SAFE-ERRORS-006, FR-RATE-LIMIT-SAFE-ERRORS-012
- Owner: backend
- Priority: Must
- Verification: API contract tests for `400`, `401`, `403`, `404`, `409`, and `500`.

### T-RATE-LIMIT-SAFE-ERRORS-004: Implement deterministic local rate limiting
- Requirements: REQ-RATE-LIMIT-SAFE-ERRORS-005, REQ-RATE-LIMIT-SAFE-ERRORS-007, REQ-RATE-LIMIT-SAFE-ERRORS-010
- Spec: FR-RATE-LIMIT-SAFE-ERRORS-005, FR-RATE-LIMIT-SAFE-ERRORS-007, FR-RATE-LIMIT-SAFE-ERRORS-010
- Owner: backend
- Priority: Must
- Verification: unit and API contract tests for deterministic `429 RATE_LIMITED` behavior and safe retry metadata.

### T-RATE-LIMIT-SAFE-ERRORS-005: Add frontend safe error categories and representative states
- Requirements: REQ-RATE-LIMIT-SAFE-ERRORS-008, REQ-RATE-LIMIT-SAFE-ERRORS-009
- Spec: FR-RATE-LIMIT-SAFE-ERRORS-008, FR-RATE-LIMIT-SAFE-ERRORS-009
- Owner: frontend
- Priority: Must
- Verification: `cd frontend && npm run typecheck && npm run test && npm run build`

### T-RATE-LIMIT-SAFE-ERRORS-006: Add backend unit and integration/API contract tests
- Requirements: REQ-RATE-LIMIT-SAFE-ERRORS-010
- Spec: FR-RATE-LIMIT-SAFE-ERRORS-010
- Owner: backend
- Priority: Must
- Verification: `cd backend && mvn verify`

### T-RATE-LIMIT-SAFE-ERRORS-007: Add frontend tests for safe error states
- Requirements: REQ-RATE-LIMIT-SAFE-ERRORS-008, REQ-RATE-LIMIT-SAFE-ERRORS-009, REQ-RATE-LIMIT-SAFE-ERRORS-010
- Spec: FR-RATE-LIMIT-SAFE-ERRORS-008, FR-RATE-LIMIT-SAFE-ERRORS-009, FR-RATE-LIMIT-SAFE-ERRORS-010
- Owner: frontend
- Priority: Must
- Verification: frontend unit/component tests.

### T-RATE-LIMIT-SAFE-ERRORS-008: Update traceability, roadmaps, scans, closeout, commit, and push
- Requirements: REQ-RATE-LIMIT-SAFE-ERRORS-011, REQ-RATE-LIMIT-SAFE-ERRORS-012
- Spec: FR-RATE-LIMIT-SAFE-ERRORS-011, FR-RATE-LIMIT-SAFE-ERRORS-012
- Owner: docs
- Priority: Must
- Verification: `git diff --check`, focused secret/private-path/real-data scan, focused network/dependency scan, `npm run agent:closeout`, commit `feat: add rate limit and safe error handling`, push to `develop-leo`.

## Dependency Plan

T-RATE-LIMIT-SAFE-ERRORS-001 -> T-RATE-LIMIT-SAFE-ERRORS-002 -> T-RATE-LIMIT-SAFE-ERRORS-003 -> T-RATE-LIMIT-SAFE-ERRORS-004 -> T-RATE-LIMIT-SAFE-ERRORS-005 -> T-RATE-LIMIT-SAFE-ERRORS-006 -> T-RATE-LIMIT-SAFE-ERRORS-007 -> T-RATE-LIMIT-SAFE-ERRORS-008.

## Risks

- Local in-memory rate limiting is not production quota enforcement.
- Frontend safe states must not overclaim production readiness.

## Open Questions

None blocking under the attached goal's preauthorization boundary.

## Completion Evidence

- T-RATE-LIMIT-SAFE-ERRORS-001: SDD artifact set and execution manifest created; SDD gate passed.
- T-RATE-LIMIT-SAFE-ERRORS-002 through T-RATE-LIMIT-SAFE-ERRORS-004: backend safe error and deterministic local rate-limit behavior implemented.
- T-RATE-LIMIT-SAFE-ERRORS-005 through T-RATE-LIMIT-SAFE-ERRORS-007: frontend typed safe-error handling and state coverage implemented.
- T-RATE-LIMIT-SAFE-ERRORS-008: traceability and roadmap evidence updated; final scans, closeout, commit, and push are tracked in closeout.
