# 溯源：Wiki Ingest v0

## 状态

Auto Wiki ingest v0 implementation 已完成验证。该切片仍是 review-required 的 Wiki Foundation capability，不代表 production readiness、review-gate 完成、linkify/lint 完成、connector sync 或 model-assisted generation。

## 切片契约

- **Slice:** `wiki-ingest-v0`
- **Goal:** 从 approved source chunks 生成 review-required Auto Wiki page candidates，并合并到现有 Wiki data model。
- **Wave:** Wave 1 / Wiki Foundation
- **Maturity target:** 已验证的 Auto Wiki ingest v0 implementation slice，包含 deterministic review-required candidate generation、安全 run/log/issue evidence 和显式 draft exposure。

## Source Documents

| Source | Status |
|---|---|
| `AGENTS.md` | Read. |
| `README.md` | Read. |
| `PROJECT_RULES.md` | Read. |
| `DEVELOPMENT_STANDARDS.md` | Read. |
| `docs/SDD-BOOTSTRAP.md` | Read. |
| `docs/SDD-BOOTSTRAP.zh-CN.md` | Read. |
| `docs/00-context/agent-goal-loop-workflow.md` | Read. |
| `docs/00-context/agent-goal-loop-workflow.zh-CN.md` | Read. |
| `docs/00-context/sdd-profile.md` | Read. |
| `docs/00-context/checklists/sdd-generation-gate.md` | Read. |
| `docs/00-context/checklists/sdd-generation-gate.zh-CN.md` | Read. |
| `docs/00-context/goal-prompts/master-goal-prompt.md` | Read. |
| `docs/00-context/goal-prompts/single-slice-goal-prompt.md` | Read. |
| `docs/01-requirements/requirement.md` | Read. |
| `docs/00-context/wiki-foundation-goal-plan.zh-CN.md` | Read. |
| `docs/00-context/execution-manifests/wiki-foundation-20260705.yaml` | Read. |
| `ROADMAP.md` | Read. |
| `ROADMAP.zh-CN.md` | Read. |
| `docs/00-context/slice-roadmap.md` | Read and updated with current next-phase wave queue status. |
| `docs/00-context/slice-roadmap.zh-CN.md` | Read and updated with current next-phase wave queue status. |
| `docs/07-acceptance/product-acceptance-report.zh-CN.md` | Read. |
| `docs/07-acceptance/internal-beta-readiness.zh-CN.md` | Read. |
| `docs/03-spec/full-stack-productization-spec.zh-CN.md` | Read. |
| `docs/06-tasks/full-stack-productization-tasks.zh-CN.md` | Read. |
| `docs/00-context/full-stack-productization-traceability.zh-CN.md` | Read. |
| `docs/00-context/wiki-data-model-traceability.md` | Read. |
| `WEKNORA_ANALYSIS_DIR` | Not read; environment variable was unset. |

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

为 review quality 额外读取：`.agents/skills/_shared/grounding-rules.md`、`.agents/skills/review-doc-quality/references/completeness-criteria.md`、`.agents/skills/review-doc-quality/references/phase-scope-guide.md`。

## Grounded Existing Implementation

| Area | Grounding |
|---|---|
| Wiki page refs and review state | `backend/src/main/java/com/atlas/metadata/domain/WikiPage.java:115` 创建带 source chunk references 的 published Wiki metadata；getters 暴露 refs 和 status。 |
| Published Wiki compatibility | `backend/src/main/java/com/atlas/metadata/service/ReviewPublishService.java:138` 只发布 approved Markdown files；`ReviewPublishService.java:144` 阻止 missing source trace。 |
| Existing upload/parser path | `backend/src/main/java/com/atlas/metadata/service/IngestionService.java:58` 摄取 PDF/ZIP uploads 并启动 local parser runs；`IngestionService.java:168` 创建 review-required file metadata。 |
| Frontend API shape | `frontend/src/types.ts:365` 定义当前 `ApiWikiPage` fields，包括 slug、refs、source mode、refresh policy、confidence 和 published status。 |

## Requirement To Story To Task Map

