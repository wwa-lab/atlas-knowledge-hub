# Tasks: Vector Adapter

## Status

Draft implementation checklist for `vector-adapter`. This document is executable only after the SDD set is accepted. This SDD pass does not implement product code.

## Source Design

- Spec: `docs/03-spec/vector-adapter-spec.md`
- Design: `docs/05-design/vector-adapter-design.md`
- API guide: `docs/05-design/contracts/vector-adapter-API_IMPLEMENTATION_GUIDE.md`
- Data model: `docs/04-architecture/vector-adapter-data-model.md`

## Constraints For Every Task

- Use a mock/in-memory vector engine for automated tests.
- Keep vector execution behind product-facing adapter contracts.
- Do not call pgvector, Milvus, Qdrant, vector SDKs, JDBC vector extensions, or outbound vector clients from controller/service/repository/domain layers.
- Do not introduce external network dependencies, cloud calls, real vector databases, real embedding/model calls, or credentials.
- Preserve source trace, confidence, and review status; vector operations do not change source chunks, file items, Wiki pages, or graph rows.
- Mask secrets, endpoints, DSNs, collection names, credentials, tokens, raw SDK output, hostnames, stack traces, and private paths.
- Use mock/sample metadata only; never persist raw confidential vectors or document content.

## Workstreams

| Workstream | Tasks |
|---|---|
| Domain and persistence | T-VA-001, T-VA-002 |
| Adapter contract | T-VA-003, T-VA-004 |
| Service behavior | T-VA-005, T-VA-006, T-VA-007 |
| API contract | T-VA-008 |
| Verification and guards | T-VA-009, T-VA-010 |

## Task Details

### T-VA-001: Add vector run domain model and migration

- **Maps to:** REQ-VA-004, REQ-VA-005, REQ-VA-006, REQ-VA-011, REQ-VA-012; spec sections "Indexing And Deindexing", "State Model"; data model `vector_run`, `vector_item_result`.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** None
- **Scope:** Add `VectorRunOperation`, `VectorRunStatus`, `VectorItemStatus`, `VectorReviewPolicy`, `VectorAdapterStatus` enums; `VectorRun` and `VectorItemResult` entities; repositories; and a Flyway migration for vector execution evidence. Do not add new `FileStatus` values and do not mutate graph/wiki tables.
- **Constraints:** Adapter boundary; mock-only test data; trace/review preservation; secret/endpoint safety.
- **Verification:**

```bash
cd backend && mvn verify
git diff --check
```

### T-VA-002: Add vector DTOs and mapping contracts

- **Maps to:** REQ-VA-003, REQ-VA-006, REQ-VA-007; spec sections "Capability Metadata", "Similarity Query", "API / Interface Surface"; API guide response shapes.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-VA-001
- **Scope:** Add vector capability, create-run request, run response, summary response, item result response, query request, query response, and query match response mapping. All responses use `ApiEnvelope` through controllers.
- **Constraints:** Secret-masked capability summaries; no raw runtime details or raw vectors in responses.
- **Verification:**

```bash
cd backend && mvn test
git diff --check
```

### T-VA-003: Define vector adapter interface and capability model

- **Maps to:** REQ-VA-001, REQ-VA-002, REQ-VA-003; spec sections "Adapter Boundary", "Capability Metadata"; design "Vector Adapter Contract".
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-VA-002
- **Scope:** Add product-facing `VectorAdapter` interface (`capability`, `index`, `delete`, `query`), capability record, index/delete/query request records, result records, and match descriptors. Include adapter key, safe status, supported dimensions, supported operations, default marker, and masked config summary.
- **Constraints:** Product concepts only; no direct vector-engine call in the interface; no network/client dependency.
- **Verification:**

```bash
cd backend && mvn -Dtest=VectorAdapterContractTest test
! rg -n "WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/adapter/VectorAdapter.java
```

### T-VA-004: Add mock and configured vector adapter implementations

- **Maps to:** REQ-VA-002, REQ-VA-010; spec sections "Adapter Boundary", "Indexing And Deindexing", "Similarity Query"; design "Vector Adapter Contract".
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-VA-003
- **Scope:** Add deterministic mock/in-memory vector adapter for CI and a configured pgvector/vector DB adapter boundary that reports safe capability metadata. Real vector execution remains behind the adapter and may be a safe placeholder until runtime topology is decided.
- **Constraints:** Mock-only verification; no external network; no raw endpoint/DSN/collection/credential/token/private path in capability response.
- **Verification:**

```bash
cd backend && mvn -Dtest=VectorAdapterContractTest test
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src/main/java/com/atlas/metadata/adapter
```

### T-VA-005: Implement vector adapter registry and capability listing

- **Maps to:** REQ-VA-001, REQ-VA-003; spec sections "Adapter Boundary", "Capability Metadata"; API guide `GET /api/vector-adapters`.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-VA-003, T-VA-004
- **Scope:** Add registry resolution for explicit/default vector adapter keys and unavailable/misconfigured statuses. Add capability listing service behavior with masked config.
- **Constraints:** Adapter boundary; secret masking; no hardcoded single implementation as the only future option.
- **Verification:**

```bash
cd backend && mvn -Dtest=VectorAdapterRegistryTest test
git diff --check
```

### T-VA-006: Implement vector run service and source-chunk validation

