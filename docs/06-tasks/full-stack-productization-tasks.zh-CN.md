# 任务：全栈产品化

## 概述

依据 `docs/03-spec/full-stack-productization-spec.md` 实现 P0 浏览器驱动 full-stack 闭环。这是在现有 Vue/Spring Boot 仓库上的 brownfield 工作。

Status: T-FSP-001 through T-FSP-010 已于 2026-07-03 实现并验证。验证证据记录在 `docs/00-context/full-stack-productization-traceability.zh-CN.md`。

## 任务详情

### T-FSP-001：新增/确认 full-stack SDD artifacts

- Requirement: REQ-FSP-001 through REQ-FSP-012
- Owner type: documentation
- Priority: Must
- Dependencies: None
- Scope: 创建双语 requirements、stories、spec、architecture、data flow、data model、design、API guide、tasks、traceability。
- Verification: 确认所有必需 EN 与 `.zh-CN.md` 文件存在，且 IDs 匹配。

### T-FSP-002：构建 typed frontend API client 和 domain models

- Requirement: REQ-FSP-001 through REQ-FSP-009
- Owner type: frontend
- Priority: Must
- Dependencies: T-FSP-001
- Scope: 为 spaces、batches、files、chunks、review queues、reviews、publish、wiki pages、graph、vector refresh、Ask 添加 typed API helpers。
- Verification: `cd frontend && npm run typecheck`

### T-FSP-003：用 API-driven space/detail flow 替换主 Vue shell

- Requirement: REQ-FSP-001, REQ-FSP-002, REQ-FSP-010
- Owner type: frontend
- Priority: Must
- Dependencies: T-FSP-002
- Scope: 将 API-backed space list 和 selected space detail 渲染为 primary UI。将 static prototype behavior 移出主操作路径或标为 coming soon/reference-only。
- Verification: Frontend unit tests 断言 startup loading、space list rendering、detail loading 和 safe error state。

### T-FSP-004：实现 Documents batch/files/chunks flow

- Requirement: REQ-FSP-003, REQ-FSP-004
- Owner type: frontend
- Priority: Must
- Dependencies: T-FSP-003
- Scope: 添加 sample metadata-only batch creation、batch list、file list、selected file source chunks、可见 source trace。
- Verification: Unit tests 与 UI-driven E2E 覆盖 batch creation 和 chunk display。

### T-FSP-005：实现 Review 和 Publish flow

- Requirement: REQ-FSP-005, REQ-FSP-006
- Owner type: frontend
- Priority: Must
- Dependencies: T-FSP-004
- Scope: 加载 review queues、approve selected file、publish approved file、刷新 files/queues/wiki pages，并展示 publish-blocked 状态。
- Verification: Unit tests 与 UI-driven E2E 覆盖 approve 和 publish。

### T-FSP-006：接通 post-publish graph/vector refresh 和 Graph tab

- Requirement: REQ-FSP-007, REQ-FSP-008
- Owner type: frontend
- Priority: Must
- Dependencies: T-FSP-005
- Scope: 在 publish 后或通过显式 refresh action 触发现有 graph projection 与 vector index endpoints。Graph read/detail 保持 API-backed，并按 selected space scoped。
- Verification: E2E 确认 graph evidence 包含 published chunk。

### T-FSP-007：通过 API 实现 Ask tab

- Requirement: REQ-FSP-009
- Owner type: frontend
- Priority: Must
- Dependencies: T-FSP-006
- Scope: 添加 question input、`POST /ask`、`GET /ask-runs/{runId}`、answer panel、citation list、no-evidence/failure/loading states、`REVIEW_REQUIRED` answer status。
- Verification: E2E 确认 browser workflow 后出现 answer 与 evidence。

### T-FSP-008：禁用未接通 mock UI

- Requirement: REQ-FSP-010
- Owner type: frontend
- Priority: Must
- Dependencies: T-FSP-003
- Scope: 对 production upload、real auth/RBAC/admin actions、provider setup 以及任何保留的 prototype-only affordances 禁用或标注 coming soon。
- Verification: Unit/E2E 断言存在 `data-testid="coming-soon"`，且 P0 loop 之外没有 enabled fake primary action。

### T-FSP-009：新增 UI-driven Playwright E2E

- Requirement: REQ-FSP-011, REQ-FSP-012
- Owner type: QA
- Priority: Must
- Dependencies: T-FSP-004 through T-FSP-008
- Scope: 新增 browser-first E2E，完成 space list -> detail -> sample batch -> files/chunks -> review -> publish -> wiki -> graph -> ask。API request setup 不得替代主浏览器路径。
- Verification: `cd frontend && npm run e2e` 和可行时 root E2E scripts。

### T-FSP-010：运行 verification gates 和 scans

- Requirement: REQ-FSP-012
- Owner type: QA/security
- Priority: Must
- Dependencies: T-FSP-009
- Scope: 运行必需检查，或报告 skipped checks 及原因。
- Verification:
  - `npm run e2e:first-layer`
  - `npm run e2e:second-layer`
  - `cd frontend && npm run typecheck && npm run test -- --run && npm run build && npm run e2e`
  - `cd backend && mvn verify`
  - `git diff --check`
  - 对 changed files 做 secret/private-path scan
  - 对新增 external calls 做 network/dependency scan

## 依赖计划

Critical path: T-FSP-001 -> T-FSP-002 -> T-FSP-003 -> T-FSP-004 -> T-FSP-005 -> T-FSP-006 -> T-FSP-007 -> T-FSP-009 -> T-FSP-010。

T-FSP-008 可在 T-FSP-003 后、final E2E 前执行。

## 风险 / 阻塞

- 现有 iframe prototype 前端测试可能需要谨慎更新，因为 P0 将 API-driven app 设为 primary。
- 如果 backend Ask 需要已索引 vectors，UI 必须在 Ask 前运行现有 vector endpoint。
- Docker 或本地 PostgreSQL 可用性可能阻塞 second-layer verification。

## 开放问题

- P0 无阻塞开放问题。
