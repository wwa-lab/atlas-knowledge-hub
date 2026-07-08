# Requirements: Frontend Componentization

## Status

Draft for SDD acceptance. Slice `frontend-componentization`. Phase 1 FE structural hardening.

## Goal

Split the monolithic Vue 3 `frontend/src/App.vue` into feature components and composables while preserving current product behavior, visual semantics, API usage, mock/sample-safe constraints, and stable test selectors.

## Scope

### In Scope

- Behavior-preserving structural extraction of `frontend/src/App.vue`.
- Feature-oriented Vue components for the current product shell: navigation, home, global chat, space detail, Documents, connectors, review/recovery, Wiki, Graph, settings, and shared UI primitives.
- Composables or focused helpers for existing state groups and side effects when extraction reduces `App.vue` complexity without changing behavior.
- Existing API helper usage through `frontend/src/api.ts`; no new backend endpoints or API contracts.
- Existing mock/sample fallback data through `frontend/src/data/atlasMock.ts`; no real company documents.
- Focused tests that prove extracted components/composables keep current behavior and selectors stable.

### Out Of Scope

- `vue-router`, URL semantics, deep links, route guards, or browser back/forward behavior.
- New product capabilities, new UX flows, or visible redesign.
- Pinia/Vuex/global store introduction.
- New backend/API behavior, new persistence, new migrations, or new adapter/runtime behavior.
- New external network calls, cloud services, production provider calls, or real company data.
- Replacing existing Playwright acceptance semantics.

## Requirements

| ID | Requirement | Priority |
|---|---|---|
| REQ-FRONTEND-COMPONENTIZATION-001 | The implementation must preserve all current user-visible behavior of the Vue product path while reducing `App.vue` from a monolithic component into focused components/composables. | Must |
| REQ-FRONTEND-COMPONENTIZATION-002 | The implementation must keep current in-memory navigation semantics based on `productView`, `activeSpaceTab`, `settingsOpen`, and `settingsPanel`; it must not introduce `vue-router` or URL-driven navigation. | Must |
| REQ-FRONTEND-COMPONENTIZATION-003 | Extracted components must preserve existing stable `data-testid` selectors used by unit and Playwright tests unless a selector is explicitly replaced with a documented equivalent and tests are updated in the same task. | Must |
| REQ-FRONTEND-COMPONENTIZATION-004 | Extracted code must continue to call Atlas APIs only through the existing frontend API boundary in `frontend/src/api.ts`; no frontend direct calls to parser, converter, model, vector, storage, provider, or runtime engines are allowed. | Must |
| REQ-FRONTEND-COMPONENTIZATION-005 | Component boundaries must preserve source trace, confidence, review status, safe error, capability, and disabled/coming-soon display rules already present in the product UI. | Must |
| REQ-FRONTEND-COMPONENTIZATION-006 | The slice must keep existing mock/sample-safe data constraints and must not introduce real company documents, screenshots, credentials, logs, private paths, or raw secrets. | Must |
| REQ-FRONTEND-COMPONENTIZATION-007 | The implementation must add or update focused tests for extracted helpers, composables, and behavior-bearing components; existing frontend unit and E2E suites must continue to pass. | Must |
| REQ-FRONTEND-COMPONENTIZATION-008 | The implementation must leave backend, database, adapter, deployment, and provider-backed E2E behavior unchanged. | Must |

## Constraints

- No `vue-router` in this slice.
- No new dependencies unless a later accepted SDD revision explicitly adds one.
- No backend/API contract change; API guide is intentionally omitted.
- No new external network call or provider integration.
- No visible redesign; CSS changes are allowed only when required to preserve existing layout after component extraction.
- Keep implementation changes small and reviewable; split tasks if a component extraction becomes too broad.

## Assumptions

- `frontend/src/App.vue` remains the root composition shell after this slice, but should become a thin orchestration layer.
- Existing `frontend/src/api.ts`, `frontend/src/types.ts`, and `frontend/src/data/atlasMock.ts` remain the source boundaries for API access, shared types, and mock/sample data.
- Current tests describe the behavior to preserve; missing tests should be added around the highest-risk extracted behavior.

## Open Questions

- None blocking for SDD acceptance. Future URL semantics, deep links, and browser history are separate product decisions.
