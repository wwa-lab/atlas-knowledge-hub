# Tasks: Storage Adapter

## Status

Draft implementation checklist for `storage-adapter`. This document is executable only after the SDD set is accepted. This SDD pass does not implement product code.

## Source Design

- Spec: `docs/03-spec/storage-adapter-spec.md`
- Design: `docs/05-design/storage-adapter-design.md`
- API guide: `docs/05-design/contracts/storage-adapter-API_IMPLEMENTATION_GUIDE.md`
- Data model: `docs/04-architecture/storage-adapter-data-model.md`

## Constraints For Every Task

- Use a mock/in-memory storage engine for automated tests.
- Keep storage execution behind product-facing adapter contracts.
- Do not call S3 SDKs, MinIO clients, filesystem object stores, or outbound HTTP clients from controller/service/repository/domain layers.
- Do not introduce external network dependencies or cloud calls.
- Preserve source trace, confidence, and review status; storage does not change `file_item` status/confidence/review status.
- Mask secrets, endpoints, buckets, regions, credentials, raw SDK output, hostnames, stack traces, and private paths.
- Use mock/sample metadata only; never persist raw object bytes in metadata.

## Workstreams

| Workstream | Tasks |
|---|---|
| Domain and persistence | T-SA-001, T-SA-002 |
| Adapter contract | T-SA-003, T-SA-004 |
| Service behavior | T-SA-005, T-SA-006, T-SA-007 |
| API contract | T-SA-008 |
| Verification and guards | T-SA-009, T-SA-010 |

## Task Details

### T-SA-001: Add storage operation domain model and migration

- **Maps to:** REQ-SA-004, REQ-SA-013, REQ-SA-014; spec sections "Storage Operations", "Layer And Namespace Policy"; data model `storage_operation`, `storage_object`.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** None
- **Scope:** Add `StorageLayer`, `StorageOperationType`, `StorageOperationStatus`, `StorageObjectStatus`, `StorageAdapterStatus` enums; `StorageOperation` and `StorageObject` entities; repositories; and a Flyway migration for storage execution evidence. Do not add new `FileStatus` values.
- **Constraints:** Adapter boundary; mock-only test data; trace/review preservation; secret/path safety.
- **Verification:**

```bash
cd backend && mvn verify
git diff --check
```

### T-SA-002: Add storage DTOs and mapping contracts

- **Maps to:** REQ-SA-003, REQ-SA-013; spec sections "Capability Metadata", "API / Interface Surface"; API guide response shapes.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-SA-001
- **Scope:** Add storage capability, create-operation request, operation response, summary response, object response, and paginated object-list response mapping. All responses use `ApiEnvelope` through controllers.
- **Constraints:** Secret-masked capability summaries; no raw runtime details.
- **Verification:**

```bash
cd backend && mvn test
git diff --check
```

### T-SA-003: Define storage adapter interface and capability model

- **Maps to:** REQ-SA-001, REQ-SA-002, REQ-SA-003; spec sections "Adapter Boundary", "Capability Metadata"; design "Storage Adapter Contract".
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-SA-002
- **Scope:** Add product-facing `StorageAdapter` interface (`capability`, `put`, `get`, `exists`, `list`, `delete`), capability record, put-request record, object-ref record, list-request record, and descriptor record. Include adapter key, safe status, supported layers, default marker, and masked config summary.
- **Constraints:** Product concepts only; no direct storage-engine call in the interface; no network/client dependency; content exchanged via reference, never raw bytes.
- **Verification:**

```bash
cd backend && mvn -Dtest=StorageAdapterContractTest test
! rg -n "WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/adapter/StorageAdapter.java
```

### T-SA-004: Add mock and configured storage adapter implementations

- **Maps to:** REQ-SA-002, REQ-SA-012; spec sections "Adapter Boundary", "Storage Operations"; design "Storage Adapter Contract".
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-SA-003
- **Scope:** Add a deterministic mock/in-memory storage adapter for CI and a configured S3-compatible storage adapter boundary that reports safe capability metadata. Real storage execution remains behind the adapter and may be a safe placeholder until runtime topology is decided.
- **Constraints:** Mock-only verification; no external network; no raw endpoint/bucket/credential/path/secret in capability response.
- **Verification:**

```bash
cd backend && mvn -Dtest=StorageAdapterContractTest test
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src/main/java/com/atlas/metadata/adapter
```

### T-SA-005: Implement storage adapter registry and capability listing

- **Maps to:** REQ-SA-001, REQ-SA-003; spec sections "Adapter Boundary", "Capability Metadata"; API guide `GET /api/storage-adapters`.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-SA-003, T-SA-004
- **Scope:** Add registry resolution for explicit/default storage adapter keys and unavailable/misconfigured statuses. Add capability listing service behavior with masked config.
- **Constraints:** Adapter boundary; secret masking; no hardcoded single implementation as the only future option.
- **Verification:**

```bash
cd backend && mvn -Dtest=StorageAdapterRegistryTest test
git diff --check
```

### T-SA-006: Implement storage operation service and layer/key validation

- **Maps to:** REQ-SA-004, REQ-SA-005, REQ-SA-006, REQ-SA-007, REQ-SA-008, REQ-SA-009, REQ-SA-013; spec sections "Storage Operations", "Layer And Namespace Policy", "Validation And Failure Behavior"; design "Storage Service".
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-SA-001, T-SA-003, T-SA-005
- **Scope:** Implement storage operation creation, layer membership and safe-key validation, namespace-scope enforcement, active-run conflict handling, adapter execution (store/get/exists/list/delete), descriptor validation, summary calculation (counts + total bytes), and safe adapter-fault handling.
- **Constraints:** Mock-engine execution in tests; reject unsafe keys and out-of-namespace targets before persistence; no external network.
- **Verification:**

