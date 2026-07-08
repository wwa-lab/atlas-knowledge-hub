# Specification: Frontend Componentization

## Status

Accepted for implementation on 2026-07-08. Source of truth for slice `frontend-componentization`.

## Overview

This slice makes `frontend/src/App.vue` structurally maintainable without changing product behavior. The accepted behavior remains the current Vue product path: product home, global chat, space detail tabs, upload/processing surfaces, review/recovery, Wiki, Graph, Trusted Ask, settings, API-backed metadata, and mock/sample-safe fallbacks.

This slice does not introduce `vue-router`. Current in-memory state remains the navigation contract until URL semantics, deep links, and browser history are explicitly accepted as product capabilities.

## Functional Requirements

### Root Shell And Navigation

- FR-FRONTEND-COMPONENTIZATION-001: `App.vue` remains the root component and coordinates application bootstrapping.
- FR-FRONTEND-COMPONENTIZATION-002: Product navigation remains driven by current in-memory state: `activeExperience`, `productView`, `activeSpaceTab`, `settingsOpen`, and `settingsPanel`.
- FR-FRONTEND-COMPONENTIZATION-003: No `vue-router`, route files, route guards, URL mutation, or browser history contract is added.
- FR-FRONTEND-COMPONENTIZATION-004: Existing primary selectors such as `data-testid="vue-product-page"`, space/home/chat/tab/settings selectors, and E2E-facing selectors remain stable.

### Component Extraction

- FR-FRONTEND-COMPONENTIZATION-005: The product shell is split into feature-oriented components for navigation/sidebar, product home, global chat, space detail, Documents, connector sync, review/recovery, Wiki, Graph, settings, and shared display primitives.
- FR-FRONTEND-COMPONENTIZATION-006: Extracted components receive data and callbacks through typed props/events, or through composables when state ownership is shared and behavior remains unchanged.
- FR-FRONTEND-COMPONENTIZATION-007: Extraction must not move unrelated business behavior into generic shared components.
- FR-FRONTEND-COMPONENTIZATION-008: `App.vue` must remain readable as an orchestration layer after extraction and must not accumulate new large template regions.

### Composables And Helpers

- FR-FRONTEND-COMPONENTIZATION-009: Existing non-trivial computed mappings and side-effect workflows may be extracted into composables or domain helpers when doing so makes ownership clearer.
- FR-FRONTEND-COMPONENTIZATION-010: Extracted composables must preserve current API sequencing, loading flags, safe errors, and fallback behavior.
- FR-FRONTEND-COMPONENTIZATION-011: API access remains behind `frontend/src/api.ts`; no extracted composable may call provider/runtime/parser/converter/vector/storage engines directly.
- FR-FRONTEND-COMPONENTIZATION-018: The follow-up slice `frontend-state-extraction` extracts `App.vue` root state into factory-function composables under `frontend/src/composables/`: `useSpaces`, `useBatches`, `useReviewQueue`, `useWikiPages`, `useGraph`, `useAsk`, `useSettings`, and `useGlobalChat`.
- FR-FRONTEND-COMPONENTIZATION-019: Each state composable owns its domain reactive state, API calls through `frontend/src/api.ts`, derived computed values, and domain business methods. Composables must not be module-level singletons.
- FR-FRONTEND-COMPONENTIZATION-020: `App.vue` remains a thin orchestration layer for composing the domain composables, switching the top-level product views (`home`, `chat`, `space`), and mounting settings/modal surfaces. The target size for `App.vue` after the state extraction is fewer than 600 lines.
- FR-FRONTEND-COMPONENTIZATION-021: Domain data flow remains explicit through props/events and top-level composable return values. This slice does not introduce `provide`/`inject`, Pinia, Vuex, `vue-router`, or new runtime dependencies.
- FR-FRONTEND-COMPONENTIZATION-022: Existing P0/workbench selectors and user-visible behavior remain available after root thinning; if needed for the line-count target, the workbench may move behind a child component while keeping behavior delegated through typed props/events.
- FR-FRONTEND-COMPONENTIZATION-023: Each extracted composable has a focused `*.test.ts` file covering core state transitions and mocked API interactions. App-level tests should focus on rendered orchestration and user flows instead of private root-state implementation details.

