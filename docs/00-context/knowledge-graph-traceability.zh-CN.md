# 溯源：知识图谱

## 状态

已实现，code-against-design 评审发现已修复。Phase 4 hardening。最后更新：2026-07-03。

## 切片契约

- **目标：** 为用户提供从已批准 Atlas 知识派生、可检查、证据支撑的知识图谱。
- **范围：** 全栈 graph projection/query/review hardening，并集成前端 Graph tab。
- **排除：** parser 原始输出建图、直连引擎、真实外部图数据库、Ask 生成、生产部署、真实数据、明文 secret。
- **API guide：** 已包含，因为本切片引入 graph endpoints。

## 来源文档与界面

| 来源 | 用途 |
|---|---|
| `PROJECT_RULES.md`、`AGENTS.md`、`DEVELOPMENT_STANDARDS.md` | 阶段、SDD、安全、适配器和验证门。 |
| `docs/00-context/sdd-profile.md` | 必需 SDD 链与 ID 约定。 |
| `docs/00-context/slice-roadmap.md` | Phase 4 hardening 行与 knowledge-graph backlog 行。 |
| `docs/01-requirements/requirement.md` | 产品级 graph、review、Ask、Phase 4 和数据安全需求。 |
| `docs/knowledge-graph-design.md` | Node/edge/evidence 图谱规则。 |
| `docs/03-spec/knowledge-space-spec.md`、`docs/05-design/knowledge-space-design.md` | 已接受 FE Graph tab 行为。 |
| `frontend/public/atlas-prototype.html`、`prototypes/index.html` | Graph UI baseline。 |
| 现有后端 `GraphNode`、`GraphEdge`、`WikiPage`、`SourceChunk` | placeholder graph metadata grounding。 |

## 需求追踪

| Requirement | Story | Spec section | Design/API | Tasks |
|---|---|---|---|---|
| REQ-KG-001 | US-KG-001、US-KG-003 | S1 | Design validation | T-KG-001、T-KG-003 |
| REQ-KG-002 | US-KG-001 | S3 | Data model、API | T-KG-006 |
| REQ-KG-003 | US-KG-001 | S3 | Data model、API | T-KG-006 |
| REQ-KG-004 | US-KG-001、US-KG-002、US-KG-004 | S3、S4 | Evidence panel、API | T-KG-002、T-KG-005、T-KG-008、T-KG-010 |
| REQ-KG-005 | US-KG-002 | S4 | API guide | T-KG-006、T-KG-007、T-KG-009 |
| REQ-KG-006 | US-KG-002、US-KG-004 | S6 | API errors/auth | T-KG-007、T-KG-012 |
| REQ-KG-007 | US-KG-003 | S2 | Architecture adapter boundary | T-KG-004 |
| REQ-KG-008 | US-KG-001 | S5 | Frontend design | T-KG-009、T-KG-010、T-KG-011 |
| REQ-KG-009 | US-KG-001、US-KG-003 | S1 | Data flow exclusions | T-KG-003 |
| REQ-KG-010 | US-KG-003、US-KG-004 | S6 | Data model/audit | T-KG-002、T-KG-005、T-KG-007、T-KG-008 |
| REQ-KG-011 | US-KG-002、US-KG-003 | S6 | API/data safety | T-KG-004、T-KG-006、T-KG-012 |
| REQ-KG-012 | US-KG-004 | Acceptance Matrix | Tasks verification | T-KG-011、T-KG-012、T-KG-013 |

## 生成的 SDD 集