```bash
cd backend && mvn -Dtest=StorageServiceTest,StorageSummaryCalculatorTest test
git diff --check
```

### T-SA-007: Implement metadata pointer write-back and descriptor persistence

- **Maps to:** REQ-SA-010, REQ-SA-011, REQ-SA-014; spec sections "Metadata Write-Back And Descriptors", "Validation And Failure Behavior"; design "Data Design".
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-SA-006
- **Scope:** Persist validated `storage_object` descriptors; update the correct `file_item` pointer field (`pdfPath`/`markdownPath`/`assetsPath`) via a review-preserving update; reject unsafe keys, unknown file ids, cross-batch results, out-of-namespace targets, negative sizes, and empty checksums for `STORED`. Sanitize safe errors before persistence.
- **Constraints:** Preserve source trace, confidence, review status, and file status; secret/path masking; no `wiki_page` creation.
- **Verification:**

```bash
cd backend && mvn -Dtest=StorageDomainInvariantTest,StorageServiceTest test
git diff --check
```

### T-SA-008: Add storage REST API endpoints

- **Maps to:** REQ-SA-003, REQ-SA-004, REQ-SA-007, REQ-SA-013; spec section "API / Interface Surface"; API guide all endpoints.
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-SA-005, T-SA-006, T-SA-007
- **Scope:** Add storage controller endpoints `GET /api/storage-adapters`, `POST /api/batches/{batchId}/storage-operations`, `GET /api/storage-operations/{operationId}`, and `GET /api/batches/{batchId}/storage-objects` (bounded/paginated). Responses must use `ApiEnvelope` and match the API guide.
- **Constraints:** Internal-only; no auth/RBAC implementation; no raw storage output; object bytes never returned in JSON.
- **Verification:**

```bash
cd backend && mvn -Dit.test=StorageApiContractIT verify
git diff --check
```

### T-SA-009: Update adapter seam and safety guards

- **Maps to:** REQ-SA-001, REQ-SA-009, REQ-SA-010, REQ-SA-012; spec sections "Adapter Boundary", "Validation And Failure Behavior"; design "Seam Guard Update (Required)".
- **Owner type:** QA/backend
- **Priority:** Must
- **Dependencies:** T-SA-003, T-SA-004, T-SA-008
- **Scope:** Update `AdapterSeamGuardTest` so the adapter-scope assertion allows storage-engine names inside the storage adapter package (relax the current `.doesNotContain("S3")`), and add storage-engine names and SDK types (`AmazonS3`, `software.amazon.awssdk`, `MinioClient`, `S3Client`) to the non-adapter forbidden `ENGINE_REFERENCES` list. Keep `WebClient`/`RestTemplate`/`HttpClient` banned outside adapter scope. Add secret/private-path scans for storage implementation and docs.
- **Constraints:** No direct storage calls outside adapter; secret-masked output only. Record the guard change intentionally in traceability.
- **Verification:**

```bash
cd backend && mvn -Dtest=AdapterSeamGuardTest test
! rg -n "AmazonS3|software\\.amazon\\.awssdk|MinioClient|MinIO|S3Client|putObject|getObject|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/storage-adapter-requirements.md docs/02-user-stories/storage-adapter-stories.md docs/03-spec/storage-adapter-spec.md docs/04-architecture/storage-adapter-architecture.md docs/04-architecture/storage-adapter-data-flow.md docs/04-architecture/storage-adapter-data-model.md docs/05-design/storage-adapter-design.md docs/05-design/contracts/storage-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/storage-adapter-tasks.md
```

### T-SA-010: Run final storage-adapter verification

- **Maps to:** REQ-SA-012, REQ-SA-013, REQ-SA-014; spec acceptance matrix AC-SA-01 through AC-SA-09.
- **Owner type:** QA/backend
- **Priority:** Must
- **Dependencies:** T-SA-001 through T-SA-009
- **Scope:** Run the complete verification set, review the diff, and update traceability with evidence if implementation changes docs/status later.
- **Constraints:** Mock-only; adapter only; no external network/cloud calls from product code; secret-masked; trace/review preserved; no new `FileStatus`.
- **Verification:**

```bash
cd backend && mvn verify
git diff --check
! rg -n "AmazonS3|software\\.amazon\\.awssdk|MinioClient|MinIO|S3Client|putObject|getObject|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/storage-adapter-requirements.md docs/02-user-stories/storage-adapter-stories.md docs/03-spec/storage-adapter-spec.md docs/04-architecture/storage-adapter-architecture.md docs/04-architecture/storage-adapter-data-flow.md docs/04-architecture/storage-adapter-data-model.md docs/05-design/storage-adapter-design.md docs/05-design/contracts/storage-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/storage-adapter-tasks.md
```

## Dependency Plan

- Critical path: T-SA-001 -> T-SA-002 -> T-SA-003 -> T-SA-004 -> T-SA-005 -> T-SA-006 -> T-SA-007 -> T-SA-008 -> T-SA-009 -> T-SA-010
- Parallel opportunities after T-SA-003: adapter contract tests and DTO mapper tests may be built alongside service tests.

## Open Questions / Risks

- OQ-SA-001: First real storage engine (S3-compatible vs filesystem) is deferred; the mock contract must not depend on it.
- OQ-SA-002: API returns descriptors only; confirm before implementation if a byte-passthrough endpoint is required.
- OQ-SA-003: Presigned/short-lived access URLs remain deferred to a later delivery/access slice.
- R-SA-003 / DT-SA-001: The existing seam guard forbids `S3` in adapter scope; T-SA-009 must update it intentionally, not incidentally.
