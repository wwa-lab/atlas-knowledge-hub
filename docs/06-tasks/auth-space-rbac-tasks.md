# Implementation Task Breakdown: auth-space-rbac

Status: Implemented with residual E2E gap
Last updated: 2026-07-06
Source spec: `docs/03-spec/auth-space-rbac-spec.md`
Source design: `docs/05-design/auth-space-rbac-design.md`

## Overview

This task plan implements backend-enforced authentication and space RBAC for controlled internal beta. The first implementation pass was completed on 2026-07-06 after SDD acceptance.

## Implementation Status

| Task | Status | Evidence |
|---|---|---|
| T-AUTH-SPACE-RBAC-001 | Done | Auth/RBAC enums, current-user context, auth decisions, and authorization matrix added. |
| T-AUTH-SPACE-RBAC-002 | Done | `V13__auth_space_rbac.sql` adds users, memberships, constraints, indexes, and sample-safe seed users. |
| T-AUTH-SPACE-RBAC-003 | Done | User/membership repositories and `SpaceMembershipService` added with last-owner protection. |
| T-AUTH-SPACE-RBAC-004 | Done | Mock-local current user resolver added via `X-Atlas-User`; missing and disabled users are denied. |
| T-AUTH-SPACE-RBAC-005 | Done | MVC interceptor and path policy enforce `401`, `403`, and safe `404` responses. |
| T-AUTH-SPACE-RBAC-006 | Done | `/api/auth/me` and `/api/spaces/{spaceId}/members` endpoints added. |
| T-AUTH-SPACE-RBAC-007 | Done | Existing core API domains are guarded through centralized path policy; graph-specific header guard removed. |
| T-AUTH-SPACE-RBAC-008 | Done | Frontend loads `/api/auth/me`, sends mock auth header, and disables representative write controls from capabilities. |
| T-AUTH-SPACE-RBAC-009 | Partial | Frontend unit coverage verifies viewer disabled controls; dedicated Playwright role E2E was not run in this pass. |
| T-AUTH-SPACE-RBAC-010 | Done | Backend full tests, frontend build, frontend typecheck, and frontend unit tests passed. |
| T-AUTH-SPACE-RBAC-011 | Done | `git diff --check`, focused secret scan, and network/dependency scan were run; findings were existing mock/test fixtures or existing configured adapter URLs. |
| T-AUTH-SPACE-RBAC-012 | Done | Traceability updated with implementation evidence and residual risks. |

## Workstreams

- Backend auth foundation: provider boundary, current user context, policy, envelope errors.
- Persistence: additive user and membership schema plus repositories/services.
- API coverage: `/api/auth/me`, membership APIs, and guards across existing domains.
- Frontend awareness: auth state and capability-driven UI.
- Verification: unit, integration, E2E, and safety scans.

Recommended sequencing:

1. T-AUTH-SPACE-RBAC-001 through T-AUTH-SPACE-RBAC-005 establish backend data and policy.
2. T-AUTH-SPACE-RBAC-006 through T-AUTH-SPACE-RBAC-008 integrate APIs and frontend.
3. T-AUTH-SPACE-RBAC-009 through T-AUTH-SPACE-RBAC-012 verify, scan, and close out.

## Task Details

### T-AUTH-SPACE-RBAC-001: Add auth and RBAC domain types

- **Objective:** Define role, capability, user status, membership status, current user context, and auth decision result types.
- **Scope:** Cover REQ-AUTH-SPACE-RBAC-004, REQ-AUTH-SPACE-RBAC-006, REQ-AUTH-SPACE-RBAC-013.
- **Dependencies:** None.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** Unit tests for enum parsing and role/capability coverage.

### T-AUTH-SPACE-RBAC-002: Add additive Flyway user and membership schema

- **Objective:** Create `atlas_user` and `space_membership` schema and mock/sample-safe seed memberships.
- **Scope:** Cover REQ-AUTH-SPACE-RBAC-005, REQ-AUTH-SPACE-RBAC-014.
- **Dependencies:** T-AUTH-SPACE-RBAC-001.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** Flyway migration validates in `mvn verify`; seed data contains only sample-safe identities.

### T-AUTH-SPACE-RBAC-003: Implement repositories and membership service

- **Objective:** Add user/membership repositories and service methods for list, create, update, remove, and last-owner validation.
- **Scope:** Cover REQ-AUTH-SPACE-RBAC-005, REQ-AUTH-SPACE-RBAC-012.
- **Dependencies:** T-AUTH-SPACE-RBAC-002.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** Unit/integration tests cover role/status validation and last active `SPACE_OWNER` protection.

### T-AUTH-SPACE-RBAC-004: Implement mock auth provider and current user resolver

- **Objective:** Resolve current user context from local/test mock auth without external network calls.
- **Scope:** Cover REQ-AUTH-SPACE-RBAC-001, REQ-AUTH-SPACE-RBAC-002, REQ-AUTH-SPACE-RBAC-003, REQ-AUTH-SPACE-RBAC-004.
- **Dependencies:** T-AUTH-SPACE-RBAC-003.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** Integration tests cover missing auth `401`, mock user success, disabled user denial, and no external dependency.

### T-AUTH-SPACE-RBAC-005: Implement authorization policy and safe denied responses

- **Objective:** Centralize action-to-capability mapping and envelope-compatible `401`/`403` handling.
- **Scope:** Cover REQ-AUTH-SPACE-RBAC-007, REQ-AUTH-SPACE-RBAC-009, REQ-AUTH-SPACE-RBAC-010, REQ-AUTH-SPACE-RBAC-011.
- **Dependencies:** T-AUTH-SPACE-RBAC-004.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** Unit tests for role matrix; integration tests for `VIEWER` write denial and cross-space non-disclosure.

