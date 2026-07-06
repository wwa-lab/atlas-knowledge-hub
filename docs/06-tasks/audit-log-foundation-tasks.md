# Implementation Task Breakdown: audit-log-foundation

Status: Implemented after user acceptance
Last updated: 2026-07-06
Source spec: `docs/03-spec/audit-log-foundation-spec.md`
Source design: `docs/05-design/audit-log-foundation-design.md`

## Overview

Implement a safe append-only audit log foundation for core Atlas governance events. The user accepted the SDD, and this task set has been implemented in the prototype stack.

## Workstreams

- Backend audit domain and persistence.
- Audit capture in auth, membership, and representative knowledge operations.
- Read-only audit APIs with RBAC and pagination.
- Frontend audit panel and typed API integration.
- Verification, safety scans, traceability, and closeout.

## Implementation Evidence

- T-AUDIT-LOG-FOUNDATION-001 through T-AUDIT-LOG-FOUNDATION-003: implemented via `AuditEvent`, audit enums, DTO/mapper, `AuditEventRepository`, `AuditLogService`, and `V14__audit_log_foundation.sql`.
- T-AUDIT-LOG-FOUNDATION-004: implemented for auth denials and membership create/update/remove/last-owner conflict flows.
- T-AUDIT-LOG-FOUNDATION-005: implemented for representative graph projection/review/access-denied bridge events, Wiki publish events, and Trusted Ask run outcomes. Wiki ingest/linkify-lint and adapter/runtime emitters are explicitly deferred follow-on coverage. Existing `graph_audit_record` behavior is preserved with parallel general audit writes.
- T-AUDIT-LOG-FOUNDATION-006: implemented with `GET /api/spaces/{spaceId}/audit-events` and `GET /api/spaces/{spaceId}/audit-events/targets/{targetType}/{targetId}`.
- T-AUDIT-LOG-FOUNDATION-007: implemented as a read-only Vue settings audit panel backed by typed API calls.
- T-AUDIT-LOG-FOUNDATION-008 and T-AUDIT-LOG-FOUNDATION-009: verification and traceability are recorded in `docs/00-context/audit-log-foundation-traceability.md`.

## Task Details

### T-AUDIT-LOG-FOUNDATION-001: Add audit domain model and enums

- **Objective:** Define audit event entity, category/result/severity enums, safe metadata rules, DTOs, and mapper.
- **Scope:** REQ-AUDIT-LOG-FOUNDATION-001, 002, 011.
- **Dependencies:** Accepted SDD.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** Unit tests cover enum values, mapper output, and safe metadata allowlist.

### T-AUDIT-LOG-FOUNDATION-002: Add Flyway migration and repository

- **Objective:** Create `atlas.audit_event` with indexes and repository query methods.
- **Scope:** REQ-AUDIT-LOG-FOUNDATION-001, 007, 012.
- **Dependencies:** T-AUDIT-LOG-FOUNDATION-001.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** `cd backend && mvn verify` validates Flyway and repository tests.

### T-AUDIT-LOG-FOUNDATION-003: Implement AuditLogService

- **Objective:** Add centralized append-only event recording, safe command validation, metadata allowlist, and query methods.
- **Scope:** REQ-AUDIT-LOG-FOUNDATION-001, 002, 007, 009.
- **Dependencies:** T-AUDIT-LOG-FOUNDATION-002.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** Service tests cover safe event creation, forbidden metadata rejection, pagination, and filters.

### T-AUDIT-LOG-FOUNDATION-004: Record auth and membership audit events

- **Objective:** Emit events for denied auth decisions and membership create/update/remove/conflict flows.
- **Scope:** REQ-AUDIT-LOG-FOUNDATION-003, 004, 008, 009.
- **Dependencies:** T-AUDIT-LOG-FOUNDATION-003.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** Integration tests cover `401`, `403`, safe `404`, membership success, and last-owner conflict audit events.

### T-AUDIT-LOG-FOUNDATION-005: Record representative knowledge-operation events

- **Objective:** Emit safe foundation events for Wiki publish, graph projection/review/access-denied, and Trusted Ask run outcomes while documenting Wiki ingest/linkify-lint and adapter/runtime emitters as deferred follow-on coverage.
- **Scope:** REQ-AUDIT-LOG-FOUNDATION-005, 006.
- **Dependencies:** T-AUDIT-LOG-FOUNDATION-003.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** Service/integration tests prove representative publish, graph, and Ask events; traceability records the deferred emitters without claiming production audit coverage.

### T-AUDIT-LOG-FOUNDATION-006: Add audit read APIs

- **Objective:** Add read-only list endpoints for space and target audit events with filters and pagination.
- **Scope:** REQ-AUDIT-LOG-FOUNDATION-007, 008, 009, 012.
- **Dependencies:** T-AUDIT-LOG-FOUNDATION-003.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** API contract tests cover filters, pagination, RBAC, invalid filters, and cross-space non-disclosure.

### T-AUDIT-LOG-FOUNDATION-007: Add frontend audit panel

- **Objective:** Add typed API calls, domain types, and read-only audit UI gated by backend capabilities.
- **Scope:** REQ-AUDIT-LOG-FOUNDATION-010.
- **Dependencies:** T-AUDIT-LOG-FOUNDATION-006.
- **Owner type:** frontend.
- **Priority:** Should.
- **Verification:** `npm --prefix frontend run typecheck`, unit tests, build, and E2E when the panel is user-facing.

### T-AUDIT-LOG-FOUNDATION-008: Run verification and safety scans

- **Objective:** Run backend, frontend, workflow, diff, secret/private-path, and network/dependency checks.
- **Scope:** REQ-AUDIT-LOG-FOUNDATION-002, 012.
- **Dependencies:** T-AUDIT-LOG-FOUNDATION-001 through T-AUDIT-LOG-FOUNDATION-007.
- **Owner type:** QA/security.
- **Priority:** Must.
- **Verification:** `npm run agent:check-sdd -- --slice audit-log-foundation --require-api-guide`, `cd backend && mvn verify`, frontend checks if touched, `git diff --check`, focused scans, and `npm run agent:closeout`.

### T-AUDIT-LOG-FOUNDATION-009: Update traceability and roadmap status

- **Objective:** Record completed tasks, verification evidence, residual risks, graph audit decision, and closeout status.
- **Scope:** Goal-driven SDD closeout.
- **Dependencies:** T-AUDIT-LOG-FOUNDATION-008.
- **Owner type:** backend/frontend.
- **Priority:** Must.
- **Verification:** Traceability and roadmap files match implementation maturity and do not claim production readiness.

## Dependency Plan

Critical path: T-001 -> T-002 -> T-003 -> T-004/T-005/T-006 -> T-007 -> T-008 -> T-009.

Parallel work:

- T-004 and T-005 can proceed after AuditLogService exists.
- T-007 can begin after API response shape stabilizes.

## Risks / Blockers

- The implementation is a prototype audit foundation, not a production retention/SIEM/export/compliance system.
- Historical `graph_audit_record` rows are preserved but not backfilled into `audit_event`.
- Full `KnowledgeGraphApiContractIT` remains blocked by pre-existing Graph RBAC/default-auth helper contract drift outside this slice.

## Definition Of Done

- All Must tasks complete.
- SDD gate and implementation verification evidence recorded.
- No raw secrets, private paths, raw content, provider payloads, or external cloud calls introduced.
- Traceability records evidence, residual risks, and deferred historical graph audit backfill.
