# Tasks: Wiki Data Model

## Status

Accepted by the current user and implemented on 2026-07-05. This task set is complete for the `wiki-data-model` slice.

## Overview

Implement the `wiki-data-model` slice as an additive Wiki Foundation data-model upgrade. The delivery objective is a compatible backend/frontend metadata foundation for future Auto Wiki work, not an ingest pipeline or production-ready Wiki system.

## Source Design

- Spec: `docs/03-spec/wiki-data-model-spec.md`
- Architecture: `docs/04-architecture/wiki-data-model-architecture.md`
- Data flow: `docs/04-architecture/wiki-data-model-data-flow.md`
- Data model: `docs/04-architecture/wiki-data-model-data-model.md`
- Design: `docs/05-design/wiki-data-model-design.md`
- API guide: `docs/05-design/contracts/wiki-data-model-API_IMPLEMENTATION_GUIDE.md`

## Workstreams

| Workstream | Tasks | Notes |
|---|---|---|
| SDD acceptance | T-WIKI-DATA-MODEL-001 | Human gate before code. |
| Persistence/domain/API | T-WIKI-DATA-MODEL-002 through T-WIKI-DATA-MODEL-007 | Additive backend foundation. |
| Frontend | T-WIKI-DATA-MODEL-008 | Metadata rendering and fallback safety. |
| Regression and safety | T-WIKI-DATA-MODEL-009 through T-WIKI-DATA-MODEL-011 | Verification and close-out evidence. |

## Task Summary

| Task | Title | Owner | Priority | Depends On |
|---|---|---|---|---|
| T-WIKI-DATA-MODEL-001 | Obtain SDD acceptance gate | product/docs | Must | None |
| T-WIKI-DATA-MODEL-002 | Add Flyway migration for Wiki Foundation model | backend | Must | T-WIKI-DATA-MODEL-001 |
| T-WIKI-DATA-MODEL-003 | Extend Wiki domain entities and repositories | backend | Must | T-WIKI-DATA-MODEL-002 |
| T-WIKI-DATA-MODEL-004 | Extend DTOs and mapping for Wiki metadata | backend | Must | T-WIKI-DATA-MODEL-003 |
| T-WIKI-DATA-MODEL-005 | Preserve publish/list/id-detail compatibility | backend | Must | T-WIKI-DATA-MODEL-004 |
| T-WIKI-DATA-MODEL-006 | Add slug, folder, run, log, and issue read APIs | backend | Must | T-WIKI-DATA-MODEL-004 |
| T-WIKI-DATA-MODEL-007 | Add backend contract and repository coverage | backend / QA | Must | T-WIKI-DATA-MODEL-005, T-WIKI-DATA-MODEL-006 |
| T-WIKI-DATA-MODEL-008 | Render new Wiki metadata in Vue | frontend | Must | T-WIKI-DATA-MODEL-004 |
| T-WIKI-DATA-MODEL-009 | Add frontend unit and E2E regression coverage | frontend / QA | Must | T-WIKI-DATA-MODEL-008 |
| T-WIKI-DATA-MODEL-010 | Run full verification gates and safety scans | QA/security | Must | T-WIKI-DATA-MODEL-007, T-WIKI-DATA-MODEL-009 |
| T-WIKI-DATA-MODEL-011 | Update traceability and completion evidence | docs | Must | T-WIKI-DATA-MODEL-010 |

## Task Details

### T-WIKI-DATA-MODEL-001: Obtain SDD acceptance gate

- **Maps to:** REQ-WIKI-DATA-MODEL-001; spec S1.
- **Objective:** Confirm the user accepts the SDD scope and implementation tasks before product code changes.
- **Scope:** Present this SDD set for review. Apply requested SDD revisions before coding.
- **Dependencies:** None.
- **Owner type:** product/docs.
- **Priority:** Must.
- **Verification:** Confirm all expected bilingual files exist and IDs match.

### T-WIKI-DATA-MODEL-002: Add Flyway migration for Wiki Foundation model

- **Maps to:** REQ-WIKI-DATA-MODEL-002, 004, 005, 006, 007, 013.
- **Objective:** Add an additive migration after V9 for new `wiki_page` columns and support tables.
- **Scope:** Create expected `V10__wiki_data_model.sql` unless another migration number is current at implementation time. Add safe defaults, checks, indexes, and constraints from the data model. Preserve V1-V9 seeded data.
- **Dependencies:** T-WIKI-DATA-MODEL-001.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** `cd backend && mvn verify`.

### T-WIKI-DATA-MODEL-003: Extend Wiki domain entities and repositories

- **Maps to:** REQ-WIKI-DATA-MODEL-002, 003, 004, 005, 006, 007.
- **Objective:** Represent new Wiki Foundation fields and tables in the backend domain/repository layer.
- **Scope:** Extend `WikiPage`; add `WikiFolder`, `WikiGenerationRun`, `WikiLogEntry`, `WikiPageIssue`; add repository methods for list/detail/slug/folder/run/log/issue reads.
- **Dependencies:** T-WIKI-DATA-MODEL-002.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** Backend unit/repository tests and `cd backend && mvn verify`.

### T-WIKI-DATA-MODEL-004: Extend DTOs and mapping for Wiki metadata

- **Maps to:** REQ-WIKI-DATA-MODEL-008, 012.
- **Objective:** Return safe typed response DTOs for page refs and support records.
- **Scope:** Extend `WikiPageResponse`; add reference/folder/run/log/issue responses; ensure arrays/lists default to empty collections.
- **Dependencies:** T-WIKI-DATA-MODEL-003.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** Backend contract tests assert response shapes and no raw unsafe content.

### T-WIKI-DATA-MODEL-005: Preserve publish/list/id-detail compatibility

