# 数据流：Frontend Componentization

## 状态

待 SDD 验收草案。

## 需求覆盖

- REQ-FRONTEND-COMPONENTIZATION-001
- REQ-FRONTEND-COMPONENTIZATION-002
- REQ-FRONTEND-COMPONENTIZATION-003
- REQ-FRONTEND-COMPONENTIZATION-004
- REQ-FRONTEND-COMPONENTIZATION-005
- REQ-FRONTEND-COMPONENTIZATION-006
- REQ-FRONTEND-COMPONENTIZATION-007
- REQ-FRONTEND-COMPONENTIZATION-008

## 需要保持的当前数据流

```text
main.ts -> App.vue bootstrap
App.vue -> api.ts -> Atlas backend API
App.vue -> atlasMock.ts -> safe mock/sample fallbacks
App.vue state/computed -> template regions -> user actions -> App.vue handlers
```

## 目标数据流

```text
main.ts -> App.vue bootstrap/orchestration
App.vue/composables -> api.ts -> Atlas backend API
App.vue/composables -> atlasMock.ts/domain helpers -> safe mock/sample view models
App.vue -> feature components via props
feature components -> App.vue/composables via emitted events/callbacks
```

## 产品导航流

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

该流程保持为纯内存状态，不修改 URL state。

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

## Fallback 与安全流

```text
API unavailable or mock-safe path
  -> existing safe fallback data
  -> source trace/confidence/review status retained
  -> user-safe copy rendered
  -> no real company data or raw secrets introduced
```

## 测试流

```text
Component/helper extraction
  -> focused unit/component tests
  -> existing App-level tests remain stable
  -> Playwright flows keep current selectors
  -> build/typecheck verify integration
```

## 流程约束

- 特性组件不拥有全局 route state。
- 特性组件不创建新 API 契约。
- 特性组件不直连外部 engines/providers。
- Events/callbacks 保留当前 action sequencing。
- Safe error 字符串保持用户安全，不暴露 stack traces、credentials、private paths 或 source document content。
