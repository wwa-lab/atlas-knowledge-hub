# Detailed Design: Storage Adapter

## Status

Draft. Phase 3 adapter slice. Derived from `docs/03-spec/storage-adapter-spec.md` and `docs/04-architecture/storage-adapter-architecture.md`.

## Source Architecture

Storage-adapter is a backend/API + adapter contract slice. It adds storage capability/operation behavior to the metadata control plane while keeping storage engine details inside adapter implementations. The design intentionally mirrors the converter-adapter and parser-adapter capability/run/report shape so Codex can implement the next adapter slice with minimal new product concepts.

## Grounded Existing Code Context

These anchors were verified before design:

| Existing element | Verified anchor | Storage-adapter use |
|---|---|---|
| File artifact pointer fields | `backend/src/main/java/com/atlas/metadata/domain/FileItem.java:45` | Reuse `pdfPath`, `markdownPath`, `assetsPath`, `errorMessage` as pointers updated via the metadata boundary. |
| Artifact write helper | `backend/src/main/java/com/atlas/metadata/domain/FileItem.java:85` | `setArtifacts(...)` is the review-preserving path for pointer write-back; do not mutate status/confidence during storage. |
| Review preservation pattern | `backend/src/main/java/com/atlas/metadata/domain/FileItem.java:92` | Storage write-back must preserve review status like conversion writes. |
| Relative path validator | `backend/src/main/java/com/atlas/metadata/validation/RelativePathValidator.java:1` | Reuse for object-key and layer-path validation. |
| Relative path annotation | `backend/src/main/java/com/atlas/metadata/validation/RelativePath.java:1` | Reuse on request DTO key fields. |
| Converter capability record | `backend/src/main/java/com/atlas/metadata/adapter/ConverterCapability.java:9` | Mirror shape for `StorageCapability`. |
| Converter adapter status enum | `backend/src/main/java/com/atlas/metadata/enums/ConverterAdapterStatus.java:1` | Pattern for `StorageAdapterStatus`. |
| Converter API pattern | `backend/src/main/java/com/atlas/metadata/controller/ConversionController.java:21` | Storage controller should follow the capability/create/get shape. |
| API envelope | `backend/src/main/java/com/atlas/metadata/dto/ApiEnvelope.java:1` | All storage responses use `ApiEnvelope`. |
| Adapter seam guard | `backend/src/test/java/com/atlas/metadata/integration/AdapterSeamGuardTest.java:15` | **Must be updated**: adapter-scope asserts `.doesNotContain("S3")` (line 42) — relax for the storage adapter package and add storage-engine names to the non-adapter forbidden list. |

## Design Scope

### In Scope

- Storage adapter domain contracts, capability metadata, request/descriptor shapes.
- Storage operation persistence and per-object descriptor persistence.
- Storage service behavior: target validation, adapter resolution, layer/key resolution, execution, descriptor validation, summary calculation, safe error handling, pointer write-back.
- Storage API guide and contract tests.
- Mock/in-memory storage adapter for deterministic tests.

### Out of Scope

- Real storage runtime topology, presigned URLs, retention/lifecycle policy, converter/parser execution, LLM/OCR, frontend UI, graph/Ask, Wiki publication, vector/model adapters, production auth/RBAC.

## Module Design

### Storage Adapter Contract

Conceptual contract:

```text
StorageAdapter
  capability() -> StorageCapability
  put(StoragePutRequest) -> StorageObjectDescriptor
  get(StorageObjectRef) -> StorageObjectDescriptor
  exists(StorageObjectRef) -> boolean
  list(StorageListRequest) -> StorageListResult
  delete(StorageObjectRef) -> StorageDeleteResult
```

`StorageCapability` contains:

- adapter key and display name
- safe version/status
- supported layers: `RAW`, `PDF`, `MARKDOWN`, `ASSETS`, `REPORTS`, `WIKI`
- default marker
- masked configuration summary (endpoint/bucket/region/credentials as status-only)

`StoragePutRequest` contains:

