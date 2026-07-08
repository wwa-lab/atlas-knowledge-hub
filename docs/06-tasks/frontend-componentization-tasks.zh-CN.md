# 任务：Frontend Componentization

## 概述

依据 `docs/03-spec/frontend-componentization-spec.md`，对 `frontend/src/App.vue` 执行行为不变的结构性组件化。

本切片明确为仅前端切片。不包含 `vue-router`、后端/API 变更、provider 调用、真实数据、新依赖或视觉重设计。

状态：T-FRONTEND-COMPONENTIZATION-001 至 T-FRONTEND-COMPONENTIZATION-007 已于 2026-07-08 实现并完成本地验证。本切片是行为不变的前端结构化 checkpoint，不是 production-readiness 声明。

追加状态：`frontend-state-extraction` 扩展这个已接受切片，在保持已组件化 UI 行为不变的前提下，将 `App.vue` 根状态迁移到领域 composables。

## 任务详情

### T-FRONTEND-COMPONENTIZATION-001：接受 SDD 契约并建立选择器基线

- Requirement: REQ-FRONTEND-COMPONENTIZATION-001 through REQ-FRONTEND-COMPONENTIZATION-008
- Owner type: documentation / QA
- Priority: Must
- Dependencies: None
- Scope: 实现前确认本 SDD 集合已被接受。记录当前高价值选择器与 frontend 验证命令作为回归基线。
- Verification:
  - `npm run agent:check-sdd -- --slice frontend-componentization`
  - `git diff --check`
- Notes: 由于本切片不涉及后端/API 契约变更，API guide 省略。

### T-FRONTEND-COMPONENTIZATION-002：抽取纯 domain helpers 与本地 view-model types

- Requirement: REQ-FRONTEND-COMPONENTIZATION-001, REQ-FRONTEND-COMPONENTIZATION-005, REQ-FRONTEND-COMPONENTIZATION-007
- Owner type: frontend
- Priority: Must
- Dependencies: T-FRONTEND-COMPONENTIZATION-001
- Scope: 只在所有权清晰处，将纯 mapping/formatting 逻辑从 `App.vue` 移入聚焦的 `domain` helper 文件。保持 source trace、confidence、review status、safe errors 与 model/settings labels。
- Verification:
  - `cd frontend && npm run typecheck`
  - `cd frontend && npm run test -- --run`

### T-FRONTEND-COMPONENTIZATION-003：抽取 product shell、sidebar、home 与 global chat

- Requirement: REQ-FRONTEND-COMPONENTIZATION-001, REQ-FRONTEND-COMPONENTIZATION-002, REQ-FRONTEND-COMPONENTIZATION-003
- Owner type: frontend
- Priority: Must
- Dependencies: T-FRONTEND-COMPONENTIZATION-002
- Scope: 创建 layout/home/chat 组件并通过 props/events 接线。保留 `productView` 内存导航，以及 product page、space cards、create-space panel、global chat、Ask input 和 Ask answer 的现有选择器。
- Verification:
  - `cd frontend && npm run typecheck`
  - `cd frontend && npm run test -- --run`
  - 对已有 specs 覆盖的 home/chat surfaces 跑聚焦 Playwright。

### T-FRONTEND-COMPONENTIZATION-004：抽取 space shell 与 tab components

- Requirement: REQ-FRONTEND-COMPONENTIZATION-001, REQ-FRONTEND-COMPONENTIZATION-002, REQ-FRONTEND-COMPONENTIZATION-003, REQ-FRONTEND-COMPONENTIZATION-005
- Owner type: frontend
- Priority: Must
- Dependencies: T-FRONTEND-COMPONENTIZATION-003
- Scope: 小步抽取 space header/tabs，以及 Documents、connectors、review/recovery、Wiki、Graph tab components。`activeSpaceTab` 保持为内存状态，保留 tab selectors 与可见产品行为。
- Verification:
  - `cd frontend && npm run typecheck`
  - `cd frontend && npm run test -- --run`
  - 对改动的 space surfaces 跑聚焦 Playwright specs。

### T-FRONTEND-COMPONENTIZATION-005：抽取 settings modal 与 settings panels

