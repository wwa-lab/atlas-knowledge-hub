# 任务：Wiki Ingest v0

## 状态

Implementation 已完成验证。Auto Wiki ingest v0 的所有 Must tasks 已完成；生成 candidates 仍保持 review-required，本切片不代表 linkify/lint、review gate、connector、model-assisted generation 或 production readiness 完成。

## 概述

将 Auto Wiki ingest v0 实现为从 approved source chunks 生成 review-required candidates 的流程。交付目标是安全、幂等的 Wiki ingest foundation，而不是 linkify/lint、review gate、connector 或 production readiness。

## Source Design

- Spec: `docs/03-spec/wiki-ingest-v0-spec.md`
- Architecture: `docs/04-architecture/wiki-ingest-v0-architecture.md`
- Data flow: `docs/04-architecture/wiki-ingest-v0-data-flow.md`
- Data model: `docs/04-architecture/wiki-ingest-v0-data-model.md`
- Design: `docs/05-design/wiki-ingest-v0-design.md`
- API guide: `docs/05-design/contracts/wiki-ingest-v0-API_IMPLEMENTATION_GUIDE.md`

## Workstreams

| Workstream | Tasks | Notes |
|---|---|---|
| SDD acceptance | T-WIKI-INGEST-V0-001 | 代码前的人工门。 |
| Backend ingest API/service | T-WIKI-INGEST-V0-002 through T-WIKI-INGEST-V0-006 | Candidate generation、merge、run/log/issue evidence。 |
| Frontend status | T-WIKI-INGEST-V0-007 | 在显式请求时展示 generated/review-required candidates。 |
| Verification and closeout | T-WIKI-INGEST-V0-008 through T-WIKI-INGEST-V0-010 | Tests、scans、traceability。 |

## Task Summary

| Task | Title | Owner | Priority | Depends On |
|---|---|---|---|---|
| T-WIKI-INGEST-V0-001 | 获得 SDD acceptance gate | product/docs | Must | None |
| T-WIKI-INGEST-V0-002 | 添加 Wiki ingest API contract 和 DTOs | backend | Must | T-WIKI-INGEST-V0-001 |
| T-WIKI-INGEST-V0-003 | 实现 approved chunk input selection | backend | Must | T-WIKI-INGEST-V0-002 |
| T-WIKI-INGEST-V0-004 | 实现 deterministic candidate builder | backend | Must | T-WIKI-INGEST-V0-003 |
| T-WIKI-INGEST-V0-005 | 实现 slug merge 与 trusted collision policy | backend | Must | T-WIKI-INGEST-V0-004 |
| T-WIKI-INGEST-V0-006 | 持久化 safe run、log 和 issue evidence | backend | Must | T-WIKI-INGEST-V0-005 |
| T-WIKI-INGEST-V0-007 | 在 Vue 中展示 generated review-required status | frontend | Should | T-WIKI-INGEST-V0-002, T-WIKI-INGEST-V0-006 |
| T-WIKI-INGEST-V0-008 | 添加 backend 和 frontend verification coverage | backend/frontend/QA | Must | T-WIKI-INGEST-V0-006, T-WIKI-INGEST-V0-007 |
| T-WIKI-INGEST-V0-009 | 运行完整 verification gates 和 safety scans | QA/security | Must | T-WIKI-INGEST-V0-008 |
| T-WIKI-INGEST-V0-010 | 更新 traceability 和 completion evidence | docs | Must | T-WIKI-INGEST-V0-009 |

## Completion Status

