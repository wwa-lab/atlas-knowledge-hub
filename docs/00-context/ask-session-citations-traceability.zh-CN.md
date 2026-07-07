# 溯源：Ask Session Citations

## 状态

已于 2026-07-07 完成 autonomous single-slice delivery 的实现与验证。

## 切片契约

| 字段 | 值 |
|---|---|
| Goal | 增加 session-scoped Trusted Ask answer citation snapshots，保留安全 source trace 和 review-aware evidence。 |
| Slice | `ask-session-citations` |
| Wave | Wave 4 / Ask And Graph Productization |
| Workflow tier | Tier 1 / Standard Single Slice |
| Autonomy boundary | SDD 和 implementation 仅在 prompt 的 Goal、Scope、Exclusions、Acceptance 和 Stop conditions 内被预授权。 |
| API guide | 已包含，因为 backend/API/persistence 在范围内。 |

## 已读来源

- `AGENTS.md`
- `README.md`
- `PROJECT_RULES.md`
- `DEVELOPMENT_STANDARDS.md`
- `docs/00-context/repo-status-roadmap.zh-CN.md`
- `docs/00-context/agent-goal-loop-workflow.md`
- `docs/00-context/agent-goal-loop-workflow.zh-CN.md`
- `docs/00-context/agent-goal-loop-quickstart.md`
- `docs/00-context/agent-goal-loop-quickstart.zh-CN.md`
- `docs/00-context/codex-goal-prompts.zh-CN.md`
- `docs/00-context/sdd-profile.md`
- `docs/SDD-BOOTSTRAP.md`
- `docs/SDD-BOOTSTRAP.zh-CN.md`
- `docs/00-context/slice-roadmap.zh-CN.md`
- `docs/00-context/ask-rag-traceability.md`
- `docs/00-context/ask-rag-traceability.zh-CN.md`
- `docs/00-context/review-publish-traceability.md`
- `docs/00-context/review-publish-traceability.zh-CN.md`
- `docs/00-context/wiki-ingest-v0-traceability.md`
- `docs/00-context/wiki-ingest-v0-traceability.zh-CN.md`
- `docs/00-context/wiki-linkify-lint-traceability.md`
- `docs/00-context/wiki-linkify-lint-traceability.zh-CN.md`
- `docs/01-requirements/requirement.md`
- Grounding 阶段识别的现有 Ask backend 与 frontend 文件。

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

额外读取：`.agents/skills/_shared/grounding-rules.md`、`.agents/skills/review-doc-quality/references/completeness-criteria.md`、`.agents/skills/review-doc-quality/references/phase-scope-guide.md` 和 `.agents/skills/architecture-to-design/references/design-patterns.md`。

SDD skill chain used: yes。

## 已依据的现有实现

| Area | Grounding |
|---|---|
| Existing Ask create/read API | `AskController` 暴露 `POST /api/spaces/{spaceId}/ask` 和 `GET /api/ask-runs/{runId}`。 |
| Existing Ask answer record | `AskRun` 存储 question、status、review policy、mode、requestedBy、answer、confidence、review status、model run id、safe message 和 timestamps。 |
| Existing citation foundation | `AskEvidence` 存储 source chunk id、file item id、source file、page、section、review status、confidence、vector item key、score 和 creation time。 |
| Existing UI | `frontend/src/App.vue` 渲染 Ask answer evidence 与 review status；`frontend/src/api.ts` 调用 create/read Ask endpoints。 |
| Existing migration | Flyway chain 包含 `V8__ask_rag.sql`；本切片前当前最新 migration 是 `V14__audit_log_foundation.sql`。 |

## 产物集

| Stage | English | Chinese |
|---|---|---|
| Requirements | `docs/01-requirements/ask-session-citations-requirements.md` | `docs/01-requirements/ask-session-citations-requirements.zh-CN.md` |
| User stories | `docs/02-user-stories/ask-session-citations-stories.md` | `docs/02-user-stories/ask-session-citations-stories.zh-CN.md` |
| Spec | `docs/03-spec/ask-session-citations-spec.md` | `docs/03-spec/ask-session-citations-spec.zh-CN.md` |
| Architecture | `docs/04-architecture/ask-session-citations-architecture.md` | `docs/04-architecture/ask-session-citations-architecture.zh-CN.md` |
| Data flow | `docs/04-architecture/ask-session-citations-data-flow.md` | `docs/04-architecture/ask-session-citations-data-flow.zh-CN.md` |
| Data model | `docs/04-architecture/ask-session-citations-data-model.md` | `docs/04-architecture/ask-session-citations-data-model.zh-CN.md` |
| Design | `docs/05-design/ask-session-citations-design.md` | `docs/05-design/ask-session-citations-design.zh-CN.md` |
| API guide | `docs/05-design/contracts/ask-session-citations-API_IMPLEMENTATION_GUIDE.md` | `docs/05-design/contracts/ask-session-citations-API_IMPLEMENTATION_GUIDE.zh-CN.md` |
| Tasks | `docs/06-tasks/ask-session-citations-tasks.md` | `docs/06-tasks/ask-session-citations-tasks.zh-CN.md` |
| Traceability | `docs/00-context/ask-session-citations-traceability.md` | `docs/00-context/ask-session-citations-traceability.zh-CN.md` |

## Requirement To Story To Task Map