- Requirement: REQ-FRONTEND-COMPONENTIZATION-001, REQ-FRONTEND-COMPONENTIZATION-003, REQ-FRONTEND-COMPONENTIZATION-005, REQ-FRONTEND-COMPONENTIZATION-006
- Owner type: frontend
- Priority: Must
- Dependencies: T-FRONTEND-COMPONENTIZATION-003
- Scope: 抽取 settings modal、rail 与带行为 panels，同时保留 `settingsOpen/settingsPanel`、capability gates、masked API display、model draft behavior 与 disabled/coming-soon controls。
- Verification:
  - `cd frontend && npm run typecheck`
  - `cd frontend && npm run test -- --run`
  - 对已有覆盖的 settings 跑聚焦 Playwright specs。

### T-FRONTEND-COMPONENTIZATION-006：只在确实降低复杂度处抽取 composables

- Requirement: REQ-FRONTEND-COMPONENTIZATION-001, REQ-FRONTEND-COMPONENTIZATION-004, REQ-FRONTEND-COMPONENTIZATION-007, REQ-FRONTEND-COMPONENTIZATION-008
- Owner type: frontend
- Priority: Should
- Dependencies: T-FRONTEND-COMPONENTIZATION-004, T-FRONTEND-COMPONENTIZATION-005
- Scope: 只有当能让依赖更清晰时，才将 API workflow 或共享 UI state 移入 composables。继续通过 `frontend/src/api.ts` 使用 API helper；不新增 API 调用或新依赖。
- Verification:
  - `cd frontend && npm run typecheck`
  - `cd frontend && npm run test -- --run`
  - 为拆出的 composables 增加聚焦测试。

### T-FRONTEND-COMPONENTIZATION-007：最终 frontend 回归与安全门

- Requirement: REQ-FRONTEND-COMPONENTIZATION-001 through REQ-FRONTEND-COMPONENTIZATION-008
- Owner type: QA/security
- Priority: Must
- Dependencies: T-FRONTEND-COMPONENTIZATION-002 through T-FRONTEND-COMPONENTIZATION-006
- Scope: 运行最终检查，记录跳过检查及原因，并将证据更新到 traceability。除非实现意外触及相关区域，否则不运行 backend/provider 检查。
- Verification:
  - `cd frontend && npm run typecheck`
  - `cd frontend && npm run test -- --run`
  - `cd frontend && npm run build`
  - `cd frontend && npm run e2e`
  - `git diff --check`
  - 对变更文件做 focused secret/private-path scan
  - 对变更文件做 focused new-network/dependency scan
  - `npm run agent:closeout`

### T-FRONTEND-COMPONENTIZATION-008：扩展 frontend 状态抽取的 SDD 契约

- Requirement: REQ-FRONTEND-COMPONENTIZATION-009 through REQ-FRONTEND-COMPONENTIZATION-011
- Owner type: documentation / frontend
- Priority: Must
- Dependencies: T-FRONTEND-COMPONENTIZATION-007
- Scope: 将 `frontend-state-extraction` 记录为本切片的行为不变扩展。记录八个目标领域 composables、工厂函数所有权、显式 props/events 数据流、`App.vue` 行数目标、不引入 Pinia/router/provide-inject 的约束，以及每个 composable 的测试要求。
- Verification:
  - `npm run agent:check-sdd -- --slice frontend-componentization`
  - `git diff --check`

### T-FRONTEND-COMPONENTIZATION-009：抽取领域状态 composables

- Requirement: REQ-FRONTEND-COMPONENTIZATION-004, REQ-FRONTEND-COMPONENTIZATION-009, REQ-FRONTEND-COMPONENTIZATION-010
- Owner type: frontend
- Priority: Must
- Dependencies: T-FRONTEND-COMPONENTIZATION-008
- Scope: 将集中在 `App.vue` 的 `ref`/`computed`/API workflow 所有权迁移到 `frontend/src/composables/useSpaces.ts`、`useBatches.ts`、`useReviewQueue.ts`、`useWikiPages.ts`、`useGraph.ts`、`useAsk.ts`、`useSettings.ts` 与 `useGlobalChat.ts`。API 访问继续通过 `frontend/src/api.ts`；不得创建模块级单例；跨域依赖通过显式 options/callbacks 传递。
- Verification:
  - 每完成一个领域抽取后，运行 `npm --prefix frontend run test -- --run <domain composable test>` 与 `npm --prefix frontend run typecheck`
  - `npm --prefix frontend run test -- --run src/composables`

