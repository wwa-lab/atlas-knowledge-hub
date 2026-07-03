# Specification: Storage Adapter

## Status

Draft. Phase 3 adapter slice. Behavior source of truth for `storage-adapter`. Derived from `docs/02-user-stories/storage-adapter-stories.md`.

## Source Documents

- `docs/01-requirements/storage-adapter-requirements.md`
- `docs/02-user-stories/storage-adapter-stories.md`
- `docs/03-spec/converter-adapter-spec.md`
- `docs/03-spec/parser-adapter-spec.md`
- `docs/04-architecture/converter-adapter-data-model.md`
- `docs/03-spec/metadata-api-spec.md`
- `docs/04-architecture/metadata-api-data-model.md`
- `docs/architecture.md`
- `docs/batch-processing-design.md`

## Scope

Atlas must support object storage for batch artifacts through a product-facing storage adapter contract. The slice includes storage capability behavior, store/get/list/delete/exists operations, a workspace-separated layer policy, stored-object descriptors, storage operation records, metadata pointer write-back, mock-engine verification, and adapter seam guards. It does not implement real S3/MinIO execution, presigned URLs, retention/lifecycle policy, converter/parser execution, LLM/OCR/graph/Ask, Wiki publication semantics, frontend UI, production auth/RBAC, or vector/model adapters.

## Constraints

- **Adapter only:** product workflow code depends on storage interfaces and registry contracts, not on S3 SDKs, MinIO clients, or filesystem object stores directly (REQ-SA-001).
- **No single hardcoded implementation:** an S3-compatible store is the first target but must be replaceable through adapter configuration and capability metadata (REQ-SA-002).
- **Mock-engine verification:** automated tests must use a mock/in-memory storage engine and must not require a real endpoint, real buckets, or cloud credentials (REQ-SA-012).
- **Secret/path safety:** all object keys and layer prefixes are safe relative paths; no raw endpoints, buckets, regions, credentials, SDK output, stack traces, or private absolute paths are returned or logged (REQ-SA-009, REQ-SA-010).
- **Trace/review preservation:** storage operations preserve source path, source type, converter/parser metadata, confidence, and review status; storing content never auto-approves it (REQ-SA-011, REQ-SA-014).
- **No external cloud calls:** this slice does not introduce external network dependencies or cloud services.

## Actors

| Actor | Role |
|---|---|
| Knowledge base administrator | Triggers or monitors storage operations for batch artifacts. |
| Platform administrator | Reviews storage adapter availability and masked configuration. |
| Delivery lead | Uses storage operation records and listings to audit artifact layers. |
| SME reviewer | Traces file metadata to stored objects during review. |
| Codex implementation agent | Implements strictly against this spec and task checklist after SDD acceptance. |

## Functional Requirements

### Adapter Boundary

- **FR-SA-001:** The storage workflow must resolve a storage adapter through a registry/capability contract before any object operation starts. (US-SA-001)
- **FR-SA-002:** Non-adapter product layers must not reference S3 SDKs, MinIO clients, filesystem object stores, or outbound HTTP/S3 clients for storage. (US-SA-001, US-SA-005)
- **FR-SA-003:** The adapter registry must support at least one configured default storage adapter and expose unavailable/misconfigured states safely. (US-SA-002)

### Capability Metadata

- **FR-SA-004:** Capability metadata must include adapter key, display name, version, supported layers, default marker, health/status, and masked configuration summary. (US-SA-002)
- **FR-SA-005:** Capability metadata must not expose raw endpoints, bucket names, regions, access keys, secrets, hostnames, raw local paths, or private endpoints. (US-SA-002)

### Storage Operations

- **FR-SA-006:** A store (put) operation must accept a workspace/batch scope, target layer, safe relative object key, content type, and a content reference, and return a stored-object descriptor. (US-SA-001, US-SA-004)
- **FR-SA-007:** A get/exists operation must resolve an object by layer and key and return a safe descriptor or a safe not-found result. (US-SA-001)
- **FR-SA-008:** A list operation must enumerate objects within a workspace/batch/layer prefix with bounded, paginated results. (US-SA-003)
- **FR-SA-009:** A delete operation must remove an object by layer and key, return a safe outcome, and must not traverse outside the workspace/batch namespace. (US-SA-005)
- **FR-SA-010:** Storage operation records must preserve adapter key, operation type, layer, object counts, total bytes, outcome counts, timestamps, and a user-safe summary. (US-SA-004)

