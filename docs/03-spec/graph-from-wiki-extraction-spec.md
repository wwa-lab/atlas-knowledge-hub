# Feature Specification: graph-from-wiki-extraction

Spec status: accepted by standard-preauthorized prompt.

## Overview

Graph projection must become Wiki-driven. The existing graph projection endpoint remains the entry point, but eligible Wiki pages become the primary extraction source and source chunks remain evidence anchors.

## Functional Requirements

### Eligibility

- REQ-GRAPH-FROM-WIKI-EXTRACTION-001: A Wiki page is trusted graph input only when its review status is `APPROVED` or `PUBLISHED`, it has at least one source reference or chunk reference, and confidence is absent or at least `0.800`.
- REQ-GRAPH-FROM-WIKI-EXTRACTION-001: Ineligible Wiki pages are not trusted graph input. Projection items record safe skip reasons: `UNAPPROVED_WIKI_PAGE`, `LOW_CONFIDENCE_WIKI_PAGE`, or `MISSING_WIKI_SOURCE_TRACE`.
- REQ-GRAPH-FROM-WIKI-EXTRACTION-005: Evidence responses expose IDs, title/section labels, review status, confidence, page, section, and safe source file names only.

### Extraction Rules

- REQ-GRAPH-FROM-WIKI-EXTRACTION-002: Graph IDs are deterministic: they are derived from space ID, Wiki page ID/slug, source document ID, source chunk ID, and normalized labels.
- REQ-GRAPH-FROM-WIKI-EXTRACTION-003: Each eligible Wiki page yields one `WIKI_PAGE` node.
- REQ-GRAPH-FROM-WIKI-EXTRACTION-003: Each safe source document ID on the Wiki page yields one `DOCUMENT` node and a `DERIVED_FROM` edge from Wiki page to document.
- REQ-GRAPH-FROM-WIKI-EXTRACTION-003: Each chunk reference yields one `CONCEPT` node labeled from the Wiki reference label or section and a `MENTIONS` edge from Wiki page to concept.
- REQ-GRAPH-FROM-WIKI-EXTRACTION-004: Wiki `outLinks` to other eligible pages yield `RELATED_TO` edges between Wiki page nodes.
- REQ-GRAPH-FROM-WIKI-EXTRACTION-004: Every node and edge stores `evidence_wiki_page_ids`; chunk-derived objects also store `evidence_chunk_ids`.

### API Behavior

- REQ-GRAPH-FROM-WIKI-EXTRACTION-006: `POST /api/spaces/{spaceId}/graph/projection-runs` keeps `scope: APPROVED_ONLY` and `adapterId: deterministic`.
- REQ-GRAPH-FROM-WIKI-EXTRACTION-006: Existing graph query filters continue to work.
- REQ-GRAPH-FROM-WIKI-EXTRACTION-007: `GET /api/spaces/{spaceId}/graph/nodes/{nodeId}` returns mixed evidence references with a `referenceType` discriminator.

### Frontend Behavior

- REQ-GRAPH-FROM-WIKI-EXTRACTION-007: The Graph detail panel lists Wiki page evidence and source chunk evidence separately by reference type.
- REQ-GRAPH-FROM-WIKI-EXTRACTION-006: Existing graph search, node/edge filters, evidence-only toggle, empty, unauthorized, and error states remain unchanged.

## Acceptance Matrix

| ID | Verification |
|---|---|
| AC-GRAPH-FROM-WIKI-EXTRACTION-001 | Backend unit/API tests prove eligible Wiki pages are extracted and ineligible pages are skipped. |
| AC-GRAPH-FROM-WIKI-EXTRACTION-002 | Backend tests prove repeated projection does not duplicate graph records. |
| AC-GRAPH-FROM-WIKI-EXTRACTION-003 | API contract tests prove node detail includes Wiki and chunk evidence without unsafe content. |
| AC-GRAPH-FROM-WIKI-EXTRACTION-004 | Frontend tests and E2E prove Wiki-derived graph evidence is visible. |

## Constraints

- No model-assisted graph extraction.
- No external graph service, cloud call, provider payload, or real data.
- No auth/RBAC/audit/secret/rate-limit semantic change.
- Existing Graph product surface must not regress.

## Task IDs

- T-GRAPH-FROM-WIKI-EXTRACTION-001
- T-GRAPH-FROM-WIKI-EXTRACTION-002
- T-GRAPH-FROM-WIKI-EXTRACTION-003
- T-GRAPH-FROM-WIKI-EXTRACTION-004
- T-GRAPH-FROM-WIKI-EXTRACTION-005
- T-GRAPH-FROM-WIKI-EXTRACTION-006
- T-GRAPH-FROM-WIKI-EXTRACTION-007
- T-GRAPH-FROM-WIKI-EXTRACTION-008
