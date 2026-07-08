# User Stories: Frontend Componentization

## Status

Draft for SDD acceptance. Derived from `frontend-componentization` requirements.

## Stories

### US-FRONTEND-COMPONENTIZATION-001: Preserve product behavior during extraction

As an Atlas user, I want the product shell to behave the same after componentization, so that the refactor does not disrupt current reviewable workflows.

Acceptance criteria:

1. Given the frontend opens on the Atlas product path, then home, global chat, space detail, tabs, settings, and mock/sample-safe fallback states render with the same behavior as before.
2. Given a user follows existing tested flows, then existing stable selectors remain available.
3. Given an API or mock fallback state is shown, then loading, empty, disabled, and safe-error behavior remains unchanged.

### US-FRONTEND-COMPONENTIZATION-002: Keep in-memory navigation

As a product owner, I want this slice to keep current in-memory navigation rather than add URL routing, so that structural cleanup does not create unapproved product semantics.

Acceptance criteria:

1. Given the user moves between Home, Global Chat, and Space Detail, then navigation is still controlled by Vue state and not by `vue-router`.
2. Given the user changes the active space tab, then tab selection still uses local component state and does not change the browser URL.
3. Given browser back/forward or deep links are not part of this slice, then no new route contract is created.

### US-FRONTEND-COMPONENTIZATION-003: Extract feature components safely

As a frontend maintainer, I want large UI regions extracted into focused components, so that future product changes can be made without editing one 5k-line file.

Acceptance criteria:

1. Given the componentization is complete, then `App.vue` delegates major regions to feature components.
2. Given a component owns behavior-bearing UI, then props/events or composables make dependencies explicit.
3. Given extraction is done in stages, then each stage is reviewable and has a focused verification path.

### US-FRONTEND-COMPONENTIZATION-004: Preserve API and adapter boundaries

As an architecture owner, I want extracted components to use the same frontend API boundary, so that componentization does not bypass Atlas backend/adapters.

Acceptance criteria:

1. Given a component needs backend data, then it receives data/actions from `App.vue` or a composable that uses `frontend/src/api.ts`.
2. Given parser, converter, model, vector, storage, provider, or runtime behavior is needed, then the frontend still reaches it only through existing backend APIs.
3. Given no backend/API behavior changes are needed, then no API implementation guide is generated for this slice.

### US-FRONTEND-COMPONENTIZATION-005: Test extracted behavior

As a maintainer, I want focused tests around extracted components and composables, so that the refactor is protected against subtle behavior drift.

Acceptance criteria:

1. Given a helper/composable is extracted from existing non-trivial logic, then a focused unit test covers the behavior or existing coverage is moved with it.
2. Given a behavior-bearing component is extracted, then tests verify key states and emitted actions where risk warrants it.
3. Given the full frontend verification suite runs, then typecheck, unit tests, build, and relevant E2E checks pass or any blocked check is reported with reason.