### T-AUTH-SPACE-RBAC-006: Add `/api/auth/me` and membership endpoints

- **Objective:** Implement current user and member management API contracts.
- **Scope:** Cover REQ-AUTH-SPACE-RBAC-004, REQ-AUTH-SPACE-RBAC-012, REQ-AUTH-SPACE-RBAC-014.
- **Dependencies:** T-AUTH-SPACE-RBAC-005.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** API contract tests cover `/api/auth/me`, member list/create/update/delete, `401`, `403`, and `409`.

### T-AUTH-SPACE-RBAC-007: Apply guards to existing core API domains

- **Objective:** Protect space, batch, file, chunk, review/publish, Wiki, graph, Ask, model, storage, vector, parser, conversion, ingestion, and downstream-refresh APIs.
- **Scope:** Cover REQ-AUTH-SPACE-RBAC-007, REQ-AUTH-SPACE-RBAC-011.
- **Dependencies:** T-AUTH-SPACE-RBAC-005.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** Representative integration tests by domain show allowed/denied outcomes; graph no longer depends on graph-only demo headers as the primary policy.

### T-AUTH-SPACE-RBAC-008: Add frontend auth state and permission-aware controls

- **Objective:** Load `/api/auth/me`, store capabilities, and update UI controls to reflect backend-provided permissions.
- **Scope:** Cover REQ-AUTH-SPACE-RBAC-008.
- **Dependencies:** T-AUTH-SPACE-RBAC-006, T-AUTH-SPACE-RBAC-007.
- **Owner type:** frontend.
- **Priority:** Must.
- **Verification:** Frontend tests cover viewer disabled/hidden writes and owner member controls.

### T-AUTH-SPACE-RBAC-009: Add role-specific E2E coverage

- **Objective:** Cover ordinary user, `KNOWLEDGE_MANAGER`, and `SPACE_OWNER` product path differences.
- **Scope:** Cover AC-AUTH-SPACE-RBAC-004 and REQ-AUTH-SPACE-RBAC-014.
- **Dependencies:** T-AUTH-SPACE-RBAC-008.
- **Owner type:** QA/frontend.
- **Priority:** Must.
- **Verification:** E2E asserts role-aware controls and backend `403` for manipulated viewer write calls.

### T-AUTH-SPACE-RBAC-010: Run backend and frontend verification

- **Objective:** Run implementation-level verification after code changes.
- **Scope:** Cover all requirements.
- **Dependencies:** T-AUTH-SPACE-RBAC-001 through T-AUTH-SPACE-RBAC-009.
- **Owner type:** QA.
- **Priority:** Must.
- **Verification:** `mvn verify`, frontend typecheck/test/build/E2E commands relevant to touched frontend, and any skipped checks reported with reasons.

### T-AUTH-SPACE-RBAC-011: Run security, network, dependency, and diff scans

- **Objective:** Prove the slice does not add real data, raw secrets, private paths, external cloud calls, or unsafe dependency drift.
- **Scope:** Cover REQ-AUTH-SPACE-RBAC-014.
- **Dependencies:** T-AUTH-SPACE-RBAC-010.
- **Owner type:** security.
- **Priority:** Must.
- **Verification:** `git diff --check`, focused secret/private-path scan, and focused network/dependency scan pass.

### T-AUTH-SPACE-RBAC-012: Update traceability and closeout evidence

- **Objective:** Update traceability, roadmap status, verification evidence, residual risks, and lessons if needed.
- **Scope:** Cover SDD closeout gates.
- **Dependencies:** T-AUTH-SPACE-RBAC-010, T-AUTH-SPACE-RBAC-011.
- **Owner type:** backend/frontend.
- **Priority:** Must.
- **Verification:** `npm run agent:check-sdd -- --slice auth-space-rbac --require-api-guide` passes after docs updates.

## Dependency Plan

- Critical path: T-AUTH-SPACE-RBAC-001 → T-AUTH-SPACE-RBAC-002 → T-AUTH-SPACE-RBAC-003 → T-AUTH-SPACE-RBAC-004 → T-AUTH-SPACE-RBAC-005 → T-AUTH-SPACE-RBAC-006/T-AUTH-SPACE-RBAC-007 → T-AUTH-SPACE-RBAC-008 → T-AUTH-SPACE-RBAC-009 → T-AUTH-SPACE-RBAC-010 → T-AUTH-SPACE-RBAC-011 → T-AUTH-SPACE-RBAC-012.
- Parallel work: T-AUTH-SPACE-RBAC-006 and T-AUTH-SPACE-RBAC-007 can proceed after policy exists; frontend starts after `/api/auth/me` shape is stable.

## Risks / Blockers

- Adding auth guard coverage across many controllers is broad; representative endpoint tests are mandatory.
- Spring Security or equivalent filter changes can alter current integration test setup; keep mock profile deterministic.
- Production SSO/OIDC, audit retention, secret manager, and rate limiting remain future slices.

## Open Questions

- OQ-AUTH-SPACE-RBAC-001: Future `AUDITOR` visibility for audit logs.
- OQ-AUTH-SPACE-RBAC-002: Future SSO group-to-role mapping policy.

## Definition Of Done

- All T-AUTH-SPACE-RBAC-001 through T-AUTH-SPACE-RBAC-012 are complete.
- All AC-AUTH-SPACE-RBAC-001 through AC-AUTH-SPACE-RBAC-006 are verified or explicitly reported blocked.
- Verification evidence and residual risks are recorded in traceability.
