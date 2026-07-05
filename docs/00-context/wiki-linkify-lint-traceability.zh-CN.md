# 溯源：Wiki 自动互链与质量检查

## 状态

当前用户已接受。Implementation 仅在已接受的 `wiki-linkify-lint` 范围内解锁。

## 切片契约

- **Slice:** `wiki-linkify-lint`
- **Wave:** Wave 1 / Wiki Foundation
- **Goal:** 在已完成的 Wiki data model 与 ingest v0 foundation 之上增加确定性的 Wiki 自动互链和质量 warning。
- **Maturity target:** SDD-ready Wiki Foundation maintenance slice。不是 production readiness。

## 来源文档

| Source | Status |
|---|---|
| Goal objective attachment | 已读。 |
| `README.md` | 已读。 |
| `PROJECT_RULES.md` | 已读。 |
| `DEVELOPMENT_STANDARDS.md` | 已读。 |
| `docs/00-context/sdd-profile.md` | 已读。 |
| `ROADMAP.md` | 已读。 |
| `ROADMAP.zh-CN.md` | 已读。 |
| `docs/07-acceptance/product-acceptance-report.zh-CN.md` | 已读。 |
| `docs/00-context/wiki-foundation-goal-plan.zh-CN.md` | 已读。 |
| `docs/00-context/execution-manifests/wiki-foundation-20260705.yaml` | 已读。 |
| `docs/00-context/slice-roadmap.md` / `.zh-CN.md` | 已读并更新。 |
| `docs/00-context/wiki-data-model-traceability.md` | 已读。 |
| `docs/00-context/wiki-ingest-v0-traceability.md` | 已读。 |
| `docs/03-spec/wiki-ingest-v0-spec.md` | 已读。 |
| `docs/04-architecture/wiki-ingest-v0-data-model.md` | 已读。 |
| `docs/05-design/contracts/wiki-ingest-v0-API_IMPLEMENTATION_GUIDE.md` | 已读。 |

## SDD Skill Chain Evidence

| Skill | File read |
|---|---|
| `atlas-sdd-generate-all` | `.agents/skills/atlas-sdd-generate-all/SKILL.md` |
| `req-to-user-story` | `.agents/skills/req-to-user-story/SKILL.md` |
| `user-story-to-spec` | `.agents/skills/user-story-to-spec/SKILL.md` |
| `spec-to-architecture` | `.agents/skills/spec-to-architecture/SKILL.md` |
| `architecture-to-design` | `.agents/skills/architecture-to-design/SKILL.md` |
| `design-to-tasks` | `.agents/skills/design-to-tasks/SKILL.md` |
| `review-doc-quality` | `.agents/skills/review-doc-quality/SKILL.md` |
| `architecture-review` | `.agents/skills/architecture-review/SKILL.md` |

SDD skill chain used: yes.

## 已依据的实现锚点

| Area | Grounding |
|---|---|
| Wiki page metadata | `backend/src/main/java/com/atlas/metadata/domain/WikiPage.java` 已包含 slug、aliases、source refs、chunk refs、in/out links、version、source mode、refresh policy、confidence、review status。 |
| Issue records | `backend/src/main/java/com/atlas/metadata/domain/WikiPageIssue.java` 存储安全 issue type、severity、status、evidence refs、message。 |
| Run/log records | `WikiGenerationRun` 与 `WikiLogEntry` 已支持安全 run summaries 与生命周期事件。 |
| Existing API surface | `ReviewPublishController` 可列出 Wiki pages、generation runs、page logs、page issues。 |
| Existing ingest | `WikiIngestService` 创建 generated `REVIEW_REQUIRED` pages 和安全 run/log/issue evidence。 |
| Frontend surface | `frontend/src/App.vue`、`frontend/src/api.ts`、`frontend/src/types.ts` 已渲染 Wiki metadata 与 Processing Center issues。 |

## Requirement To Story To Task Map