### Behavior Preservation

- FR-FRONTEND-COMPONENTIZATION-012: Home, create-space panel, global chat, selected space detail, tab switching, Documents workflow, connector sync, review/dead-letter operations, Wiki publish display, Graph selection/search, model/settings panels, and mock upload workflow keep their existing visible behavior.
- FR-FRONTEND-COMPONENTIZATION-013: Source trace, confidence, review status, capability gates, safe error copy, masked secret display, and coming-soon/disabled states remain visible where they are visible today.
- FR-FRONTEND-COMPONENTIZATION-014: CSS changes are limited to preserving existing layout after extraction; no visual redesign is accepted in this slice.

### Tests And Verification

- FR-FRONTEND-COMPONENTIZATION-015: Behavior-bearing extracted helpers/composables/components must have focused tests or explicitly reused existing coverage.
- FR-FRONTEND-COMPONENTIZATION-016: Existing frontend unit tests and Playwright flows must continue to verify the same user-facing behavior.
- FR-FRONTEND-COMPONENTIZATION-017: Verification includes frontend typecheck, unit tests, build, relevant E2E coverage, `git diff --check`, secret/private-path scan, and new-network/dependency scan.

## State Rules

```text
activeExperience === "atlas" -> product shell
productView: home | chat | space
activeSpaceTab: docs | connectors | review | wiki | graph
settingsOpen/settingsPanel -> modal state only
selectedSpaceId -> API/mock product context
```

These remain in-memory state rules. They are not route contracts.

## Acceptance Matrix

| Requirement | Spec Sections | Observable Check |
|---|---|---|
| REQ-FRONTEND-COMPONENTIZATION-001 | Component Extraction, Behavior Preservation | `App.vue` delegates major UI regions and existing flows still pass. |
| REQ-FRONTEND-COMPONENTIZATION-002 | Root Shell And Navigation, State Rules | No router package/config/routes are added; URL remains unchanged during product navigation. |
| REQ-FRONTEND-COMPONENTIZATION-003 | Root Shell And Navigation, Tests And Verification | Current E2E/unit selectors remain present or are replaced with documented equivalents. |
| REQ-FRONTEND-COMPONENTIZATION-004 | Composables And Helpers | Extracted code uses `frontend/src/api.ts` and does not call engines/providers directly. |
| REQ-FRONTEND-COMPONENTIZATION-005 | Behavior Preservation | Trace/confidence/review/safe-error/disabled states still render. |
| REQ-FRONTEND-COMPONENTIZATION-006 | Behavior Preservation | No real data, secrets, private paths, or external calls are introduced. |
| REQ-FRONTEND-COMPONENTIZATION-007 | Tests And Verification | Focused tests and frontend verification suite pass. |
| REQ-FRONTEND-COMPONENTIZATION-008 | Composables And Helpers, Tests And Verification | Backend/API/adapters/provider-backed E2E behavior remains unchanged. |
| REQ-FRONTEND-COMPONENTIZATION-009 | Composables And Helpers | `App.vue` is below 600 lines and delegates domain state to the eight named factory composables. |
| REQ-FRONTEND-COMPONENTIZATION-010 | Composables And Helpers, Behavior Preservation | Composables use `frontend/src/api.ts`, preserve explicit props/events, and do not introduce Pinia, router, `provide`/`inject`, runtime dependencies, or provider/adapter calls. |
| REQ-FRONTEND-COMPONENTIZATION-011 | Tests And Verification | Every extracted state composable has focused unit coverage and the full frontend verification suite passes. |

## Explicit Non-Goals

- URL semantics, deep links, browser history, or route-level permissions.
- Product feature expansion.
- Visual redesign.
- Backend/API expansion.
- Production auth/RBAC, production provider integration, production storage/vector/model engine changes.
- Real company data ingestion.

## SDD Quality Notes

This document follows the Atlas SDD chain model: `req-to-user-story`, `user-story-to-spec`, `spec-to-architecture`, `architecture-to-design`, `design-to-tasks`, and `review-doc-quality` as the review checklist.
