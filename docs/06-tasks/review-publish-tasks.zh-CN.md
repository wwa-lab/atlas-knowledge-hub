# 任务：Review Publish

## 状态

已实现。通过 `design-to-tasks` 推导，经接受后完成实现，验证证据见下方。

## 验证行

Phase 4 hardening：对所改层运行完整 unit + integration + E2E。同时运行 baseline checks：`git diff --check`、新增网络/依赖扫描、secret/private-path 扫描。

## 任务摘要

| Task | 标题 | Owner | 优先级 | 依赖 |
|---|---|---|---|---|
| T-REVIEW-PUBLISH-001 | Add publish-ready queue contract | backend | Must | None |
| T-REVIEW-PUBLISH-002 | Harden review transition tests | backend | Must | None |
| T-REVIEW-PUBLISH-003 | Implement publish eligibility service | backend | Must | T-REVIEW-PUBLISH-001 |
| T-REVIEW-PUBLISH-004 | Add Wiki publish endpoints | backend | Must | T-REVIEW-PUBLISH-003 |
| T-REVIEW-PUBLISH-005 | Add/adjust persistence for publish metadata | backend | Must | T-REVIEW-PUBLISH-003 |
| T-REVIEW-PUBLISH-006 | Wire frontend Processing Center publish states | frontend | Must | T-REVIEW-PUBLISH-001 |
| T-REVIEW-PUBLISH-007 | Wire frontend Wiki published metadata state | frontend | Must | T-REVIEW-PUBLISH-004 |
| T-REVIEW-PUBLISH-008 | Add E2E review-to-publish coverage | QA | Must | T-REVIEW-PUBLISH-004, T-REVIEW-PUBLISH-007 |
| T-REVIEW-PUBLISH-009 | Run security, adapter, and no-network gates | security | Must | T-REVIEW-PUBLISH-001..008 |

## 任务详情

### T-REVIEW-PUBLISH-001: Add publish-ready queue contract

- **映射到：** REQ-REVIEW-PUBLISH-001, REQ-REVIEW-PUBLISH-008；spec `Review Queue And Processing Center`。
- **范围：** 增加 `GET /api/spaces/{spaceId}/review-queues` 的 backend DTO/service/controller 行为，仅使用 metadata，并为每个 queue 返回安全 representative item metadata。
- **约束：** 无网络调用；无 direct adapter calls；保留 trace/confidence/review metadata。
- **验证：** `cd backend && mvn test -Dtest='ReviewPublishServiceTest,ReviewPublishApiContractIT'`

### T-REVIEW-PUBLISH-002: Harden review transition tests

- **映射到：** REQ-REVIEW-PUBLISH-002, REQ-REVIEW-PUBLISH-003；spec `SME Review State Machine`。
- **范围：** 确保 `APPROVE`、`NEED_FIX`、`OCR_REQUIRED` 映射正确；证明 review action 不设置 `PUBLISHED`；验证 append-only history。
- **约束：** 用户安全 errors；无 secret/path 泄露。
- **验证：** `cd backend && mvn test -Dtest='DomainInvariantTest,MetadataApiContractIT'`

### T-REVIEW-PUBLISH-003: Implement publish eligibility service

- **映射到：** REQ-REVIEW-PUBLISH-004, REQ-REVIEW-PUBLISH-006, REQ-REVIEW-PUBLISH-010；spec `Publish Eligibility`。
- **范围：** 增加 service logic，在 mutation 前校验 approved status、relative Markdown path、confidence、source document ids、source trace coverage 和 blocked statuses。
- **约束：** 仅 metadata；不直连 parser/converter/model/vector/storage/search。
- **验证：** `cd backend && mvn test -Dtest='ReviewPublishServiceTest'`

### T-REVIEW-PUBLISH-004: Add Wiki publish endpoints

- **映射到：** REQ-REVIEW-PUBLISH-005, REQ-REVIEW-PUBLISH-007；spec `API Behavior`。
- **范围：** 按 API guide 实现 `POST /api/files/{fileId}/publish`、`GET /api/spaces/{spaceId}/wiki-pages`、`GET /api/wiki-pages/{wikiPageId}`。
- **约束：** 使用 envelope；用户安全 errors；仅相对路径；secret masked/status-only。
- **验证：** `cd backend && mvn test -Dtest='ReviewPublishApiContractIT'`

### T-REVIEW-PUBLISH-005: Add/adjust persistence for publish metadata

- **映射到：** REQ-REVIEW-PUBLISH-005, REQ-REVIEW-PUBLISH-006；spec `Publish Result`。
- **范围：** 仅当现有 `wiki_page` metadata 不足时增加 repositories/mappers/migrations；否则在实现 notes 中记录无需 migration。
- **约束：** 不修改 raw parser output 或 source chunks；无真实数据。
- **验证：** `cd backend && mvn verify`

### T-REVIEW-PUBLISH-006: Wire frontend Processing Center publish states

