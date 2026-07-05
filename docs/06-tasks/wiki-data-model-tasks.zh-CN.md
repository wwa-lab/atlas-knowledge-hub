# 任务：Wiki 数据模型

## 状态

已由当前用户接受，并于 2026-07-05 完成实现。本任务集对应的 `wiki-data-model` 切片已完成。

## 概述

将 `wiki-data-model` 切片实现为 additive Wiki Foundation 数据模型升级。交付目标是为未来 Auto Wiki 工作提供兼容的后端/前端 metadata foundation，不是 ingest pipeline 或生产就绪 Wiki 系统。

## 来源设计

- Spec: `docs/03-spec/wiki-data-model-spec.zh-CN.md`
- Architecture: `docs/04-architecture/wiki-data-model-architecture.zh-CN.md`
- Data flow: `docs/04-architecture/wiki-data-model-data-flow.zh-CN.md`
- Data model: `docs/04-architecture/wiki-data-model-data-model.zh-CN.md`
- Design: `docs/05-design/wiki-data-model-design.zh-CN.md`
- API guide: `docs/05-design/contracts/wiki-data-model-API_IMPLEMENTATION_GUIDE.zh-CN.md`

## 工作流

| Workstream | Tasks | Notes |
|---|---|---|
| SDD acceptance | T-WIKI-DATA-MODEL-001 | 编码前人工门。 |
| Persistence/domain/API | T-WIKI-DATA-MODEL-002 through T-WIKI-DATA-MODEL-007 | Additive backend foundation。 |
| Frontend | T-WIKI-DATA-MODEL-008 | Metadata rendering 和 fallback safety。 |
| Regression and safety | T-WIKI-DATA-MODEL-009 through T-WIKI-DATA-MODEL-011 | Verification 和 close-out evidence。 |

## 任务摘要

| Task | Title | Owner | Priority | Depends On |
|---|---|---|---|---|
| T-WIKI-DATA-MODEL-001 | 获取 SDD 接受门 | product/docs | Must | None |
| T-WIKI-DATA-MODEL-002 | 为 Wiki Foundation model 添加 Flyway migration | backend | Must | T-WIKI-DATA-MODEL-001 |
| T-WIKI-DATA-MODEL-003 | 扩展 Wiki domain entities 和 repositories | backend | Must | T-WIKI-DATA-MODEL-002 |
| T-WIKI-DATA-MODEL-004 | 扩展 Wiki metadata DTOs 和 mapping | backend | Must | T-WIKI-DATA-MODEL-003 |
| T-WIKI-DATA-MODEL-005 | 保留 publish/list/id-detail 兼容性 | backend | Must | T-WIKI-DATA-MODEL-004 |
| T-WIKI-DATA-MODEL-006 | 新增 slug、folder、run、log、issue 读取 APIs | backend | Must | T-WIKI-DATA-MODEL-004 |
| T-WIKI-DATA-MODEL-007 | 增加 backend contract 和 repository 覆盖 | backend / QA | Must | T-WIKI-DATA-MODEL-005, T-WIKI-DATA-MODEL-006 |
| T-WIKI-DATA-MODEL-008 | 在 Vue 中渲染新增 Wiki metadata | frontend | Must | T-WIKI-DATA-MODEL-004 |
| T-WIKI-DATA-MODEL-009 | 增加 frontend unit 和 E2E regression 覆盖 | frontend / QA | Must | T-WIKI-DATA-MODEL-008 |
| T-WIKI-DATA-MODEL-010 | 运行完整 verification gates 和 safety scans | QA/security | Must | T-WIKI-DATA-MODEL-007, T-WIKI-DATA-MODEL-009 |
| T-WIKI-DATA-MODEL-011 | 更新 traceability 和 completion evidence | docs | Must | T-WIKI-DATA-MODEL-010 |

## 任务详情

### T-WIKI-DATA-MODEL-001: 获取 SDD 接受门

- **Maps to:** REQ-WIKI-DATA-MODEL-001；spec S1。
- **Objective:** 在产品代码变更前确认用户接受 SDD 范围和实现任务。
- **Scope:** 提交本 SDD 集供审阅。编码前应用用户要求的 SDD 修订。
- **Dependencies:** None。
- **Owner type:** product/docs。
- **Priority:** Must。
- **Verification:** 确认所有预期双语文件存在且 IDs 匹配。

