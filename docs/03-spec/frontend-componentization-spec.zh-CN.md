# 规格：Frontend Componentization

## 状态

已于 2026-07-08 接受并进入实现。切片 `frontend-componentization` 的行为真相源。

## 概述

本切片在不改变产品行为的前提下，让 `frontend/src/App.vue` 在结构上更可维护。已接受行为仍是当前 Vue 产品路径：首页、全局 Chat、空间详情 tabs、上传/处理、审核/恢复、Wiki、Graph、Trusted Ask、settings、API-backed metadata 与 mock/sample-safe fallback。

本切片不引入 `vue-router`。在 URL 语义、深链与浏览器历史被明确接受为产品能力前，当前内存状态仍是导航契约。

## 功能需求

### 根外壳与导航

- FR-FRONTEND-COMPONENTIZATION-001：`App.vue` 仍作为根组件并协调应用启动。
- FR-FRONTEND-COMPONENTIZATION-002：产品导航继续由当前内存状态驱动：`activeExperience`、`productView`、`activeSpaceTab`、`settingsOpen` 与 `settingsPanel`。
- FR-FRONTEND-COMPONENTIZATION-003：不新增 `vue-router`、route 文件、route guards、URL mutation 或浏览器历史契约。
- FR-FRONTEND-COMPONENTIZATION-004：保留现有主要选择器，例如 `data-testid="vue-product-page"`、space/home/chat/tab/settings 以及面向 E2E 的选择器。

### 组件抽取

- FR-FRONTEND-COMPONENTIZATION-005：产品外壳拆成按特性组织的组件：导航/sidebar、产品首页、全局 Chat、空间详情、Documents、connector sync、review/recovery、Wiki、Graph、settings 与共享展示原语。
- FR-FRONTEND-COMPONENTIZATION-006：拆出的组件通过类型化 props/events 接收数据和回调；若状态跨组件共享且行为不变，可使用 composables。
- FR-FRONTEND-COMPONENTIZATION-007：不得把无关业务行为搬进泛化 shared 组件。
- FR-FRONTEND-COMPONENTIZATION-008：拆分后 `App.vue` 必须可读为编排层，不应继续累积大型 template 区块。

### Composables 与 Helpers

- FR-FRONTEND-COMPONENTIZATION-009：当能让所有权更清晰时，可将现有非平凡 computed 映射与副作用 workflow 抽成 composables 或 domain helpers。
- FR-FRONTEND-COMPONENTIZATION-010：拆出的 composables 必须保留当前 API sequencing、loading flags、safe errors 与 fallback 行为。
- FR-FRONTEND-COMPONENTIZATION-011：API 访问继续置于 `frontend/src/api.ts` 之后；拆出的 composable 不得直连 provider/runtime/parser/converter/vector/storage 引擎。

### 行为保持

- FR-FRONTEND-COMPONENTIZATION-012：首页、创建空间面板、全局 Chat、选中空间详情、tab 切换、Documents workflow、connector sync、review/dead-letter operations、Wiki publish display、Graph selection/search、model/settings panels 与 mock upload workflow 保持现有可见行为。
- FR-FRONTEND-COMPONENTIZATION-013：source trace、confidence、review status、capability gates、safe error copy、masked secret display 与 coming-soon/disabled 状态在当前可见位置继续可见。
- FR-FRONTEND-COMPONENTIZATION-014：CSS 变更仅限于保持拆分后的现有布局；本切片不接受视觉重设计。

### 测试与验证

- FR-FRONTEND-COMPONENTIZATION-015：带行为的拆出 helper/composable/component 必须有聚焦测试，或明确复用现有覆盖。
- FR-FRONTEND-COMPONENTIZATION-016：现有 frontend unit tests 与 Playwright flows 必须继续验证同一用户可见行为。
- FR-FRONTEND-COMPONENTIZATION-017：验证包括 frontend typecheck、unit tests、build、相关 E2E、`git diff --check`、secret/private-path scan 与 new-network/dependency scan。

## 状态规则

```text
activeExperience === "atlas" -> product shell
productView: home | chat | space
activeSpaceTab: docs | connectors | review | wiki | graph
settingsOpen/settingsPanel -> modal state only
selectedSpaceId -> API/mock product context
```

这些仍是内存状态规则，不是 route 契约。

## 验收矩阵

| Requirement | Spec Sections | Observable Check |
|---|---|---|
| REQ-FRONTEND-COMPONENTIZATION-001 | Component Extraction, Behavior Preservation | `App.vue` 委托主要 UI 区域，现有流程继续通过。 |
| REQ-FRONTEND-COMPONENTIZATION-002 | Root Shell And Navigation, State Rules | 不新增 router package/config/routes；产品导航期间 URL 不变。 |
| REQ-FRONTEND-COMPONENTIZATION-003 | Root Shell And Navigation, Tests And Verification | 当前 E2E/unit 选择器保持存在，或有记录的等价替换。 |
| REQ-FRONTEND-COMPONENTIZATION-004 | Composables And Helpers | 拆出代码使用 `frontend/src/api.ts`，不直连 engines/providers。 |
| REQ-FRONTEND-COMPONENTIZATION-005 | Behavior Preservation | trace/confidence/review/safe-error/disabled 状态继续渲染。 |
| REQ-FRONTEND-COMPONENTIZATION-006 | Behavior Preservation | 不引入真实数据、secret、私有路径或外部调用。 |
| REQ-FRONTEND-COMPONENTIZATION-007 | Tests And Verification | 聚焦测试和 frontend 验证套件通过。 |
| REQ-FRONTEND-COMPONENTIZATION-008 | Composables And Helpers, Tests And Verification | 后端/API/adapters/provider-backed E2E 行为保持不变。 |

## 明确非目标

- URL 语义、深链、浏览器历史或 route-level permissions。
- 产品功能扩展。
- 视觉重设计。
- 后端/API 扩展。
- 生产 auth/RBAC、生产 provider 集成、生产 storage/vector/model engine 变更。
- 真实公司数据摄取。

## SDD 质量说明

本文档遵循 Atlas SDD chain model：`req-to-user-story`、`user-story-to-spec`、`spec-to-architecture`、`architecture-to-design`、`design-to-tasks`，并使用 `review-doc-quality` 作为自检清单。