- **Maps to:** REQ-VA-004, REQ-VA-005, REQ-VA-006, REQ-VA-010, REQ-VA-011, REQ-VA-012; spec sections "Indexing And Deindexing", "Validation And Failure Behavior"; design "Vector Service".
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-VA-001, T-VA-003, T-VA-005
- **Scope:** Implement index/deindex run creation, source-chunk/file/batch/space validation, review policy filtering, dimension validation, adapter execution, result validation, summary calculation, terminal status mapping, and safe adapter-fault handling.
- **Constraints:** Mock-engine execution in tests; reject invalid scope/dimensions before adapter execution; no source metadata mutation.
- **Verification:**

```bash
cd backend && mvn -Dtest=VectorServiceTest,VectorSummaryCalculatorTest test
git diff --check
```

### T-VA-007: Implement review-aware query evidence mapping

- **Maps to:** REQ-VA-007, REQ-VA-009, REQ-VA-011; spec sections "Similarity Query", "Validation And Failure Behavior"; design "Successful Query".
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-VA-006
- **Scope:** Implement vector query service behavior: validate query vector/mock token and bounded limit, resolve adapter, map adapter matches back to source chunk/file metadata, default to approved-only filtering, support explicit review-required inclusion, sort by score descending and chunk id ascending, and sanitize safe metadata.
- **Constraints:** No raw vectors or engine internals in responses; review-required evidence remains visibly review-required.
- **Verification:**

```bash
cd backend && mvn -Dtest=VectorQueryServiceTest,VectorServiceTest test
git diff --check
```

### T-VA-008: Add vector REST API endpoints

- **Maps to:** REQ-VA-003, REQ-VA-005, REQ-VA-007, REQ-VA-014; spec section "API / Interface Surface"; API guide all endpoints.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-VA-005, T-VA-006, T-VA-007
- **Scope:** Add vector controller endpoints `GET /api/vector-adapters`, `POST /api/spaces/{spaceId}/vector-runs`, `GET /api/vector-runs/{runId}`, and `POST /api/spaces/{spaceId}/vector-query`. Responses must use `ApiEnvelope` and match the API guide.
- **Constraints:** Internal-only; no auth/RBAC implementation; no raw vector output; no Ask/RAG answer synthesis.
- **Verification:**

```bash
cd backend && mvn -Dit.test=VectorApiContractIT verify
git diff --check
```

### T-VA-009: Update adapter seam and safety guards

- **Maps to:** REQ-VA-001, REQ-VA-009, REQ-VA-010, REQ-VA-013; spec sections "Adapter Boundary", "Validation And Failure Behavior"; design "Seam Guard Update (Required)".
- **Owner type:** QA/backend
- **Priority:** Must
- **Dependencies:** T-VA-003, T-VA-004, T-VA-008
- **Scope:** Update `AdapterSeamGuardTest` so adapter-scope assertions allow vector-engine names inside the vector adapter package, while non-adapter product layers continue banning `pgvector`, `Milvus`, `Qdrant`, vector SDK/client names, JDBC vector extension calls, `WebClient`, `RestTemplate`, and `HttpClient`. Add secret/private-path scans for vector implementation and docs.
- **Constraints:** No direct vector calls outside adapter; secret-masked output only. Record the guard change intentionally in traceability.
- **Verification:**

```bash
cd backend && mvn -Dtest=AdapterSeamGuardTest test
! rg -n "pgvector|Milvus|Qdrant|EmbeddingClient|VectorStore|VectorDb|JDBC vector|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/vector-adapter-requirements.md docs/02-user-stories/vector-adapter-stories.md docs/03-spec/vector-adapter-spec.md docs/04-architecture/vector-adapter-architecture.md docs/04-architecture/vector-adapter-data-flow.md docs/04-architecture/vector-adapter-data-model.md docs/05-design/vector-adapter-design.md docs/05-design/contracts/vector-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/vector-adapter-tasks.md
```

### T-VA-010: Run final vector-adapter verification

- **Maps to:** REQ-VA-010, REQ-VA-011, REQ-VA-013, REQ-VA-014; spec acceptance matrix AC-VA-01 through AC-VA-08.
- **Owner type:** QA/backend
- **Priority:** Must
- **Dependencies:** T-VA-001 through T-VA-009
- **Scope:** Run the complete verification set, review the diff, and update traceability with evidence if implementation changes docs/status later.
- **Constraints:** Mock-only; adapter only; no external network/cloud/model calls; secret-masked; trace/review preserved; no graph/wiki/source metadata mutation.
- **Verification:**

```bash
cd backend && mvn verify
git diff --check
! rg -n "pgvector|Milvus|Qdrant|EmbeddingClient|VectorStore|VectorDb|JDBC vector|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/vector-adapter-requirements.md docs/02-user-stories/vector-adapter-stories.md docs/03-spec/vector-adapter-spec.md docs/04-architecture/vector-adapter-architecture.md docs/04-architecture/vector-adapter-data-flow.md docs/04-architecture/vector-adapter-data-model.md docs/05-design/vector-adapter-design.md docs/05-design/contracts/vector-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/vector-adapter-tasks.md
```

## Dependency Plan

- Critical path: T-VA-001 -> T-VA-002 -> T-VA-003 -> T-VA-004 -> T-VA-005 -> T-VA-006 -> T-VA-007 -> T-VA-008 -> T-VA-009 -> T-VA-010
- Parallel opportunities after T-VA-003: adapter contract tests and DTO mapper tests may be built alongside service tests.

## Open Questions / Risks

- OQ-VA-001: First real vector engine is deferred; the mock contract must not depend on it.
- OQ-VA-002: Real embedding generation is deferred to model-adapter or worker scope.
- OQ-VA-003: Query defaults to approved-only; product may revisit review-required inclusion during Ask hardening.
- R-VA-004: The existing seam guard forbids vector engine names in adapter scope; T-VA-009 must update it intentionally, not incidentally.