### T-WIKI-DATA-MODEL-002: 为 Wiki Foundation model 添加 Flyway migration

- **Maps to:** REQ-WIKI-DATA-MODEL-002, 004, 005, 006, 007, 013。
- **Objective:** 在 V9 后添加 additive migration，包含新的 `wiki_page` columns 和支撑表。
- **Scope:** 除非实现时已有更新 migration 号，否则创建预期的 `V10__wiki_data_model.sql`。添加 data model 中的安全 defaults、checks、indexes 和 constraints。保留 V1-V9 seeded data。
- **Dependencies:** T-WIKI-DATA-MODEL-001。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** `cd backend && mvn verify`。

### T-WIKI-DATA-MODEL-003: 扩展 Wiki domain entities 和 repositories

- **Maps to:** REQ-WIKI-DATA-MODEL-002, 003, 004, 005, 006, 007。
- **Objective:** 在后端 domain/repository 层表达新增 Wiki Foundation 字段和表。
- **Scope:** 扩展 `WikiPage`；新增 `WikiFolder`、`WikiGenerationRun`、`WikiLogEntry`、`WikiPageIssue`；增加 list/detail/slug/folder/run/log/issue 读取 repository methods。
- **Dependencies:** T-WIKI-DATA-MODEL-002。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** Backend unit/repository tests 和 `cd backend && mvn verify`。

### T-WIKI-DATA-MODEL-004: 扩展 Wiki metadata DTOs 和 mapping

- **Maps to:** REQ-WIKI-DATA-MODEL-008, 012。
- **Objective:** 为 page refs 和支撑 records 返回安全 typed response DTOs。
- **Scope:** 扩展 `WikiPageResponse`；增加 reference/folder/run/log/issue responses；确保 arrays/lists 默认 empty collections。
- **Dependencies:** T-WIKI-DATA-MODEL-003。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** Backend contract tests 断言 response shapes 且无 raw unsafe content。

### T-WIKI-DATA-MODEL-005: 保留 publish/list/id-detail 兼容性

- **Maps to:** REQ-WIKI-DATA-MODEL-002, 009, 012, 013。
- **Objective:** 确保现有 review-publish 行为继续工作，同时返回 extended page metadata。
- **Scope:** 更新 publish defaults、list pages 和 get by id，不改变 eligibility rules 或现有 trusted read behavior。
- **Dependencies:** T-WIKI-DATA-MODEL-004。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** `cd backend && mvn test -Dtest='ReviewPublishServiceTest,ReviewPublishApiContractIT'`。

### T-WIKI-DATA-MODEL-006: 新增 slug、folder、run、log、issue 读取 APIs

- **Maps to:** REQ-WIKI-DATA-MODEL-003, 004, 005, 006, 007, 008。
- **Objective:** 暴露新的最小读取契约。
- **Scope:** 根据 API guide 实现 endpoints，使用 `ApiEnvelope` 和 safe errors。
- **Dependencies:** T-WIKI-DATA-MODEL-004。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** Backend API contract tests 覆盖 slug lookup、folders、runs、logs、issues。

### T-WIKI-DATA-MODEL-007: 增加 backend contract 和 repository 覆盖

- **Maps to:** REQ-WIKI-DATA-MODEL-013, 015。
- **Objective:** 证明 migration 兼容性和 API contracts。
- **Scope:** 增加测试覆盖 Flyway application、legacy rows/defaults、space-scoped slug lookup、support reads 和 safe errors。
- **Dependencies:** T-WIKI-DATA-MODEL-005, T-WIKI-DATA-MODEL-006。
- **Owner type:** backend / QA。
- **Priority:** Must。
- **Verification:** `cd backend && mvn verify`。

### T-WIKI-DATA-MODEL-008: 在 Vue 中渲染新增 Wiki metadata

