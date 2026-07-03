# Specification: Vector Adapter

## Status

Draft. Phase 3 adapter slice. Behavior source of truth for `vector-adapter`. Derived from `docs/02-user-stories/vector-adapter-stories.md`.

## Source Documents

- `docs/01-requirements/vector-adapter-requirements.md`
- `docs/02-user-stories/vector-adapter-stories.md`
- `docs/03-spec/parser-adapter-spec.md`
- `docs/03-spec/metadata-api-spec.md`
- `docs/architecture.md`
- `docs/markdown-standard.md`
- `docs/knowledge-graph-design.md`
- `docs/00-context/slice-roadmap.md`

## Scope

Atlas must support vector indexing and vector similarity evidence retrieval through a product-facing vector adapter contract. The slice includes vector capability behavior, index/deindex runs, query evidence responses, source-chunk trace preservation, vector run records, per-chunk result records, mock-engine verification, adapter seam guards, and internal API/adapter guidance. It does not implement real vector database execution, embedding/model calls, Ask answer generation, graph extraction, Wiki publication, frontend UI, production auth/RBAC, or production secret management.

## Constraints

- **Adapter only:** product workflow code depends on vector interfaces and registry contracts, not pgvector, Milvus, Qdrant, vector SDKs, JDBC vector extensions, or outbound vector clients directly (REQ-VA-001).
- **No single hardcoded implementation:** pgvector/vector DB is one target class but must be replaceable through adapter configuration and capability metadata (REQ-VA-002).
- **Mock-engine verification:** automated tests use a mock/in-memory vector engine and require no real vector database, embedding provider, network, or credentials (REQ-VA-010).
- **Secret/endpoint safety:** capability responses and errors must not expose raw endpoint, DSN, collection name, credentials, tokens, hostnames, stack traces, or private paths (REQ-VA-003, REQ-VA-009).
- **Trace/review preservation:** index and query results preserve source chunk trace, confidence, and review status; vector operations never auto-approve content (REQ-VA-004, REQ-VA-011).
- **Phase discipline:** vector runs do not generate embeddings, create graph nodes/edges, publish Wiki pages, or answer Ask queries (REQ-VA-012).

## Actors

| Actor | Role |
|---|---|
| Knowledge base administrator | Starts index/deindex runs and inspects run reports. |
| Platform administrator | Reviews vector adapter availability and masked configuration. |
| Knowledge user / future Ask flow | Consumes traceable similarity evidence. |
| SME reviewer | Relies on review status staying visible for retrieved evidence. |
| Codex implementation agent | Implements strictly against this spec and task checklist after SDD acceptance. |

## Functional Requirements

### Adapter Boundary

- **FR-VA-001:** Vector workflows must resolve a vector adapter through a registry/capability contract before any index, deindex, or query operation starts. (US-VA-001, US-VA-005)
- **FR-VA-002:** Non-adapter product layers must not reference pgvector, Milvus, Qdrant, vector SDKs, JDBC vector extensions, or outbound vector clients for vector work. (US-VA-005)
- **FR-VA-003:** The adapter registry must support at least one configured default vector adapter and expose unavailable/misconfigured states safely. (US-VA-002)

### Capability Metadata

- **FR-VA-004:** Capability metadata must include adapter key, display name, version, supported dimensions, supported operations, default marker, health/status, and masked configuration summary. (US-VA-002)
- **FR-VA-005:** Capability metadata must not expose raw endpoints, DSNs, collection names, credentials, tokens, hostnames, or private paths. (US-VA-002)

### Indexing And Deindexing

