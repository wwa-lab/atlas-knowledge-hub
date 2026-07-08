# Tasks: Frontend Componentization

## Overview

Implement behavior-preserving structural componentization for `frontend/src/App.vue` against `docs/03-spec/frontend-componentization-spec.md`.

This slice is intentionally frontend-only. It does not include `vue-router`, backend/API changes, provider calls, real data, new dependencies, or visual redesign.

Status: T-FRONTEND-COMPONENTIZATION-001 through T-FRONTEND-COMPONENTIZATION-007 implemented and locally verified on 2026-07-08. The slice is a behavior-preserving frontend structural checkpoint, not a production-readiness claim.

Follow-up status: `frontend-state-extraction` extends this accepted slice to move `App.vue` root state into domain composables while preserving the already componentized UI behavior.

## Task Details

### T-FRONTEND-COMPONENTIZATION-001: Accept SDD contract and baseline selectors

- Requirement: REQ-FRONTEND-COMPONENTIZATION-001 through REQ-FRONTEND-COMPONENTIZATION-008
- Owner type: documentation / QA
- Priority: Must
- Dependencies: None
- Scope: Confirm this SDD set is accepted before implementation. Capture current high-value selectors and frontend verification commands used as the regression baseline.
- Verification:
  - `npm run agent:check-sdd -- --slice frontend-componentization`
  - `git diff --check`
- Notes: API guide is omitted because no backend/API contract changes are in scope.

### T-FRONTEND-COMPONENTIZATION-002: Extract pure domain helpers and local view-model types

- Requirement: REQ-FRONTEND-COMPONENTIZATION-001, REQ-FRONTEND-COMPONENTIZATION-005, REQ-FRONTEND-COMPONENTIZATION-007
- Owner type: frontend
- Priority: Must
- Dependencies: T-FRONTEND-COMPONENTIZATION-001
- Scope: Move pure mapping/formatting logic from `App.vue` into focused `domain` helper files only where ownership is clear. Preserve source trace, confidence, review status, safe errors, and model/settings labels.
- Verification:
  - `cd frontend && npm run typecheck`
  - `cd frontend && npm run test -- --run`

### T-FRONTEND-COMPONENTIZATION-003: Extract product shell, sidebar, home, and global chat

- Requirement: REQ-FRONTEND-COMPONENTIZATION-001, REQ-FRONTEND-COMPONENTIZATION-002, REQ-FRONTEND-COMPONENTIZATION-003
- Owner type: frontend
- Priority: Must
- Dependencies: T-FRONTEND-COMPONENTIZATION-002
- Scope: Create layout/home/chat components and wire them through props/events. Preserve `productView` memory navigation and existing selectors for product page, space cards, create-space panel, global chat, Ask input, and Ask answer.
- Verification:
  - `cd frontend && npm run typecheck`
  - `cd frontend && npm run test -- --run`
  - Focused Playwright for home/chat surfaces where existing specs cover them.

### T-FRONTEND-COMPONENTIZATION-004: Extract space shell and tab components

- Requirement: REQ-FRONTEND-COMPONENTIZATION-001, REQ-FRONTEND-COMPONENTIZATION-002, REQ-FRONTEND-COMPONENTIZATION-003, REQ-FRONTEND-COMPONENTIZATION-005
- Owner type: frontend
- Priority: Must
- Dependencies: T-FRONTEND-COMPONENTIZATION-003
- Scope: Extract space header/tabs plus Documents, connectors, review/recovery, Wiki, and Graph tab components in small stages. Keep `activeSpaceTab` state in memory and preserve tab selectors and visible product behavior.
- Verification:
  - `cd frontend && npm run typecheck`
  - `cd frontend && npm run test -- --run`
  - Focused Playwright specs for changed space surfaces.

### T-FRONTEND-COMPONENTIZATION-005: Extract settings modal and settings panels

