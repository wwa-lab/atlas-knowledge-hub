# Architecture: Frontend Componentization

## Status

Draft for SDD acceptance.

## Architectural Intent

The current Vue app has the right product direction but concentrates application bootstrapping, API workflows, computed view models, settings logic, mock upload behavior, and the entire template inside `frontend/src/App.vue`. This slice introduces frontend structure without changing runtime architecture.

## Requirement Coverage

- REQ-FRONTEND-COMPONENTIZATION-001
- REQ-FRONTEND-COMPONENTIZATION-002
- REQ-FRONTEND-COMPONENTIZATION-003
- REQ-FRONTEND-COMPONENTIZATION-004
- REQ-FRONTEND-COMPONENTIZATION-005
- REQ-FRONTEND-COMPONENTIZATION-006
- REQ-FRONTEND-COMPONENTIZATION-007
- REQ-FRONTEND-COMPONENTIZATION-008

## Proposed Frontend Module Shape

```text
frontend/src/
  App.vue
  api.ts
  types.ts
  data/atlasMock.ts
  components/
    layout/
    home/
    chat/
    space/
    documents/
    connectors/
    review/
    wiki/
    graph/
    settings/
    shared/
  composables/
  domain/
```

The exact file count can be adjusted during implementation, but feature ownership must stay clear and no single extracted component should become a second monolith.

## Ownership Boundaries

| Boundary | Responsibility | Must Not Own |
|---|---|---|
| `App.vue` | Root bootstrapping, top-level orchestration, cross-feature state wiring. | Large feature templates or direct engine/provider calls. |
| `components/layout` | Product shell layout, sidebar navigation, modal mounting. | Feature-specific business workflows. |
| `components/home` | Knowledge Space home cards and create-space panel. | Space detail tabs or settings internals. |
| `components/chat` | Global multi-space Trusted Ask surface. | Backend API client implementation. |
| `components/space` | Space header and tab shell. | Documents/Wiki/Graph internals. |
| `components/documents` | Upload metadata UI, manual URL form, batches/files/chunks display, mock upload report. | Parser/converter/storage runtime calls. |
| `components/connectors` | Connector sync registry/run/item UI. | Real connector provider auth/fetching. |
| `components/review` | Processing center, review queues, dead-letter recovery UI. | Backend worker implementation. |
| `components/wiki` | Wiki index/page/issues/publish controls. | Markdown generation backend logic. |
| `components/graph` | Graph list/detail/search/evidence display. | Graph projection engine. |
| `components/settings` | General, profile, space info, members, audit, API, messages, model, vector/parser/storage panels. | Raw secrets or production provider behavior. |
| `components/shared` | Small reusable display primitives with no hidden domain side effects. | Cross-feature orchestration. |
| `composables` | Shared Vue state/effect logic for existing workflows. | New product behavior or new API contracts. |
| `domain` | Pure mapping/formatting helpers extracted from `App.vue`. | Vue side effects or network calls. |

## Dependency Rules

- Components may import shared types and pure domain helpers.
- Components may emit events or receive callbacks for actions.
- Composables may call existing functions from `frontend/src/api.ts`.
- Components/composables must not call `fetch` directly if an existing API helper exists.
- Components/composables must not add backend URLs, provider URLs, engine commands, or private filesystem paths.
- Shared components must stay presentational unless the behavior is truly generic.

## Routing Decision

`vue-router` is intentionally excluded. The current application has product views and tabs, but no accepted product requirement for:

- URL-addressable product states.
- Deep-linked knowledge spaces/tabs.
- Browser back/forward semantics.
- Route-level guards.

Those capabilities require separate requirements and acceptance criteria.

## Risk Controls

- Extract in vertical slices, keeping tests green after each stage.
- Preserve `data-testid` selectors across component boundaries.
- Prefer props/events for first-pass extraction; introduce composables only where state ownership is clearer.
- Keep CSS class names stable unless a layout preservation fix requires a narrow change.
- Add tests around behavior-bearing extracted logic, especially settings, upload/review/publish, graph selection, and model state.

## Alternatives Considered

| Alternative | Decision | Reason |
|---|---|---|
| Big-bang rewrite of the frontend | Rejected | High visual/behavior drift risk and violates project change discipline. |
| Introduce `vue-router` now | Rejected | User explicitly scoped this slice to behavior-preserving structure; URL semantics are not yet product requirements. |
| Introduce Pinia/Vuex | Rejected | Adds architecture surface beyond the current refactor need. |
| Keep all state in `App.vue` and only extract templates | Allowed only as an early step | Useful for low-risk extraction, but final state should also isolate non-trivial helpers/composables where appropriate. |
