# 溯源：Wiki 数据模型

## 状态

已由当前用户接受，并于 2026-07-05 完成实现。`wiki-data-model` 切片已作为 Wiki Foundation 数据模型底座完成。这不等于 Auto Wiki ingest 完成，也不代表生产就绪。

## 切片契约

- **Slice:** `wiki-data-model`
- **Goal:** 将 Atlas Wiki 从 published metadata API-backed surface 升级为 Auto Wiki-ready data model foundation。
- **Phase:** 4 hardening / Wiki Foundation。
- **Current maturity target:** Wiki Foundation data-model readiness。这不等于 Auto Wiki ingest 完成，也不等于生产就绪。

## 来源文档

| Source | Status |
|---|---|
| `README.md` | 已读。 |
| `PROJECT_RULES.md` | 已读。 |
| `DEVELOPMENT_STANDARDS.md` | 已读。 |
| `docs/00-context/sdd-profile.md` | 已读。 |
| `docs/01-requirements/requirement.md` | 已读。 |
| `docs/00-context/wiki-foundation-goal-plan.zh-CN.md` | 已读。 |
| `docs/00-context/execution-manifests/wiki-foundation-20260705.yaml` | 已读。 |
| `ROADMAP.zh-CN.md` | 已读。 |
| `docs/07-acceptance/product-acceptance-report.zh-CN.md` | 已读。 |
| `docs/07-acceptance/internal-beta-readiness.zh-CN.md` | 已读。 |
| `docs/03-spec/full-stack-productization-spec.zh-CN.md` | 已读。 |
| `docs/06-tasks/full-stack-productization-tasks.zh-CN.md` | 已读。 |
| `docs/00-context/full-stack-productization-traceability.zh-CN.md` | 已读。 |
| `WEKNORA_ANALYSIS_DIR` | 未读；环境变量未设置。 |

## 已 grounding 的现有实现

| Area | Grounding |
|---|---|
| Existing Wiki domain | `WikiPage` 存储 id、space id、title、Markdown path、source document ids、confidence、review status、owner、last updated。 |
| Existing Wiki API | `ReviewPublishController` 暴露 publish、list pages、get page by id。 |
| Existing publish behavior | `ReviewPublishService` 发布前验证 approved file、relative Markdown path、confidence、source chunks。 |
| Existing schema | `V1__init_schema.sql` 创建 `atlas.wiki_page`；V2 seed 一个 sample row。 |
| Existing frontend | `ApiWikiPage` 和 `App.vue` 映射 API Wiki pages，并保留 fallback sample pages。 |
| Existing E2E | Phase I4-I7 Playwright 覆盖 API-backed Wiki、graph、Ask 和 model metadata。 |

## Requirement To Story To Task Map