- `docs/01-requirements/knowledge-graph-requirements.md`
- `docs/01-requirements/knowledge-graph-requirements.zh-CN.md`
- `docs/02-user-stories/knowledge-graph-stories.md`
- `docs/02-user-stories/knowledge-graph-stories.zh-CN.md`
- `docs/03-spec/knowledge-graph-spec.md`
- `docs/03-spec/knowledge-graph-spec.zh-CN.md`
- `docs/04-architecture/knowledge-graph-architecture.md`
- `docs/04-architecture/knowledge-graph-architecture.zh-CN.md`
- `docs/04-architecture/knowledge-graph-data-flow.md`
- `docs/04-architecture/knowledge-graph-data-flow.zh-CN.md`
- `docs/04-architecture/knowledge-graph-data-model.md`
- `docs/04-architecture/knowledge-graph-data-model.zh-CN.md`
- `docs/05-design/knowledge-graph-design.md`
- `docs/05-design/knowledge-graph-design.zh-CN.md`
- `docs/05-design/contracts/knowledge-graph-API_IMPLEMENTATION_GUIDE.md`
- `docs/05-design/contracts/knowledge-graph-API_IMPLEMENTATION_GUIDE.zh-CN.md`
- `docs/06-tasks/knowledge-graph-tasks.md`
- `docs/06-tasks/knowledge-graph-tasks.zh-CN.md`
- `docs/00-context/knowledge-graph-traceability.md`
- `docs/00-context/knowledge-graph-traceability.zh-CN.md`

## 实现证据

- **后端：** 已添加 additive graph projection 持久化、deterministic `GraphProjectionAdapter`、projection/query/detail/review 服务、带 header-based mock RBAC 的 API endpoints、safe envelopes、source trace、confidence、review status 以及 audit/review records。
- **前端：** 已添加类型化 graph API response handling，以及 API-backed `[data-tab="graph"]` Knowledge Space Graph surface，包含 search、node/edge filters、evidence-only toggle、SVG canvas、legend、hover/focus affordances、node/edge selection、source-trace evidence detail、empty state、fallback state 和 unauthorized state，同时保留已接受 prototype iframe 入口。
- **测试：** 已添加后端 unit 与 PostgreSQL contract 覆盖 approved-only projection、跳过未批准来源、graph query/detail evidence、鉴权失败和 edge review actions。已添加前端组件测试覆盖 evidence metadata rendering，并添加 Playwright 覆盖 Graph tab search/filter、node 和 edge selection、evidence detail、unauthorized state 与 empty state。
- **全局契约：** 已更新 metadata contract tests，以反映已实现的 ask/graph routes 和 additive slice tables，并让 graph contract tests 与共享 integration-test 数据隔离。
- **延期工作：** 生产 graph layout、真实 graph database/engine integration 和真实数据 ingestion 仍按切片契约排除。当前实现保持 mock/sample 且位于 adapter boundary 内。

## Code-Against-Design Review 证据

- **已应用后端修正：** 增加 `MISSING_SOURCE_TRACE` projection skip 覆盖、无效 graph edge review body 的 `400` validation 覆盖，以及 space-scoped graph auth failures 的 `ACCESS_DENIED` audit 覆盖。
- **已应用前端修正：** 真实 Vue `[data-tab="graph"]` surface 现在承载 API-backed graph 数据路径，并覆盖 Spec S5、T-KG-010 和 T-KG-011 行为。
- **已更新防复发产物：** `docs/05-design/knowledge-graph-design.zh-CN.md`、`docs/06-tasks/knowledge-graph-tasks.zh-CN.md`、`frontend/tests/e2e/knowledge-graph.spec.ts`、`docs/00-context/lessons-learned.md`。

## Review-Doc-Quality 门

**文档类型：** 用于实现交接的完整 SDD 集。

**结论：** Ready with minor product sequencing questions。

**已检查：**

- 每个生成产物都有英文与中文 companion 文件。
- REQ/US/T ID 跨语言一致。
- 需求映射到用户故事、spec sections、design/API 和 Codex tasks。
- 因 backend/API 在范围内，已包含 API guide。
- Tasks 包含确切验证命令与 Phase 4 约束。
- Adapter boundary、no-network/mock verification、secret masking、trace/confidence/review preservation 均已明确。

**开放风险：** `review-publish` 顺序和生产图谱布局引擎仍待定，但 deterministic approved-fixture 路径让实现交接保持可执行。

## 推荐 Codex 交接命令

```text
Implement the knowledge-graph slice strictly against docs/03-spec/knowledge-graph-spec.md and docs/06-tasks/knowledge-graph-tasks.md: complete every task in ID order, respect the stated Constraints and Verification per task, treat docs/03-spec as the behavior source of truth, do not expand scope, and if implementation would diverge from the spec stop and surface the mismatch instead of coding around it.
```
