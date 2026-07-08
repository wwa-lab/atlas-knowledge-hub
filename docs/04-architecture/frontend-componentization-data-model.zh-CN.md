# 数据模型：Frontend Componentization

## 状态

待 SDD 验收草案。

## 需求覆盖

- REQ-FRONTEND-COMPONENTIZATION-004
- REQ-FRONTEND-COMPONENTIZATION-005
- REQ-FRONTEND-COMPONENTIZATION-006
- REQ-FRONTEND-COMPONENTIZATION-008

## 数据模型决策

本切片不新增持久化数据模型、数据库表、迁移、后端 DTO 或 API schemas。这里的数据模型是针对现有前端 state 与 view model 的所有权模型。

## 现有数据来源

| Source | Current Role | Componentization Rule |
|---|---|---|
| `frontend/src/types.ts` | 共享 API/domain TypeScript 类型。 | 复用；不要在组件中复制结构等价类型。 |
| `frontend/src/api.ts` | 类型化 API helper 边界。 | 复用；本切片不新增后端契约。 |
| `frontend/src/data/atlasMock.ts` | 安全 mock/sample 产品数据。 | 复用；保持 mock/sample-safe 约束。 |
| `frontend/src/App.vue` local interfaces | Vue-specific view models 与本地 UI state。 | 只有所有权清晰时才移动到聚焦的 `domain`/component 文件。 |

## 需要保持的状态组

| State Group | Examples | Target Ownership |
|---|---|---|
| Navigation state | `activeExperience`、`productView`、`activeSpaceTab`、`settingsOpen`、`settingsPanel` | `App.vue` 编排或窄 navigation composable；不使用 router。 |
| Space/API state | `spaces`、`selectedSpace`、`selectedSpaceId`、`batches`、`files`、`chunks`、loading/error flags | `App.vue` 或调用 `api.ts` 的 API workflow composables。 |
| Product mock workflow state | `uploadSession`、`activeProductBatchFiles`、report/modal state | Documents 特性组件/composable，保持 mock-safe 行为。 |
| Wiki/Graph/Ask view models | `productWikiPages`、graph node/edge lists、Ask answer mapping | 特性组件加纯 mapping helpers。 |
| Settings state | General settings、member mock state、model draft、masked API display | Settings components/composables，保持 masked/disabled 行为。 |
| Governance/recovery state | Review queues、audit events、connector sync、dead-letter entries | Review/connectors/settings 组件，沿用现有 API 边界。 |

## View Model 规则

- 当展示数据直接映射后端响应时，优先使用现有 API 类型。
- 仅对 UI 特有派生状态使用本地 view model 类型。
- 保持 source trace、confidence、review status、safe error 与 capability metadata 完整。
- 本切片不引入持久化 client storage。
- 本切片不引入 route-derived state。
- 本切片不引入新的 auth、permission 或 secret 数据模型。

## API Guide 省略说明

本切片不新增或修改后端端点、request/response payload、持久化、adapter contract 或 provider/runtime 行为，因此省略 API implementation guide。