| Task | Status | Evidence |
|---|---|---|
| T-WIKI-INGEST-V0-001 | Complete | 用户已在实现前接受 SDD gate。 |
| T-WIKI-INGEST-V0-002 | Complete | 已新增 DTOs 和 `WikiIngestController`；API contract tests 通过。 |
| T-WIKI-INGEST-V0-003 | Complete | `WikiIngestService` 按 space、approval、traceability 和 safe statuses 过滤输入。 |
| T-WIKI-INGEST-V0-004 | Complete | Deterministic candidate builder 写入安全 Markdown artifacts，并拒绝 `model-assisted`。 |
| T-WIKI-INGEST-V0-005 | Complete | Service tests 覆盖 generated slug merge、space-distinct generated page IDs 与 trusted collision safe issue policy。 |
| T-WIKI-INGEST-V0-006 | Complete | Run/log/issue evidence 已持久化，并通过 safe DTOs 返回。 |
| T-WIKI-INGEST-V0-007 | Complete | Vue 请求 `includeDrafts=true` 并展示 generated review-required fields。 |
| T-WIKI-INGEST-V0-008 | Complete | Backend unit/API tests 与 frontend type/unit/build/E2E checks 通过。 |
| T-WIKI-INGEST-V0-009 | Complete | 完整 verification gates 与 focused scans 已通过。 |
| T-WIKI-INGEST-V0-010 | Complete | Traceability、roadmap 与 task evidence 已更新。 |

## Task Details

### T-WIKI-INGEST-V0-001: 获得 SDD acceptance gate

- **Maps to:** REQ-WIKI-INGEST-V0-001; spec S1。
- **Objective:** 确认用户在产品代码变更前接受本 SDD。
- **Scope:** 呈现完整双语 SDD set，应用用户要求的 SDD revisions，并记录 acceptance。
- **Dependencies:** None。
- **Owner type:** product/docs。
- **Priority:** Must。
- **Verification:** 确认所有预期双语文件存在且 IDs 匹配。

### T-WIKI-INGEST-V0-002: 添加 Wiki ingest API contract 和 DTOs

- **Maps to:** REQ-WIKI-INGEST-V0-010。
- **Objective:** 使用 Atlas envelope conventions 添加 start/read run contracts。
- **Scope:** 为 `POST /api/spaces/{spaceId}/wiki-ingest-runs` 和 run detail read 添加 request/response records。保留现有 published Wiki endpoints。
- **Dependencies:** T-WIKI-INGEST-V0-001。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** Backend API contract tests 编译并断言 safe response shape。

### T-WIKI-INGEST-V0-003: 实现 approved chunk input selection

- **Maps to:** REQ-WIKI-INGEST-V0-002, 013。
- **Objective:** 只选择 requested space 内 approved、traceable chunks。
- **Scope:** 通过 repositories/services 加载 chunks，按 space scope，排除 non-approved 或 untraced evidence，并记录 counts。
- **Dependencies:** T-WIKI-INGEST-V0-002。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** Service tests 覆盖 approved、review-required、need-fix、OCR-required、failed、unsupported、missing-trace 和 cross-space cases。

### T-WIKI-INGEST-V0-004: 实现 deterministic candidate builder

- **Maps to:** REQ-WIKI-INGEST-V0-003, 004, 007, 012。
- **Objective:** 在不直接外部调用的前提下构建安全 review-required candidates。
- **Scope:** 派生 title、slug、refs、`TOPIC` page type、source mode、refresh policy、review status、confidence 和 generated Markdown artifact path。在 v0 中拒绝或禁用 model-assisted mode。
- **Dependencies:** T-WIKI-INGEST-V0-003。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** Unit tests 断言 fields、minimum-confidence aggregation、generated Markdown artifact creation、no direct provider call、model-assisted rejection/disablement 和 `REVIEW_REQUIRED` default。

### T-WIKI-INGEST-V0-005: 实现 slug merge 与 trusted collision policy

- **Maps to:** REQ-WIKI-INGEST-V0-005, 006。
- **Objective:** 让 reruns 幂等，并保留 trusted published pages。
- **Scope:** 按 `(spaceId, slug)` 合并 generated review-required candidates；对 trusted collisions 记录 safe conflict issue。
- **Dependencies:** T-WIKI-INGEST-V0-004。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** Tests 证明 repeated run 不增加 page count，且 trusted `PUBLISHED_FILE` pages 不变。

### T-WIKI-INGEST-V0-006: 持久化 safe run、log 和 issue evidence