| Requirement | Stories | Tasks |
|---|---|---|
| REQ-WIKI-INGEST-V0-001 | US-WIKI-INGEST-V0-001 | T-WIKI-INGEST-V0-001, T-WIKI-INGEST-V0-010 |
| REQ-WIKI-INGEST-V0-002 | US-WIKI-INGEST-V0-002 | T-WIKI-INGEST-V0-003, T-WIKI-INGEST-V0-008 |
| REQ-WIKI-INGEST-V0-003 | US-WIKI-INGEST-V0-002 | T-WIKI-INGEST-V0-004, T-WIKI-INGEST-V0-008 |
| REQ-WIKI-INGEST-V0-004 | US-WIKI-INGEST-V0-003 | T-WIKI-INGEST-V0-004, T-WIKI-INGEST-V0-009 |
| REQ-WIKI-INGEST-V0-005 | US-WIKI-INGEST-V0-004 | T-WIKI-INGEST-V0-005, T-WIKI-INGEST-V0-008 |
| REQ-WIKI-INGEST-V0-006 | US-WIKI-INGEST-V0-004 | T-WIKI-INGEST-V0-005 |
| REQ-WIKI-INGEST-V0-007 | US-WIKI-INGEST-V0-002 | T-WIKI-INGEST-V0-004, T-WIKI-INGEST-V0-007 |
| REQ-WIKI-INGEST-V0-008 | US-WIKI-INGEST-V0-005 | T-WIKI-INGEST-V0-006 |
| REQ-WIKI-INGEST-V0-009 | US-WIKI-INGEST-V0-005 | T-WIKI-INGEST-V0-006 |
| REQ-WIKI-INGEST-V0-010 | US-WIKI-INGEST-V0-005 | T-WIKI-INGEST-V0-002 |
| REQ-WIKI-INGEST-V0-011 | US-WIKI-INGEST-V0-006 | T-WIKI-INGEST-V0-007 |
| REQ-WIKI-INGEST-V0-012 | US-WIKI-INGEST-V0-003 | T-WIKI-INGEST-V0-004, T-WIKI-INGEST-V0-009 |
| REQ-WIKI-INGEST-V0-013 | US-WIKI-INGEST-V0-003 | T-WIKI-INGEST-V0-003, T-WIKI-INGEST-V0-009 |
| REQ-WIKI-INGEST-V0-014 | US-WIKI-INGEST-V0-006 | T-WIKI-INGEST-V0-008, T-WIKI-INGEST-V0-009, T-WIKI-INGEST-V0-010 |

## SDD Artifact Set

