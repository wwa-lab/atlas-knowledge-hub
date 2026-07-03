# User Stories: Knowledge Graph

## Status

Draft. Derived from `docs/01-requirements/knowledge-graph-requirements.md`.

## Stories

### US-KG-001: Explore An Evidence-Backed Graph

As a knowledge user, I want to explore graph nodes and relationships that are backed by approved evidence, so that I can understand a knowledge space without trusting unreviewed content.

**Acceptance criteria**

1. Given a knowledge space has approved graph data, when I open Graph, then I see nodes, edges, type legend, counts, and search/filter controls.
2. Given I hover or focus a node, when detail is available, then the UI exposes label, type, confidence, and review status.
3. Given I click a node or edge, when evidence exists, then I see linked Wiki/source chunk references without raw confidential document bodies.
4. Given a source item is unapproved or missing source trace, when graph data is projected, then it is excluded from trusted graph results.

**Maps to:** REQ-KG-001, REQ-KG-002, REQ-KG-003, REQ-KG-004, REQ-KG-008, REQ-KG-009.

### US-KG-002: Query Graph Data Through A Hardened API

As a frontend engineer, I want a stable graph API contract, so that the Graph tab can render trusted graph data without knowing persistence or extraction details.

**Acceptance criteria**

1. Given a valid user has access to a space, when the frontend requests `/api/spaces/{spaceId}/graph`, then the API returns an Atlas envelope with bounded nodes, edges, counts, filters, and selected detail metadata.
2. Given invalid filters or unknown ids are submitted, when the API validates the request, then it returns field-level safe errors.
3. Given an unauthorized user calls the API, when authorization fails, then the backend returns `401` or `403` without exposing sensitive internals.
4. Given graph evidence references are returned, then they include source trace, confidence, and review status.

**Maps to:** REQ-KG-004, REQ-KG-005, REQ-KG-006, REQ-KG-011.

### US-KG-003: Project Graph Records Behind A Replaceable Boundary

As a platform administrator, I want graph extraction to be isolated behind a product-facing projection boundary, so that Atlas can change graph engines later without changing product workflows.

**Acceptance criteria**

1. Given approved Wiki/source trace records exist, when projection runs, then graph nodes and edges are created or refreshed through a product-facing service boundary.
2. Given a future graph/model engine is added, then engine-specific code remains inside adapter/worker boundary code and not in controllers, frontend components, or generic metadata services.
3. Given projection encounters unsupported, unapproved, or unsafe evidence, then the item is skipped with a safe reason and audit evidence.
4. Given projection fails partially, then valid outputs remain review-aware and failure details are sanitized.

**Maps to:** REQ-KG-001, REQ-KG-007, REQ-KG-009, REQ-KG-010, REQ-KG-011.

### US-KG-004: Audit And Govern Graph Trust

As an SME reviewer or delivery lead, I want graph projection and review actions to be auditable, so that trusted graph relationships can be explained and disputed.

**Acceptance criteria**

1. Given projection creates or updates graph records, when the run completes, then audit metadata records actor/system, time, scope, counts, and safe summary.
2. Given an SME reviews a graph edge, when the action is saved, then the edge review status and append-only review/audit record reflect the action.
3. Given a relationship is not backed by evidence, when it is inspected, then it cannot be marked as trusted.
4. Given graph data is used by downstream Ask later, then source trace, confidence, and review status remain present.

**Maps to:** REQ-KG-004, REQ-KG-006, REQ-KG-010, REQ-KG-012.

## Dependencies

- Phase 2 metadata API and graph placeholder tables.
- Phase 3 parser/storage/vector/model adapter seams.
- `review-publish` or approved fixture data for approved/published Wiki and source trace.

## Out Of Scope

- Real external graph database integration.
- Ask/RAG answer generation.
- Raw parser-output graphing.
- Rich graph editing UI beyond inspect/review actions.

## Open Questions

- See OQ-KG-001 through OQ-KG-003 in requirements.
