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

## Product Goal Batch 5 任务扩展

### T-FSP-011：将 Phase I1-I3 Vue 界面切换到既有 Atlas API

- Requirement: REQ-FSP-001, REQ-FSP-002, REQ-FSP-003, REQ-FSP-004, REQ-FSP-005, REQ-FSP-011, REQ-FSP-012
- Owner type: frontend / QA
- Priority: Product Goal Batch 5 必须完成
- Dependencies: T-FSP-002 through T-FSP-010
- Scope: 在真实 Vue 产品路径中，使用既有 Atlas API helpers 渲染 API-backed Knowledge Space metadata、batch/file/chunk metadata 与 review queue metadata。API 不可用时仅保留安全、确定的 sample fallback。不新增生产上传、auth/RBAC、真实数据、provider 调用或新的后端契约。
- Verification:
  - `cd frontend && npm run typecheck && npm run test && npm run build`
  - `cd frontend && npx playwright test tests/e2e/phase-i1-i3-api-backed-metadata.spec.ts --project=chromium`
  - `cd frontend && npx playwright test tests/e2e/phase-a-b-product-shell.spec.ts tests/e2e/phase-c-d-upload-processing.spec.ts tests/e2e/phase-e-f-g-knowledge-surfaces.spec.ts tests/e2e/phase-h-settings-administration.spec.ts tests/e2e/phase-i1-i3-api-backed-metadata.spec.ts --project=chromium`
  - `cd backend && mvn verify`
  - `git diff --check`
  - focused secret/private-path scan
  - focused network/dependency scan
- Status: 2026-07-05 已完成 L3 API-backed 验收准备证据；最终产品验收仍待用户决定。

## Core Knowledge Loop v1 任务补充

### T-FSP-013：更新用户可运行闭环的 SDD 契约

- Requirement：REQ-FSP-013 through REQ-FSP-020
- Owner type：product / architecture
- Priority：Must
- Dependencies：T-FSP-012
- Scope：在代码变更前记录 v1 范围、disabled capabilities、API addendum、traceability 与 verification plan。
- Verification：Spec、API guide、task list、traceability 都引用相同的 v1 requirement IDs。

### T-FSP-014：实现运行时 DeepSeek model configuration API

- Requirement：REQ-FSP-013, REQ-FSP-020
- Owner type：backend
- Priority：Must
- Dependencies：T-FSP-013
- Scope：新增 backend-held DeepSeek configuration 的 masked read、save、clear endpoints。Configured adapter 必须先使用 runtime configuration，再 fallback 到 process environment。
- Verification：Backend tests 覆盖 masked response、不返回 raw key、clear behavior 与 adapter capability state。

### T-FSP-015：实现真实 PDF 与 ZIP-of-PDF ingestion API

- Requirement：REQ-FSP-014, REQ-FSP-020
- Owner type：backend
- Priority：Must
- Dependencies：T-FSP-013
- Scope：新增按 space scoped 的 multipart upload endpoint。将 PDF bytes 存储到 local artifact area，安全展开 ZIP，安全拒绝 unsupported types，创建 batch/file metadata，并返回 created parser run。
- Verification：Backend integration/unit tests 覆盖 PDF upload、ZIP upload、unsupported file handling、path traversal rejection、metadata response。

### T-FSP-016：实现 local PDF text parser adapter

- Requirement：REQ-FSP-015, REQ-FSP-020
- Owner type：backend
- Priority：Must
- Dependencies：T-FSP-015
- Scope：通过 adapter-backed PDF text extraction 生成 Markdown 与 review-required chunks，保留 source trace。Office/OCR 路径保持 disabled，除非显式配置。
- Verification：Backend tests 证明 chunks 通过 adapter boundary 生成并保留 source trace。

### T-FSP-017：将 file review updates 传播到 chunks

- Requirement：REQ-FSP-016, REQ-FSP-020
- Owner type：backend
- Priority：Must
- Dependencies：T-FSP-013
- Scope：当 review action 指定 affected chunks，只更新属于该 file 的 chunks；当未指定 chunks，更新该 file 的全部 chunks。跨 file chunk IDs 必须被拒绝。
- Verification：Backend tests 覆盖 selected chunk update、all-chunk fallback、cross-file rejection。

### T-FSP-018：实现 backend downstream refresh orchestration

- Requirement：REQ-FSP-017, REQ-FSP-020
- Owner type：backend
- Priority：Must
- Dependencies：T-FSP-016, T-FSP-017
- Scope：新增 backend endpoint，用于刷新 selected scope 内 approved/published chunks 的 graph projection 与 vector index。前端调用该 endpoint，而不是直连 vector/parser/model engines。
- Verification：Backend tests 覆盖 refresh response 与 no eligible evidence state。

### T-FSP-019：将 Vue 产品路径切到真实上传/配置并灰掉未实现功能

- Requirement：REQ-FSP-013, REQ-FSP-014, REQ-FSP-017, REQ-FSP-018, REQ-FSP-019
- Owner type：frontend
- Priority：Must
- Dependencies：T-FSP-014 through T-FSP-018
- Scope：把 sample-only primary action 替换为 file upload；model settings save/clear 接入 API；downstream refresh 调后端；unsupported Office/OCR/RBAC/vector-store/admin controls disabled。
- Verification：Frontend unit/E2E checks 覆盖 upload controls、disabled states，以及可行范围内的 closed-loop happy path。

### T-FSP-020：运行闭环验证与安全门

- Requirement：REQ-FSP-020
- Owner type：QA/security
- Priority：Must
- Dependencies：T-FSP-014 through T-FSP-019
- Scope：运行 backend tests、frontend typecheck/tests/build、targeted E2E 或 manual closed-loop evidence、`git diff --check`、focused secret scan、dependency/network scan。
- Verification：最终报告记录 passed checks、skipped checks with reasons 与 residual L5 production gaps。

### T-FSP-012：将 Phase I4-I7 Vue 知识界面切换到既有 Atlas API

- Requirement: REQ-FSP-006, REQ-FSP-007, REQ-FSP-008, REQ-FSP-009, REQ-FSP-010, REQ-FSP-011, REQ-FSP-012
- Owner type: frontend / QA
- Priority: Product Goal Batch 6 必须完成
- Dependencies: T-FSP-011
- Scope: 在真实 Vue 产品路径中，使用既有 Atlas API helpers 渲染 API-backed Wiki pages、graph evidence、Ask run citations 与 masked model adapter capability metadata。必要时在产品路径增加 approve/publish/Ask 控件，避免依赖 workbench。不新增生产 provider 调用、明文 secret、真实数据、auth/RBAC 或新的后端契约。
- Verification:
  - `cd frontend && npm run typecheck && npm run test && npm run build`
  - `cd frontend && npx playwright test tests/e2e/phase-i4-i7-api-backed-knowledge-surfaces.spec.ts --project=chromium`
  - `cd frontend && npx playwright test tests/e2e/phase-a-b-product-shell.spec.ts tests/e2e/phase-c-d-upload-processing.spec.ts tests/e2e/phase-e-f-g-knowledge-surfaces.spec.ts tests/e2e/phase-h-settings-administration.spec.ts tests/e2e/phase-i1-i3-api-backed-metadata.spec.ts tests/e2e/phase-i4-i7-api-backed-knowledge-surfaces.spec.ts --project=chromium`
  - `cd backend && mvn verify`
  - `git diff --check`
  - focused secret/private-path scan
  - focused network/dependency scan
- Status: 2026-07-05 已完成 L3 API-backed 验收准备证据；最终产品验收仍待用户决定。
