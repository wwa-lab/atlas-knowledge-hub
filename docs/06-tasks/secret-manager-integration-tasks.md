# Tasks: secret-manager-integration

Status: Implemented and closeout verified locally
Last updated: 2026-07-07

## Workstreams

- Backend DTO/API contracts.
- Backend service and adapter capability mapping.
- Frontend type and settings display.
- Tests, scans, traceability, roadmap, closeout.

## Task Details

### T-SECRET-MANAGER-INTEGRATION-001: Add Shared Secret Status DTOs

- Objective: Add `SecretReferenceResponse` and `SecretStatusResponse`.
- Scope: Backend DTOs only; no persistence.
- Requirements: REQ-001, REQ-007.
- Verification: backend compile through targeted tests.
- Status: Done.

### T-SECRET-MANAGER-INTEGRATION-002: Extend Model Configuration Contract

- Objective: Return typed secret statuses from model configuration read/save/clear.
- Scope: `ModelConfigurationResponse`, `ModelRuntimeConfigurationService`, relevant tests.
- Requirements: REQ-001, REQ-002, REQ-003, REQ-008.
- Verification: `cd backend && mvn -Dtest=ModelRuntimeConfigurationServiceTest,ModelApiContractIT test`.
- Status: Done; full backend verification passed after Docker/Testcontainers became available.

### T-SECRET-MANAGER-INTEGRATION-003: Extend Adapter Capability Contracts

- Objective: Add typed secret statuses to model, parser, converter, storage, and vector capability responses.
- Scope: Capability response records, adapter capability records or mappers, existing adapters.
- Requirements: REQ-004, REQ-005, REQ-007, REQ-008.
- Verification: backend capability/API contract tests.
- Status: Done.

### T-SECRET-MANAGER-INTEGRATION-004: Update Frontend Settings Display

- Objective: Consume `secretStatuses` where available and render status-only labels.
- Scope: frontend API types, model/adapter settings display, local transient secret input handling.
- Requirements: REQ-006, REQ-008.
- Verification: `cd frontend && npm run typecheck && npm run test`.
- Status: Done.

### T-SECRET-MANAGER-INTEGRATION-005: Add Redaction Tests

- Objective: Prove write-only secrets, endpoints, commands, and private paths are absent from read responses/UI tests.
- Scope: backend unit/API tests and frontend component tests.
- Requirements: REQ-008.
- Verification: targeted backend/frontend tests.
- Status: Done.

### T-SECRET-MANAGER-INTEGRATION-006: Update Traceability And Roadmaps

- Objective: Record SDD skill chain, implementation evidence, residual risks, and status.
- Scope: secret traceability plus slice/repo roadmap status.
- Requirements: REQ-008.
- Verification: `npm run agent:check-sdd -- --slice secret-manager-integration --require-api-guide` and closeout.
- Status: Done; traceability and roadmap/status documents updated.

### T-SECRET-MANAGER-INTEGRATION-007: Run Full Verification And Closeout

- Objective: Run required checks and fix scoped failures.
- Scope: backend/frontend verification, scans, diff check, closeout.
- Requirements: all.
- Verification:
  - `cd backend && mvn verify`
  - `cd frontend && npm run typecheck`
  - `cd frontend && npm run test`
  - `cd frontend && npm run build`
  - `npm run agent:closeout`
  - `git diff --check`
  - focused secret/private-path/real-data scan
- Status: Done.

## Dependency Plan

Critical path: T-001 -> T-002 -> T-003 -> T-004 -> T-005 -> T-006 -> T-007.

## Risks

- Runtime state remains in-memory and is not production secret storage.
- Existing request payload field names remain for compatibility but are treated as write-only.