- Requirement: REQ-FRONTEND-COMPONENTIZATION-001, REQ-FRONTEND-COMPONENTIZATION-003, REQ-FRONTEND-COMPONENTIZATION-005, REQ-FRONTEND-COMPONENTIZATION-006
- Owner type: frontend
- Priority: Must
- Dependencies: T-FRONTEND-COMPONENTIZATION-003
- Scope: Extract settings modal, rail, and behavior-bearing panels while preserving `settingsOpen/settingsPanel`, capability gates, masked API display, model draft behavior, and disabled/coming-soon controls.
- Verification:
  - `cd frontend && npm run typecheck`
  - `cd frontend && npm run test -- --run`
  - Focused Playwright specs for settings where existing coverage exists.

### T-FRONTEND-COMPONENTIZATION-006: Extract composables only where they reduce real complexity

- Requirement: REQ-FRONTEND-COMPONENTIZATION-001, REQ-FRONTEND-COMPONENTIZATION-004, REQ-FRONTEND-COMPONENTIZATION-007, REQ-FRONTEND-COMPONENTIZATION-008
- Owner type: frontend
- Priority: Should
- Dependencies: T-FRONTEND-COMPONENTIZATION-004, T-FRONTEND-COMPONENTIZATION-005
- Scope: Move API workflow or shared UI state into composables only when it makes dependencies clearer. Preserve API helper usage through `frontend/src/api.ts`; do not introduce new API calls or new dependencies.
- Verification:
  - `cd frontend && npm run typecheck`
  - `cd frontend && npm run test -- --run`
  - Focused tests for extracted composables.

### T-FRONTEND-COMPONENTIZATION-007: Final frontend regression and safety gates

- Requirement: REQ-FRONTEND-COMPONENTIZATION-001 through REQ-FRONTEND-COMPONENTIZATION-008
- Owner type: QA/security
- Priority: Must
- Dependencies: T-FRONTEND-COMPONENTIZATION-002 through T-FRONTEND-COMPONENTIZATION-006
- Scope: Run final checks, document skipped checks with reasons, and update traceability with evidence. Do not run backend/provider checks unless implementation unexpectedly touches those areas.
- Verification:
  - `cd frontend && npm run typecheck`
  - `cd frontend && npm run test -- --run`
  - `cd frontend && npm run build`
  - `cd frontend && npm run e2e`
  - `git diff --check`
  - Focused secret/private-path scan over changed files
  - Focused new-network/dependency scan over changed files
  - `npm run agent:closeout`

### T-FRONTEND-COMPONENTIZATION-008: Extend the SDD contract for frontend state extraction

- Requirement: REQ-FRONTEND-COMPONENTIZATION-009 through REQ-FRONTEND-COMPONENTIZATION-011
- Owner type: documentation / frontend
- Priority: Must
- Dependencies: T-FRONTEND-COMPONENTIZATION-007
- Scope: Record `frontend-state-extraction` as a behavior-preserving extension of this slice. Document the eight target domain composables, factory-function ownership, explicit props/events data flow, `App.vue` line-count target, no Pinia/router/provide-inject constraint, and per-composable tests.
- Verification:
  - `npm run agent:check-sdd -- --slice frontend-componentization`
  - `git diff --check`

### T-FRONTEND-COMPONENTIZATION-009: Extract domain state composables

- Requirement: REQ-FRONTEND-COMPONENTIZATION-004, REQ-FRONTEND-COMPONENTIZATION-009, REQ-FRONTEND-COMPONENTIZATION-010
- Owner type: frontend
- Priority: Must
- Dependencies: T-FRONTEND-COMPONENTIZATION-008
- Scope: Move centralized `ref`/`computed`/API workflow ownership from `App.vue` into `frontend/src/composables/useSpaces.ts`, `useBatches.ts`, `useReviewQueue.ts`, `useWikiPages.ts`, `useGraph.ts`, `useAsk.ts`, `useSettings.ts`, and `useGlobalChat.ts`. Keep API access behind `frontend/src/api.ts`, do not create module-level singletons, and pass cross-domain dependencies through explicit options/callbacks.
- Verification:
  - After each domain extraction, run `npm --prefix frontend run test -- --run <domain composable test>` and `npm --prefix frontend run typecheck`
  - `npm --prefix frontend run test -- --run src/composables`