- operation id
- batch/workspace scope
- layer
- safe relative object key
- content type
- content reference (stream/handle; never raw bytes in metadata)

`StorageObjectDescriptor` contains:

- layer, object key
- size, checksum, content type
- adapter key, status, safe error

`StorageListRequest` contains:

- batch/workspace scope, layer prefix
- page size limit and page/continuation marker

### Storage Service

Responsibilities:

- Validate batch/workspace and any target file ids.
- Validate each operation: layer membership, safe relative key, namespace ownership.
- Resolve adapter by explicit key or default marker.
- Create storage operation, mark running, execute adapter, persist validated descriptors.
- Write returned keys back to the correct `file_item` pointer field via `setArtifacts(...)`-style updates, preserving status/confidence/review status.
- Compute summary (counts + total bytes) and terminal status.
- Sanitize safe messages and safe errors before persistence/response.

### Storage Summary Calculator

The summary is derived from persisted stored-object descriptors:

| Count | Rule |
|---|---|
| `total` | All requested operations. |
| `stored` | Status `STORED`. |
| `deleted` | Status `DELETED`. |
| `missing` | Status `MISSING`. |
| `failed` | Status `FAILED`. |
| `skipped` | Operations rejected/ineligible before adapter execution. |
| `totalBytes` | Sum of `sizeBytes` for `STORED` descriptors. |

### Storage Persistence

Add logical domain entities equivalent to the data model:

- `StorageOperation`
- `StorageObject`
- `StorageOperationType`, `StorageOperationStatus`, `StorageObjectStatus`, `StorageLayer`
- `StorageAdapterStatus` mirrors the converter status naming pattern.

Use repositories only for storage operation/object persistence and existing file pointer updates. `wiki_page` remains untouched.

### Storage Mapper / DTOs

DTOs should match the API guide:

- `StorageCapabilityResponse`
- `CreateStorageOperationRequest`
- `StorageOperationResponse`
- `StorageObjectResponse`
- `StorageOperationSummaryResponse`
- `StorageObjectListResponse` (paginated)

All API responses use `ApiEnvelope`.

## API / Interface Design

The API implementation guide is authoritative for payloads:

- `GET /api/storage-adapters`
- `POST /api/batches/{batchId}/storage-operations`
- `GET /api/storage-operations/{operationId}`
- `GET /api/batches/{batchId}/storage-objects`

Validation failures use the existing API envelope/error handling style. Authentication remains deferred/internal-only for this slice.

## Data Design

Data model lives in `docs/04-architecture/storage-adapter-data-model.md`.

Important invariants:

- No new `FileStatus` values; storage does not change file status/confidence/review status.
- Object keys and layer paths must pass relative-path validation within the namespace.
- `source_chunk` and `wiki_page` rows are untouched.
- Storage operation summaries are derived from descriptor rows.
- `checksum` is required for `STORED` descriptors.

## Workflow / Execution Design

### Successful Store

1. API receives storage operation request.
2. Service validates batch/workspace and file ids.
3. Service validates each operation (layer, safe key, namespace).
4. Registry resolves storage adapter.
5. Service creates storage operation and marks it `RUNNING`.
6. Adapter stores each object and returns descriptors.
7. Service validates each descriptor.
8. Service persists `storage_object`, updates `file_item` pointers, persists operation record.
9. Service computes summary and terminal status.
10. API returns storage operation response.

### Adapter Unavailable

- Storage operation becomes `FAILED`.
- File item pointers remain unchanged.
- Response contains safe message only.

### Unsafe Operation

- Validation fails before any object is written or deleted.
- Response uses validation error shape.
- No partial writes: reject the operation set as invalid; default to no metadata mutation.

## Validation And Error Handling

| Case | Expected handling |
|---|---|
| Unknown batch | 404 safe not found. |
| Unknown target file (pointer write-back) | 404 safe not found. |
| Cross-batch file id | 400 validation error or 404 safe not found; do not reveal unrelated batch membership. |
| Unknown/unsupported layer | 400 validation error with safe field message. |
| Unsafe key (absolute/URI/drive/traversal) | 400 validation error before persistence. |
| Out-of-namespace target | 400 validation error; no object touched. |
| Adapter throws runtime exception | Storage operation `FAILED`; pointers unchanged; sanitized safe message. |
| Storage output contains raw endpoints/buckets/secrets | Store only sanitized `safeError`/`safeMessage`. |

