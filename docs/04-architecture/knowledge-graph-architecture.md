# Architecture: Knowledge Graph

## Status

Draft. Phase 4 hardening. Derived from `docs/03-spec/knowledge-graph-spec.md`.

## Overview

The knowledge graph architecture turns existing graph metadata placeholders into a trusted graph surface. The backend owns authorization, projection orchestration, API contracts, audit, and persistence; frontend owns rendering and interaction; graph extraction stays behind a replaceable projection boundary.

## Architectural Drivers

| Driver | Impact |
|---|---|
| Evidence-first graph | Nodes and edges are derived from approved/published Wiki and source trace only. |
| Phase 4 hardening | Backend auth/RBAC, audit, user-safe errors, and full layer testing are required. |
| Adapter neutrality | Projection logic is product-facing and replaceable; no graph/model/vector engine is hardcoded into product workflow. |
| FE baseline parity | Graph tab keeps accepted prototype canvas, legend, search, hover, click detail, and evidence path behavior. |
| Data safety | API responses show evidence metadata, not raw confidential document bodies or secrets. |

## System Context

| Boundary | Responsibility |
|---|---|
| Frontend Graph tab | Renders bounded graph data, filters/searches, and evidence detail. |
| Backend Graph API | Validates requests, enforces auth, returns Atlas envelopes, and hides sensitive internals. |
| Graph projection service | Builds/refreshes graph records from eligible Wiki/source-trace metadata. |
| Projection adapter boundary | Future graph/model extraction engines live here only. |
| PostgreSQL metadata | Stores graph nodes, edges, projection runs, evidence references, and audit/review records. |

## High-Level Architecture

```text
--------------------------------------------------------------+
| Users                                                        |
| Knowledge user · SME reviewer · Delivery lead · Admin        |
+------------------------------+-------------------------------+
                               |
                               | HTTPS / JSON
                               v
+-------------------------------------------------------------+
| Vue Frontend                                                  |
| Graph tab · filters/search · canvas · evidence detail         |
+------------------------------+-------------------------------+
                               |
                               | REST / Atlas envelope
                               v
+-------------------------------------------------------------+
| Spring Boot Metadata API                                      |
| GraphController · auth/RBAC · validation · safe errors        |
+------------------------------+-------------------------------+
                               |
                               v
+-------------------------------------------------------------+
| Graph Domain Services                                         |
| Query service · projection service · review/audit service     |
+------------------------------+-------------------------------+
                               |
             product-facing projection/adapter seam
                               v
+------------------------------+       +-----------------------+
| Metadata Persistence         |       | Future worker/engine   |
| wiki/source chunks/graph/audit|       | Optional, replaceable  |
+------------------------------+       +-----------------------+
```

## Component Breakdown

### Frontend Components

- **GraphViewer:** Renders the graph canvas, selected state, legend, and detail panel.
- **GraphFilters:** Search, type filters, review-status filter, confidence threshold, and evidence-only toggle.
- **GraphEvidencePanel:** Displays selected node/edge evidence references, confidence, review status, and safe source summaries.
- **GraphApiClient/Store:** Fetches graph envelopes and exposes loading/error/unauthorized/empty states.

### Backend Services

- **GraphController:** Owns HTTP endpoints and envelope responses.
- **GraphQueryService:** Returns bounded graph views, node detail, counts, and filters.
- **GraphProjectionService:** Creates or refreshes graph records from eligible approved/published metadata.
- **GraphReviewService:** Applies review actions to graph edges and writes append-only review/audit records.
- **GraphAuditService:** Records projection and governance events with sanitized summaries.

### Projection Adapter Boundary

- **GraphProjectionAdapter:** Product-facing contract for future extraction engines. It accepts Atlas source descriptors and returns candidate graph descriptors, never vendor-specific payloads.
- **DeterministicGraphProjectionAdapter:** Initial mock/deterministic adapter for CI and local verification.
- Future graph/model engines are allowed only behind this boundary.

## Security And Data Safety

- Backend authorization is mandatory for all graph endpoints.
- `401` and `403` responses use the Atlas envelope and do not expose internal policy details.
- API responses do not include raw secrets, private absolute paths, raw vectors, raw prompts, stack traces, SQL, or confidential document body text.
- Source references use relative paths and chunk/page identifiers.

## State Strategy

Projection run states:

```text
REQUESTED -> RUNNING -> SUCCEEDED
                     -> PARTIAL_FAILED
                     -> FAILED
```

Graph trust states reuse `ReviewStatus`: `REVIEW_REQUIRED`, `APPROVED`, `NEED_FIX`, `OCR_REQUIRED`, `PUBLISHED`. Trusted graph results default to approved/published only.

## Architecture Risks

| ID | Risk | Mitigation |
|---|---|---|
| R-KG-001 | `review-publish` may not exist before this slice. | Use approved fixture data and record dependency; never bypass review status. |
| R-KG-002 | Graph visualization could grow too dense. | API returns bounded views and counts; UI supports filters and detail-on-select. |
| R-KG-003 | Future extraction engine could leak into product code. | Add seam guard tests and keep adapter package as the only engine boundary. |