- **FR-VA-006:** An index run must accept a space/batch scope, optional file/chunk filters, adapter key, mode, review inclusion policy, and a bounded set of chunk vector payloads or deterministic mock vectors. (US-VA-001)
- **FR-VA-007:** Index input must reference existing source chunks and preserve chunk id, file item id, source file, page/section, confidence, and review status. (US-VA-001)
- **FR-VA-008:** A successful index result must record vector item key, adapter key, source chunk trace, review status, status, and timestamps. (US-VA-001)
- **FR-VA-009:** A deindex run must remove vector entries by space, batch, file item, or chunk id without deleting source chunks or changing file/chunk review state. (US-VA-004)
- **FR-VA-010:** Vector run summaries must include total, indexed, deleted, skipped, failed counts, adapter key, mode, terminal status, timestamps, and safe message. (US-VA-001, US-VA-004)

### Similarity Query

- **FR-VA-011:** Similarity query must accept a space scope, query vector or deterministic mock query token, result limit, and review inclusion flag. (US-VA-003)
- **FR-VA-012:** Similarity query results must be bounded and sorted by score descending with stable tie-break by chunk id. (US-VA-003)
- **FR-VA-013:** Query results must include chunk id, file item id, source file, page/section, score, confidence, review status, and safe metadata only. (US-VA-003)
- **FR-VA-014:** Query results must default to approved evidence only; review-required evidence is returned only when explicitly requested and remains visibly review-required. (US-VA-003)
- **FR-VA-015:** Query responses must not return raw vector values, raw collection internals, SDK payloads, or database-specific diagnostics. (US-VA-003)

### Validation And Failure Behavior

- **FR-VA-016:** Unknown adapter, space, batch, file item, or source chunk references must fail safely before index/deindex mutation. (US-VA-001, US-VA-004)
- **FR-VA-017:** Vector dimensions must match adapter capability when dimensions are provided; invalid dimensions fail validation before adapter execution. (US-VA-001)
- **FR-VA-018:** Adapter faults must produce sanitized safe messages and leave source metadata unchanged. (US-VA-004, US-VA-005)
- **FR-VA-019:** Seam guard verification must allow vector-engine names only inside adapter scope and ban them in controller/service/repository/domain layers. (US-VA-005)

## Non-Functional Requirements

| Category | Requirement |
|---|---|
| Security | No raw endpoints, DSNs, collection names, credentials, tokens, hostnames, private paths, stack traces, or raw SDK output in responses or persisted safe messages. |
| Reliability | Mock/in-memory vector tests cover capability listing, index, deindex, query, review policy, invalid dimension, unavailable adapter, safe error masking, and seam guard behavior. |
| Extensibility | Vector adapter interface allows future pgvector, Milvus, Qdrant, or other vector implementations without changing product workflow callers. |
| Auditability | Vector run and result records are traceable to space, batch, file item, source chunk, adapter identity, review status, and timestamps. |
| Data safety | Use mock/sample metadata only; no real company documents, raw vectors from confidential data, private paths, external network calls, or production credentials in tests. |

## Workflow

```text
+------------------------------+
| Source chunks + review state  |
+---------------+--------------+
                |
                v
+------------------------------+        unavailable/misconfigured
| Resolve vector adapter        |--------------------------------+
+---------------+--------------+                                |
                | available                                     v
                v                                      +------------------+
+------------------------------+                       | Safe run failure |
| Validate scope, policy,       |                       | no unsafe leak   |
| dimensions, chunk references  |                       +------------------+
+---------------+--------------+
                |
                v
+------------------------------+
| Execute vector adapter        |
| mock/in-memory in CI          |
+---------------+--------------+
                |
                v
+------------------------------+
| Persist run/result evidence   |
| preserve trace/review state   |
+------------------------------+
```

## State Model

### Vector Run Status

`REQUESTED -> RUNNING -> SUCCEEDED | PARTIAL_FAILED | FAILED`

- `REQUESTED -> RUNNING`: adapter resolved and validation passed.
- `RUNNING -> SUCCEEDED`: all requested index/deindex evidence operations completed safely.
- `RUNNING -> PARTIAL_FAILED`: at least one item succeeded and at least one failed or was skipped.
- `RUNNING -> FAILED`: no item succeeded, or an adapter-level failure prevented per-item results.

