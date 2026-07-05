# Traceability: Knowledge Graph

## Status

Implemented; code-against-design findings resolved. Phase 4 hardening. Last updated: 2026-07-03.

## Slice Contract

- **Goal:** Give users an inspectable, evidence-backed knowledge graph derived from approved Atlas knowledge.
- **Scope:** Full-stack graph projection/query/review hardening with frontend Graph tab integration.
- **Exclusions:** Raw parser graphing, direct engines, real external graph database, Ask generation, production deployment, real data, raw secrets.
- **API guide:** Included because this slice introduces graph endpoints.

## Source Documents And Surfaces

| Source | Use |
|---|---|
| `PROJECT_RULES.md`, `AGENTS.md`, `DEVELOPMENT_STANDARDS.md` | Phase, SDD, security, adapter, and verification gates. |
| `docs/00-context/sdd-profile.md` | Required SDD chain and ID conventions. |
| `docs/00-context/slice-roadmap.md` | Phase 4 hardening row and knowledge-graph backlog row. |
| `docs/01-requirements/requirement.md` | Product graph, review, Ask, Phase 4, and data-safety requirements. |
| `docs/knowledge-graph-design.md` | Node/edge/evidence graph rule. |
| `docs/03-spec/knowledge-space-spec.md`, `docs/05-design/knowledge-space-design.md` | Accepted FE Graph tab behavior. |
| `frontend/public/atlas-prototype.html`, `prototypes/index.html` | Graph UI baseline. |
| Existing backend `GraphNode`, `GraphEdge`, `WikiPage`, `SourceChunk` | Grounding for placeholder graph metadata. |

## Requirement Trace

| Requirement | Story | Spec section | Design/API | Tasks |
|---|---|---|---|---|
| REQ-KG-001 | US-KG-001, US-KG-003 | S1 | Design validation | T-KG-001, T-KG-003 |
| REQ-KG-002 | US-KG-001 | S3 | Data model, API | T-KG-006 |
| REQ-KG-003 | US-KG-001 | S3 | Data model, API | T-KG-006 |
| REQ-KG-004 | US-KG-001, US-KG-002, US-KG-004 | S3, S4 | Evidence panel, API | T-KG-002, T-KG-005, T-KG-008, T-KG-010 |
| REQ-KG-005 | US-KG-002 | S4 | API guide | T-KG-006, T-KG-007, T-KG-009 |
| REQ-KG-006 | US-KG-002, US-KG-004 | S6 | API errors/auth | T-KG-007, T-KG-012 |
| REQ-KG-007 | US-KG-003 | S2 | Architecture adapter boundary | T-KG-004 |
| REQ-KG-008 | US-KG-001 | S5 | Frontend design | T-KG-009, T-KG-010, T-KG-011 |
| REQ-KG-009 | US-KG-001, US-KG-003 | S1 | Data flow exclusions | T-KG-003 |
| REQ-KG-010 | US-KG-003, US-KG-004 | S6 | Data model/audit | T-KG-002, T-KG-005, T-KG-007, T-KG-008 |
| REQ-KG-011 | US-KG-002, US-KG-003 | S6 | API/data safety | T-KG-004, T-KG-006, T-KG-012 |
| REQ-KG-012 | US-KG-004 | Acceptance Matrix | Tasks verification | T-KG-011, T-KG-012, T-KG-013 |

## Generated SDD Set

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

## Implementation Evidence

