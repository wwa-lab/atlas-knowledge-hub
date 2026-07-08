# Tasks: Frontend Componentization

## Overview

Implement behavior-preserving structural componentization for `frontend/src/App.vue` against `docs/03-spec/frontend-componentization-spec.md`.

This slice is intentionally frontend-only. It does not include `vue-router`, backend/API changes, provider calls, real data, new dependencies, or visual redesign.

Status: T-FRONTEND-COMPONENTIZATION-001 through T-FRONTEND-COMPONENTIZATION-007 implemented and locally verified on 2026-07-08. The slice is a behavior-preserving frontend structural checkpoint, not a production-readiness claim.

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

## Dependency Plan

Critical path: T-FRONTEND-COMPONENTIZATION-001 -> T-FRONTEND-COMPONENTIZATION-002 -> T-FRONTEND-COMPONENTIZATION-003 -> T-FRONTEND-COMPONENTIZATION-004 -> T-FRONTEND-COMPONENTIZATION-006 -> T-FRONTEND-COMPONENTIZATION-007.

T-FRONTEND-COMPONENTIZATION-005 may run after T-FRONTEND-COMPONENTIZATION-003 and before final closeout.

## Risks / Blockers

- Large component props may become noisy if extraction moves templates before clarifying state ownership.
- Existing `App.test.ts` may need careful splitting to avoid reducing coverage.
- Playwright selectors must remain stable across component boundaries.
- Any desire for URL/deep-link/browser-history behavior requires a separate accepted slice before router work.

## Open Questions

- None blocking. `vue-router` is explicitly deferred unless URL semantics become a product requirement.

## API Guide Omission

No API guide is generated for this slice because no backend endpoint, API payload, persistence model, adapter contract, or provider/runtime behavior changes are in scope.
