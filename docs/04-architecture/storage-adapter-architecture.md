# Architecture: Storage Adapter

## Status

Draft. Phase 3 adapter slice. Derived from `docs/03-spec/storage-adapter-spec.md`.

## Overview

Storage-adapter extends the existing metadata control plane with a replaceable object-storage seam. The backend owns storage operation state, key/layer validation, operation records, and metadata pointer write-back; object storage execution is isolated behind a product-facing adapter contract. An S3-compatible store is the first named target, but the architecture keeps filesystem stores and other S3-compatible engines replaceable.

## Architectural Drivers

| Driver | Impact |
|---|---|
| Adapter neutrality | Product services resolve storage adapters through registry/capability contracts. |
| Layer separation | Raw inputs, PDF, Markdown, assets, reports, and Wiki output live under distinct layer prefixes. |
| Trace preservation | Stored-object descriptors and pointer write-back carry workspace/batch/file scope, layer, key, and checksum without changing review status. |
| Mock-only verification | Tests use a mock/in-memory storage engine and do not require a real endpoint, buckets, or cloud credentials. |
| Secret/path safety | Keys are relative; errors and capability summaries are sanitized. |
| Phase discipline | Storage-adapter stores/reads/lists/deletes objects but does not publish Wiki pages, run converters/parsers, or issue presigned URLs. |

## Existing Metadata Context

The current metadata control plane already owns file items with artifact path fields, review status, confidence, path safety rules, and adapter seam guarding. Storage-adapter should reuse those product concepts and treat existing artifact path fields as pointers to stored objects, instead of creating a separate storage-owned metadata island. Code-level grounding anchors are recorded in the design and traceability artifacts, where implementation-facing detail belongs.

## System Context

| Boundary | Responsibility |
|---|---|
| Frontend | Out of scope for this slice. Existing UI may later consume storage capability/listing APIs, but no frontend changes are part of storage-adapter. |
| Backend API / metadata control plane | Owns storage capability endpoints, storage operation lifecycle, validation, descriptor persistence, pointer write-back, and listings. |
| Storage adapter seam | Encapsulates storage engine-specific execution and returns Atlas product concepts (descriptors). |
| Storage engine / worker | External to product workflow. S3/MinIO or filesystem stores sit behind the adapter contract only. |
| PostgreSQL metadata | Stores storage operation records, stored-object descriptors, and artifact pointer updates on file items. |

## High-Level Architecture

```text
+------------------------------------------------------------+
| Users / agents                                             |
| Admin, delivery lead, SME reviewer, Codex implementation    |
+------------------------------+-----------------------------+
                               |
                               | REST / JSON
                               v
+------------------------------------------------------------+
| Spring Boot metadata API                                   |
| Storage capability endpoint, storage operation endpoint,    |
| storage object listing endpoint                             |
+------------------------------+-----------------------------+
                               |
                               v
+------------------------------------------------------------+
| Storage application service                                |
| Layer/key validation, adapter resolution, descriptor        |
| mapping, safe error handling, metadata pointer write-back   |
+------------------------------+-----------------------------+
                               |
                   product-facing adapter interface
                               v
+------------------------------+        +---------------------+
| Storage adapter registry     |        | Storage adapters     |
| capability + default policy  |------->| s3-compatible store  |
+------------------------------+        | mock/in-memory tests |
                                        +----------+----------+
                                                   |
                                                   | engine/worker boundary
                                                   v
                                        +---------------------+
                                        | Object storage       |
                                        | outside product flow |
                                        +---------------------+
                               |
                               v
+------------------------------------------------------------+
| PostgreSQL metadata                                        |
| file_item pointers, storage_operation, storage_object       |
+------------------------------------------------------------+
```

## Component Breakdown

### Backend API

- **Storage adapter capability API:** lists storage capabilities and masked configuration.
- **Storage operation API:** executes store/delete operation sets for batches and returns operation records.
- **Storage object listing API:** lists stored-object descriptors within a workspace/batch/layer prefix.
- **File API reuse:** existing file APIs remain the read surface for artifact pointer fields.