## Edge Case Trace

### Layer Resolution

Rule: `layer + objectKey` resolve to one safe relative path in the namespace.

| Input | Result |
|---|---|
| `MARKDOWN` + `batch-001/BRD.md` | Accepted -> `markdown/batch-001/BRD.md` |
| `PDF` + `../raw/BRD.docx` | Rejected (traversal) |
| unknown layer `TEMP` | Rejected (unknown layer) |

### Key Safety

Rule: object keys must be safe relative keys.

| Input | Result |
|---|---|
| `batch-001/report.json` | Accepted |
| `/var/data/report.json` | Rejected |
| `s3://bucket/report.json` | Rejected |
| Windows drive-prefixed key | Rejected |

### Descriptor Safety

Rule: `STORED` descriptors require checksum and non-negative size.

| Input | Result |
|---|---|
| `STORED`, size `1024`, checksum `sha256:...` | Accepted |
| `STORED`, size `1024`, checksum empty | Rejected |
| `STORED`, size `-1` | Rejected |

## Seam Guard Update (Required)

`AdapterSeamGuardTest` currently asserts the adapter package `.doesNotContain("S3")` (line 42). The storage adapter legitimately references S3-compatible engine concepts, so implementation must:

- Relax the adapter-scope assertion so the storage adapter package may contain storage-engine names (mirror the existing `trinity-office` allowance in adapter scope).
- Add storage-engine names and SDK types (e.g. `AmazonS3`, `software.amazon.awssdk`, `MinioClient`, `S3Client`) to the non-adapter `ENGINE_REFERENCES` forbidden list so they are still banned outside the adapter package.
- Keep `WebClient`, `RestTemplate`, and `HttpClient` forbidden in non-adapter layers.

Record this change in the tasks and traceability so the guard update is intentional, not incidental.

## Testing Considerations

Required implementation verification:

```bash
cd backend && mvn verify
git diff --check
! rg -n "AmazonS3|software\\.amazon\\.awssdk|MinioClient|MinIO|S3Client|putObject|getObject|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/storage-adapter-requirements.md docs/02-user-stories/storage-adapter-stories.md docs/03-spec/storage-adapter-spec.md docs/04-architecture/storage-adapter-architecture.md docs/04-architecture/storage-adapter-data-flow.md docs/04-architecture/storage-adapter-data-model.md docs/05-design/storage-adapter-design.md docs/05-design/contracts/storage-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/storage-adapter-tasks.md
```

Test coverage must include:

- Adapter contract tests with the mock storage engine (store/get/exists/list/delete).
- Summary calculator unit tests.
- Service validation, layer resolution, and namespace-scope tests.
- Pointer write-back tests that assert review status/confidence/status are preserved.
- API contract integration tests.
- Seam guard test update for storage engine references.
- Secret/path sanitization tests.

## Risks / Design Tradeoffs

| ID | Risk / Tradeoff | Decision |
|---|---|---|
| DT-SA-001 | The existing seam guard forbids `S3` in adapter scope. | Update the guard to allow storage-engine names inside the storage adapter package only, and ban them elsewhere. |
| DT-SA-002 | Passing bytes through the API could bloat/leak. | API returns descriptors only; bytes stay behind the adapter (OQ-SA-002). |
| DT-SA-003 | Per-object invalid result could be partially persisted. | Default to rejecting the operation set before persistence to keep metadata clean. |
| DT-SA-004 | Storage could accidentally change file lifecycle status. | Storage write-back updates pointer fields only; status/confidence/review status are preserved. |

## Open Questions

- OQ-SA-001: First real storage engine (S3-compatible vs filesystem).
- OQ-SA-002: Whether object bytes ever transit the API.
- OQ-SA-003: Deferral of presigned/short-lived access URLs.