| Requirement | Stories | Tasks |
|---|---|---|
| REQ-ASK-SESSION-CITATIONS-001 | US-ASK-SESSION-CITATIONS-001 | T-ASK-SESSION-CITATIONS-002, T-ASK-SESSION-CITATIONS-003 |
| REQ-ASK-SESSION-CITATIONS-002 | US-ASK-SESSION-CITATIONS-004 | T-ASK-SESSION-CITATIONS-003, T-ASK-SESSION-CITATIONS-004 |
| REQ-ASK-SESSION-CITATIONS-003 | US-ASK-SESSION-CITATIONS-001 | T-ASK-SESSION-CITATIONS-004 |
| REQ-ASK-SESSION-CITATIONS-004 | US-ASK-SESSION-CITATIONS-002 | T-ASK-SESSION-CITATIONS-002, T-ASK-SESSION-CITATIONS-003 |
| REQ-ASK-SESSION-CITATIONS-005 | US-ASK-SESSION-CITATIONS-002 | T-ASK-SESSION-CITATIONS-003, T-ASK-SESSION-CITATIONS-005 |
| REQ-ASK-SESSION-CITATIONS-006 | US-ASK-SESSION-CITATIONS-002 | T-ASK-SESSION-CITATIONS-005 |
| REQ-ASK-SESSION-CITATIONS-007 | US-ASK-SESSION-CITATIONS-003 | T-ASK-SESSION-CITATIONS-004 |
| REQ-ASK-SESSION-CITATIONS-008 | US-ASK-SESSION-CITATIONS-003 | T-ASK-SESSION-CITATIONS-004 |
| REQ-ASK-SESSION-CITATIONS-009 | US-ASK-SESSION-CITATIONS-002 | T-ASK-SESSION-CITATIONS-003, T-ASK-SESSION-CITATIONS-005 |
| REQ-ASK-SESSION-CITATIONS-010 | US-ASK-SESSION-CITATIONS-003 | T-ASK-SESSION-CITATIONS-006, T-ASK-SESSION-CITATIONS-007, T-ASK-SESSION-CITATIONS-008 |
| REQ-ASK-SESSION-CITATIONS-011 | US-ASK-SESSION-CITATIONS-004 | T-ASK-SESSION-CITATIONS-004, T-ASK-SESSION-CITATIONS-009 |
| REQ-ASK-SESSION-CITATIONS-012 | US-ASK-SESSION-CITATIONS-004 | T-ASK-SESSION-CITATIONS-010 |

## Review-Doc-Quality Result

| Gate | Result |
|---|---|
| Expected artifacts exist | Pass |
| Bilingual IDs match | Pass |
| API guide included | Pass |
| Tasks are executable | Pass |
| Adapter boundaries explicit | Pass |
| Open questions explicit | Pass |
| Deferred decision scan | Pass |

## SDD Gate Acceptance Note

- **SDD accepted by preauthorization:** yes，因为 SDD 保持在附件 prompt 的 Goal、Scope、Exclusions、Acceptance 和 Autonomy boundary 内。
- **User acceptance required before code:** 该 autonomous goal 不需要额外接受，除非触发 stop condition。
- **Decision:** 按 task ID 顺序进入 implementation。

## Implementation Evidence

本切片在从 `develop-leo` 创建的干净 detached worktree 中完成，以避免主工作区中同时出现的 `answer-review-governance`、`graph-from-wiki-extraction` 与 `retrieval-quality-metrics` 非本切片变更混入交付。

| Area | Evidence |
|---|---|
| Persistence | 新增 `AskSession`、`AskSessionRepository`、`AskRun.sessionId`、`AskEvidence` citation snapshot fields，以及 Flyway migration `V15__ask_session_citations.sql`。 |
| Backend service | `AskService` 现在会 create/reuse sessions，在终态后更新 session 时间，派生 safe citation labels/locators/status，并返回 session list/detail responses。 |
| API contract | `AskController` 暴露 `GET /api/spaces/{spaceId}/ask-sessions` 与 `GET /api/ask-sessions/{sessionId}`；`AskApiContractIT` 覆盖 session identity、session history 与 citation fields。 |
| Frontend | `frontend/src/types.ts`、`frontend/src/api.ts` 与 `frontend/src/App.vue` 已支持 Ask session summaries/details，并展示 citation id、label、locator、eligibility status 与 exclusion reason。 |
| Fixtures/tests | Frontend mock data 与 P0 API mocks 已加入 session list/detail endpoints 和更丰富的 citation metadata。 |
| Scope control | 未加入 provider/model adapter 变更、production auth/RBAC/audit/secret/rate-limit 语义变更、真实数据、cloud calls、answer governance、retrieval metrics 或 graph extraction 行为。 |

## Verification Evidence

| Check | Result |
|---|---|
| `npm run agent:check-sdd -- --slice ask-session-citations --require-api-guide` | Pass；仍保留 generated artifact 的非阻塞 warnings：architecture/design/API guide ID density 与可选 `--report` 未提供。 |
| `cd backend && mvn verify` | Pass；129 个 unit tests 与 61 个 integration tests 通过，2 个 configured runtime smoke tests 按设计 skipped。 |
| `cd frontend && npm run typecheck` | Pass。 |
| `cd frontend && npm run test` | Pass；3 files / 21 tests。 |
| `cd frontend && npm run build` | Pass；包含 lint、typecheck 与 Vite production build。 |
| `npm run agent:closeout` | Pass；workflow YAML、workflow private-path/secret scans、`git diff --check` 与 changed-slice SDD gates 均通过。 |
| `git diff --check` | Pass。 |
| Added-line safety scan | Pass；新增 slice lines 中未发现 new secrets、raw endpoints、JDBC URLs 或 private paths。 |

## Residual Risks

- Production answer approval 和 governance 延后到 `answer-review-governance`。
- Retrieval metrics 延后到 `retrieval-quality-metrics`。
- Citation eligibility 是 snapshot-based 且 review-aware；production-grade citation quality scoring 不在本切片范围内。
- 本交付 worktree 有意排除了主工作区中的非本切片 in-progress files。
