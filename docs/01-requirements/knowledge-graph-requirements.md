# Requirements: Knowledge Graph

## Status

Draft. Phase 4 hardening. Slice `knowledge-graph`. Last updated: 2026-07-03.

## Slice Contract

**Goal:** Give users an inspectable, evidence-backed knowledge graph derived from approved Atlas knowledge, so graph relationships can be trusted, reviewed, and traced back to Wiki pages and source chunks.

**Scope:** Node and edge projection from approved/published Wiki and source trace metadata; graph query API; RBAC/audit/error-hardening for graph endpoints; frontend Graph tab integration with inspectable evidence details; tests and E2E coverage for the touched layers.

**Exclusions:** Raw parser-output graphing, direct model/vector/parser/storage engine calls, real external graph engines, Ask answer generation, Wiki publish-state implementation, production deployment work, real company data, raw secrets, and copying any external product implementation or assets.

**Sources:** `README.md`, `PROJECT_RULES.md`, `DEVELOPMENT_STANDARDS.md`, `docs/00-context/sdd-profile.md`, `docs/00-context/slice-roadmap.md`, `docs/01-requirements/requirement.md`, `docs/knowledge-graph-design.md`, `docs/03-spec/knowledge-space-spec.md`, `docs/05-design/knowledge-space-design.md`, `docs/04-architecture/metadata-api-architecture.md`, `docs/05-design/contracts/metadata-api-API_IMPLEMENTATION_GUIDE.md`, `frontend/public/atlas-prototype.html`, `prototypes/index.html`, and existing backend graph placeholder entities.

## Product Requirements

| ID | Requirement | Priority | Source |
|---|---|---:|---|
| REQ-KG-001 | The graph must be derived only from approved or published Wiki/source-trace metadata, never directly from unreviewed raw parser output. | Must | REQ-PROD-034 |
| REQ-KG-002 | Graph nodes must support `KNOWLEDGE_SPACE`, `DOCUMENT`, `WIKI_PAGE`, `CONCEPT`, `ENTITY`, and `SOURCE_CHUNK`. | Must | `docs/knowledge-graph-design.md` |
| REQ-KG-003 | Graph edges must support `CONTAINS`, `DERIVED_FROM`, `MENTIONS`, `DEFINES`, `RELATED_TO`, `BELONGS_TO`, `USES`, `DEPENDS_ON`, and `REVIEWED_BY`. | Must | `docs/knowledge-graph-design.md` |
| REQ-KG-004 | Every trustable edge must carry evidence references to at least one Wiki page or source chunk, plus confidence and review status. | Must | REQ-PROD-037 |
| REQ-KG-005 | The graph query API must expose bounded node/edge lists, counts, filters, and a selected-node evidence detail payload using the Atlas response envelope. | Must | Phase 4 API guide requirement |
| REQ-KG-006 | Graph endpoints must enforce backend authorization and return user-safe errors without stack traces, SQL, secrets, private paths, or raw engine diagnostics. | Must | REQ-PROD-053, REQ-PROD-077 |
| REQ-KG-007 | Graph projection work must stay behind a product-facing projection/adapter boundary so future extraction engines can be replaced. | Must | Adapter boundary rule |
| REQ-KG-008 | The frontend Graph tab must preserve the accepted prototype interaction model: graph canvas, search/filter, legend, hover affordance, click detail, and evidence-focused detail panel. | Must | FE baseline |
| REQ-KG-009 | Review-required, low-confidence, missing-source-trace, failed, or unapproved material must be blocked from trusted graph projection and visibly excluded or reported. | Must | Processing Center gate |
| REQ-KG-010 | Graph operations must write audit evidence for projection runs, manual relationship review actions, endpoint access failures, and publish-to-graph changes. | Must | Phase 4 hardening |
| REQ-KG-011 | Graph responses must never expose raw secrets, internal endpoints, absolute private paths, raw vectors, raw model prompts, or confidential document bodies. | Must | Data safety |
| REQ-KG-012 | Verification must include full unit, integration, API contract, and frontend E2E checks for touched layers, plus diff hygiene, network/dependency, and secret scans. | Must | Slice roadmap Phase 4 |

## Assumptions

- Existing Phase 2 graph tables are placeholders; this slice may add fields, repositories, services, endpoints, and migrations required by the accepted API guide.
- `review-publish` is an upstream gate. If it is not implemented when this slice starts, Codex must use approved/published seed or fixture data and record the dependency rather than bypassing review status.
- Real graph extraction may later use a model or graph engine, but this slice must verify with deterministic/mock projection and no external network calls.

## Open Questions

| ID | Question | Default for this SDD |
|---|---|---|
| OQ-KG-001 | Should manual SME editing of graph edges be delivered in this slice or in `review-publish`? | Include review action records for graph edges, but keep rich graph editing out of scope. |
| OQ-KG-002 | What is the production graph layout engine? | Not selected. Use frontend-rendered graph data and keep engine-specific layout replaceable. |
| OQ-KG-003 | Is `review-publish` complete before implementation starts? | Treat as an entry dependency; use approved fixtures if needed for tests. |
