# Architecture: graph-from-wiki-extraction

## Summary

This slice extends the existing layered Graph implementation. `GraphService` remains the orchestrator, `GraphProjectionAdapter` remains the deterministic extraction seam, and Wiki metadata becomes the trusted input set.

## Layered Architecture

```text
Users
  |
Vue Graph tab
  |
GraphController REST API
  |
GraphService
  |-- WikiPageRepository / SourceChunkRepository
  |-- GraphProjectionAdapter (deterministic local extraction)
  |-- GraphNodeRepository / GraphEdgeRepository / GraphProjectionItemRepository
  |
PostgreSQL via Flyway-managed tables
```

## Components

- REQ-GRAPH-FROM-WIKI-EXTRACTION-001: `WikiPageRepository` provides eligible and all space Wiki pages for extraction and skip evidence.
- REQ-GRAPH-FROM-WIKI-EXTRACTION-002: `GraphProjectionAdapter` receives Wiki descriptors and returns stable node/edge candidates.
- REQ-GRAPH-FROM-WIKI-EXTRACTION-004: `GraphService` persists graph objects with chunk and Wiki page evidence arrays.
- REQ-GRAPH-FROM-WIKI-EXTRACTION-005: `GraphEvidenceReferenceResponse` becomes a safe mixed evidence DTO.
- REQ-GRAPH-FROM-WIKI-EXTRACTION-007: Vue `App.vue` renders mixed graph evidence without changing layout architecture.

## Boundaries

- No parser, model, vector, or external graph engine is called by product logic.
- Existing authorization interceptors and path policy are reused without semantic changes.
- Audit behavior remains existing graph projection/review audit behavior only.
- Flyway migration is additive and limited to indexes or safe schema support if needed.

## Risks

- Wiki pages may not yet include rich entity metadata; deterministic extraction intentionally uses current metadata only.
- Natural-language relationship extraction is deferred because it would require model-assisted behavior.

## Review Result

Architecture-review result: ready for implementation within Tier 1 because it extends existing API/persistence contracts without changing auth, audit, secrets, providers, or production readiness semantics.

## Task IDs

- T-GRAPH-FROM-WIKI-EXTRACTION-001
- T-GRAPH-FROM-WIKI-EXTRACTION-002
- T-GRAPH-FROM-WIKI-EXTRACTION-003
- T-GRAPH-FROM-WIKI-EXTRACTION-004
- T-GRAPH-FROM-WIKI-EXTRACTION-005
- T-GRAPH-FROM-WIKI-EXTRACTION-006
- T-GRAPH-FROM-WIKI-EXTRACTION-007
- T-GRAPH-FROM-WIKI-EXTRACTION-008