| Requirement | Stories | Tasks |
|---|---|---|
| REQ-WIKI-DATA-MODEL-001 | US-WIKI-DATA-MODEL-001 | T-WIKI-DATA-MODEL-001, T-WIKI-DATA-MODEL-011 |
| REQ-WIKI-DATA-MODEL-002 | US-WIKI-DATA-MODEL-002, US-WIKI-DATA-MODEL-003 | T-WIKI-DATA-MODEL-002, T-WIKI-DATA-MODEL-003, T-WIKI-DATA-MODEL-005 |
| REQ-WIKI-DATA-MODEL-003 | US-WIKI-DATA-MODEL-002 | T-WIKI-DATA-MODEL-003, T-WIKI-DATA-MODEL-006, T-WIKI-DATA-MODEL-007 |
| REQ-WIKI-DATA-MODEL-004 | US-WIKI-DATA-MODEL-004 | T-WIKI-DATA-MODEL-002, T-WIKI-DATA-MODEL-003, T-WIKI-DATA-MODEL-006 |
| REQ-WIKI-DATA-MODEL-005 | US-WIKI-DATA-MODEL-005 | T-WIKI-DATA-MODEL-002, T-WIKI-DATA-MODEL-003, T-WIKI-DATA-MODEL-006 |
| REQ-WIKI-DATA-MODEL-006 | US-WIKI-DATA-MODEL-005 | T-WIKI-DATA-MODEL-002, T-WIKI-DATA-MODEL-003, T-WIKI-DATA-MODEL-006 |
| REQ-WIKI-DATA-MODEL-007 | US-WIKI-DATA-MODEL-005 | T-WIKI-DATA-MODEL-002, T-WIKI-DATA-MODEL-003, T-WIKI-DATA-MODEL-006 |
| REQ-WIKI-DATA-MODEL-008 | US-WIKI-DATA-MODEL-005 | T-WIKI-DATA-MODEL-004, T-WIKI-DATA-MODEL-006, T-WIKI-DATA-MODEL-007 |
| REQ-WIKI-DATA-MODEL-009 | US-WIKI-DATA-MODEL-002 | T-WIKI-DATA-MODEL-005, T-WIKI-DATA-MODEL-007, T-WIKI-DATA-MODEL-009 |
| REQ-WIKI-DATA-MODEL-010 | US-WIKI-DATA-MODEL-006 | T-WIKI-DATA-MODEL-008, T-WIKI-DATA-MODEL-009 |
| REQ-WIKI-DATA-MODEL-011 | US-WIKI-DATA-MODEL-006 | T-WIKI-DATA-MODEL-008, T-WIKI-DATA-MODEL-009 |
| REQ-WIKI-DATA-MODEL-012 | US-WIKI-DATA-MODEL-003, US-WIKI-DATA-MODEL-006 | T-WIKI-DATA-MODEL-004, T-WIKI-DATA-MODEL-005, T-WIKI-DATA-MODEL-008 |
| REQ-WIKI-DATA-MODEL-013 | US-WIKI-DATA-MODEL-003, US-WIKI-DATA-MODEL-007 | T-WIKI-DATA-MODEL-002, T-WIKI-DATA-MODEL-005, T-WIKI-DATA-MODEL-007 |
| REQ-WIKI-DATA-MODEL-014 | US-WIKI-DATA-MODEL-007 | T-WIKI-DATA-MODEL-010 |
| REQ-WIKI-DATA-MODEL-015 | US-WIKI-DATA-MODEL-007 | T-WIKI-DATA-MODEL-007, T-WIKI-DATA-MODEL-009, T-WIKI-DATA-MODEL-010, T-WIKI-DATA-MODEL-011 |

## SDD Artifact Set