- **Backend:** Added additive graph projection persistence, deterministic `GraphProjectionAdapter`, projection/query/detail/review services, API endpoints with header-based mock RBAC, safe envelopes, source trace, confidence, review status, and audit/review records.
- **Frontend:** Added typed graph API response handling and an API-backed `[data-tab="graph"]` Knowledge Space Graph surface with search, node/edge filters, evidence-only toggle, SVG canvas, legend, hover/focus affordances, node/edge selection, source-trace evidence detail, empty state, fallback state, and unauthorized state while preserving the accepted prototype iframe entry.
- **Tests:** Added backend unit and PostgreSQL contract coverage for approved-only projection, skipped unapproved sources, graph query/detail evidence, auth failures, and edge review actions. Added frontend component coverage for evidence metadata rendering and Playwright coverage for Graph tab search/filter, node and edge selection, evidence detail, unauthorized state, and empty state.
- **Global contracts:** Updated metadata contract tests to reflect implemented ask/graph routes and additive slice tables, and isolated graph contract tests from shared integration-test data.
- **Deferred work:** Production graph layout, real graph database/engine integration, and real data ingestion remain excluded by the slice contract. The current implementation remains mock/sample and adapter-bound.

## Code-Against-Design Review Evidence

- **Backend corrections applied:** Added `MISSING_SOURCE_TRACE` projection skip coverage, `400` validation coverage for invalid graph edge review bodies, and `ACCESS_DENIED` audit coverage for space-scoped graph auth failures.
- **Frontend correction applied:** The real Vue `[data-tab="graph"]` surface now owns the API-backed graph data path and covers Spec S5, T-KG-010, and T-KG-011 behaviors.
- **Prevention artifacts updated:** `docs/05-design/knowledge-graph-design.md`, `docs/06-tasks/knowledge-graph-tasks.md`, `frontend/tests/e2e/knowledge-graph.spec.ts`, and `docs/00-context/lessons-learned.md`.

## Review-Doc-Quality Gate

**Document type:** Full SDD set for implementation handoff.

**Verdict:** Ready with minor product sequencing questions.

**Checks performed:**

- English and Chinese companion files exist for every generated artifact.
- REQ/US/T IDs are identical across languages.
- Requirements map to stories, spec sections, design/API, and Codex tasks.
- API guide is included because backend/API work is in scope.
- Tasks include exact verification commands and Phase 4 constraints.
- Adapter boundary, no-network/mock verification, secret masking, and trace/confidence/review preservation are explicit.

**Open risks:** `review-publish` sequencing and production graph layout engine remain open, but the deterministic approved-fixture path keeps implementation handoff actionable.

## Recommended Codex Handoff

```text
Implement the knowledge-graph slice strictly against docs/03-spec/knowledge-graph-spec.md and docs/06-tasks/knowledge-graph-tasks.md: complete every task in ID order, respect the stated Constraints and Verification per task, treat docs/03-spec as the behavior source of truth, do not expand scope, and if implementation would diverge from the spec stop and surface the mismatch instead of coding around it.
```

## Product Goal Batch 3 Status

| Phase | Task IDs | Maturity | Evidence |
|---|---|---|---|
| Phase F Knowledge Graph | `T-KG-014` | L2 Vue parity | `frontend/tests/e2e/phase-e-f-g-knowledge-surfaces.spec.ts`; `docs/00-context/evidence/phase-f-knowledge-graph.png`; real Vue Knowledge Space Graph tab shows a graph canvas, node types, search, legend, selected node detail, confidence, review status, and evidence/source trace. |

This status updates product-surface maturity only. It does not add production graph database/layout behavior, real data ingestion, or new backend/API contracts.

## Product Goal Batch 6 Status

| Phase | Task IDs | Maturity | Evidence |
|---|---|---|---|
| Phase I5 Graph evidence API-backed Vue cutover | `T-FSP-012` consuming existing graph read/detail APIs | L3 API-backed | `frontend/tests/e2e/phase-i4-i7-api-backed-knowledge-surfaces.spec.ts`; `docs/00-context/evidence/phase-i4-i7-api-backed-knowledge-surfaces.png`; real Vue Graph tab maps API graph nodes and source-trace evidence into the product graph surface. |

This status upgrades the product graph surface only. It does not add production graph extraction, real graph layout engines, real company data, or new backend/API contracts.
