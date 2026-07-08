# 任务：Frontend Componentization

## 概述

依据 `docs/03-spec/frontend-componentization-spec.md`，对 `frontend/src/App.vue` 执行行为不变的结构性组件化。

本切片明确为仅前端切片。不包含 `vue-router`、后端/API 变更、provider 调用、真实数据、新依赖或视觉重设计。

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

## 依赖计划

关键路径：T-FRONTEND-COMPONENTIZATION-001 -> T-FRONTEND-COMPONENTIZATION-002 -> T-FRONTEND-COMPONENTIZATION-003 -> T-FRONTEND-COMPONENTIZATION-004 -> T-FRONTEND-COMPONENTIZATION-006 -> T-FRONTEND-COMPONENTIZATION-007。

T-FRONTEND-COMPONENTIZATION-005 可在 T-FRONTEND-COMPONENTIZATION-003 后并行推进，并在最终 closeout 前完成。

## 风险 / 阻塞

- 如果先搬 template 而未澄清状态所有权，组件 props 可能变得过长。
- 现有 `App.test.ts` 需要谨慎拆分，避免降低覆盖。
- Playwright 选择器必须跨组件边界保持稳定。
- 任何 URL/deep-link/browser-history 需求都需要单独接受的切片后才能做 router。

## 未决问题

- 无阻塞项。除非 URL 语义成为产品需求，否则 `vue-router` 明确延后。

## API Guide 省略

本切片不涉及后端端点、API payload、持久化模型、adapter contract 或 provider/runtime 行为变更，因此不生成 API guide。
