# Implementation Tasks: Ask Session Citations

## Overview

Implement Ask session persistence, session read APIs, richer citation snapshot fields, frontend session/citation display, tests, and closeout documentation.

## Task Details

### T-ASK-SESSION-CITATIONS-001: Accept SDD Gate
- **Objective:** Confirm the full bilingual SDD set is complete and inside the autonomous prompt boundary.
- **Dependencies:** None.
- **Owner type:** docs
- **Priority:** Must
- **Verification:** `npm run agent:check-sdd -- --slice ask-session-citations --require-api-guide`

### T-ASK-SESSION-CITATIONS-002: Add Persistence Model
- **Objective:** Add `AskSession`, `session_id` on `AskRun`, citation metadata fields on `AskEvidence`, repositories, and Flyway migration `V15__ask_session_citations.sql`.
- **Dependencies:** T-ASK-SESSION-CITATIONS-001
- **Owner type:** backend
- **Priority:** Must
- **Verification:** `cd backend && mvn -Dtest=AskDomainInvariantTest,AskStateImmutabilityTest test`

### T-ASK-SESSION-CITATIONS-003: Extend Ask Service
- **Objective:** Create/reuse sessions during Ask creation, derive safe citation labels/status, list sessions, and read session detail.
- **Dependencies:** T-ASK-SESSION-CITATIONS-002
- **Owner type:** backend
- **Priority:** Must
- **Verification:** `cd backend && mvn -Dtest=AskServiceTest,AskRequestValidationTest test`

### T-ASK-SESSION-CITATIONS-004: Extend API DTOs And Controller
- **Objective:** Add session request fields, session response DTOs, richer citation DTO fields, and session endpoints.
- **Dependencies:** T-ASK-SESSION-CITATIONS-003
- **Owner type:** backend
- **Priority:** Must
- **Verification:** `cd backend && mvn -Dit.test=AskSessionCitationsApiContractIT verify`

### T-ASK-SESSION-CITATIONS-005: Preserve Safe Evidence Boundaries
- **Objective:** Ensure citations expose safe labels and review eligibility only, never raw source content, private paths, endpoints, secrets, or stack traces.
- **Dependencies:** T-ASK-SESSION-CITATIONS-003
- **Owner type:** security
- **Priority:** Must
- **Verification:** focused diff scan for secret/private-path/network hits.

### T-ASK-SESSION-CITATIONS-006: Update Frontend Types And API Client
- **Objective:** Add TypeScript types and client helpers for Ask sessions and richer citation fields.
- **Dependencies:** T-ASK-SESSION-CITATIONS-004
- **Owner type:** frontend
- **Priority:** Must
- **Verification:** `cd frontend && npm run typecheck`

### T-ASK-SESSION-CITATIONS-007: Render Session History And Citation Detail
- **Objective:** Update Trusted Ask UI to display recent sessions, selected session answer history, and citation metadata while preserving existing answer behavior.
- **Dependencies:** T-ASK-SESSION-CITATIONS-006
- **Owner type:** frontend
- **Priority:** Must
- **Verification:** `cd frontend && npm run test`

### T-ASK-SESSION-CITATIONS-008: Update Frontend E2E Fixtures
- **Objective:** Extend API mocks and E2E coverage for session list/detail and citation fields.
- **Dependencies:** T-ASK-SESSION-CITATIONS-007
- **Owner type:** QA
- **Priority:** Should
- **Verification:** `cd frontend && npm run build`

### T-ASK-SESSION-CITATIONS-009: Run Full Verification
- **Objective:** Run required backend, frontend, closeout, diff, and safety checks.
- **Dependencies:** T-ASK-SESSION-CITATIONS-002 through T-ASK-SESSION-CITATIONS-008
- **Owner type:** QA
- **Priority:** Must
- **Verification:** `cd backend && mvn verify`; `cd frontend && npm run typecheck`; `cd frontend && npm run test`; `cd frontend && npm run build`; `npm run agent:closeout`; `git diff --check`

### T-ASK-SESSION-CITATIONS-010: Update Traceability And Roadmaps
- **Objective:** Record completed tasks, verification evidence, residual risks, and Wave 4 status.
- **Dependencies:** T-ASK-SESSION-CITATIONS-009
- **Owner type:** docs
- **Priority:** Must
- **Verification:** traceability and roadmap files include final evidence.

## Dependency Plan

Critical path: T-ASK-SESSION-CITATIONS-001 -> T-ASK-SESSION-CITATIONS-002 -> T-ASK-SESSION-CITATIONS-003 -> T-ASK-SESSION-CITATIONS-004 -> T-ASK-SESSION-CITATIONS-006 -> T-ASK-SESSION-CITATIONS-007 -> T-ASK-SESSION-CITATIONS-009 -> T-ASK-SESSION-CITATIONS-010.

## Risks / Blockers

- Stop if implementation requires changing provider/model, auth/RBAC, audit, secret, or rate-limit semantics.
- Stop if citation detail would expose raw source content or unsafe labels.

## Open Questions

- None.

## Completion Evidence

| Task | Status | Evidence |
|---|---|---|
| T-ASK-SESSION-CITATIONS-001 | Complete | SDD gate passed with `npm run agent:check-sdd -- --slice ask-session-citations --require-api-guide`. |
| T-ASK-SESSION-CITATIONS-002 | Complete | Added session/citation persistence model and `V15__ask_session_citations.sql`; covered by `mvn verify`. |
| T-ASK-SESSION-CITATIONS-003 | Complete | `AskServiceTest` covers session history and review-aware citation status. |
| T-ASK-SESSION-CITATIONS-004 | Complete | `AskApiContractIT` covers create/get run plus session list/detail and citation fields. |
| T-ASK-SESSION-CITATIONS-005 | Complete | Citation labels/locators use sanitized bounded fields; safety scan included in final diff review. |
| T-ASK-SESSION-CITATIONS-006 | Complete | `npm run typecheck` passed. |
| T-ASK-SESSION-CITATIONS-007 | Complete | `npm run test` passed with Ask UI contract coverage. |
| T-ASK-SESSION-CITATIONS-008 | Complete | Frontend P0 mocks include session list/detail and richer citation fields; `npm run build` passed. |
| T-ASK-SESSION-CITATIONS-009 | Complete | Backend and frontend verification passed; closeout and diff checks recorded in traceability. |
| T-ASK-SESSION-CITATIONS-010 | Complete | Traceability and roadmap/status docs updated. |
