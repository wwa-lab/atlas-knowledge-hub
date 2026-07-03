# User Stories: Vector Adapter

## Status

Draft. Derived from `docs/01-requirements/vector-adapter-requirements.md`.

## Story Map

| Story | Title | Requirements |
|---|---|---|
| US-VA-001 | Index source chunks through a vector adapter boundary | REQ-VA-001, REQ-VA-004, REQ-VA-005, REQ-VA-006, REQ-VA-010 |
| US-VA-002 | Inspect vector adapter capabilities safely | REQ-VA-002, REQ-VA-003, REQ-VA-009 |
| US-VA-003 | Query vector evidence with trace and review status | REQ-VA-007, REQ-VA-011 |
| US-VA-004 | Deindex vector entries without changing source metadata | REQ-VA-008, REQ-VA-012 |
| US-VA-005 | Guard vector seams and verification | REQ-VA-001, REQ-VA-009, REQ-VA-010, REQ-VA-013, REQ-VA-014 |

## US-VA-001: Index source chunks through a vector adapter boundary

**Story:**
As a delivery lead,
I want approved or review-aware source chunks indexed through a replaceable vector adapter,
so that later Ask and retrieval workflows can use searchable evidence without binding Atlas to one vector engine.

### Acceptance Criteria

1. **Given** source chunks exist for a batch
   **When** a vector indexing run is requested
   **Then** Atlas resolves a vector adapter before any index operation executes.
2. **Given** a chunk is indexed
   **When** the per-chunk result is recorded
   **Then** it includes chunk id, file item id, source file, page/section, confidence, review status, adapter key, vector item key, status, and safe error if any.
3. **Given** a chunk is review-required
   **When** it is indexed
   **Then** its review status remains review-required and the index result must not mark it as trusted or approved.
4. **Given** automated tests run
   **When** vector indexing is verified
   **Then** the tests use a mock/in-memory vector engine and require no real vector database, embeddings provider, network, or credentials.

### Notes / Assumptions

- Existing parser metadata provides source chunks and review status.
- This slice indexes traceable chunk metadata and vector payloads; it does not generate embeddings.

### Dependencies

- Metadata API and parser adapter source chunk persistence.

### Out of Scope

- Real vector database execution.
- Model/embedding provider calls.
- Ask answer generation.

### Open Questions

- OQ-VA-002: Source of real embeddings remains a future model/worker decision.

## US-VA-002: Inspect vector adapter capabilities safely

**Story:**
As a platform administrator,
I want to see vector adapter capability and health metadata,
so that I can confirm retrieval infrastructure is configured without exposing secrets or private endpoints.

### Acceptance Criteria

1. **Given** vector adapters are configured
   **When** capabilities are listed
   **Then** the response includes adapter key, display name, version/status, supported dimensions, supported operations, default marker, and masked configuration.
2. **Given** an adapter is unavailable or misconfigured
   **When** capabilities are listed
   **Then** Atlas returns a safe status and no raw endpoint, DSN, collection name, credential, token, hostname, or private path.
3. **Given** multiple adapters exist
   **When** the registry resolves an adapter
   **Then** an explicit adapter key is honored, otherwise the default adapter is selected.

### Notes / Assumptions

- pgvector/vector DB is named as a target class, not a hardcoded product dependency.

### Dependencies

- Backend API envelope and adapter registry patterns.

### Out of Scope

- Production secret manager integration.

### Open Questions

- OQ-VA-001: First real vector engine choice.

## US-VA-003: Query vector evidence with trace and review status

**Story:**
As a knowledge user,
I want vector similarity results to include source references and review status,
so that later Ask workflows can separate trusted evidence from material that still needs SME review.

### Acceptance Criteria

1. **Given** indexed chunks exist
   **When** a similarity query runs
   **Then** the response returns bounded results with chunk id, file item id, source file, page/section, score, confidence, review status, and safe metadata.
2. **Given** no matching chunks are found
   **When** a query runs
   **Then** Atlas returns an empty result set with a safe message and no raw vector internals.
3. **Given** review-required chunks exist
   **When** a query does not explicitly include review-required evidence
   **Then** only approved evidence is returned by default.
4. **Given** a query explicitly includes review-required evidence
   **When** results include such chunks
   **Then** those results remain visibly `REVIEW_REQUIRED`.

### Notes / Assumptions

- Query input may use deterministic mock vectors or precomputed vectors in this slice.

### Dependencies

- Vector index records and source chunk metadata.

### Out of Scope

- Natural-language question answering, reranking, answer synthesis, or citation formatting.

### Open Questions

- OQ-VA-003: Product default for review-required query inclusion after Ask hardening.

## US-VA-004: Deindex vector entries without changing source metadata

**Story:**
As a knowledge base administrator,
I want to remove vector index entries for a space, batch, file, or chunk,
so that stale retrieval entries can be cleaned up without deleting source metadata or review history.

### Acceptance Criteria

1. **Given** vector entries exist for a batch
   **When** a deindex operation is requested
   **Then** Atlas removes only vector index entries and records a vector run/result summary.
2. **Given** source chunks and file items exist
   **When** deindex completes
   **Then** source chunks, file item status, confidence, and review status remain unchanged.
3. **Given** the adapter fails during deindex
   **When** the run is reported
   **Then** Atlas records safe failure details without raw engine output.

### Notes / Assumptions

- Deindex is metadata/index lifecycle cleanup only.

### Dependencies

- Vector run and per-item result records.

### Out of Scope

- Deleting source chunks, Wiki pages, graph nodes, or files.

### Open Questions

- None.

## US-VA-005: Guard vector seams and verification

**Story:**
As a Codex implementation agent,
I want executable tasks and seam guards for vector adapter work,
so that implementation can proceed without direct engine coupling or secret leakage.

### Acceptance Criteria

1. **Given** implementation tasks are executed
   **When** guard tests scan non-adapter product layers
   **Then** vector engine names and SDK/client references are forbidden outside adapter scope.
2. **Given** the vector adapter package is scanned
   **When** the seam guard runs
   **Then** vector-engine names may appear only in the vector adapter boundary or implementation, while outbound network clients remain banned unless a future worker contract explicitly approves them.
3. **Given** final verification runs
   **When** `mvn verify`, seam scans, diff hygiene, and secret/private-path scans complete
   **Then** the result can be reported with evidence and no unrun check implied as passed.

### Notes / Assumptions

- Existing `AdapterSeamGuardTest` already forbids `pgvector`, `Milvus`, and `Qdrant` globally; this slice must update it intentionally for adapter scope only.

### Dependencies

- Accepted SDD docs and backend test harness.

### Out of Scope

- Remote services, paid jobs, external cloud dependencies, or production credentials.

### Open Questions

- None.
