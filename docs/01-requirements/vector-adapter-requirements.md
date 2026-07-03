# Requirements: Vector Adapter

## Status

Draft. Phase 3 adapter slice. SDD only; no product code is implemented in this pass.

## Slice Contract

- **Goal:** Atlas can index approved or review-aware source chunks into a replaceable vector adapter and run source-trace-preserving similarity queries without coupling product workflows to pgvector, Milvus, Qdrant, or any single vector database engine.
- **Slice:** `vector-adapter`
- **Phase:** 3 adapter
- **Sources:** `README.md`, `PROJECT_RULES.md`, `DEVELOPMENT_STANDARDS.md`, `docs/00-context/sdd-profile.md`, `docs/00-context/slice-roadmap.md`, `docs/01-requirements/requirement.md`, `docs/architecture.md`, `docs/markdown-standard.md`, `docs/knowledge-graph-design.md`, `docs/03-spec/parser-adapter-spec.md`, `docs/03-spec/metadata-api-spec.md`, and existing backend metadata code verified during grounding.
- **Verification row:** Phase 3 adapter requires unit + integration tests against mock engines.
- **Hard constraints:** Parser/converter/model/vector/storage must go only through product-facing adapters; tools must never be called directly; one implementation must not be hardcoded; secrets and private endpoints must be masked; source trace, confidence, and review status must be preserved.

## In Scope

- A product-facing vector adapter contract for index upsert, delete, query, and capability metadata.
- Capability metadata for replaceable vector engines with masked configuration and default marker.
- Vector collection/index policy scoped by Knowledge Space, batch, file item, and source chunk.
- Indexable chunk descriptors based on existing source trace metadata: source file, file item, page/section, chunk id, confidence, review status, and text reference.
- Vector run records and per-chunk index result records for audit and troubleshooting.
- Similarity query results that return source chunk references, score, confidence, review status, and safe metadata only.
- Mock/in-memory vector adapter, unit/integration tests, and adapter seam guard updates.
- Internal API and adapter contract guidance for implementation.

## Exclusions

- Real pgvector/Milvus/Qdrant execution, real embedding generation, model-provider calls, RAG answer generation, Ask UI behavior, graph extraction, Wiki publication, ranking/re-ranking, production auth/RBAC, production secret manager integration, and frontend changes.
- Indexing unreviewed content as trusted knowledge. Review-required chunks may be indexed only if clearly marked as review-required in metadata and query results.
- Real company documents, private endpoints, raw credentials, logs, external cloud calls, or external network dependencies.

## Requirements

| ID | Requirement | Priority | Source / Rationale |
|---|---|---|---|
| REQ-VA-001 | Product workflow code must resolve vector operations through a vector adapter registry and must not call pgvector, Milvus, Qdrant, JDBC vector extensions, vector SDKs, or outbound vector clients directly outside adapter scope. | Must | REQ-PROD-015, REQ-PROD-017, Adapter Standards |
| REQ-VA-002 | pgvector/vector DB must be represented as one replaceable vector adapter option, not the only possible implementation; future engines must remain configurable. | Must | REQ-PROD-017, REQ-PROD-060, REQ-PROD-061 |
| REQ-VA-003 | Vector capability metadata must expose adapter key, display name, version/status, supported dimensions, supported operations, default marker, and masked configuration summary without raw endpoint, DSN, collection name, credentials, token, or private path. | Must | REQ-PROD-049, REQ-PROD-058, REQ-PROD-061 |
| REQ-VA-004 | Index input must be derived from Atlas source chunks or approved/review-aware Markdown metadata and must preserve workspace, batch, file item, source file, page/section, chunk id, confidence, and review status. | Must | REQ-PROD-023, REQ-PROD-034, REQ-PROD-041 |
| REQ-VA-005 | Vector indexing must support bounded upsert runs by space/batch/file/chunk selection and record run status, adapter key, mode, counts, timestamps, and safe message. | Must | Phase 3 adapter evidence |
| REQ-VA-006 | Per-chunk vector index results must record chunk id, file item id, source trace, review status, vector item key, status, score/index metadata, and safe error. | Must | Source trace preservation |
| REQ-VA-007 | Similarity query must return bounded results with chunk id, file item id, source file, page/section, score, confidence, review status, and safe metadata; it must not return raw vectors or raw engine internals. | Must | REQ-PROD-040, REQ-PROD-042 |
| REQ-VA-008 | Delete/deindex operations must support safe removal by space, batch, file item, or chunk id without deleting source metadata or changing review state. | Should | Lifecycle hygiene |
| REQ-VA-009 | Vector adapter failures must return sanitized errors with no raw SDK output, stack trace, credentials, endpoint, DSN, hostname, collection name, or private/absolute path. | Must | Security/Data Standards |
| REQ-VA-010 | Automated verification must use a mock/in-memory vector engine and must not require pgvector, Milvus, Qdrant, real embeddings, external network calls, or credentials. | Must | Phase 3 verification row |
| REQ-VA-011 | Query and index behavior must distinguish approved evidence from review-required evidence; review-required content must remain visibly review-required and must not be promoted to trusted answer material. | Must | REQ-PROD-026, REQ-PROD-041, REQ-PROD-042 |
| REQ-VA-012 | Vector runs must not create graph nodes/edges, publish Wiki pages, or invoke model adapters; they only prepare traceable vector index metadata. | Must | Phase discipline |
| REQ-VA-013 | Adapter seam guards must allow vector-engine names inside the vector adapter package only and continue banning them from controller/service/repository/domain product layers. | Must | Adapter boundary |
| REQ-VA-014 | SDD and implementation tasks must keep backend/API contracts explicit and Codex-actionable with exact verification commands. | Must | Goal-driven SDD |

## Acceptance

- Complete bilingual SDD artifacts exist for requirements, stories, spec, architecture, data flow, data model, design, API guide, tasks, and traceability.
- REQ/US/T IDs match across English and Simplified Chinese copies.
- Tasks map to requirement IDs and spec sections and include exact verification commands.
- API guide is included because this Phase 3 adapter slice defines internal backend/API and adapter contracts.
- No product code is changed in this SDD pass.

## Open Questions

| ID | Question | Impact |
|---|---|---|
| OQ-VA-001 | Should the first real vector engine be PostgreSQL/pgvector or a standalone vector database? | Deployment detail; does not block the mock adapter contract. |
| OQ-VA-002 | Should embeddings be supplied by a future model-adapter slice or accepted as precomputed vectors from an offline worker? | This SDD uses precomputed vector payloads or deterministic mock vectors and does not call model providers. |
| OQ-VA-003 | Should review-required chunks be queryable by default, or only with an explicit include-review-required flag? | This SDD defaults to approved-only query behavior unless explicitly requested. |
