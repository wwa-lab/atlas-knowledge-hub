# Tasks: connector-sync-v0

Status: Implemented and verified locally
Last updated: 2026-07-07

## Workstreams

- SDD gate and manifest confirmation.
- Backend connector adapter boundary, persistence, API, and tests.
- Frontend connector sync UI/API binding and tests.
- Roadmap, traceability, closeout, scans, commit, and push.

## Task Details

### T-CONNECTOR-SYNC-V0-001: Confirm SDD and execution manifest
- Requirements: REQ-CONNECTOR-SYNC-V0-012
- Spec: FR-CONNECTOR-SYNC-V0-012
- Owner: docs
- Priority: Must
- Verification: `npm run agent:check-sdd -- --slice connector-sync-v0 --require-api-guide --report docs/00-context/connector-sync-v0-sdd-completion-report.md`
- Status: Done. SDD gate passed on 2026-07-07.

### T-CONNECTOR-SYNC-V0-002: Add connector domain model and Flyway migration
- Requirements: REQ-CONNECTOR-SYNC-V0-002, REQ-CONNECTOR-SYNC-V0-003, REQ-CONNECTOR-SYNC-V0-004, REQ-CONNECTOR-SYNC-V0-005, REQ-CONNECTOR-SYNC-V0-006
- Spec: FR-CONNECTOR-SYNC-V0-002, FR-CONNECTOR-SYNC-V0-003, FR-CONNECTOR-SYNC-V0-004, FR-CONNECTOR-SYNC-V0-005, FR-CONNECTOR-SYNC-V0-006
- Owner: backend
- Priority: Must
- Verification: backend compile and repository/integration tests.
- Status: Done. Backend compile and full `mvn verify` passed on 2026-07-07.

### T-CONNECTOR-SYNC-V0-003: Implement connector adapter boundary and mock/local fixture adapter
- Requirements: REQ-CONNECTOR-SYNC-V0-001, REQ-CONNECTOR-SYNC-V0-009
- Spec: FR-CONNECTOR-SYNC-V0-001, FR-CONNECTOR-SYNC-V0-009
- Owner: backend
- Priority: Must
- Verification: unit tests assert adapter output is deterministic and does not expose real provider fields.
- Status: Done. `MockLocalFixtureConnectorAdapterTest` passed during `mvn verify`.

### T-CONNECTOR-SYNC-V0-004: Implement connector sync service and deterministic transitions
- Requirements: REQ-CONNECTOR-SYNC-V0-003, REQ-CONNECTOR-SYNC-V0-004, REQ-CONNECTOR-SYNC-V0-005, REQ-CONNECTOR-SYNC-V0-006
- Spec: FR-CONNECTOR-SYNC-V0-003, FR-CONNECTOR-SYNC-V0-004, FR-CONNECTOR-SYNC-V0-005, FR-CONNECTOR-SYNC-V0-006
- Owner: backend
- Priority: Must
- Verification: unit/integration tests for QUEUED -> RUNNING -> REVIEW_REQUIRED, FAILED, item persistence, and review-required artifacts.
- Status: Done in code and covered by connector API contract test; full backend verify passed.

### T-CONNECTOR-SYNC-V0-005: Implement connector sync API contracts
- Requirements: REQ-CONNECTOR-SYNC-V0-002, REQ-CONNECTOR-SYNC-V0-003, REQ-CONNECTOR-SYNC-V0-007
- Spec: FR-CONNECTOR-SYNC-V0-002, FR-CONNECTOR-SYNC-V0-003, FR-CONNECTOR-SYNC-V0-007
- Owner: backend
- Priority: Must
- Verification: API contract tests for definition list, create job/run, get run, list items, not found, validation, and safe redaction.
- Status: Done in `ConnectorSyncV0ApiContractIT`; full backend verify passed.

### T-CONNECTOR-SYNC-V0-006: Add backend tests
- Requirements: REQ-CONNECTOR-SYNC-V0-010
- Spec: FR-CONNECTOR-SYNC-V0-010
- Owner: backend
- Priority: Must
- Verification: `cd backend && mvn verify`
- Status: Done. `cd backend && mvn verify` passed on 2026-07-07.

### T-CONNECTOR-SYNC-V0-007: Add frontend types and API bindings
- Requirements: REQ-CONNECTOR-SYNC-V0-008, REQ-CONNECTOR-SYNC-V0-011
- Spec: FR-CONNECTOR-SYNC-V0-008, FR-CONNECTOR-SYNC-V0-011
- Owner: frontend
- Priority: Must
- Verification: `cd frontend && npm run typecheck`
- Status: Done. Typecheck passed on 2026-07-07.

### T-CONNECTOR-SYNC-V0-008: Add frontend connector sync UI and tests
- Requirements: REQ-CONNECTOR-SYNC-V0-008, REQ-CONNECTOR-SYNC-V0-011
- Spec: FR-CONNECTOR-SYNC-V0-008, FR-CONNECTOR-SYNC-V0-011
- Owner: frontend
- Priority: Must
- Verification: `cd frontend && npm run test`; `cd frontend && npm run build`
- Status: Done. Frontend tests passed (3 files, 22 tests) and build passed on 2026-07-07.

### T-CONNECTOR-SYNC-V0-009: Update evidence, run closeout, scan, commit, and push
- Requirements: REQ-CONNECTOR-SYNC-V0-012
- Spec: FR-CONNECTOR-SYNC-V0-012
- Owner: full-stack
- Priority: Must
- Verification: `npm run agent:closeout`; `git diff --check`; secret/private path/network dependency scan; commit `feat: add connector sync v0`; push to `develop-leo`.
- Status: Verification done. `git diff --check` and `npm run agent:closeout` passed; commit/push remain pending scoped staging.

## Dependency Plan

Critical path: T-CONNECTOR-SYNC-V0-001 -> T-CONNECTOR-SYNC-V0-002 -> T-CONNECTOR-SYNC-V0-003 -> T-CONNECTOR-SYNC-V0-004 -> T-CONNECTOR-SYNC-V0-005 -> T-CONNECTOR-SYNC-V0-006 -> T-CONNECTOR-SYNC-V0-007 -> T-CONNECTOR-SYNC-V0-008 -> T-CONNECTOR-SYNC-V0-009.

## Risks

- v0 does not prove real provider behavior.
- Synchronous execution is not production worker recovery.
- Review-required artifacts are metadata handoffs and are not approved Wiki pages.

## Open Questions

None blocking under the attached goal's preauthorization boundary.
