# 溯源：graph-from-wiki-extraction

## 状态

已按接受版 prototype slice 完成实现与验证。

## Goal Objective

完成 graph-from-wiki-extraction 从 SDD 到实现、验证、closeout、commit、push 的单 slice 交付。

## Workflow

- Goal mode: autonomous-single-slice
- Workflow tier: Tier 1 / Standard Single Slice
- Slice: graph-from-wiki-extraction
- Wave: Wave 4 / Ask And Graph Productization
- Manifest: `docs/00-context/execution-manifests/graph-from-wiki-extraction-20260707.yaml`
- SDD accepted by preauthorization: yes

## SDD Skill Chain Evidence

SDD skill chain used: yes.

- `.agents/skills/atlas-sdd-generate-all/SKILL.md`
- `.agents/skills/req-to-user-story/SKILL.md`
- `.agents/skills/user-story-to-spec/SKILL.md`
- `.agents/skills/spec-to-architecture/SKILL.md`
- `.agents/skills/architecture-to-design/SKILL.md`
- `.agents/skills/design-to-tasks/SKILL.md`
- `.agents/skills/review-doc-quality/SKILL.md`
- `.agents/skills/architecture-review/SKILL.md`

Review-doc-quality result: Ready. Architecture-review result: Ready for scoped extension.

## Artifact Set

- Requirements: `docs/01-requirements/graph-from-wiki-extraction-requirements.zh-CN.md`
- Stories: `docs/02-user-stories/graph-from-wiki-extraction-stories.zh-CN.md`
- Spec: `docs/03-spec/graph-from-wiki-extraction-spec.zh-CN.md`
- Architecture: `docs/04-architecture/graph-from-wiki-extraction-architecture.zh-CN.md`
- Data flow: `docs/04-architecture/graph-from-wiki-extraction-data-flow.zh-CN.md`
- Data model: `docs/04-architecture/graph-from-wiki-extraction-data-model.zh-CN.md`
- Design: `docs/05-design/graph-from-wiki-extraction-design.zh-CN.md`
- API guide: `docs/05-design/contracts/graph-from-wiki-extraction-API_IMPLEMENTATION_GUIDE.zh-CN.md`
- Tasks: `docs/06-tasks/graph-from-wiki-extraction-tasks.zh-CN.md`

English companion files exist for each artifact.

## Requirement Trace

| Requirement | Stories | Tasks |
|---|---|---|
| REQ-GRAPH-FROM-WIKI-EXTRACTION-001 | US-GRAPH-FROM-WIKI-EXTRACTION-001 | T-GRAPH-FROM-WIKI-EXTRACTION-002 |
| REQ-GRAPH-FROM-WIKI-EXTRACTION-002 | US-GRAPH-FROM-WIKI-EXTRACTION-001 | T-GRAPH-FROM-WIKI-EXTRACTION-003 |
| REQ-GRAPH-FROM-WIKI-EXTRACTION-003 | US-GRAPH-FROM-WIKI-EXTRACTION-001 | T-GRAPH-FROM-WIKI-EXTRACTION-001, T-GRAPH-FROM-WIKI-EXTRACTION-003 |
| REQ-GRAPH-FROM-WIKI-EXTRACTION-004 | US-GRAPH-FROM-WIKI-EXTRACTION-002 | T-GRAPH-FROM-WIKI-EXTRACTION-004, T-GRAPH-FROM-WIKI-EXTRACTION-005 |
| REQ-GRAPH-FROM-WIKI-EXTRACTION-005 | US-GRAPH-FROM-WIKI-EXTRACTION-002 | T-GRAPH-FROM-WIKI-EXTRACTION-002, T-GRAPH-FROM-WIKI-EXTRACTION-005 |
| REQ-GRAPH-FROM-WIKI-EXTRACTION-006 | US-GRAPH-FROM-WIKI-EXTRACTION-003 | T-GRAPH-FROM-WIKI-EXTRACTION-004, T-GRAPH-FROM-WIKI-EXTRACTION-007 |
| REQ-GRAPH-FROM-WIKI-EXTRACTION-007 | US-GRAPH-FROM-WIKI-EXTRACTION-002 | T-GRAPH-FROM-WIKI-EXTRACTION-006, T-GRAPH-FROM-WIKI-EXTRACTION-007 |
| REQ-GRAPH-FROM-WIKI-EXTRACTION-008 | US-GRAPH-FROM-WIKI-EXTRACTION-003 | T-GRAPH-FROM-WIKI-EXTRACTION-008 |

## Verification Evidence

- `npm run agent:check-sdd -- --slice graph-from-wiki-extraction --require-api-guide --report docs/00-context/graph-from-wiki-extraction-traceability.md` — PASS。
- `cd backend && mvn verify` — PASS。
- `cd frontend && npm run typecheck` — PASS。
- `cd frontend && npm run test` — PASS。
- `cd frontend && npm run build` — PASS。
- `cd frontend && npx playwright test tests/e2e/knowledge-graph.spec.ts` — PASS。

## Implementation Evidence

- Backend graph projection 已接收 eligible Wiki page descriptors，按 review status、source trace 与 confidence 过滤 Wiki pages，并用 `evidence_wiki_page_ids` 持久化 deterministic Wiki-derived nodes/edges。
- Graph detail evidence response 现在返回 typed `SOURCE_CHUNK` 与 `WIKI_PAGE` references，不暴露 raw source payloads 或 private paths。
- Vue Graph evidence detail 将 Wiki page evidence 与 source chunk evidence 分开展示。
- Backend unit 与 API contract tests 覆盖 eligibility filtering、idempotency、evidence preservation 与 safe mixed evidence。
- Frontend component 与 Graph E2E fixtures 覆盖 Wiki-derived evidence display。

## Completed Task IDs

- T-GRAPH-FROM-WIKI-EXTRACTION-001
- T-GRAPH-FROM-WIKI-EXTRACTION-002
- T-GRAPH-FROM-WIKI-EXTRACTION-003
- T-GRAPH-FROM-WIKI-EXTRACTION-004
- T-GRAPH-FROM-WIKI-EXTRACTION-005
- T-GRAPH-FROM-WIKI-EXTRACTION-006
- T-GRAPH-FROM-WIKI-EXTRACTION-007
- T-GRAPH-FROM-WIKI-EXTRACTION-008

## Residual Risks

- Deterministic extraction 使用当前 Wiki metadata，不从 raw body text 推断关系。
- 复杂 entity deduplication 和 graph quality metrics 留给 future slices。
- 当前 worktree 还包含 ask sessions、answer review governance 与 retrieval quality metrics 的无关 Wave 4 改动；graph-from-wiki-extraction staging 必须保持 scoped。
