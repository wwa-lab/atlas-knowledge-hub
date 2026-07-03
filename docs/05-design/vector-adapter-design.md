# Detailed Design: Vector Adapter

## Status

Draft. Phase 3 adapter slice. Derived from `docs/03-spec/vector-adapter-spec.md` and `docs/04-architecture/vector-adapter-architecture.md`.

## Source Architecture

Vector-adapter is a backend/API + adapter contract slice. It adds vector capability, index/deindex, and query evidence behavior to the metadata control plane while keeping vector engine details inside adapter implementations. The design mirrors the converter/parser adapter run/report shape so Codex can implement the slice with existing local patterns.

## Grounded Existing Code Context

These anchors were verified before design:

| Existing element | Verified anchor | Vector-adapter use |
|---|---|---|
| Source chunk trace fields | `backend/src/main/java/com/atlas/metadata/domain/SourceChunk.java:21` | Reuse file item id, source file, page/section, confidence, and review status as vector evidence. |
| File item trace/review fields | `backend/src/main/java/com/atlas/metadata/domain/FileItem.java:24` | Validate batch/file scope while preserving status, confidence, and review status. |
| Parser run evidence pattern | `backend/src/main/java/com/atlas/metadata/domain/ParserRun.java:13` | Mirror run lifecycle and safe message pattern. |
| Parser result evidence pattern | `backend/src/main/java/com/atlas/metadata/domain/ParserFileResult.java:14` | Mirror per-item result persistence style. |
| Parser adapter contract | `backend/src/main/java/com/atlas/metadata/adapter/ParserAdapter.java:3` | Mirror product-facing adapter interface shape. |
| Parser capability metadata | `backend/src/main/java/com/atlas/metadata/adapter/ParserCapability.java:9` | Mirror masked capability record. |
| Parser registry | `backend/src/main/java/com/atlas/metadata/service/ParserAdapterRegistry.java:11` | Mirror explicit/default adapter resolution. |
| API envelope | `backend/src/main/java/com/atlas/metadata/dto/ApiEnvelope.java:1` | All vector responses use `ApiEnvelope`. |
| Parser controller pattern | `backend/src/main/java/com/atlas/metadata/controller/ParserController.java:18` | Follow capability/create/get route shape. |
| Adapter seam guard | `backend/src/test/java/com/atlas/metadata/integration/AdapterSeamGuardTest.java:15` | Must be updated: vector-engine names are currently banned in adapter scope too. |

## Design Scope

### In Scope

- Vector adapter domain contracts, capability metadata, request/result shapes.
- Vector run persistence and per-item result persistence.
- Vector service behavior: target validation, adapter resolution, review policy, dimension validation, execution, summary calculation, safe error handling, and query result mapping.
- Vector API guide and contract tests.
- Mock/in-memory vector adapter for deterministic tests.

### Out of Scope

- Real vector runtime topology, embedding/model provider calls, Ask answer synthesis, graph extraction, Wiki publication, frontend UI, production auth/RBAC, and production secret manager integration.

## Module Design

### Vector Adapter Contract

Conceptual contract:

```text
VectorAdapter
  capability() -> VectorCapability
  index(VectorIndexRequest) -> VectorIndexResult
  delete(VectorDeleteRequest) -> VectorDeleteResult
  query(VectorQueryRequest) -> VectorQueryResult
```

`VectorCapability` contains:

- adapter key and display name
- safe version/status
- supported dimensions
- supported operations: `INDEX`, `DEINDEX`, `QUERY`
- default marker
- masked configuration summary (endpoint, DSN, collection, credentials as status-only)

`VectorIndexRequest` contains:

- run id, space/batch scope, review policy, mode
- index items containing source chunk id, file item id, trace metadata, review status, confidence, and vector payload/reference

`VectorQueryRequest` contains:

- space id, query vector or deterministic mock query token, limit, review policy

`VectorQueryResult` contains:

- scored item matches with safe vector item keys and source chunk references

### Vector Service

Responsibilities:

- Validate space, optional batch, file item, and source chunk scope.
- Validate review policy, result limit, dimensions, and adapter key.
- Select eligible chunks: approved-only by default, review-required included only when explicitly requested.
- Resolve adapter by explicit key or default marker.
- Create vector run, mark running, execute adapter, persist validated item results.
- Compute summary counts and terminal status.
- Return query evidence by joining adapter matches back to source chunk/file metadata.
- Sanitize safe messages and safe errors before persistence/response.

### Vector Summary Calculator

| Count | Rule |
|---|---|
| `total` | All candidate items considered by the run. |
| `indexed` | Status `INDEXED`. |
| `deleted` | Status `DELETED`. |
| `skipped` | Status `SKIPPED`. |
| `failed` | Status `FAILED`. |

Terminal run status:

- `SUCCEEDED` when all executable items succeed.
- `PARTIAL_FAILED` when at least one item succeeds and at least one item fails or is skipped.
- `FAILED` when no item succeeds or adapter-level failure prevents per-item results.

### Persistence

Add logical domain entities equivalent to the data model:

- `VectorRun`
- `VectorItemResult`
- `VectorRunOperation`, `VectorRunStatus`, `VectorItemStatus`, `VectorReviewPolicy`, `VectorAdapterStatus`

Use repositories only for vector run/result persistence and existing source chunk/file lookups. Do not mutate source chunks, file items, Wiki pages, or graph rows.