- **映射到：** REQ-REVIEW-PUBLISH-001, REQ-REVIEW-PUBLISH-008, REQ-REVIEW-PUBLISH-009；spec `Review Queue And Processing Center`。
- **范围：** 从 typed mock/API-shaped data 渲染 blocked queues、ready-to-publish count 和 publish action states。
- **约束：** 除非使用已接受本地 API adapter，否则无外部网络调用；不声明 production RBAC。
- **验证：** `cd frontend && npm run test -- --run`

### T-REVIEW-PUBLISH-007: Wire frontend Wiki published metadata state

- **映射到：** REQ-REVIEW-PUBLISH-005, REQ-REVIEW-PUBLISH-008, REQ-REVIEW-PUBLISH-009；spec `Publish Result`。
- **范围：** 在 Wiki/Global Chat surfaces 展示 published status、source/confidence metadata 和 trust copy。
- **约束：** 保留 FE baseline；不实现 graph extraction 或 Ask generation。
- **验证：** `cd frontend && npm run typecheck && npm run build`

### T-REVIEW-PUBLISH-008: Add E2E review-to-publish coverage

- **映射到：** REQ-REVIEW-PUBLISH-011；spec `Acceptance Matrix`。
- **范围：** 通过 UI/API 可见行为覆盖 approve-to-publish happy path 和 blocked missing-trace path。
- **约束：** 仅 mock/sample data；无外部服务。
- **验证：** `cd frontend && npm run e2e`

### T-REVIEW-PUBLISH-009: Run security, adapter, and no-network gates

- **映射到：** REQ-REVIEW-PUBLISH-007, REQ-REVIEW-PUBLISH-010, REQ-REVIEW-PUBLISH-011；spec `Non-Functional Requirements`。
- **范围：** 确认 publish logic 无 direct engine calls、无新增外部依赖/网络调用、无 raw secrets/private paths/real data，并检查 diff hygiene。
- **约束：** Adapter boundary 和 secret masking 为强制。
- **验证：** `git diff --check`; `rg -n "https?://|fetch\\(|axios|XMLHttpRequest" backend/src frontend/src prototypes/index.html frontend/public/atlas-prototype.html`; `rg -n "(api[_-]?key|secret|password|token|/Users/|C:\\\\|BEGIN (RSA|OPENSSH|PRIVATE))" . -g '!frontend/node_modules/**' -g '!backend/target/**'`

## 依赖计划

关键路径：T-REVIEW-PUBLISH-001 -> T-REVIEW-PUBLISH-003 -> T-REVIEW-PUBLISH-004 -> T-REVIEW-PUBLISH-007 -> T-REVIEW-PUBLISH-008 -> T-REVIEW-PUBLISH-009。

T-REVIEW-PUBLISH-002 可与 T-REVIEW-PUBLISH-001 并行。T-REVIEW-PUBLISH-006 可在 queue DTO contract 稳定后开始。

## 完成定义

- 所有 Must tasks 完成。
- `cd backend && mvn verify` 通过。
- `cd frontend && npm run typecheck && npm run test -- --run && npm run build && npm run e2e` 通过。
- `git diff --check`、network/dependency scan 和 secret/private-path scan 通过。
- 任何 skipped check 都说明原因。

## 待确认问题

- 实现时已决策：duplicate publish 采用幂等行为，更新/返回已有 Wiki metadata。
- 实现时已决策：publish 只设置 `wiki_page.review_status=PUBLISHED`；`file_item` 和 `source_chunk` review metadata 保持不变。

## 实现证据

2026-07-03 实现验证后更新。

| Task | 状态 | Evidence |
|---|---|---|
| T-REVIEW-PUBLISH-001 | Complete | 已增加带安全 representative items 的 review queue DTO/service/controller 行为；`mvn test -Dtest='ReviewPublishServiceTest,ReviewPublishApiContractIT,DomainInvariantTest'` 通过。 |
| T-REVIEW-PUBLISH-002 | Complete | 已增加 review publish service tests，覆盖 review action mapping 与 publish separation；targeted backend tests 通过。 |
| T-REVIEW-PUBLISH-003 | Complete | 已增加 publish eligibility service，校验 approved status、safe relative Markdown path、confidence、source trace 和 blocked statuses。 |
| T-REVIEW-PUBLISH-004 | Complete | 已按 API guide 增加 publish 和 Wiki page endpoints。 |
| T-REVIEW-PUBLISH-005 | Complete | 复用现有 `wiki_page` table；必需字段已存在，因此无需 migration。 |
| T-REVIEW-PUBLISH-006 | Complete | 已增加 typed frontend review queue mock data 和 Processing Center publish state hooks。 |
| T-REVIEW-PUBLISH-007 | Complete | 已增加 published Wiki metadata mock data，并在 prototype 中显示 source/confidence/published status。 |
| T-REVIEW-PUBLISH-008 | Complete | 已增加 Playwright E2E，覆盖 ready-to-publish 和 missing-trace blocked states；`npm run e2e` 通过。 |
| T-REVIEW-PUBLISH-009 | Complete | `git diff --check`、network scan、secret/private-path scan、backend targeted tests、frontend typecheck、frontend unit tests、E2E、完整 `cd backend && mvn verify` 和 `cd frontend && npm run build` 均已通过；此前 ask/knowledge-graph 与 `App.vue` lint 阻塞已解决。 |