| Artifact | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/wiki-data-model-requirements.md` | `docs/01-requirements/wiki-data-model-requirements.zh-CN.md` |
| User stories | `docs/02-user-stories/wiki-data-model-stories.md` | `docs/02-user-stories/wiki-data-model-stories.zh-CN.md` |
| Spec | `docs/03-spec/wiki-data-model-spec.md` | `docs/03-spec/wiki-data-model-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/wiki-data-model-architecture.md` | `docs/04-architecture/wiki-data-model-architecture.zh-CN.md` |
| Data flow | `docs/04-architecture/wiki-data-model-data-flow.md` | `docs/04-architecture/wiki-data-model-data-flow.zh-CN.md` |
| Data model | `docs/04-architecture/wiki-data-model-data-model.md` | `docs/04-architecture/wiki-data-model-data-model.zh-CN.md` |
| Design | `docs/05-design/wiki-data-model-design.md` | `docs/05-design/wiki-data-model-design.zh-CN.md` |
| API guide | `docs/05-design/contracts/wiki-data-model-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/wiki-data-model-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/wiki-data-model-tasks.md` | `docs/06-tasks/wiki-data-model-tasks.zh-CN.md` |
| Traceability | `docs/00-context/wiki-data-model-traceability.md` | `docs/00-context/wiki-data-model-traceability.zh-CN.md` |

## 验证计划

- SDD gate: file existence、bilingual ID match、acceptance before code。
- Backend: `cd backend && mvn verify`。
- Frontend: `cd frontend && npm run typecheck && npm run test && npm run build`。
- E2E: `cd frontend && npm run e2e`。
- Full stack: `npm run e2e:second-layer`。
- Hygiene: `git diff --check`。
- Safety: focused secret/private-path scan 和 focused network/dependency scan。

## 实现完成证据

| Task | Status | Evidence |
|---|---|---|
| T-WIKI-DATA-MODEL-001 | Complete | 当前用户已在产品代码变更前明确接受 SDD。 |
| T-WIKI-DATA-MODEL-002 | Complete | 新增 `backend/src/main/resources/db/migration/V10__wiki_data_model.sql`；`cd backend && mvn verify` 已应用/校验 V1-V10 共 11 个 migrations。 |
| T-WIKI-DATA-MODEL-003 | Complete | 扩展 `WikiPage`；新增 `WikiFolder`、`WikiGenerationRun`、`WikiLogEntry`、`WikiPageIssue`、`WikiReference` 和对应 repositories。 |
| T-WIKI-DATA-MODEL-004 | Complete | 扩展 `WikiPageResponse`；新增 reference/folder/run/log/issue response DTOs 和 mapper 覆盖。 |
| T-WIKI-DATA-MODEL-005 | Complete | 现有 publish/list/id-detail APIs 仍由 `ReviewPublishController` 暴露；publish 默认填充 slug、refs、version、source mode、refresh policy。 |
| T-WIKI-DATA-MODEL-006 | Complete | 按接受的 API guide 新增 slug、folders、generation runs、logs、issues 读取 endpoints。 |
| T-WIKI-DATA-MODEL-007 | Complete | 在 `ReviewPublishServiceTest`、`ReviewPublishApiContractIT`、`RepositoryIT` 中增加 backend service/API/repository 断言。 |
| T-WIKI-DATA-MODEL-008 | Complete | Vue Wiki tab 展示 slug、page type、aliases、refs、link counts、version、source mode、refresh policy，并保留 sample fallback safety。 |
| T-WIKI-DATA-MODEL-009 | Complete | 更新 frontend unit 和 Playwright 覆盖，保护 API-backed Wiki metadata display 与现有 Graph/Ask 回归路径。 |
| T-WIKI-DATA-MODEL-010 | Complete | 完成完整 verification 和 focused safety scans。 |
| T-WIKI-DATA-MODEL-011 | Complete | 本 traceability 文件和 roadmap/progress 状态已更新完成证据。 |

## 验证结果

| Check | Result | Evidence |
|---|---|---|
| Backend verify | Passed | `cd backend && mvn verify` 通过：98 个 unit tests 和 39 个 integration tests 通过，Flyway 校验 V1-V10 共 11 个 migrations。 |
| Frontend typecheck/test/build | Passed | `cd frontend && npm run typecheck && npm run test && npm run build` 通过；18 个 Vitest tests 通过，production build 成功。 |
| Frontend E2E | Passed | `cd frontend && npm run e2e` 通过 13 个 Playwright tests。 |
| Second-layer E2E | Passed | `npm run e2e:second-layer` 通过 2 个本地全栈 Playwright tests。 |
| Diff hygiene | Passed | `git diff --check` 无发现。 |
| Secret/private-path scan | Passed | 对变更文本文件的 focused scan 未发现 raw secrets、credentials 或 private paths。 |
| Network/dependency scan | Passed with reviewed existing hits | Focused scan 只命中既有模型配置/mock URL 文案；diff scan 未发现新增 external URL、dependency declaration 或 network client。 |

## 残留风险

- `wiki-data-model` 仅提供 data-model/API/UI foundation；Auto Wiki ingest、linkify/lint、refresh/retract、生产 RBAC、connector sync、operations hardening 仍属于后续切片。
- 支撑记录是最小安全 metadata 和 sample-safe seed/test records；不代表 generation 或 lint workflow 已完成。
- `WEKNORA_ANALYSIS_DIR` 在 SDD 生成期间未设置，因此本切片未读取本地 WeKnora analysis files。

## SDD 质量门结果

记录本次用户接受前的 SDD-only pass：

| Gate | Result | Evidence |
|---|---|---|
| Expected artifacts exist | Passed | `wiki-data-model` 的 20 个双语 SDD 文件均存在。 |
| Bilingual ID parity | Passed | 英文和中文 companion 中的 `REQ`、`US`、`T`、`OQ` IDs 一致。 |
| Objective field coverage | Passed | 目标字段 `slug`、`page_type`、`aliases`、`source_refs`、`chunk_refs`、`in_links`、`out_links`、`version`、`source_mode`、`refresh_policy` 均已体现在 SDD 集中。 |
| Objective table coverage | Passed | `wiki_folder`、`wiki_generation_run`、`wiki_log_entry`、`wiki_page_issue` 已体现在 requirements、spec/design、data model、API guide 和 tasks 中。 |
| Deferred-decision scan | Passed | 生成的 SDD 文件中未发现 `TBD`、`TODO`、`FIXME`、`implementation will decide` 或 `grep later`。 |
| Diff hygiene | Passed | `git diff --check` 未报告 whitespace errors。 |
| New-doc whitespace | Passed | 对新 SDD 文件的 focused trailing-whitespace scan 在清理后无发现。 |
| Secret/private-path scan | Passed with expected policy-text hits | 命中仅为 "do not store secrets" 等护栏文字；未发现 raw secret、private path 或 credential value。 |
| Network/dependency scan | Passed with expected policy-text hits | 命中仅为排除/护栏文字；未引入新依赖或 external call 指令。 |
| Product-code gate | Passed | 本次 SDD pass 未修改 backend 或 frontend 产品代码。 |

## 用户接受审阅清单

实现开始前，可用此清单接受 SDD 或要求修订。

| Review item | Acceptance signal |
|---|---|
| Scope | SDD 仅覆盖 Wiki Foundation data model 工作，未声称 Auto Wiki ingest、linkify/lint、生产 RBAC 或 provider integration 已完成。 |
| Data model | `wiki_page` 扩展和四个支撑表符合第一切片 foundation 预期。 |
| API surface | 现有 publish/list/id-detail APIs 保持兼容，新增 slug/folder/run/log/issue read endpoints 可接受。 |
| Frontend behavior | Vue Wiki metadata 展示和 fallback sample safety 足以覆盖本切片。 |
| Verification | 任务清单包含 backend、frontend、E2E、second-layer、diff、secret/path、network/dependency checks。 |
| Open questions | OQ-WIKI-DATA-MODEL-001 到 OQ-WIKI-DATA-MODEL-003 可以延后到后续切片。 |
| Implementation gate | 接受后，Codex 可按顺序执行 `T-WIKI-DATA-MODEL-001` 到 `T-WIKI-DATA-MODEL-011`。 |

## 待确认问题

- OQ-WIKI-DATA-MODEL-001: 未来 generated-page initial status。
- OQ-WIKI-DATA-MODEL-002: 未来 checksum/freshness metadata。
- OQ-WIKI-DATA-MODEL-003: 未来 folder ordering behavior。

## SDD 质量说明

- 已应用技能链：`atlas-sdd-generate-all`、`req-to-user-story`、`user-story-to-spec`、`spec-to-architecture`、`architecture-to-design`、`design-to-tasks` 和 `review-doc-quality` checklist。
- SDD 中已处理 architecture review 关注点：adapter boundaries、additive Flyway migration、DTO/API compatibility、safe metadata only。
- 本 SDD pass 未修改产品代码。