- **Maps to:** REQ-WIKI-INGEST-V0-008, 009。
- **Objective:** 记录可检查的 run lifecycle evidence。
- **Scope:** 使用 `wiki_generation_run`、`wiki_log_entry` 和 `wiki_page_issue` 记录 run status、safe summary、created/updated pages、issues 与 failures。
- **Dependencies:** T-WIKI-INGEST-V0-005。
- **Owner type:** backend。
- **Priority:** Must。
- **Verification:** Repository/service tests 断言 safe metadata，且无 raw source text、prompt、provider payload、secret、private path 或 stack trace。

### T-WIKI-INGEST-V0-007: 在 Vue 中展示 generated review-required status

- **Maps to:** REQ-WIKI-INGEST-V0-011。
- **Objective:** 当 generated candidates 暴露在产品界面时，明确展示 draft/review-required。
- **Scope:** 为 `includeDrafts=true` generated candidates 新增或更新 Vue API mapping 与 UI labels。保留 existing published Wiki default 和 fallback behavior。
- **Dependencies:** T-WIKI-INGEST-V0-002, T-WIKI-INGEST-V0-006。
- **Owner type:** frontend。
- **Priority:** Should。
- **Verification:** `cd frontend && npm run typecheck && npm run test`；如 UI changes 可见，则跑 focused E2E。

### T-WIKI-INGEST-V0-008: 添加 backend 和 frontend verification coverage

- **Maps to:** REQ-WIKI-INGEST-V0-014。
- **Objective:** 保护行为和回归。
- **Scope:** 添加 backend unit/integration/API tests；如触碰 UI，添加 frontend tests；保留现有 Wiki/Graph/Ask flows。
- **Dependencies:** T-WIKI-INGEST-V0-006, T-WIKI-INGEST-V0-007。
- **Owner type:** backend/frontend/QA。
- **Priority:** Must。
- **Verification:** `cd backend && mvn verify`；`cd frontend && npm run typecheck && npm run test && npm run build`。

### T-WIKI-INGEST-V0-009: 运行完整 verification gates 和 safety scans

- **Maps to:** REQ-WIKI-INGEST-V0-012, 013, 014。
- **Objective:** 证明该 slice 遵守 quality、adapter 与 data-safety gates。
- **Scope:** 运行必需命令，或报告 skipped checks with reasons。
- **Dependencies:** T-WIKI-INGEST-V0-008。
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

### T-WIKI-INGEST-V0-010: 更新 traceability 和 completion evidence

- **Maps to:** REQ-WIKI-INGEST-V0-001, 014。
- **Objective:** 记录最终实现状态和成熟度。
- **Scope:** 仅当 implementation status 变化时，更新 traceability、verification evidence、residual risks 和 roadmap/progress status。
- **Dependencies:** T-WIKI-INGEST-V0-009。
- **Owner type:** docs。
- **Priority:** Must。
- **Verification:** Final report 列出 docs changed、code changed、verification run、skipped checks、residual risks、SDD skill chain used 和 maturity statement。

## Dependency Plan

Critical path: T-WIKI-INGEST-V0-001 -> T-WIKI-INGEST-V0-002 -> T-WIKI-INGEST-V0-003 -> T-WIKI-INGEST-V0-004 -> T-WIKI-INGEST-V0-005 -> T-WIKI-INGEST-V0-006 -> T-WIKI-INGEST-V0-008 -> T-WIKI-INGEST-V0-009 -> T-WIKI-INGEST-V0-010。

T-WIKI-INGEST-V0-007 可在 backend response shapes 稳定后执行。

## Risks / Blockers

- Deterministic candidates 刻意保持轻量，因此必须保留 review-required labeling。
- Trusted slug collisions 在本切片中通过现有 safe issue taxonomy 记录。
- 当前 worktree 已包含之前 `wiki-data-model` changes；本次 implementation 未回滚或重排这些改动。

## Definition Of Done

- SDD 被用户接受。
- 所有 Must tasks 完成。
- Verification 已运行，或明确报告 skipped checks。
- Final response 明确：Auto Wiki ingest v0 completed；linkify/lint、review gate、connector 和 production readiness 未完成。