### Vector Item Status

| Outcome | Item Status | Notes |
|---|---|---|
| Chunk indexed successfully | `INDEXED` | Requires source chunk trace and vector item key. |
| Chunk deindexed successfully | `DELETED` | Removes vector entry only. |
| Chunk skipped | `SKIPPED` | Policy or eligibility excluded it before adapter execution. |
| Item failed | `FAILED` | User-safe error summary required. |
| Invalid target | rejected | Validation error before index/deindex; no state change. |

## Validation Rules

- Space, batch, file item, and source chunk ids must reference existing metadata records for scoped operations.
- Index input must carry a source chunk id and file item id; source trace must be recoverable from existing metadata.
- Review policy values are `approved_only` and `include_review_required`; default is `approved_only`.
- Result limit must be bounded and positive.
- Dimensions, when provided, must equal the adapter capability dimension.
- Similarity scores must be finite numbers in the inclusive range `0.000` to `1.000`.
- Safe messages and safe errors must be bounded and stripped of raw SDK output, stack traces, private paths, endpoints, DSNs, collection names, hostnames, tokens, and credentials.
- Adapter key must resolve to a registered adapter or fail with a safe unavailable/misconfigured status.

## API / Interface Surface

The full contract lives in `docs/05-design/contracts/vector-adapter-API_IMPLEMENTATION_GUIDE.md`.

| Interface | Behavior |
|---|---|
| `GET /api/vector-adapters` | Lists configured vector adapter capabilities with masked configuration. |
| `POST /api/spaces/{spaceId}/vector-runs` | Executes an index or deindex run for scoped source chunks. |
| `GET /api/vector-runs/{runId}` | Returns vector run summary and per-chunk outcomes. |
| `POST /api/spaces/{spaceId}/vector-query` | Runs a bounded similarity query over indexed evidence. |
| Internal vector adapter interface | Indexes/deletes/queries vector evidence behind a product-facing contract; mock/in-memory implementations are required for tests. |

## Acceptance Matrix

| Check | Requirement | Observable Result |
|---|---|---|
| AC-VA-01 | REQ-VA-001, REQ-VA-013 | Static guard tests fail on direct vector-engine references outside adapter implementation. |
| AC-VA-02 | REQ-VA-002, REQ-VA-003 | Capability contract returns masked adapter metadata and replaceable default marker. |
| AC-VA-03 | REQ-VA-004, REQ-VA-006 | Mock index run records source chunk trace, confidence, and review status per indexed item. |
| AC-VA-04 | REQ-VA-007, REQ-VA-011 | Query returns bounded, scored results with source trace and review status; approved-only is default. |
| AC-VA-05 | REQ-VA-008, REQ-VA-012 | Deindex removes vector entries without deleting source metadata or graph/wiki records. |
| AC-VA-06 | REQ-VA-009 | Error responses/log assertions show no raw secrets, endpoints, collection names, private paths, or SDK output. |
| AC-VA-07 | REQ-VA-010 | `cd backend && mvn verify` passes using the mock/in-memory vector engine only. |
| AC-VA-08 | REQ-VA-014 | Tasks include REQ/spec mapping and exact commands. |

## Out Of Scope

- Real pgvector/Milvus/Qdrant execution, embedding generation, model-provider calls, RAG answer generation, Ask UI, reranking, graph derivation, Wiki publication, frontend screens, production auth/RBAC, and production secret-manager integration.

## Open Questions

| ID | Question | Impact |
|---|---|---|
| OQ-VA-001 | pgvector vs standalone vector DB as the first real engine. | Affects deployment topology, not the current mock contract. |
| OQ-VA-002 | Future embedding source: model-adapter vs offline worker. | This SDD accepts precomputed/mock vectors and does not call model providers. |
| OQ-VA-003 | Review-required query inclusion default for future Ask. | Current contract defaults to approved-only and allows explicit inclusion. |