| Requirement | Stories | Tasks |
|---|---|---|
| REQ-WIKI-LINKIFY-LINT-001 | US-WIKI-LINKIFY-LINT-001 | T-WIKI-LINKIFY-LINT-001, T-WIKI-LINKIFY-LINT-012 |
| REQ-WIKI-LINKIFY-LINT-002 | US-WIKI-LINKIFY-LINT-002 | T-WIKI-LINKIFY-LINT-002, T-WIKI-LINKIFY-LINT-003 |
| REQ-WIKI-LINKIFY-LINT-003 | US-WIKI-LINKIFY-LINT-003 | T-WIKI-LINKIFY-LINT-004 |
| REQ-WIKI-LINKIFY-LINT-004 | US-WIKI-LINKIFY-LINT-003 | T-WIKI-LINKIFY-LINT-005 |
| REQ-WIKI-LINKIFY-LINT-005 | US-WIKI-LINKIFY-LINT-003 | T-WIKI-LINKIFY-LINT-005 |
| REQ-WIKI-LINKIFY-LINT-006 | US-WIKI-LINKIFY-LINT-003 | T-WIKI-LINKIFY-LINT-006 |
| REQ-WIKI-LINKIFY-LINT-007 | US-WIKI-LINKIFY-LINT-004 | T-WIKI-LINKIFY-LINT-007 |
| REQ-WIKI-LINKIFY-LINT-008 | US-WIKI-LINKIFY-LINT-004 | T-WIKI-LINKIFY-LINT-007 |
| REQ-WIKI-LINKIFY-LINT-009 | US-WIKI-LINKIFY-LINT-005 | T-WIKI-LINKIFY-LINT-007 |
| REQ-WIKI-LINKIFY-LINT-010 | US-WIKI-LINKIFY-LINT-005 | T-WIKI-LINKIFY-LINT-007 |
| REQ-WIKI-LINKIFY-LINT-011 | US-WIKI-LINKIFY-LINT-002 | T-WIKI-LINKIFY-LINT-008 |
| REQ-WIKI-LINKIFY-LINT-012 | US-WIKI-LINKIFY-LINT-006 | T-WIKI-LINKIFY-LINT-010 |
| REQ-WIKI-LINKIFY-LINT-013 | US-WIKI-LINKIFY-LINT-006 | T-WIKI-LINKIFY-LINT-009 |
| REQ-WIKI-LINKIFY-LINT-014 | US-WIKI-LINKIFY-LINT-006 | T-WIKI-LINKIFY-LINT-011, T-WIKI-LINKIFY-LINT-012 |

## SDD 产物集

