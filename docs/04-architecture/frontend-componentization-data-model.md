# Data Model: Frontend Componentization

## Status

Draft for SDD acceptance.

## Requirement Coverage

- REQ-FRONTEND-COMPONENTIZATION-004
- REQ-FRONTEND-COMPONENTIZATION-005
- REQ-FRONTEND-COMPONENTIZATION-006
- REQ-FRONTEND-COMPONENTIZATION-008

## Data Model Decision

This slice does not add persistent data models, database tables, migrations, backend DTOs, or API schemas. The data model is a frontend ownership model for existing state and view models.

## Existing Data Sources

| Source | Current Role | Componentization Rule |
|---|---|---|
| `frontend/src/types.ts` | Shared API/domain TypeScript types. | Reuse; do not duplicate structurally equivalent types in components. |
| `frontend/src/api.ts` | Typed API helper boundary. | Reuse; no new backend contract in this slice. |
| `frontend/src/data/atlasMock.ts` | Safe mock/sample product data. | Reuse; keep mock/sample-safe constraints. |
| `frontend/src/App.vue` local interfaces | Vue-specific view models and local UI state. | Move to focused `domain`/component files only when ownership is clear. |

## State Groups To Preserve

| State Group | Examples | Target Ownership |
|---|---|---|
| Navigation state | `activeExperience`, `productView`, `activeSpaceTab`, `settingsOpen`, `settingsPanel` | `App.vue` orchestration or a narrow navigation composable; no router. |
| Space/API state | `spaces`, `selectedSpace`, `selectedSpaceId`, `batches`, `files`, `chunks`, loading/error flags | `App.vue` or API workflow composables that call `api.ts`. |
| Product mock workflow state | `uploadSession`, `activeProductBatchFiles`, report/modal state | Documents feature component/composable, preserving mock-safe behavior. |
| Wiki/Graph/Ask view models | `productWikiPages`, graph node/edge lists, Ask answer mapping | Feature components plus pure mapping helpers. |
| Settings state | General settings, member mock state, model draft, masked API display | Settings components/composables, preserving masked/disabled behavior. |
| Governance/recovery state | Review queues, audit events, connector sync, dead-letter entries | Review/connectors/settings components with existing API boundaries. |

## View Model Rules

- Prefer existing API types when the displayed data already maps directly to backend responses.
- Use local view model types only for UI-specific derived state.
- Keep source trace, confidence, review status, safe error, and capability metadata intact.
- Do not introduce persisted client storage in this slice.
- Do not introduce route-derived state.
- Do not introduce new auth, permission, or secret data models.

## API Guide Omission

API implementation guide is omitted because this slice does not add or change backend endpoints, request/response payloads, persistence, adapter contracts, or provider/runtime behavior.