### T-FRONTEND-COMPONENTIZATION-010：将 App.vue 瘦身为编排外壳

- Requirement: REQ-FRONTEND-COMPONENTIZATION-001, REQ-FRONTEND-COMPONENTIZATION-002, REQ-FRONTEND-COMPONENTIZATION-009, REQ-FRONTEND-COMPONENTIZATION-010
- Owner type: frontend
- Priority: Must
- Dependencies: T-FRONTEND-COMPONENTIZATION-009
- Scope: 重新接线 `App.vue`，让它在顶层调用各领域 composables 一次，并继续通过现有组件 props/events 下发状态。保留内存态 `home`/`chat`/`space` 导航、settings 挂载、稳定 E2E 选择器和 P0/workbench 行为。必要时将遗留 workbench view 抽成子组件，以保证 `App.vue` 少于 600 行。
- Verification:
  - `wc -l frontend/src/App.vue` 显示少于 600 行
  - `npm --prefix frontend run typecheck`
  - `npm --prefix frontend run test -- --run src/App.test.ts`

### T-FRONTEND-COMPONENTIZATION-011：增加 composable 覆盖并完成最终回归

- Requirement: REQ-FRONTEND-COMPONENTIZATION-007, REQ-FRONTEND-COMPONENTIZATION-011
- Owner type: frontend / QA
- Priority: Must
- Dependencies: T-FRONTEND-COMPONENTIZATION-009, T-FRONTEND-COMPONENTIZATION-010
- Scope: 为每个拆出的 composable 增加相邻 focused tests，并让 `App.test.ts` 聚焦渲染编排/用户流程，而不是根组件私有状态实现。运行请求的 frontend regression、diff hygiene 与安全扫描。
- Verification:
  - `npm --prefix frontend run lint`
  - `npm --prefix frontend run typecheck`
  - `npm --prefix frontend run test`
  - `npm --prefix frontend run build`
  - `npm --prefix frontend run e2e`
  - `git diff --check`
  - `frontend/src` 下 focused no-`any` scan
  - 对变更文件做 focused secret/private-path 与 new-network/dependency scans

## 依赖计划

关键路径：T-FRONTEND-COMPONENTIZATION-001 -> T-FRONTEND-COMPONENTIZATION-002 -> T-FRONTEND-COMPONENTIZATION-003 -> T-FRONTEND-COMPONENTIZATION-004 -> T-FRONTEND-COMPONENTIZATION-006 -> T-FRONTEND-COMPONENTIZATION-007 -> T-FRONTEND-COMPONENTIZATION-008 -> T-FRONTEND-COMPONENTIZATION-009 -> T-FRONTEND-COMPONENTIZATION-010 -> T-FRONTEND-COMPONENTIZATION-011。

T-FRONTEND-COMPONENTIZATION-005 可在 T-FRONTEND-COMPONENTIZATION-003 后并行推进，并在最终 closeout 前完成。

## 风险 / 阻塞

- 如果先搬 template 而未澄清状态所有权，组件 props 可能变得过长。
- 现有 `App.test.ts` 需要谨慎拆分，避免降低覆盖。
- Playwright 选择器必须跨组件边界保持稳定。
- 任何 URL/deep-link/browser-history 需求都需要单独接受的切片后才能做 router。
- 选择空间、刷新 review/Wiki 状态、发布选中文件等跨域操作必须使用显式 composable options/callbacks。如果某个领域无法在不造成行为漂移的前提下干净拆分，应先在此记录阻塞点再继续。

## 未决问题

- 无阻塞项。除非 URL 语义成为产品需求，否则 `vue-router` 明确延后。

## API Guide 省略

本切片不涉及后端端点、API payload、持久化模型、adapter contract 或 provider/runtime 行为变更，因此不生成 API guide。