### Layer And Namespace Policy

- **FR-SA-011:** Objects must be stored under distinct layer prefixes: `raw`, `pdf`, `markdown`, `assets`, `reports`, `wiki`. (US-SA-003)
- **FR-SA-012:** Layer + key must resolve to a single safe relative path inside the workspace/batch namespace; cross-layer or cross-namespace targets are rejected. (US-SA-003, US-SA-005)
- **FR-SA-013:** An unknown or unsupported layer must fail validation before any storage operation. (US-SA-003)

### Metadata Write-Back And Descriptors

- **FR-SA-014:** A successful store must record the returned storage key/descriptor into the correct `file_item` artifact field (or report/wiki pointer) through the metadata boundary. (US-SA-004)
- **FR-SA-015:** Stored-object descriptors must record object key, layer, content type, size, checksum, adapter key, status, and timestamps, with no raw storage internals. (US-SA-004)
- **FR-SA-016:** Storage write-back must preserve source path, source type, confidence, and review status unless a workflow explicitly maps a change. (US-SA-004)

### Validation And Failure Behavior

- **FR-SA-017:** Storage operations must reject absolute paths, URI-prefixed paths, drive-prefixed paths, traversal paths, out-of-namespace targets, and unknown layers before any object is written or deleted. (US-SA-005)
- **FR-SA-018:** Storage error summaries must be sanitized to remove raw SDK output, stack traces, secrets, endpoints, bucket ARNs, hostnames, and absolute/private paths. (US-SA-005)
- **FR-SA-019:** Unexpected adapter faults must return user-safe errors and leave metadata unchanged unless a safe per-object failure result exists. (US-SA-005)
- **FR-SA-020:** Storage operation records must summarize succeeded, failed, and skipped object outcomes. (US-SA-004)

## Non-Functional Requirements

| Category | Requirement |
|---|---|
| Security | No raw endpoints, buckets, regions, access keys, secrets, hostnames, private paths, stack traces, or raw SDK output in responses or persisted safe messages. |
| Reliability | Mock/in-memory engine tests cover store, get/exists, list pagination, delete, unsafe key rejection, unknown layer, out-of-namespace target, unavailable adapter, and adapter fault cases. |
| Extensibility | Storage adapter interface allows future storage implementations (filesystem, other S3-compatible engines) without changing product workflow callers. |
| Auditability | Storage operation records and stored-object descriptors are traceable to workspace/batch/file ids, adapter identity, layer, key, size, and checksum. |
| Data safety | Use mock/sample content only; no real company documents, raw document bytes in metadata, private paths, or external cloud calls in test fixtures. |

## Workflow

```text
+--------------------------+
| Batch/file metadata      |
| generated artifacts      |
+------------+-------------+
             |
             v
+--------------------------+       unavailable/misconfigured
| Resolve storage adapter  |------------------------------+
+------------+-------------+                              |
             | available                                  v
             v                                    +----------------+
+--------------------------+                      | Safe run error |
| Validate layer + keys    |                      | no unsafe leak |
+------------+-------------+                      +----------------+
             |
             v
+--------------------------+
| Execute storage adapter  |
| mock/in-memory in CI     |
+------------+-------------+
             |
             v
+--------------------------+
| Descriptors + summary    |
| pointer write-back       |
+------------+-------------+
             |
             v
+--------------------------+
| Persist descriptors +    |
| storage operation record |
+--------------------------+
```

## State Model

### Storage Operation Status

`REQUESTED -> RUNNING -> SUCCEEDED | PARTIAL_FAILED | FAILED`

- `REQUESTED -> RUNNING`: adapter resolved and input validation passed.
- `RUNNING -> SUCCEEDED`: all requested objects were stored/deleted safely.
- `RUNNING -> PARTIAL_FAILED`: at least one object succeeded and at least one failed or was skipped.
- `RUNNING -> FAILED`: no requested object succeeded, or an adapter-level failure prevented per-object results.

### Stored-Object Status

