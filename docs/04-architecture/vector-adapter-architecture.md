# Architecture: Vector Adapter

## Status

Draft. Phase 3 adapter slice. Derived from `docs/03-spec/vector-adapter-spec.md`.

## Overview

Vector-adapter extends the metadata control plane with a replaceable vector retrieval seam. The backend owns vector run state, source-chunk eligibility, review-aware query policy, safe error handling, and result persistence; vector index execution is isolated behind a product-facing adapter contract. pgvector/vector DB is the first named target class, but the architecture keeps Milvus, Qdrant, and future engines replaceable.

## Architectural Drivers

| Driver | Impact |
|---|---|
| Adapter neutrality | Product services resolve vector adapters through registry/capability contracts. |
| Source trace preservation | Every index/query result points back to source chunks, file items, source file, page/section, confidence, and review status. |
| Review-aware retrieval | Query defaults to approved evidence and visibly marks review-required evidence when explicitly included. |
| Mock-only verification | Tests use a mock/in-memory vector engine and do not require real vector DBs, embeddings, network, or credentials. |
| Secret/endpoint safety | Capability and error output is masked and bounded. |
| Phase discipline | Vector-adapter prepares retrieval evidence but does not generate embeddings, synthesize Ask answers, publish Wiki, or derive graph entities. |

## Existing Metadata Context

The current metadata control plane already owns source chunks, file items, review status, confidence, graph metadata placeholders, API envelopes, parser adapter run patterns, and adapter seam guarding. Vector-adapter should reuse these product concepts instead of creating a vector-owned knowledge model. Code-level grounding anchors are recorded in the design and traceability artifacts, where implementation-facing detail belongs.

## System Context

| Boundary | Responsibility |
|---|---|
| Frontend | Out of scope for this slice. Existing UI may later consume vector query APIs, but no frontend changes are part of vector-adapter. |
| Backend API / metadata control plane | Owns vector capability endpoints, vector run lifecycle, validation, result persistence, and query responses. |
| Vector adapter seam | Encapsulates vector engine-specific index/delete/query execution and returns Atlas product concepts. |
| Vector engine / worker | External to product workflow. pgvector, Milvus, Qdrant, or other engines sit behind the adapter contract only. |
| PostgreSQL metadata | Stores vector run records and vector item result evidence; source chunks/file items remain the source of truth for trace and review state. |

## High-Level Architecture

```text
+------------------------------------------------------------+
| Users / agents                                             |
| Admin, platform admin, future Ask flow, Codex implementation|
+------------------------------+-----------------------------+
                               |
                               | REST / JSON
                               v
+------------------------------------------------------------+
| Spring Boot metadata API                                   |
| Vector capabilities, vector runs, vector query evidence     |
+------------------------------+-----------------------------+
                               |
                               v
+------------------------------------------------------------+
| Vector application service                                 |
| Scope validation, chunk eligibility, adapter resolution,    |
| review policy, safe error handling, run/result persistence  |
+------------------------------+-----------------------------+
                               |
                   product-facing adapter interface
                               v
+------------------------------+        +---------------------+
| Vector adapter registry      |        | Vector adapters      |
| capability + default policy  |------->| mock/in-memory       |
+------------------------------+        | pgvector boundary    |
                                        | Milvus/Qdrant future |
                                        +----------+----------+
                                                   |
                                                   | engine/worker boundary
                                                   v
                                        +---------------------+
                                        | Vector database      |
                                        | outside product flow |
                                        +---------------------+
                               |
                               v
+------------------------------------------------------------+
| PostgreSQL metadata                                        |
| source_chunk, file_item, vector_run, vector_item_result     |
+------------------------------------------------------------+
```

## Component Breakdown

### Backend API

- **Vector adapter capability API:** lists vector capabilities and masked configuration.
- **Vector run API:** executes index/deindex runs for scoped source chunks and returns run records.
- **Vector query API:** returns bounded similarity evidence with source trace and review status.

### Application Services

- **Vector service:** validates space/batch/file/chunk targets, review policy, dimensions, and result limits; resolves adapter; executes mock/configured mode; maps results; persists run evidence; and builds query responses.
- **Vector summary calculator:** derives total/indexed/deleted/skipped/failed counts from per-item results.
- **Vector adapter registry:** owns default adapter resolution and unavailable/misconfigured behavior.
- **Safety helpers:** reuse safe relative-path/error-masking practices and add vector-specific masking for endpoints, DSNs, collection names, and raw SDK output.

### Integration Adapters

- **VectorAdapter contract:** accepts Atlas vector requests and returns Atlas descriptors/results.
- **MockVectorAdapter:** deterministic in-memory implementation for CI and integration tests.
- **PgVectorAdapter:** real adapter boundary placeholder or configured implementation. It may know engine details, but product layers must not.

### Persistence

- Reuse `source_chunk` and `file_item` as trace/review source of truth.
- Add `vector_run` and `vector_item_result` logical entities for execution evidence and index/query result metadata.
- Do not create `wiki_page`, `graph_node`, or `graph_edge` rows in this slice.

## State And Status Strategy

Vector run status:

```text
REQUESTED -> RUNNING -> SUCCEEDED
                     -> PARTIAL_FAILED
                     -> FAILED
```

Vector item status mapping:

- index success -> `INDEXED`
- deindex success -> `DELETED`
- policy or eligibility exclusion -> `SKIPPED`
- item failure -> `FAILED`
- invalid target -> rejected before adapter execution

Vector operations do not change `file_item.review_status`, `source_chunk.review_status`, confidence, Wiki state, or graph state.

## API / Interface Boundaries

| Interface | Consumer | Purpose |
|---|---|---|
| `GET /api/vector-adapters` | Admin / implementation tests | List masked vector capabilities. |
| `POST /api/spaces/{spaceId}/vector-runs` | Internal workflow / future UI | Execute index/deindex runs for source chunks. |
| `GET /api/vector-runs/{runId}` | Delivery lead / future UI | Read run record and per-item outcomes. |
| `POST /api/spaces/{spaceId}/vector-query` | Future Ask/search flow | Return bounded similarity evidence. |
| `VectorAdapter` | Vector service | Execute vector work behind product-facing interface. |

## Security / Reliability / Observability

- Capability responses are masked/status-only.
- Adapter errors are bounded and sanitized before persistence or response.
- Query responses never return raw vector values or engine diagnostics.
- Run/result evidence preserves auditable trace (space, batch, file item, source chunk, review status, score).
- Mock/in-memory engines are mandatory for automated verification.
- The adapter seam guard must allow vector-engine names inside the vector adapter package only and keep them forbidden in non-adapter product layers.

## Risks / Tradeoffs

| ID | Risk / Tradeoff | Mitigation |
|---|---|---|
| R-VA-001 | Real vector engine topology is not finalized. | Keep execution behind adapter; mock contract remains stable. |
| R-VA-002 | Embedding generation belongs to model/worker scope, not this slice. | Accept precomputed/mock vectors and defer model-provider calls. |
| R-VA-003 | Review-required chunks could be mistaken for trusted evidence. | Default queries to approved-only and preserve visible review status. |
| R-VA-004 | Existing seam guard forbids vector engine names even inside adapter scope. | Update guard intentionally so vector names are allowed only in adapter package. |

## Open Questions

- OQ-VA-001: First real vector engine.
- OQ-VA-002: Future embedding source.
- OQ-VA-003: Review-required query inclusion policy for future Ask.
