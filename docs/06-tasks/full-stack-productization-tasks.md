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

## Product Goal Batch 5 Task Extension

### T-FSP-011: Cut Phase I1-I3 Vue surfaces to existing Atlas APIs

- Requirement: REQ-FSP-001, REQ-FSP-002, REQ-FSP-003, REQ-FSP-004, REQ-FSP-005, REQ-FSP-011, REQ-FSP-012
- Owner type: frontend / QA
- Priority: Must for Product Goal Batch 5
- Dependencies: T-FSP-002 through T-FSP-010
- Scope: In the real Vue product path, render API-backed Knowledge Space metadata, batch/file/chunk metadata, and review queue metadata using the existing Atlas API helpers. Keep safe deterministic sample fallback where the API is unavailable. Do not add production uploads, auth/RBAC, real data, provider calls, or new backend contracts.
- Verification:
  - `cd frontend && npm run typecheck && npm run test && npm run build`
  - `cd frontend && npx playwright test tests/e2e/phase-i1-i3-api-backed-metadata.spec.ts --project=chromium`
  - `cd frontend && npx playwright test tests/e2e/phase-a-b-product-shell.spec.ts tests/e2e/phase-c-d-upload-processing.spec.ts tests/e2e/phase-e-f-g-knowledge-surfaces.spec.ts tests/e2e/phase-h-settings-administration.spec.ts tests/e2e/phase-i1-i3-api-backed-metadata.spec.ts --project=chromium`
  - `cd backend && mvn verify`
  - `git diff --check`
  - Focused secret/private-path scan
  - Focused network/dependency scan
- Status: Completed on 2026-07-05 at L3 API-backed readiness evidence; final product acceptance remains pending user decision.

## Core Knowledge Loop v1 Task Extension

### T-FSP-013: Update SDD contract for user-runnable closed loop

- Requirement: REQ-FSP-013 through REQ-FSP-020
- Owner type: product / architecture
- Priority: Must
- Dependencies: T-FSP-012
- Scope: Record the v1 scope, disabled capabilities, API addendum, traceability, and verification plan before code changes.
- Verification: Spec, API guide, task list, and traceability all reference the same v1 requirement IDs.

### T-FSP-014: Implement runtime DeepSeek model configuration API

- Requirement: REQ-FSP-013, REQ-FSP-020
- Owner type: backend
- Priority: Must
- Dependencies: T-FSP-013
- Scope: Add masked read, save, and clear endpoints for backend-held DeepSeek configuration. The configured adapter must use runtime configuration before falling back to process environment values.
- Verification: Backend tests cover masked response, no raw key return, clear behavior, and adapter capability state.

### T-FSP-015: Implement real PDF and ZIP-of-PDF ingestion API

- Requirement: REQ-FSP-014, REQ-FSP-020
- Owner type: backend
- Priority: Must
- Dependencies: T-FSP-013
- Scope: Add multipart upload endpoint scoped to a space. Store PDF bytes in a local artifact area, expand ZIPs safely, reject unsupported types safely, create batch/file metadata, and return the created parser run.
- Verification: Backend integration/unit tests cover PDF upload, ZIP upload, unsupported file handling, path traversal rejection, and metadata response.

### T-FSP-016: Implement local PDF text parser adapter

- Requirement: REQ-FSP-015, REQ-FSP-020
- Owner type: backend
- Priority: Must
- Dependencies: T-FSP-015
- Scope: Add adapter-backed PDF text extraction into Markdown and review-required chunks with source trace. Keep Office/OCR paths disabled unless explicitly configured.
- Verification: Backend tests prove chunks are generated through the adapter boundary and preserve source trace.

### T-FSP-017: Propagate file review updates to chunks

- Requirement: REQ-FSP-016, REQ-FSP-020
- Owner type: backend
- Priority: Must
- Dependencies: T-FSP-013
- Scope: When review actions name affected chunks, update only chunks belonging to the file; when the action omits chunks, update all chunks for the file. Keep invalid cross-file chunk IDs rejected.
- Verification: Backend tests cover selected chunk update, all-chunk fallback, and cross-file rejection.

### T-FSP-018: Implement backend downstream refresh orchestration

- Requirement: REQ-FSP-017, REQ-FSP-020
- Owner type: backend
- Priority: Must
- Dependencies: T-FSP-016, T-FSP-017
- Scope: Add a backend endpoint that refreshes graph projection and vector index for approved/published chunks in the selected scope. The frontend must call this endpoint instead of vector/parser/model engines.
- Verification: Backend tests cover refresh response and no eligible evidence state.

### T-FSP-019: Cut Vue product path to real upload/config and grey disabled functions

- Requirement: REQ-FSP-013, REQ-FSP-014, REQ-FSP-017, REQ-FSP-018, REQ-FSP-019
- Owner type: frontend
- Priority: Must
- Dependencies: T-FSP-014 through T-FSP-018
- Scope: Replace the sample-only primary action with file upload, wire model settings save/clear to API, call backend downstream refresh, and disable unsupported Office/OCR/RBAC/vector-store/admin controls.
- Verification: Frontend unit/E2E checks cover upload controls, disabled states, and closed-loop happy path where feasible.

### T-FSP-020: Run closed-loop verification and safety gates

- Requirement: REQ-FSP-020
- Owner type: QA/security
- Priority: Must
- Dependencies: T-FSP-014 through T-FSP-019
- Scope: Run backend tests, frontend typecheck/tests/build, targeted E2E or manual closed-loop evidence, `git diff --check`, focused secret scan, and dependency/network scan.
- Verification: Final report records passed checks, skipped checks with reasons, and residual L5 production gaps.

### T-FSP-012: Cut Phase I4-I7 Vue knowledge surfaces to existing Atlas APIs

- Requirement: REQ-FSP-006, REQ-FSP-007, REQ-FSP-008, REQ-FSP-009, REQ-FSP-010, REQ-FSP-011, REQ-FSP-012
- Owner type: frontend / QA
- Priority: Must for Product Goal Batch 6
- Dependencies: T-FSP-011
- Scope: In the real Vue product path, render API-backed Wiki pages, graph evidence, Ask run citations, and masked model adapter capability metadata using existing Atlas API helpers. Add product-path approve/publish/Ask controls where needed to avoid relying on the workbench. Do not add production provider calls, raw secrets, real data, auth/RBAC, or new backend contracts.
- Verification:
  - `cd frontend && npm run typecheck && npm run test && npm run build`
  - `cd frontend && npx playwright test tests/e2e/phase-i4-i7-api-backed-knowledge-surfaces.spec.ts --project=chromium`
  - `cd frontend && npx playwright test tests/e2e/phase-a-b-product-shell.spec.ts tests/e2e/phase-c-d-upload-processing.spec.ts tests/e2e/phase-e-f-g-knowledge-surfaces.spec.ts tests/e2e/phase-h-settings-administration.spec.ts tests/e2e/phase-i1-i3-api-backed-metadata.spec.ts tests/e2e/phase-i4-i7-api-backed-knowledge-surfaces.spec.ts --project=chromium`
  - `cd backend && mvn verify`
  - `git diff --check`
  - Focused secret/private-path scan
  - Focused network/dependency scan
- Status: Completed on 2026-07-05 at L3 API-backed readiness evidence; final product acceptance remains pending user decision.
