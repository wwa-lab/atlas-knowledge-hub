# 溯源：Wiki 自动互链与质量检查

## 状态

SDD 草稿已生成，等待用户审阅。产品代码尚未实现，且在用户接受前保持阻塞。

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

- **SDD gate result:** 草稿已生成；等待用户接受。
- **User acceptance required before code:** yes.
- **Product code changed:** no.
- **Decision:** pending.

接受后的推荐实现交接：

```text
Implement the wiki-linkify-lint slice strictly against docs/03-spec/wiki-linkify-lint-spec.md and docs/06-tasks/wiki-linkify-lint-tasks.md: complete every task in ID order, respect the stated constraints and verification per task, treat docs/03-spec as the behavior source of truth, do not expand scope, and if implementation would diverge from the spec stop and surface the mismatch instead of coding around it.
```

## 残留风险

- Markdown linkification 有意保持确定性和保守；复杂自然语言 entity linking 延后。
- 复用现有 issue taxonomy；ambiguous alias 与 unsafe artifact cases 映射为 `REVIEW_REQUIRED`。
- 实现阶段需要为 `linkify-lint` 增加一个小的 schema mode constraint update。
- 本 SDD pass 没有改产品代码。