| Artifact | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/wiki-ingest-v0-requirements.md` | `docs/01-requirements/wiki-ingest-v0-requirements.zh-CN.md` |
| User stories | `docs/02-user-stories/wiki-ingest-v0-stories.md` | `docs/02-user-stories/wiki-ingest-v0-stories.zh-CN.md` |
| Spec | `docs/03-spec/wiki-ingest-v0-spec.md` | `docs/03-spec/wiki-ingest-v0-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/wiki-ingest-v0-architecture.md` | `docs/04-architecture/wiki-ingest-v0-architecture.zh-CN.md` |
| Data flow | `docs/04-architecture/wiki-ingest-v0-data-flow.md` | `docs/04-architecture/wiki-ingest-v0-data-flow.zh-CN.md` |
| Data model | `docs/04-architecture/wiki-ingest-v0-data-model.md` | `docs/04-architecture/wiki-ingest-v0-data-model.zh-CN.md` |
| Design | `docs/05-design/wiki-ingest-v0-design.md` | `docs/05-design/wiki-ingest-v0-design.zh-CN.md` |
| API guide | `docs/05-design/contracts/wiki-ingest-v0-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/wiki-ingest-v0-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/wiki-ingest-v0-tasks.md` | `docs/06-tasks/wiki-ingest-v0-tasks.zh-CN.md` |
| Traceability | `docs/00-context/wiki-ingest-v0-traceability.md` | `docs/00-context/wiki-ingest-v0-traceability.zh-CN.md` |

## 人工接受交接

实现接受前请审阅这些文档：

- `docs/03-spec/wiki-ingest-v0-spec.md`
- `docs/06-tasks/wiki-ingest-v0-tasks.md`
- `docs/05-design/contracts/wiki-ingest-v0-API_IMPLEMENTATION_GUIDE.md`

接受表示用户认可 v0 范围：从 approved、traceable source chunks 进行 deterministic Auto Wiki candidate generation；生成页面保持 review-required；记录安全的 run/log/issue evidence；按 slug 幂等合并；且不覆盖 trusted page。接受不表示批准 linkify/lint、review approval workflow、refresh/retract、connector sync、production RBAC、model-assisted generation、外部 provider calls、真实公司数据或 production readiness。

接受后，implementation 应从 T-WIKI-INGEST-V0-001 开始并按任务顺序推进。推荐 implementation 交接命令：

```text
严格依据 docs/03-spec/wiki-ingest-v0-spec.md 与 docs/06-tasks/wiki-ingest-v0-tasks.md 实现 wiki-ingest-v0 切片：按 ID 顺序完成每一条任务，遵守每条任务标注的 constraints 与 verification，以 docs/03-spec 为行为唯一真相源，不扩大范围；若实现将偏离 spec，停下并指出不一致，而不是绕过它编码。
```

## SDD Gate Acceptance Note

- **SDD gate result:** 已由用户接受；implementation 仅在已接受范围内解锁。
- **Slice:** `wiki-ingest-v0`
- **Skill chain evidence:** 已记录使用 project-local SDD chain；由于 API、persistence、adapter boundary、security 和 data-flow 行为在范围内，已记录 `architecture-review`。
- **Documents checked:** 完整双语 SDD artifact set、`docs/00-context/checklists/sdd-generation-gate.md`、必需 repository rules、execution manifest、roadmap/progress docs 和 grounded implementation anchors。
- **Issues found:** 当前 traceability evidence 中无剩余 critical SDD gate issue。Residual risks 见下方。
- **User acceptance required before code:** yes。
- **Decision:** accepted。

## Implementation Completion Evidence

| Task | Completion evidence |
|---|---|
| T-WIKI-INGEST-V0-001 | 用户已在产品代码变更前接受双语 SDD gate。 |
| T-WIKI-INGEST-V0-002 | 已新增 `CreateWikiIngestRunRequest`、`WikiIngestRunResponse`、`WikiIngestController` 和 start/read API contract coverage。 |
| T-WIKI-INGEST-V0-003 | `WikiIngestService` 只选择 space-scoped approved、traceable chunks，并记录 eligible/excluded counts。 |
| T-WIKI-INGEST-V0-004 | Deterministic candidate builder 创建 `TOPIC`、`AUTO_GENERATED`、`ON_SOURCE_CHANGE`、`REVIEW_REQUIRED` pages，通过 local artifact storage 写入 generated Markdown，以最低 chunk confidence 聚合 confidence，并拒绝 v0 `model-assisted` mode。 |
| T-WIKI-INGEST-V0-005 | Space-scoped slug lookup 会合并 generated candidates；generated page ID 包含 normalized space slug，避免跨 space ID 冲突；trusted `PUBLISHED_FILE` pages 会保留并记录 safe issues。 |
| T-WIKI-INGEST-V0-006 | 已用 safe summaries、page ids、issue ids 和 counts 持久化 `wiki_generation_run`、`wiki_log_entry` 与 `wiki_page_issue` evidence。 |
| T-WIKI-INGEST-V0-007 | Vue 对 Wiki surfaces 请求 `GET /api/spaces/{spaceId}/wiki-pages?includeDrafts=true`，并可展示 generated `REVIEW_REQUIRED` / `AUTO_GENERATED` status，不作 trusted 声称。 |
| T-WIKI-INGEST-V0-008 | 已新增或更新 backend service/API tests 与 frontend type/unit/build/E2E coverage。 |
| T-WIKI-INGEST-V0-009 | 完整 verification gates 与 focused safety scans 已通过；见下方验证结果。 |
| T-WIKI-INGEST-V0-010 | 本 traceability、task status 和 roadmap rows 已记录 completion evidence 与 residual risks。 |

## Verification Results

| Check | Result | Evidence |
|---|---|---|
| Backend focused unit tests | Passed | `cd backend && mvn -q -Dtest=WikiIngestServiceTest,ReviewPublishServiceTest -DfailIfNoTests=false test` |
| Backend focused API contract tests | Passed | `cd backend && mvn -q -Dtest=WikiIngestApiContractIT,ReviewPublishApiContractIT -DfailIfNoTests=false test` |
| Backend full unit tests | Passed | `cd backend && mvn -q test` |
| Backend verify with PostgreSQL/Testcontainers | Passed | `cd backend && mvn -q verify -DforkCount=1 -DreuseForks=true -Dmaven.compiler.useIncrementalCompilation=false`；当前 worktree Flyway 已应用到 `V14__audit_log_foundation.sql`，包含 `V11__wiki_ingest_v0_run_counts.sql`。 |
| Frontend typecheck/unit/build | Passed | `cd frontend && npm run typecheck && npm run test && npm run build` |
| Frontend E2E | Passed | `cd frontend && npm run e2e`；16 个 Playwright tests passed。 |
| Second-layer full-stack E2E | Passed | `npm run e2e:second-layer`；本地 backend/frontend/PostgreSQL 路径 2 个 Playwright tests passed。 |
| Diff hygiene | Passed | `git diff --check` 无发现。 |
| Focused secret/private-path scan | Passed with reviewed false positive | 新增 ingest 文件 diff scan 无发现；更宽的 scoped diff 只命中 negative API contract assertion 中的 `password` 字面量。 |
| Focused network/dependency scan | Passed | 新增 ingest backend files 不包含 direct HTTP/provider/parser/vector/storage engine coupling；generated Markdown 使用现有 `LocalArtifactStorageService`。 |

## SDD Quality Notes

- Skill chain applied: `atlas-sdd-generate-all`、`req-to-user-story`、`user-story-to-spec`、`spec-to-architecture`、`architecture-to-design`、`design-to-tasks` 和 `review-doc-quality` checklist。
- Architecture review concerns addressed in SDD: adapter boundaries、API contract、persistence state、safe logs、deterministic-only v0 behavior、generated Markdown artifact output 和 review-required generated output。
- 产品代码仅在用户接受后修改，并且保持在已接受的 `wiki-ingest-v0` implementation tasks 范围内。

## SDD 质量审查结果

| Gate | Result | Evidence |
|---|---|---|
| Required context | Passed | Repository rules、SDD bootstrap docs、goal-loop workflow docs、execution manifest、SDD generation checklist、roadmap/progress docs 和 active slice SDD docs 已读取或已记录在 source evidence。 |
| Companion files | Passed | 20 个 English 与 Simplified Chinese `wiki-ingest-v0` SDD 文件全部存在。 |
| ID parity | Passed | `REQ`、`US`、`T` IDs 在英文和中文 companion 中一致。 |
| Deferred-decision scan | Passed | Candidate 关键决策已解决：v0 使用 `TOPIC` pages、generated Markdown artifacts、minimum confidence aggregation 和 deterministic-only generation。 |
| Phase discipline | Passed | 用户已在 implementation 前接受 SDD；随后产品代码严格依据已接受 spec/tasks 修改。 |
| Adapter boundary | Passed | v0 拒绝或禁用 model-assisted mode；未来 model use 必须通过 ModelAdapter。 |
| Safety language | Passed | SDD 排除真实公司数据、raw secrets、raw prompts、provider payloads、private paths、stack traces 和默认外部调用。 |
| Context status | Passed | `docs/00-context/slice-roadmap.md` 与 `.zh-CN.md` 已记录 `wiki-ingest-v0` 当前成熟度目标已完成 implementation verification。 |
| Acceptance handoff | Passed | Traceability 已列出 review documents、accepted v0 scope、exclusions 和 implementation handoff command。 |
| SDD gate acceptance note | Passed | Traceability 已记录 SDD gate result、documents checked、accepted decision state 和 implementation scope boundary。 |
| Diff hygiene | Passed | 本 SDD pass 后 `git diff --check` 无发现。 |

## Residual Risks

- 生成的 deterministic candidates 刻意保持轻量，并始终为 `REVIEW_REQUIRED`；在后续 governance slices 之前，不作为 trusted Ask 或 Graph evidence。
- `wiki_page_issue.issue_type` 对 trusted slug collisions 复用现有 safe issue taxonomy，而不是在本切片新增 collision-specific enum。
- 当前 worktree 包含之前的 `wiki-data-model` 与 roadmap/documentation changes；本次未回滚或重排这些改动。
- `WEKNORA_ANALYSIS_DIR` 未设置，因此未读取本地 WeKnora analysis files。

## 已解决决策

- v0 只创建 `TOPIC` pages。
- v0 写入带确定性安全摘要和安全 source/chunk labels 的 generated Markdown artifacts。
- v0 使用 included chunk confidence 的最低值聚合 confidence。
- v0 拒绝或禁用 model-assisted mode；未来 model assistance 必须使用 ModelAdapter。