### Application Services

- **Storage operation service:** validates workspace/batch/file targets, layer, and keys; resolves adapter; executes mock/configured mode; maps descriptors; persists operation records; and writes back pointers.
- **Storage summary calculator:** derives totals (succeeded/failed/skipped, total bytes) from stored-object results.
- **Storage adapter registry:** owns default adapter resolution and unavailable/misconfigured behavior.
- **Safety helpers:** reuse relative path validation and safe error masking patterns; add layer-prefix resolution.

### Integration Adapters

- **StorageAdapter contract:** accepts Atlas storage requests and returns Atlas descriptors.
- **MockObjectStorageAdapter:** deterministic in-memory implementation for CI and integration tests.
- **S3CompatibleStorageAdapter:** real adapter boundary placeholder or configured implementation. It may know engine details (endpoints, buckets, SDK types), but product layers must not.

### Persistence

- Reuse `file_item` artifact path fields (`pdf_path`, `markdown_path`, `assets_path`) as pointers to stored objects.
- Add `storage_operation` and `storage_object` logical entities for execution evidence and descriptors.
- Do not create `wiki_page` rows in this slice.

## State And Status Strategy

Storage operation status:

```text
REQUESTED -> RUNNING -> SUCCEEDED
                     -> PARTIAL_FAILED
                     -> FAILED
```

Stored-object status mapping:

- store success -> `STORED`
- delete success -> `DELETED`
- get/exists miss -> `MISSING`
- object failure -> `FAILED`
- unsafe/invalid target -> rejected before write/delete

Storing or deleting an object does not change `file_item.review_status`, `status`, or `confidence` unless a workflow explicitly maps it.

## API / Interface Boundaries

| Interface | Consumer | Purpose |
|---|---|---|
| `GET /api/storage-adapters` | Admin / implementation tests | List masked storage capabilities. |
| `POST /api/batches/{batchId}/storage-operations` | Internal workflow / future UI | Execute store/delete operations for batch artifacts. |
| `GET /api/storage-operations/{operationId}` | Delivery lead / future UI | Read operation record and per-object outcomes. |
| `GET /api/batches/{batchId}/storage-objects` | Delivery lead / future UI | List stored-object descriptors by layer prefix. |
| `StorageAdapter` | Storage service | Execute storage work behind product-facing interface. |

## Security / Reliability / Observability

- Capability responses are masked/status-only.
- Storage errors are bounded and sanitized before persistence or response.
- Object keys and layer paths are validated as safe relative paths within the namespace.
- Descriptors preserve auditable evidence (layer, key, size, checksum) without embedding raw document bytes in metadata or logs.
- Mock/in-memory engines are mandatory for automated verification.
- The adapter seam guard must scan non-adapter product layers for direct storage-engine, SDK, and outbound-client references; the adapter-scope assertion that currently forbids `S3` must be relaxed for the storage adapter package only.

## Risks / Tradeoffs

| ID | Risk / Tradeoff | Mitigation |
|---|---|---|
| R-SA-001 | Real storage runtime topology (S3 vs filesystem) is not finalized. | Keep real execution behind adapter; mock contract remains stable. |
| R-SA-002 | Storage output may contain raw endpoints, buckets, or SDK errors. | Validate keys and sanitize safe messages before persistence. |
| R-SA-003 | The existing seam guard forbids `S3` in adapter scope, which would block a legitimate S3 adapter. | Update the guard to allow storage-engine names inside the storage adapter package only, mirroring the `trinity-office` exception. |
| R-SA-004 | Passing object bytes through the API could bloat responses and leak content. | Commit to descriptor-only API responses; keep bytes behind the adapter. |

## Open Questions

- OQ-SA-001: First real storage engine (S3-compatible vs filesystem).
- OQ-SA-002: Whether object bytes ever transit the API.
- OQ-SA-003: Deferral of presigned/short-lived access URLs.
