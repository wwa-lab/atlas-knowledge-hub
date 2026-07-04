# Tasks: Full-Stack Productization

## Overview

Implement the P0 browser-driven full-stack loop against `docs/03-spec/full-stack-productization-spec.md`. This is brownfield work in the existing Vue/Spring Boot repository.

Status: T-FSP-001 through T-FSP-010 implemented and verified on 2026-07-03. Verification evidence is recorded in `docs/00-context/full-stack-productization-traceability.md`.

## Task Details

### T-FSP-001: Add/confirm full-stack SDD artifacts

- Requirement: REQ-FSP-001 through REQ-FSP-012
- Owner type: documentation
- Priority: Must
- Dependencies: None
- Scope: Create bilingual requirements, stories, spec, architecture, data flow, data model, design, API guide, tasks, and traceability.
- Verification: Confirm every required EN and `.zh-CN.md` file exists and IDs match.

### T-FSP-002: Build typed frontend API client and domain models

- Requirement: REQ-FSP-001 through REQ-FSP-009
- Owner type: frontend
- Priority: Must
- Dependencies: T-FSP-001
- Scope: Add typed API helpers for spaces, batches, files, chunks, review queues, reviews, publish, wiki pages, graph, vector refresh, and Ask.
- Verification: `cd frontend && npm run typecheck`

### T-FSP-003: Replace primary Vue shell with API-driven space/detail flow

- Requirement: REQ-FSP-001, REQ-FSP-002, REQ-FSP-010
- Owner type: frontend
- Priority: Must
- Dependencies: T-FSP-002
- Scope: Render API-backed space list and selected space detail as the primary UI. Move static prototype behavior out of the main operational path or mark it coming soon/reference-only.
- Verification: Frontend unit tests assert startup loading, space list rendering, detail loading, and safe error state.

### T-FSP-004: Implement Documents batch/files/chunks flow

- Requirement: REQ-FSP-003, REQ-FSP-004
- Owner type: frontend
- Priority: Must
- Dependencies: T-FSP-003
- Scope: Add sample metadata-only batch creation, batch list, file list, selected file source chunks, and visible source trace.
- Verification: Unit tests and UI-driven E2E cover batch creation and chunk display.

### T-FSP-005: Implement Review and Publish flow

- Requirement: REQ-FSP-005, REQ-FSP-006
- Owner type: frontend
- Priority: Must
- Dependencies: T-FSP-004
- Scope: Load review queues, approve selected file, publish approved file, refresh files/queues/wiki pages, and show publish-blocked states.
- Verification: Unit tests and UI-driven E2E cover approve and publish.

### T-FSP-006: Connect post-publish graph/vector refresh and Graph tab

- Requirement: REQ-FSP-007, REQ-FSP-008
- Owner type: frontend
- Priority: Must
- Dependencies: T-FSP-005
- Scope: Trigger existing graph projection and vector index endpoints after publish or via an explicit refresh action. Keep graph read/detail API-backed and scoped to selected space.
- Verification: E2E confirms graph evidence includes the published chunk.

### T-FSP-007: Implement Ask tab through API

- Requirement: REQ-FSP-009
- Owner type: frontend
- Priority: Must
- Dependencies: T-FSP-006
- Scope: Add question input, `POST /ask`, `GET /ask-runs/{runId}`, answer panel, citation list, no-evidence/failure/loading states, and `REVIEW_REQUIRED` answer status.
- Verification: E2E confirms answer and evidence after browser workflow.

### T-FSP-008: Disable unconnected mock UI

- Requirement: REQ-FSP-010
- Owner type: frontend
- Priority: Must
- Dependencies: T-FSP-003
- Scope: Disable or label coming soon for production upload, real auth/RBAC/admin actions, provider setup, and any retained prototype-only affordances.
- Verification: Unit/E2E assertions find `data-testid="coming-soon"` and no enabled fake primary action outside P0 loop.

### T-FSP-009: Add UI-driven Playwright E2E

- Requirement: REQ-FSP-011, REQ-FSP-012
- Owner type: QA
- Priority: Must
- Dependencies: T-FSP-004 through T-FSP-008
- Scope: Add a browser-first E2E test that completes space list -> detail -> sample batch -> files/chunks -> review -> publish -> wiki -> graph -> ask. API request setup may not replace the main browser path.
- Verification: `cd frontend && npm run e2e` and root E2E scripts where feasible.

### T-FSP-010: Run verification gates and scans

- Requirement: REQ-FSP-012
- Owner type: QA/security
- Priority: Must
- Dependencies: T-FSP-009
- Scope: Run required checks or report skipped checks with reasons.
- Verification:
  - `npm run e2e:first-layer`
  - `npm run e2e:second-layer`
  - `cd frontend && npm run typecheck && npm run test -- --run && npm run build && npm run e2e`
  - `cd backend && mvn verify`
  - `git diff --check`
  - Secret/private-path scan over changed files
  - Network/dependency scan for new external calls

## Dependency Plan

Critical path: T-FSP-001 -> T-FSP-002 -> T-FSP-003 -> T-FSP-004 -> T-FSP-005 -> T-FSP-006 -> T-FSP-007 -> T-FSP-009 -> T-FSP-010.

T-FSP-008 can run after T-FSP-003 and before final E2E.

## Risks / Blockers

- Existing frontend tests for the iframe prototype may need careful updates because P0 makes the API-driven app primary.
- If backend Ask requires indexed vectors, UI must run the existing vector endpoint before Ask.
- Docker or local PostgreSQL availability may block second-layer verification.

## Open Questions

- None blocking for P0.