| Outcome | Object Status | Notes |
|---|---|---|
| Object stored successfully | `STORED` | Requires safe layer + key and a descriptor. |
| Object deleted successfully | `DELETED` | Requires safe layer + key. |
| Object not found on get/exists | `MISSING` | Safe not-found result; no error leak. |
| Object operation failed | `FAILED` | User-safe error summary required. |
| Unsafe/invalid target | rejected | Validation error before any write/delete; no state change. |

## Validation Rules

- Workspace, batch, and file ids must reference existing metadata records where a pointer write-back is requested.
- Layer must be one of `raw`, `pdf`, `markdown`, `assets`, `reports`, `wiki`.
- Object keys and resolved layer paths must be safe relative paths within the workspace/batch namespace.
- Delete and get operations must not resolve outside the requested namespace.
- Size values, when present, must be non-negative; checksums must be non-empty for `STORED` descriptors.
- List operations must be bounded by a page size limit and support a page/continuation marker.
- Error summaries must be bounded, user-safe, and stripped of raw SDK output, stack traces, private paths, endpoints, buckets, and secrets.
- Adapter key must resolve to a registered adapter or fail with a user-safe unavailable/misconfigured status.

## API / Interface Surface

The full contract lives in `docs/05-design/contracts/storage-adapter-API_IMPLEMENTATION_GUIDE.md`.

| Interface | Behavior |
|---|---|
| `GET /api/storage-adapters` | Lists configured storage adapter capabilities with masked configuration. |
| `POST /api/batches/{batchId}/storage-operations` | Executes a store/delete operation set for batch artifacts and records results. |
| `GET /api/storage-operations/{operationId}` | Returns storage operation summary, descriptors, and per-object outcomes. |
| `GET /api/batches/{batchId}/storage-objects` | Lists stored-object descriptors within a workspace/batch/layer prefix (bounded, paginated). |
| Internal storage adapter interface | Stores/reads/lists/deletes objects behind a product-facing contract; mock/in-memory implementations are required for tests. |

## Acceptance Matrix

| Check | Requirement | Observable Result |
|---|---|---|
| AC-SA-01 | REQ-SA-001, REQ-SA-012 | Static guard tests fail on direct storage-engine references outside adapter implementation. |
| AC-SA-02 | REQ-SA-002, REQ-SA-003 | Capability contract returns masked adapter metadata and replaceable default marker. |
| AC-SA-03 | REQ-SA-005, REQ-SA-006 | Mock store then get returns a stored-object descriptor for a safe layer + key. |
| AC-SA-04 | REQ-SA-004, REQ-SA-007, REQ-SA-009 | Objects are placed in correct layer prefixes; list is bounded/paginated; unsafe keys are rejected. |
| AC-SA-05 | REQ-SA-008 | Delete removes an object by layer + key and never resolves outside the namespace. |
| AC-SA-06 | REQ-SA-011, REQ-SA-014 | Pointer write-back records the key in the correct `file_item` field and preserves review status. |
| AC-SA-07 | REQ-SA-012 | `cd backend && mvn verify` passes using the mock/in-memory storage engine only. |
| AC-SA-08 | REQ-SA-013 | Storage operation record includes all required outcome counts and total bytes. |
| AC-SA-09 | REQ-SA-010 | Error responses/log assertions show no raw secrets, endpoints, buckets, private paths, or SDK output. |

## Out Of Scope

- Real S3/MinIO execution, presigned URLs, retention/lifecycle policy, and bucket provisioning beyond adapter seam contract.
- Converter/parser execution, OCR, LLM enrichment, graph derivation, Ask/RAG, and Wiki publication semantics.
- Vector and model adapter behavior.
- Frontend screens or settings UI updates.
- Production auth/RBAC and secret manager integration.

## Open Questions

| ID | Question | Impact |
|---|---|---|
| OQ-SA-001 | S3-compatible object store vs local filesystem adapter as the first real engine. | Affects deployment topology, not the current mock-engine contract. |
| OQ-SA-002 | Whether object bytes ever transit the API or only descriptors are exchanged. | This SDD commits to descriptor-only API responses; confirm before implementation if byte passthrough is required. |
| OQ-SA-003 | Presigned/short-lived access URLs deferred to a later delivery/access slice. | Affects access flow, not the storage result contract. |