| Artifact | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/wiki-linkify-lint-requirements.md` | `docs/01-requirements/wiki-linkify-lint-requirements.zh-CN.md` |
| User stories | `docs/02-user-stories/wiki-linkify-lint-stories.md` | `docs/02-user-stories/wiki-linkify-lint-stories.zh-CN.md` |
| Spec | `docs/03-spec/wiki-linkify-lint-spec.md` | `docs/03-spec/wiki-linkify-lint-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/wiki-linkify-lint-architecture.md` | `docs/04-architecture/wiki-linkify-lint-architecture.zh-CN.md` |
| Data flow | `docs/04-architecture/wiki-linkify-lint-data-flow.md` | `docs/04-architecture/wiki-linkify-lint-data-flow.zh-CN.md` |
| Data model | `docs/04-architecture/wiki-linkify-lint-data-model.md` | `docs/04-architecture/wiki-linkify-lint-data-model.zh-CN.md` |
| Design | `docs/05-design/wiki-linkify-lint-design.md` | `docs/05-design/wiki-linkify-lint-design.zh-CN.md` |
| API guide | `docs/05-design/contracts/wiki-linkify-lint-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/wiki-linkify-lint-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/wiki-linkify-lint-tasks.md` | `docs/06-tasks/wiki-linkify-lint-tasks.zh-CN.md` |
| Traceability | `docs/00-context/wiki-linkify-lint-traceability.md` | `docs/00-context/wiki-linkify-lint-traceability.zh-CN.md` |

## 人工接受交接

接受实现前请审阅：

- `docs/03-spec/wiki-linkify-lint-spec.md`
- `docs/06-tasks/wiki-linkify-lint-tasks.md`
- `docs/05-design/contracts/wiki-linkify-lint-API_IMPLEMENTATION_GUIDE.md`

接受表示同意 deterministic linkify/lint、安全 issue recording、link metadata updates、Processing Center/Wiki warnings，以及不改变 trust-state。

## SDD 质量评审结果

| Gate | Result | Evidence |
|---|---|---|
| Required context | Passed | 已读取 goal objective attachment、repository rules、SDD profile、roadmap/progress docs、product acceptance report、Wiki data-model traceability、Wiki ingest traceability/spec/data model/API guide 与实现锚点。 |
| Skill chain evidence | Passed | 已读取并使用 `atlas-sdd-generate-all`、`req-to-user-story`、`user-story-to-spec`、`spec-to-architecture`、`architecture-to-design`、`design-to-tasks`、`review-doc-quality`、`architecture-review` skill 文件。 |
| Expected artifacts exist | Passed | 20 个 English 与 Simplified Chinese `wiki-linkify-lint` SDD 文件全部存在。 |
| Bilingual ID parity | Passed | 聚焦 parity 检查确认 English 与 Chinese companion files 的 `REQ`、`US`、`T` IDs 一致。 |
| Deferred-decision scan | Passed | 生成的 SDD 文件中未发现 `TBD`、`TODO`、`FIXME`、`implementation will decide` 或 `grep later`。 |
| Architecture/API review | Passed | SDD 已记录 additive API 与 persistence changes、run mode constraint update、安全 issue reuse、不改变 trust-state、以及不直连 parser/model/provider。 |
| Diff hygiene | Passed | 本 SDD pass 后 `git diff --check` 无发现。 |
| Secret/private-path scan | Passed with expected policy-text hits | 聚焦扫描命中为 "secret"、"private path" 等护栏文本；未发现 raw credential、私有绝对路径或真实数据值。 |
| Network/dependency scan | Passed with expected policy-text hits | 聚焦扫描命中为 "no external cloud call" 等禁止性约束；未引入新的外部调用或 dependency 指令。 |
| Product-code gate | Passed | 本 SDD pass 没有改 backend 或 frontend 产品代码。 |

## SDD 门禁接受说明

- **SDD gate result:** 用户已接受；implementation 仅在已接受范围内解锁。
- **User acceptance required before code:** satisfied.
- **Product code changed after acceptance:** yes.
- **Decision:** 已接受，并按 deterministic Wave 1 成熟度目标完成实现。

接受后的推荐实现交接：

```text
Implement the wiki-linkify-lint slice strictly against docs/03-spec/wiki-linkify-lint-spec.md and docs/06-tasks/wiki-linkify-lint-tasks.md: complete every task in ID order, respect the stated constraints and verification per task, treat docs/03-spec as the behavior source of truth, do not expand scope, and if implementation would diverge from the spec stop and surface the mismatch instead of coding around it.
```

## 实现完成证据

| Task | Result | Evidence |
|---|---|---|
| T-WIKI-LINKIFY-LINT-001 | Completed | 产品代码改动前已记录用户接受。 |
| T-WIKI-LINKIFY-LINT-002 | Completed | 新增 `V12__wiki_linkify_lint.sql`；扩展 issue repository query paths 和 run mode response mapping。 |
| T-WIKI-LINKIFY-LINT-003 | Completed | 新增 `POST /api/spaces/{spaceId}/wiki-linkify-lint-runs` 与 `GET /api/spaces/{spaceId}/wiki-page-issues` 的 DTO/controller/service 路径，并覆盖 API contract tests。 |
| T-WIKI-LINKIFY-LINT-004 | Completed | 实现同空间 slug/alias target index；有歧义 term 不自动 link，并记录 `REVIEW_REQUIRED` issue。 |
| T-WIKI-LINKIFY-LINT-005 | Completed | 新增 protected-region-aware `WikiMarkdownLinkifier`，unit tests 覆盖 frontmatter、code、Markdown links/images、existing Wiki links、self links、one-link-per-target。 |
| T-WIKI-LINKIFY-LINT-006 | Completed | `WikiPage.applyLinkMetadata` 仅在 link metadata 或 artifact content 变化时更新 sorted unique `inLinks`/`outLinks`。 |
| T-WIKI-LINKIFY-LINT-007 | Completed | 实现 broken link、orphan page、missing source、stale source、thin content、unreadable artifact、ambiguous alias warnings。 |
| T-WIKI-LINKIFY-LINT-008 | Completed | Run/log/issue evidence 使用安全 summary 和确定性 issue id，避免重复膨胀。 |
| T-WIKI-LINKIFY-LINT-009 | Completed | Vue Processing Center 和 Wiki page detail 展示 space/page 级 Wiki issue warnings。 |
| T-WIKI-LINKIFY-LINT-010 | Completed | publish/list/ingest 回归已包含在聚焦和完整 backend verification 中。 |
| T-WIKI-LINKIFY-LINT-011 | Completed | 已运行 backend、frontend、E2E、second-layer E2E、diff hygiene、secret/path、network/dependency scans。 |
| T-WIKI-LINKIFY-LINT-012 | Completed | 实现后已更新 traceability、roadmap、spec 和 task status。 |

## 验证结果

| Check | Result | Notes |
|---|---|---|
| TDD RED | 按预期失败 | `cd backend && mvn -q -Dtest=WikiMarkdownLinkifierTest,WikiLinkifyLintServiceTest,WikiLinkifyLintApiContractIT -DfailIfNoTests=false test` 在实现前因 linkify/lint classes 尚不存在而失败。 |
| Focused backend | Passed | `cd backend && mvn -q -Dtest=WikiMarkdownLinkifierTest,WikiLinkifyLintServiceTest,WikiLinkifyLintApiContractIT,ReviewPublishApiContractIT -DfailIfNoTests=false test` |
| Full backend tests | Passed | `cd backend && mvn -q test`；出现既有 `GlobalExceptionHandlerTest` 预期 stack trace，但 exit code 为 0。 |
| Backend verify | Passed | `cd backend && mvn -q verify`；Flyway 校验到 V12。 |
| Focused frontend unit | Passed | `cd frontend && npm run test -- App.test.ts`；12 个聚焦 tests 通过。 |
| Frontend quality gate | Passed | `cd frontend && npm run typecheck && npm run test && npm run build`；18 个 Vitest tests 通过。 |
| Frontend E2E | 修复 mock route 后 Passed | 第一次 run 暴露 `/wiki-page-issues` E2E mock 缺口；补 mock route 后 `cd frontend && npm run e2e` 13 个 tests 通过。 |
| Second-layer E2E | Passed | `npm run e2e:second-layer` 通过，包含本地 Postgres/backend/frontend 和 2 个 Playwright tests。 |
| Diff hygiene | Passed | documentation closeout 前 `git diff --check` 无发现。 |
| Secret/private-path scan | Passed | broad scan 命中既有 frontend 名称如 `safeToken`/`apiKeyInput`；diff-only scan 未发现新增 secret 或 private-path hit。 |
| Network/dependency scan | Passed | broad scan 命中既有 API base/fetch/Ollama/DeepSeek references；diff-only scan 未发现新增 external call 或 dependency。 |
| Design fidelity review | 已应用 minor correction 后 Passed | Review 发现 ambiguous alias auto-linking 风险和 `updatedPageIds` 过宽；均已修正并由聚焦 backend tests 覆盖。 |

## Code vs Design Review Result

- **Alignment rating:** 96%.
- **Verdict:** 与已接受 deterministic 成熟度边界对齐。
- **Corrections applied:** ambiguous aliases 不再 auto-link，相关页面记录 `REVIEW_REQUIRED`，`updatedPageIds` 只报告实际变化或 dry-run 会变化的 pages。
- **Blockers before next Wave 1 closeout:** accepted deterministic linkify/lint 范围内未发现 blocker。
- **Not included by design:** SME approval workflow、refresh/retract automation、connector/runtime integration、model-assisted linking、production RBAC、production readiness。

## 残留风险

- Markdown linkification 有意保持确定性和保守；复杂自然语言 entity linking 延后。
- 复用现有 issue taxonomy；ambiguous alias 与 unsafe artifact cases 映射为 `REVIEW_REQUIRED`。
- 实现保持本地 deterministic；更完整的 parser/runtime integration 仍由后续 `real-office-parser-runtime` 切片门控。
- UI 暴露的是面向审核的 warnings，不是最终 SME approval 或 trust-state transition。