### T-FRONTEND-COMPONENTIZATION-010: Thin App.vue into an orchestration shell

- Requirement: REQ-FRONTEND-COMPONENTIZATION-001, REQ-FRONTEND-COMPONENTIZATION-002, REQ-FRONTEND-COMPONENTIZATION-009, REQ-FRONTEND-COMPONENTIZATION-010
- Owner type: frontend
- Priority: Must
- Dependencies: T-FRONTEND-COMPONENTIZATION-009
- Scope: Rewire `App.vue` so it calls the domain composables once at the top level and delegates state through existing component props/events. Preserve in-memory `home`/`chat`/`space` navigation, settings mounting, stable E2E selectors, and P0/workbench behavior. Extract the legacy workbench view into a child component if needed to keep `App.vue` below 600 lines.
- Verification:
  - `wc -l frontend/src/App.vue` reports fewer than 600 lines
  - `npm --prefix frontend run typecheck`
  - `npm --prefix frontend run test -- --run src/App.test.ts`

### T-FRONTEND-COMPONENTIZATION-011: Add composable coverage and final regression

- Requirement: REQ-FRONTEND-COMPONENTIZATION-007, REQ-FRONTEND-COMPONENTIZATION-011
- Owner type: frontend / QA
- Priority: Must
- Dependencies: T-FRONTEND-COMPONENTIZATION-009, T-FRONTEND-COMPONENTIZATION-010
- Scope: Add focused tests beside every extracted composable and keep `App.test.ts` focused on rendered orchestration/user flows rather than private root-state implementation. Run the requested frontend regression, diff hygiene, and safety scans.
- Verification:
  - `npm --prefix frontend run lint`
  - `npm --prefix frontend run typecheck`
  - `npm --prefix frontend run test`
  - `npm --prefix frontend run build`
  - `npm --prefix frontend run e2e`
  - `git diff --check`
  - Focused no-`any` scan under `frontend/src`
  - Focused secret/private-path and new-network/dependency scans over changed files

## Dependency Plan

Critical path: T-FRONTEND-COMPONENTIZATION-001 -> T-FRONTEND-COMPONENTIZATION-002 -> T-FRONTEND-COMPONENTIZATION-003 -> T-FRONTEND-COMPONENTIZATION-004 -> T-FRONTEND-COMPONENTIZATION-006 -> T-FRONTEND-COMPONENTIZATION-007 -> T-FRONTEND-COMPONENTIZATION-008 -> T-FRONTEND-COMPONENTIZATION-009 -> T-FRONTEND-COMPONENTIZATION-010 -> T-FRONTEND-COMPONENTIZATION-011.

T-FRONTEND-COMPONENTIZATION-005 may run after T-FRONTEND-COMPONENTIZATION-003 and before final closeout.

## Risks / Blockers

- Large component props may become noisy if extraction moves templates before clarifying state ownership.
- Existing `App.test.ts` may need careful splitting to avoid reducing coverage.
- Playwright selectors must remain stable across component boundaries.
- Any desire for URL/deep-link/browser-history behavior requires a separate accepted slice before router work.
- Cross-domain operations such as selecting a space, refreshing review/Wiki state, and publishing selected files must use explicit composable options/callbacks. If a domain cannot be cleanly separated without behavior drift, record the blocker here before continuing.

## Open Questions

- None blocking. `vue-router` is explicitly deferred unless URL semantics become a product requirement.

## API Guide Omission

No API guide is generated for this slice because no backend endpoint, API payload, persistence model, adapter contract, or provider/runtime behavior changes are in scope.
