# Tasks: Wiki Ingest v0

## Status

Implementation verified. All Must tasks for Auto Wiki ingest v0 are complete; generated candidates remain review-required and this does not complete linkify/lint, review gate, connector, model-assisted generation, or production readiness.

## Overview

Implement Auto Wiki ingest v0 as a review-required candidate generation flow from approved source chunks. The delivery objective is a safe, idempotent Wiki ingest foundation, not linkify/lint, review gate, connector, or production readiness.

## Source Design

- Spec: `docs/03-spec/wiki-ingest-v0-spec.md`
- Architecture: `docs/04-architecture/wiki-ingest-v0-architecture.md`
- Data flow: `docs/04-architecture/wiki-ingest-v0-data-flow.md`
- Data model: `docs/04-architecture/wiki-ingest-v0-data-model.md`
- Design: `docs/05-design/wiki-ingest-v0-design.md`
- API guide: `docs/05-design/contracts/wiki-ingest-v0-API_IMPLEMENTATION_GUIDE.md`

## Workstreams

| Workstream | Tasks | Notes |
|---|---|---|
| SDD acceptance | T-WIKI-INGEST-V0-001 | Human gate before code. |
| Backend ingest API/service | T-WIKI-INGEST-V0-002 through T-WIKI-INGEST-V0-006 | Candidate generation, merge, run/log/issue evidence. |
| Frontend status | T-WIKI-INGEST-V0-007 | Display generated/review-required candidates when explicitly requested. |
| Verification and closeout | T-WIKI-INGEST-V0-008 through T-WIKI-INGEST-V0-010 | Tests, scans, traceability. |

## Task Summary

| Task | Title | Owner | Priority | Depends On |
|---|---|---|---|---|
| T-WIKI-INGEST-V0-001 | Obtain SDD acceptance gate | product/docs | Must | None |
| T-WIKI-INGEST-V0-002 | Add Wiki ingest API contract and DTOs | backend | Must | T-WIKI-INGEST-V0-001 |
| T-WIKI-INGEST-V0-003 | Implement approved chunk input selection | backend | Must | T-WIKI-INGEST-V0-002 |
| T-WIKI-INGEST-V0-004 | Implement deterministic candidate builder | backend | Must | T-WIKI-INGEST-V0-003 |
| T-WIKI-INGEST-V0-005 | Implement slug merge and trusted collision policy | backend | Must | T-WIKI-INGEST-V0-004 |
| T-WIKI-INGEST-V0-006 | Persist safe run, log, and issue evidence | backend | Must | T-WIKI-INGEST-V0-005 |
| T-WIKI-INGEST-V0-007 | Show generated review-required status in Vue | frontend | Should | T-WIKI-INGEST-V0-002, T-WIKI-INGEST-V0-006 |
| T-WIKI-INGEST-V0-008 | Add backend and frontend verification coverage | backend/frontend/QA | Must | T-WIKI-INGEST-V0-006, T-WIKI-INGEST-V0-007 |
| T-WIKI-INGEST-V0-009 | Run full verification gates and safety scans | QA/security | Must | T-WIKI-INGEST-V0-008 |
| T-WIKI-INGEST-V0-010 | Update traceability and completion evidence | docs | Must | T-WIKI-INGEST-V0-009 |

## Completion Status

| Task | Status | Evidence |
|---|---|---|
| T-WIKI-INGEST-V0-001 | Complete | User accepted the SDD gate before implementation. |
| T-WIKI-INGEST-V0-002 | Complete | DTOs and `WikiIngestController` added; API contract tests pass. |
| T-WIKI-INGEST-V0-003 | Complete | `WikiIngestService` filters by space, approval, traceability, and safe statuses. |
| T-WIKI-INGEST-V0-004 | Complete | Deterministic candidate builder writes safe Markdown artifacts and rejects `model-assisted`. |
| T-WIKI-INGEST-V0-005 | Complete | Generated slug merge and trusted collision safe issue policy covered by service tests. |
| T-WIKI-INGEST-V0-006 | Complete | Run/log/issue evidence persisted and returned through safe DTOs. |
| T-WIKI-INGEST-V0-007 | Complete | Vue requests `includeDrafts=true` and renders generated review-required fields. |
| T-WIKI-INGEST-V0-008 | Complete | Backend unit/API tests and frontend type/unit/build/E2E checks pass. |
| T-WIKI-INGEST-V0-009 | Complete | Full verification gates and focused scans passed. |
| T-WIKI-INGEST-V0-010 | Complete | Traceability, roadmap, and task evidence updated. |

## Task Details

### T-WIKI-INGEST-V0-001: Obtain SDD acceptance gate

- **Maps to:** REQ-WIKI-INGEST-V0-001; spec S1.
- **Objective:** Confirm the user accepts this SDD before product code changes.
- **Scope:** Present the complete bilingual SDD set, apply requested SDD revisions, and record acceptance.
- **Dependencies:** None.
- **Owner type:** product/docs.
- **Priority:** Must.
- **Verification:** Confirm all expected bilingual files exist and IDs match.

### T-WIKI-INGEST-V0-002: Add Wiki ingest API contract and DTOs

- **Maps to:** REQ-WIKI-INGEST-V0-010.
- **Objective:** Add start/read run contracts using Atlas envelope conventions.
- **Scope:** Add request/response records for `POST /api/spaces/{spaceId}/wiki-ingest-runs` and run detail read. Preserve existing published Wiki endpoints.
- **Dependencies:** T-WIKI-INGEST-V0-001.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** Backend API contract tests compile and assert safe response shape.

### T-WIKI-INGEST-V0-003: Implement approved chunk input selection

