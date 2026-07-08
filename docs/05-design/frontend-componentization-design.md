# Design: Frontend Componentization

## Status

Draft for SDD acceptance.

## Design Principle

Componentization must feel invisible to the product user and obvious to the maintainer. The visible Atlas UI should not change; the internal shape should become feature-oriented, testable, and easier to review.

## Requirement Coverage

- REQ-FRONTEND-COMPONENTIZATION-001
- REQ-FRONTEND-COMPONENTIZATION-002
- REQ-FRONTEND-COMPONENTIZATION-003
- REQ-FRONTEND-COMPONENTIZATION-004
- REQ-FRONTEND-COMPONENTIZATION-005
- REQ-FRONTEND-COMPONENTIZATION-006
- REQ-FRONTEND-COMPONENTIZATION-007
- REQ-FRONTEND-COMPONENTIZATION-008

## Target Component Map

| Current Region In `App.vue` | Proposed Component Boundary | Notes |
|---|---|---|
| Product page wrapper and sidebar | `layout/ProductShell.vue`, `layout/ProductSidebar.vue` | Keeps `vue-product-page` and navigation actions. |
| Home library and create space | `home/HomeView.vue`, `home/CreateSpacePanel.vue` | Preserves create-space selectors/status/errors. |
| Global chat | `chat/GlobalChatView.vue` | Preserves selected spaces, question, Ask answer, quality chips. |
| Space header and tabs | `space/SpaceDetailView.vue`, `space/SpaceTabs.vue` | Holds tab buttons; no router. |
| Documents workflow | `documents/DocumentsTab.vue` plus smaller list/form/report components | Keeps API metadata, manual URL, mock upload, batch report. |
| Connector sync | `connectors/ConnectorSyncTab.vue` | Keeps mock/local connector sync display. |
| Review and recovery | `review/ReviewTab.vue`, `review/DeadLetterPanel.vue` | Keeps queue, approve, retry, acknowledge flows. |
| Wiki | `wiki/WikiTab.vue` | Keeps publish action, page detail, issues. |
| Graph | `graph/GraphTab.vue` | Keeps search, selected node, evidence detail. |
| Settings modal | `settings/SettingsModal.vue` plus panel components | Keeps panel navigation and current capability gates. |
| Repeated badges/cards/lists | `shared/*` | Only small presentational primitives. |

Implementation may adjust names, but the same boundaries should remain recognizable.

## Interaction Rules

- Product navigation buttons emit actions; the root updates `productView`.
- Space tab buttons emit tab changes; the root or `SpaceDetailView` updates `activeSpaceTab`.
- Settings navigation emits panel changes; modal open/close remains state-driven.
- Components do not mutate parent state implicitly; state changes go through props/events or explicit composable actions.
- Existing disabled states and capability checks remain visible and functional.

## Selector Rules

- Existing E2E/unit selectors remain in the rendered DOM.
- If markup is moved into a component, the selector moves with the same visible element.
- If a selector must change because the semantic element changes, the task must document the equivalent selector and update tests in the same commit.

## Styling Rules

- Reuse existing classes from `frontend/src/styles.css`.
- Do not create a new visual system.
- Avoid CSS churn during extraction.
- Keep responsive behavior and stable dimensions intact.
- Do not add decorative UI unrelated to the current product surface.

## Test Design

| Test Area | Expected Coverage |
|---|---|
| Pure domain helpers | Unit tests for mapping/formatting extracted from `App.vue`. |
| Composables | Unit tests for state transitions, API sequencing, loading/error behavior where feasible. |
| Components | Component or App-level tests for behavior-bearing extracted views. |
| App integration | Existing `App.test.ts` behavior remains covered, split only when useful. |
| E2E | Existing Playwright flows remain stable; run focused specs for changed surfaces and full frontend E2E before closeout. |

## Manual Review Checklist

- Home, Chat, Space Detail, Documents, Connectors, Review, Wiki, Graph, and Settings still appear and behave as before.
- URL does not change during internal navigation.
- Source trace/confidence/review status remain visible.
- Masked secrets remain masked.
- Coming-soon and disabled controls remain non-operational.
- No new backend/API/provider behavior is visible.