- **Maps to:** REQ-WIKI-DATA-MODEL-010, 011, 012。
- **Objective:** 在真实 Vue Wiki tab 展示新增 page metadata，并不破坏 fallback sample safety。
- **Scope:** 更新 frontend types/API mapping 和 Wiki tab rendering，展示 slug、page type、aliases、refs、link counts/ids、version、source mode、refresh policy。
- **Dependencies:** T-WIKI-DATA-MODEL-004。
- **Owner type:** frontend。
- **Priority:** Must。
- **Verification:** `cd frontend && npm run typecheck && npm run test`。

### T-WIKI-DATA-MODEL-009: 增加 frontend unit 和 E2E regression 覆盖

- **Maps to:** REQ-WIKI-DATA-MODEL-010, 011, 015。
- **Objective:** 保护 visible metadata 和现有关键流程。
- **Scope:** 新增/更新 unit tests 和 Playwright tests，覆盖 API-backed Wiki metadata display、fallback sample safety、review-publish、graph、Ask regressions。
- **Dependencies:** T-WIKI-DATA-MODEL-008。
- **Owner type:** frontend / QA。
- **Priority:** Must。
- **Verification:** `cd frontend && npm run typecheck && npm run test && npm run build && npm run e2e`。

### T-WIKI-DATA-MODEL-010: 运行完整 verification gates 和 safety scans

- **Maps to:** REQ-WIKI-DATA-MODEL-014, 015。
- **Objective:** 证明切片不回退当前 Wiki/Graph/Ask 行为或数据安全。
- **Scope:** 运行必需命令和 focused scans。
- **Dependencies:** T-WIKI-DATA-MODEL-007, T-WIKI-DATA-MODEL-009。
- **Owner type:** QA/security。
- **Priority:** Must。
- **Verification:**
  - `cd backend && mvn verify`
  - `cd frontend && npm run typecheck && npm run test && npm run build`
  - `cd frontend && npm run e2e`
  - `npm run e2e:second-layer`
  - `git diff --check`
  - focused secret/private-path scan
  - focused network/dependency scan

### T-WIKI-DATA-MODEL-011: 更新 traceability 和 completion evidence

- **Maps to:** REQ-WIKI-DATA-MODEL-001, 015。
- **Objective:** 记录最终实现状态、verification evidence、skipped checks 和 residual risks。
- **Scope:** 更新 `docs/00-context/wiki-data-model-traceability.md` 和 `.zh-CN.md`；仅当实现期间 product status 变化时更新 roadmap/status files。
- **Dependencies:** T-WIKI-DATA-MODEL-010。
- **Owner type:** docs。
- **Priority:** Must。
- **Verification:** Final report 列出 docs changed、code changed、verification run、skipped checks 和 maturity statement。

## 依赖计划

Critical path: T-WIKI-DATA-MODEL-001 -> T-WIKI-DATA-MODEL-002 -> T-WIKI-DATA-MODEL-003 -> T-WIKI-DATA-MODEL-004 -> T-WIKI-DATA-MODEL-005/T-WIKI-DATA-MODEL-006 -> T-WIKI-DATA-MODEL-007 -> T-WIKI-DATA-MODEL-008 -> T-WIKI-DATA-MODEL-009 -> T-WIKI-DATA-MODEL-010 -> T-WIKI-DATA-MODEL-011。

T-WIKI-DATA-MODEL-008 可在 DTO shape 稳定后开始，但最终 frontend verification 依赖 backend contract 完成。

## 风险 / 阻塞

- 代码变更前需要用户接受。
- Flyway migration 必须处理 slug backfill，且不得破坏现有 seeded rows。
- 支撑表可能让人误解 workflows 已完成；UI/docs 必须标注 foundation metadata only。
- Docker/PostgreSQL 可用性可能阻塞 second-layer verification。

## 待确认问题

- OQ-WIKI-DATA-MODEL-001: 未来 generated-page initial status。
- OQ-WIKI-DATA-MODEL-002: 未来 checksum/freshness metadata。
- OQ-WIKI-DATA-MODEL-003: 未来 folder ordering behavior。

## Definition Of Done

- SDD 被用户接受。
- 所有 Must tasks 完成。
- 必需 verification 已运行，或明确报告 skipped reason。
- 最终响应声明：Wiki Foundation data model 已完成；Auto Wiki ingest 未完成；不声明生产就绪。