- **Maps to:** REQ-WIKI-INGEST-V0-002, 013.
- **Objective:** Select only approved, traceable chunks in the requested space.
- **Scope:** Load chunks through repositories/services, scope by space, exclude non-approved or untraced evidence, and record counts.
- **Dependencies:** T-WIKI-INGEST-V0-002.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** Service tests cover approved, review-required, need-fix, OCR-required, failed, unsupported, missing-trace, and cross-space cases.

### T-WIKI-INGEST-V0-004: Implement deterministic candidate builder

- **Maps to:** REQ-WIKI-INGEST-V0-003, 004, 007, 012.
- **Objective:** Build safe review-required candidates without direct external calls.
- **Scope:** Derive title, slug, refs, `TOPIC` page type, source mode, refresh policy, review status, confidence, and generated Markdown artifact path. Reject or disable model-assisted mode in v0.
- **Dependencies:** T-WIKI-INGEST-V0-003.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** Unit tests assert fields, minimum-confidence aggregation, generated Markdown artifact creation, no direct provider call, model-assisted rejection/disablement, and `REVIEW_REQUIRED` default.

### T-WIKI-INGEST-V0-005: Implement slug merge and trusted collision policy

- **Maps to:** REQ-WIKI-INGEST-V0-005, 006.
- **Objective:** Make reruns idempotent and preserve trusted published pages.
- **Scope:** Merge generated review-required candidates by `(spaceId, slug)` and record safe conflict issue for trusted collisions.
- **Dependencies:** T-WIKI-INGEST-V0-004.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** Tests prove repeated run does not increase page count and trusted `PUBLISHED_FILE` pages are unchanged.

### T-WIKI-INGEST-V0-006: Persist safe run, log, and issue evidence

- **Maps to:** REQ-WIKI-INGEST-V0-008, 009.
- **Objective:** Record inspectable run lifecycle evidence.
- **Scope:** Use `wiki_generation_run`, `wiki_log_entry`, and `wiki_page_issue` records for run status, safe summary, created/updated pages, issues, and failures.
- **Dependencies:** T-WIKI-INGEST-V0-005.
- **Owner type:** backend.
- **Priority:** Must.
- **Verification:** Repository/service tests assert safe metadata and no raw source text, prompt, provider payload, secret, private path, or stack trace.

### T-WIKI-INGEST-V0-007: Show generated review-required status in Vue

- **Maps to:** REQ-WIKI-INGEST-V0-011.
- **Objective:** Make generated candidates visibly draft/review-required when exposed in the product surface.
- **Scope:** Add or update Vue API mapping and UI labels for `includeDrafts=true` generated candidates. Preserve existing published Wiki default and fallback behavior.
- **Dependencies:** T-WIKI-INGEST-V0-002, T-WIKI-INGEST-V0-006.
- **Owner type:** frontend.
- **Priority:** Should.
- **Verification:** `cd frontend && npm run typecheck && npm run test`; focused E2E if UI changes are visible.

### T-WIKI-INGEST-V0-008: Add backend and frontend verification coverage

- **Maps to:** REQ-WIKI-INGEST-V0-014.
- **Objective:** Protect behavior and regressions.
- **Scope:** Add backend unit/integration/API tests; add frontend tests if UI changes; preserve existing Wiki/Graph/Ask flows.
- **Dependencies:** T-WIKI-INGEST-V0-006, T-WIKI-INGEST-V0-007.
- **Owner type:** backend/frontend/QA.
- **Priority:** Must.
- **Verification:** `cd backend && mvn verify`; `cd frontend && npm run typecheck && npm run test && npm run build`.

### T-WIKI-INGEST-V0-009: Run full verification gates and safety scans

- **Maps to:** REQ-WIKI-INGEST-V0-012, 013, 014.
- **Objective:** Prove the slice respects quality, adapter, and data-safety gates.
- **Scope:** Run required commands or report skipped checks with reasons.
- **Dependencies:** T-WIKI-INGEST-V0-008.
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

### T-WIKI-INGEST-V0-010: Update traceability and completion evidence

- **Maps to:** REQ-WIKI-INGEST-V0-001, 014.
- **Objective:** Record final implementation status and maturity.
- **Scope:** Update traceability, verification evidence, residual risks, and roadmap/progress status only when implementation status changes.
- **Dependencies:** T-WIKI-INGEST-V0-009.
- **Owner type:** docs.
- **Priority:** Must.
- **Verification:** Final report lists docs changed, code changed, verification run, skipped checks, residual risks, SDD skill chain used, and maturity statement.

## Dependency Plan

Critical path: T-WIKI-INGEST-V0-001 -> T-WIKI-INGEST-V0-002 -> T-WIKI-INGEST-V0-003 -> T-WIKI-INGEST-V0-004 -> T-WIKI-INGEST-V0-005 -> T-WIKI-INGEST-V0-006 -> T-WIKI-INGEST-V0-008 -> T-WIKI-INGEST-V0-009 -> T-WIKI-INGEST-V0-010.

T-WIKI-INGEST-V0-007 may run after backend response shapes are stable.

## Risks / Blockers

- Deterministic candidates are intentionally thin, so review-required labeling remains mandatory.
- Trusted slug collisions are recorded through the existing safe issue taxonomy in this slice.
- Existing worktree already contains prior `wiki-data-model` changes; this implementation avoided reverting or restyling them.

## Definition Of Done

- SDD accepted by user.
- All Must tasks complete.
- Verification run or skipped checks explicitly reported.
- Final response states: Auto Wiki ingest v0 completed; linkify/lint, review gate, connector, and production readiness not completed.
