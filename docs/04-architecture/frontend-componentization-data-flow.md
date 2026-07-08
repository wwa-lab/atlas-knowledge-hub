# Data Flow: Frontend Componentization

## Status

Draft for SDD acceptance.

## Requirement Coverage

- REQ-FRONTEND-COMPONENTIZATION-001
- REQ-FRONTEND-COMPONENTIZATION-002
- REQ-FRONTEND-COMPONENTIZATION-003
- REQ-FRONTEND-COMPONENTIZATION-004
- REQ-FRONTEND-COMPONENTIZATION-005
- REQ-FRONTEND-COMPONENTIZATION-006
- REQ-FRONTEND-COMPONENTIZATION-007
- REQ-FRONTEND-COMPONENTIZATION-008

## Current Data Flow To Preserve

```text
main.ts -> App.vue bootstrap
App.vue -> api.ts -> Atlas backend API
App.vue -> atlasMock.ts -> safe mock/sample fallbacks
App.vue state/computed -> template regions -> user actions -> App.vue handlers
```

## Target Data Flow

```text
main.ts -> App.vue bootstrap/orchestration
App.vue/composables -> api.ts -> Atlas backend API
App.vue/composables -> atlasMock.ts/domain helpers -> safe mock/sample view models
App.vue -> feature components via props
feature components -> App.vue/composables via emitted events/callbacks
```

## Product Navigation Flow

```text
Sidebar action
  -> showProductHome/showProductChat/openProductSpace
  -> productView updates
  -> App.vue renders HomeView/GlobalChatView/SpaceShell

Space tab click
  -> activeSpaceTab updates
  -> SpaceShell renders Documents/Connectors/Review/Wiki/Graph tab component

Settings action
  -> settingsOpen/settingsPanel updates
  -> SettingsModal renders selected settings panel
```

This flow remains memory-only and does not mutate URL state.

## API Workflow Flow

```text
User action in component
  -> event/callback
  -> App.vue or composable handler
  -> existing api.ts helper
  -> typed response / ApiError
  -> existing loading/error/data state
  -> props update
  -> component re-renders existing state
```

## Fallback And Safety Flow

```text
API unavailable or mock-safe path
  -> existing safe fallback data
  -> source trace/confidence/review status retained
  -> user-safe copy rendered
  -> no real company data or raw secrets introduced
```

## Testing Flow

```text
Component/helper extraction
  -> focused unit/component tests
  -> existing App-level tests remain stable
  -> Playwright flows keep current selectors
  -> build/typecheck verify integration
```

## Flow Constraints

- Feature components do not own global route state.
- Feature components do not create new API contracts.
- Feature components do not directly call external engines/providers.
- Events/callbacks preserve current action sequencing.
- Safe error strings remain user-safe and do not expose stack traces, credentials, private paths, or source document content.