- **Maps to:** REQ-WIKI-DATA-MODEL-002, 009, 012, 013.
- **Objective:** Ensure existing review-publish behavior continues while returning extended page metadata.
- **Scope:** Update publish defaults, list pages, and get by id without changing eligibility rules or existing trusted read behavior.
- **Dependencies:** T-WIKI-DATA-MODEL-004.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** `cd backend && mvn test -Dtest='ReviewPublishServiceTest,ReviewPublishApiContractIT'`.

### T-WIKI-DATA-MODEL-006: Add slug, folder, run, log, and issue read APIs

- **Maps to:** REQ-WIKI-DATA-MODEL-003, 004, 005, 006, 007, 008.
- **Objective:** Expose the new minimal read contracts.
- **Scope:** Implement endpoints from the API guide using `ApiEnvelope` and safe errors.
- **Dependencies:** T-WIKI-DATA-MODEL-004.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** Backend API contract tests for slug lookup, folders, runs, logs, and issues.

### T-WIKI-DATA-MODEL-007: Add backend contract and repository coverage

- **Maps to:** REQ-WIKI-DATA-MODEL-013, 015.
- **Objective:** Prove migration compatibility and API contracts.
- **Scope:** Add tests for Flyway application, legacy rows/defaults, space-scoped slug lookup, support reads, and safe errors.
- **Dependencies:** T-WIKI-DATA-MODEL-005, T-WIKI-DATA-MODEL-006.
- **Owner type:** backend / QA.
- **Priority:** Must.
- **Verification:** `cd backend && mvn verify`.

### T-WIKI-DATA-MODEL-008: Render new Wiki metadata in Vue

- **Maps to:** REQ-WIKI-DATA-MODEL-010, 011, 012.
- **Objective:** Show new page metadata in the real Vue Wiki tab without breaking fallback sample safety.
- **Scope:** Update frontend types/API mapping and Wiki tab rendering for slug, page type, aliases, refs, link counts/ids, version, source mode, and refresh policy.
- **Dependencies:** T-WIKI-DATA-MODEL-004.
- **Owner type:** frontend.
- **Priority:** Must.
- **Verification:** `cd frontend && npm run typecheck && npm run test`.

### T-WIKI-DATA-MODEL-009: Add frontend unit and E2E regression coverage

- **Maps to:** REQ-WIKI-DATA-MODEL-010, 011, 015.
- **Objective:** Protect visible metadata and existing critical flows.
- **Scope:** Add/update unit tests and Playwright tests for API-backed Wiki metadata display, fallback sample safety, review-publish, graph, and Ask regressions.
- **Dependencies:** T-WIKI-DATA-MODEL-008.
- **Owner type:** frontend / QA.
- **Priority:** Must.
- **Verification:** `cd frontend && npm run typecheck && npm run test && npm run build && npm run e2e`.

### T-WIKI-DATA-MODEL-010: Run full verification gates and safety scans

- **Maps to:** REQ-WIKI-DATA-MODEL-014, 015.
- **Objective:** Prove the slice does not regress current Wiki/Graph/Ask behavior or data safety.
- **Scope:** Run required commands and focused scans.
- **Dependencies:** T-WIKI-DATA-MODEL-007, T-WIKI-DATA-MODEL-009.
- **Owner type:** QA/security.
- **Priority:** Must.
- **Verification:**
  - `cd backend && mvn verify`
  - `cd frontend && npm run typecheck && npm run test && npm run build`
  - `cd frontend && npm run e2e`
  - `npm run e2e:second-layer`
  - `git diff --check`
  - focused secret/private-path scan
  - focused network/dependency scan

### T-WIKI-DATA-MODEL-011: Update traceability and completion evidence

- **Maps to:** REQ-WIKI-DATA-MODEL-001, 015.
- **Objective:** Record final implementation status, verification evidence, skipped checks, and residual risks.
- **Scope:** Update `docs/00-context/wiki-data-model-traceability.md` and `.zh-CN.md`; update roadmap/status files only if product status changes during implementation.
- **Dependencies:** T-WIKI-DATA-MODEL-010.
- **Owner type:** docs.
- **Priority:** Must.
- **Verification:** Final report lists docs changed, code changed, verification run, skipped checks, and maturity statement.

## Dependency Plan

Critical path: T-WIKI-DATA-MODEL-001 -> T-WIKI-DATA-MODEL-002 -> T-WIKI-DATA-MODEL-003 -> T-WIKI-DATA-MODEL-004 -> T-WIKI-DATA-MODEL-005/T-WIKI-DATA-MODEL-006 -> T-WIKI-DATA-MODEL-007 -> T-WIKI-DATA-MODEL-008 -> T-WIKI-DATA-MODEL-009 -> T-WIKI-DATA-MODEL-010 -> T-WIKI-DATA-MODEL-011.

T-WIKI-DATA-MODEL-008 may begin after DTO shape is stable, but final frontend verification depends on backend contract completion.

## Risks / Blockers

- User acceptance is required before code changes.
- Flyway migration must handle slug backfill without breaking existing seeded rows.
- Support tables may appear to imply completed generation/lint workflows; UI/docs must label them as foundation metadata only.
- Docker/PostgreSQL availability may block second-layer verification.

## Open Questions

- OQ-WIKI-DATA-MODEL-001: Future generated-page initial status.
- OQ-WIKI-DATA-MODEL-002: Future checksum/freshness metadata.
- OQ-WIKI-DATA-MODEL-003: Future folder ordering behavior.

## Definition Of Done

- SDD accepted by user.
- All Must tasks complete.
- Required verification run or explicitly reported skipped with reasons.
- Final response states: Wiki Foundation data model completed; Auto Wiki ingest not completed; production readiness not claimed.