### DTOs And Mapping

DTOs should match the API guide:

- `VectorCapabilityResponse`
- `CreateVectorRunRequest`
- `VectorRunResponse`
- `VectorRunSummaryResponse`
- `VectorItemResultResponse`
- `VectorQueryRequest`
- `VectorQueryResponse`
- `VectorQueryMatchResponse`

All API responses use `ApiEnvelope`.

## API / Interface Design

The API implementation guide is authoritative for payloads:

- `GET /api/vector-adapters`
- `POST /api/spaces/{spaceId}/vector-runs`
- `GET /api/vector-runs/{runId}`
- `POST /api/spaces/{spaceId}/vector-query`

Authentication remains deferred/internal-only for this slice.

## Data Design

Data model lives in `docs/04-architecture/vector-adapter-data-model.md`.

Important invariants:

- No new `FileStatus` values.
- Source chunk and file item review/confidence/status are never changed by vector operations.
- `wiki_page`, `graph_node`, and `graph_edge` rows are untouched.
- Query results do not expose raw vectors or engine internals.
- Review-required evidence remains labeled as review-required.

## Workflow / Execution Design

### Successful Index

1. API receives vector run request with operation `index`.
2. Service validates space, optional batch/file/chunk scope, review policy, and dimensions.
3. Service loads eligible source chunks and file items.
4. Registry resolves vector adapter.
5. Service creates vector run and marks it `RUNNING`.
6. Adapter indexes each item and returns per-item outcomes.
7. Service validates adapter results and persists `vector_item_result` rows.
8. Service computes summary and terminal status.
9. API returns vector run response.

### Successful Query

1. API receives vector query request.
2. Service validates space, limit, review policy, and query vector/mock token.
3. Registry resolves vector adapter.
4. Adapter returns scored matches.
5. Service joins matches back to source chunk/file metadata.
6. Service applies approved-only filtering by default and stable score sorting.
7. API returns safe evidence matches.

### Adapter Unavailable

- Vector run becomes `FAILED` for index/deindex.
- Query returns safe adapter unavailable error.
- Source metadata remains unchanged.

### Unsafe Output

- Raw vectors, endpoints, DSNs, collection names, SDK output, stack traces, and private paths are stripped from safe messages/errors.

## Validation And Error Handling

| Case | Expected handling |
|---|---|
| Unknown space | 404 safe not found. |
| Unknown batch/file/chunk in requested scope | 404 safe not found or 400 validation error without leaking unrelated membership. |
| Unknown adapter key | 400 validation error. |
| Adapter unavailable/misconfigured | Safe failed run or safe query error. |
| Invalid dimension | 400 validation error before adapter execution. |
| Invalid result limit | 400 validation error. |
| Raw vector in response candidate | Drop raw vector fields and persist only safe metadata. |
| Adapter exception | Safe `FAILED` status/message; source metadata unchanged. |

## Edge Case Trace

### Review Policy

Rule: query defaults to `APPROVED_ONLY`.

| Input | Result |
|---|---|
| Approved chunk, default policy | Returned when score qualifies. |
| Review-required chunk, default policy | Filtered out. |
| Review-required chunk, `INCLUDE_REVIEW_REQUIRED` | Returned with `REVIEW_REQUIRED` label. |

### Score Sorting

Rule: sort by score descending, then chunk id ascending for ties.

| Input | Result |
|---|---|
| `chunk-b=0.91`, `chunk-a=0.87` | `chunk-b`, then `chunk-a`. |
| `chunk-b=0.91`, `chunk-a=0.91` | `chunk-a`, then `chunk-b`. |
| score missing or non-finite | Result rejected as invalid adapter output. |

### Dimension Validation

Rule: provided vector dimension must equal adapter capability dimension.

| Input | Result |
|---|---|
| capability `1536`, vector length `1536` | Accepted. |
| capability `1536`, vector length `768` | Rejected before adapter execution. |
| mock token with no vector length | Accepted only in mock mode. |

## Seam Guard Update (Required)

`AdapterSeamGuardTest` currently forbids `pgvector`, `Milvus`, and `Qdrant` in all adapter source. The vector adapter legitimately references these vector-engine concepts, so implementation must:

- Relax the adapter-scope assertion so the vector adapter package may contain vector-engine names.
- Add vector-engine names and SDK/client types to the non-adapter forbidden list so they remain banned outside adapter scope.
- Keep `WebClient`, `RestTemplate`, and `HttpClient` forbidden outside adapter scope.

Record this change in tasks and traceability so the guard update is intentional.

## Testing Considerations

Required implementation verification:

```bash
cd backend && mvn verify
git diff --check
! rg -n "pgvector|Milvus|Qdrant|EmbeddingClient|VectorStore|VectorDb|JDBC vector|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/vector-adapter-requirements.md docs/02-user-stories/vector-adapter-stories.md docs/03-spec/vector-adapter-spec.md docs/04-architecture/vector-adapter-architecture.md docs/04-architecture/vector-adapter-data-flow.md docs/04-architecture/vector-adapter-data-model.md docs/05-design/vector-adapter-design.md docs/05-design/contracts/vector-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/vector-adapter-tasks.md
```

Test coverage must include:

- Adapter contract tests with mock vector engine.
- Summary calculator unit tests.
- Service validation, review policy, dimensions, and query sorting tests.
- API contract integration tests.
- Seam guard test update for vector engine references.
