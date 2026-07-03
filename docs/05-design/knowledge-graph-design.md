# Detailed Design: Knowledge Graph

## Overview

This design defines the UI, backend service, projection, API, validation, and testing contract for the Phase 4 `knowledge-graph` slice.

## Design Scope

- Full-stack graph query and projection contract.
- Frontend Graph tab backed by API data while preserving prototype interaction patterns.
- Backend authorization, audit, safe errors, and adapter boundary discipline.
- No production graph database, no real external engine, no Ask answer generation.

## Module Design

### Frontend

| Module | Design |
|---|---|
| `GraphViewer` | Uses bounded `nodes` and `edges` from API; keeps canvas/legend/detail interactions aligned with `frontend/public/atlas-prototype.html`. |
| `GraphFilters` | Search text, node type filter, edge type filter, review status filter, and evidence-only toggle. |
| `GraphEvidencePanel` | Shows evidence summaries: Wiki page id/title, source chunk id, source file display name, page/section, confidence, review status. |
| `graphApi` | Typed client for graph view, node detail, projection run, projection run read, and edge review action. |
| Graph store | Owns loading, error, unauthorized, selected node/edge, filters, and refresh state. |

### Backend

| Module | Design |
|---|---|
| Graph controller | Thin REST layer, request validation, envelope responses, auth annotations/policy hooks. |
| Graph query service | Applies filters/limits and maps records to safe graph DTOs. |
| Graph projection service | Validates eligible evidence, calls projection adapter, persists graph records and item outcomes. |
| Graph review service | Validates review action and writes append-only review/audit records. |
| Graph projection adapter | Product-facing seam; deterministic adapter first, real engines deferred. |

## API / Interface Design

The API guide is included because this slice introduces graph endpoints.

Primary endpoints:

- `GET /api/spaces/{spaceId}/graph`
- `GET /api/spaces/{spaceId}/graph/nodes/{nodeId}`
- `POST /api/spaces/{spaceId}/graph/projection-runs`
- `GET /api/graph/projection-runs/{runId}`
- `POST /api/spaces/{spaceId}/graph/edges/{edgeId}/review-actions`

All responses use Atlas `ApiEnvelope`. Lists are bounded and include `meta`.

## Validation And Error Handling

- `spaceId`, `nodeId`, `edgeId`, and `runId` must be stable identifiers.
- `limit` is bounded; invalid filters return `400` field-level errors.
- Unknown space/node/edge/run returns `404`.
- Unauthorized access returns `401`; forbidden access returns `403`.
- Projection rejects unapproved, missing-trace, unsupported, unsafe-path, or no-evidence candidates.
- User-visible errors must be sanitized.

## UI / User Flow Design

1. User opens Knowledge Space Graph tab.
2. Frontend loads graph view and shows loading state.
3. Empty graph state explains that approved/published evidence is required.
4. User searches or filters graph.
5. User selects a node or edge.
6. Detail panel shows type, review status, confidence, adjacent relationships, and evidence references.
7. SME reviewer can submit a review action for eligible edge if authorization allows it.

## Accessibility And Responsive Behavior

- Graph nodes and list equivalents must be keyboard reachable.
- Detail panel must expose selected graph object textually, not only visually.
- Tablet/mobile may use internal graph scrolling while preserving readable filters and evidence details.
- No text overlap or clipped controls at supported widths.

## Security / Audit / Reliability Design

- Backend auth/RBAC is enforced server-side.
- Projection and review actions are audited append-only.
- External network calls are not used for deterministic projection tests.
- Adapter seam guard prevents graph/model/vector engine leakage into product layers.
- Raw confidential content is not returned in graph DTOs.

## Testing Considerations

- Backend unit: eligibility, idempotent projection ids, exclusion reason codes, safe error masking.
- Backend integration/API: endpoint contracts, auth failures, persistence, audit append-only behavior.
- Frontend unit/component: graph mapping, filters, selected detail, error/empty/unauthorized states.
- E2E: open Graph, filter/search, select evidence-backed node/edge, verify trace/confidence/review fields.
- Scans: `git diff --check`, no new external network/dependencies, no raw secrets/private paths.

## Post-Implementation Review Guard

- The API-backed frontend acceptance surface is the Knowledge Space Graph tab itself.
- A shell-level or host-level hardening panel may be useful during transition, but it does not satisfy `GraphViewer`, `GraphFilters`, or Graph tab E2E acceptance by itself.
- Future close-out must include an E2E assertion inside `[data-tab="graph"]` that exercises search/filter controls, selection, evidence detail, and unauthorized/empty/error states.

## Risks / Design Tradeoffs

- Dense graph rendering is controlled by bounded API responses and filters.
- Real extraction engine is deferred to preserve adapter neutrality.
- `review-publish` dependency may require approved fixture data during implementation.

## Open Questions

- OQ-KG-001 through OQ-KG-003 remain open for product sequencing but do not block deterministic SDD handoff.
