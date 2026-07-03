# Storage Adapter — API / Adapter Implementation Guide

## Status

Draft. Required before Phase 3 storage-adapter implementation. Slice `storage-adapter`.

## Overview

This guide defines the internal API and adapter contracts for object storage of batch artifacts. The API is internal to Atlas; it does not expose raw object bytes, raw SDK output, raw endpoint/bucket/credential configuration, external cloud calls, or private runtime paths.

## Base Conventions

- Base path: `/api`.
- Envelope: reuse `ApiEnvelope`.
- JSON only. Object bytes never transit the API; the adapter exchanges content references/streams.
- Auth/RBAC: deferred; internal-only for this slice.
- Keys: relative only; reject traversal, drive prefixes, URI prefixes, host prefixes, and private absolute paths.
- Secrets: masked/status-only; never return raw endpoints, buckets, regions, access keys, credentials, hostnames, or private paths.
- Mode: tests use `mock`; configured runtime behavior remains behind the adapter.

## Adapter Capability Contract

### `GET /api/storage-adapters`

Purpose: list configured storage adapters.

Response:

```json
{
  "success": true,
  "data": [
    {
      "adapterKey": "s3-compatible",
      "displayName": "S3-Compatible Object Storage",
      "version": "configured",
      "supportedLayers": ["raw", "pdf", "markdown", "assets", "reports", "wiki"],
      "defaultAdapter": true,
      "status": "AVAILABLE",
      "maskedConfigSummary": {
        "endpoint": "configured",
        "bucket": "configured",
        "region": "configured",
        "credentials": "configured",
        "externalNetwork": "disabled"
      }
    }
  ],
  "error": null,
  "meta": null
}
```

`status` values: `AVAILABLE`, `DISABLED`, `MISCONFIGURED`.

Validation/security:

- Do not return raw endpoints, bucket names, regions, access keys, credentials, or absolute paths.
- `version` may be a safe adapter version or `configured`; do not call the storage engine only to discover a version during capability listing.

## Create Storage Operation

### `POST /api/batches/{batchId}/storage-operations`

Purpose: execute a store/delete operation set for batch artifacts through the storage adapter contract.

Request:

```json
{
  "adapterKey": "s3-compatible",
  "requestedBy": "delivery-lead",
  "mode": "mock",
  "operations": [
    {
      "operationType": "STORE",
      "layer": "markdown",
      "objectKey": "batch-2026-001/BRD.md",
      "contentType": "text/markdown",
      "fileId": "file-001"
    },
    {
      "operationType": "DELETE",
      "layer": "pdf",
      "objectKey": "batch-2026-001/OBSOLETE.pdf"
    }
  ]
}
```

Rules:

- `adapterKey` is optional; omitted means the default storage adapter.
- `mode` is optional and must be `mock` or `configured`; tests use `mock`.
- `operations[].layer` must be one of `raw`, `pdf`, `markdown`, `assets`, `reports`, `wiki`.
- `operations[].objectKey` must be a safe relative key within the workspace/batch namespace.
- `operations[].fileId` is optional; when present it triggers a pointer write-back and must reference a file in the batch.
- Content bytes are supplied out-of-band via a content reference/stream, never inline in JSON.
- The service validates all operations before starting the run.

Success response:

```json
{
  "success": true,
  "data": {
    "operationId": "storage-op-2026-07-03-001",
    "batchId": "batch-2026-001",
    "adapterKey": "s3-compatible",
    "operationType": "STORE",
    "status": "PARTIAL_FAILED",
    "safeMessage": "Mock storage completed with one failed object.",
    "summary": {
      "total": 2,
      "stored": 1,
      "deleted": 0,
      "missing": 0,
      "failed": 1,
      "skipped": 0,
      "totalBytes": 20480
    },
    "objects": [
      {
        "layer": "markdown",
        "objectKey": "batch-2026-001/BRD.md",
        "contentType": "text/markdown",
        "sizeBytes": 20480,
        "checksum": "sha256:configured",
        "adapterKey": "s3-compatible",
        "status": "STORED",
        "fileId": "file-001",
        "safeError": null
      }
    ],
    "startedAt": "2026-07-03T00:00:00Z",
    "completedAt": "2026-07-03T00:00:02Z"
  },
  "error": null,
  "meta": null
}
```

Error cases:

| HTTP | Code | When |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Unknown adapter key, unsafe key, unknown layer, invalid mode, out-of-namespace target, cross-batch file id, invalid descriptor. |
| 404 | `NOT_FOUND` | Batch or file id is unknown. |
| 409 | `CONFLICT` | Storage operation cannot start because the batch already has an active storage operation. |
| 500 | `INTERNAL_ERROR` | Unexpected adapter fault; details logged safely server-side only. |

## Get Storage Operation

### `GET /api/storage-operations/{operationId}`

Purpose: return storage operation summary, per-object descriptors, and outcomes.

Response shape: same `data` body as create storage operation.

## List Storage Objects

### `GET /api/batches/{batchId}/storage-objects`

Purpose: list stored-object descriptors within a workspace/batch/layer prefix.

Query parameters:

| Param | Required | Description |
|---|---|---|
| `layer` | No | Filter by `StorageLayer` value; omitted lists all layers. |
| `pageSize` | No | Bounded page size; server enforces a maximum. |
| `pageToken` | No | Continuation marker for the next page. |

Response:

```json
{
  "success": true,
  "data": {
    "objects": [
      {
        "layer": "markdown",
        "objectKey": "batch-2026-001/BRD.md",
        "contentType": "text/markdown",
        "sizeBytes": 20480,
        "checksum": "sha256:configured",
        "adapterKey": "s3-compatible",
        "status": "STORED",
        "fileId": "file-001"
      }
    ]
  },
  "error": null,
  "meta": {
    "pageSize": 50,
    "nextPageToken": null
  }
}
```

## Internal Adapter Interface Contract

Conceptual interface:

```text
StorageAdapter
  capability() -> StorageCapability
  put(StoragePutRequest) -> StorageObjectDescriptor
  get(StorageObjectRef) -> StorageObjectDescriptor
  exists(StorageObjectRef) -> boolean
  list(StorageListRequest) -> StorageListResult
  delete(StorageObjectRef) -> StorageDeleteResult
```

Required put-request fields:

| Field | Description |
|---|---|
| `operationId` | Server-created storage operation id. |
| `batchId` | Existing batch id. |
| `layer` | Target `StorageLayer`. |
| `objectKey` | Safe relative object key. |
| `contentType` | User-safe content type. |
| `contentRef` | Stream/handle to content; never raw bytes in metadata. |

Required descriptor fields:

| Field | Description |
|---|---|
| `layer` | Layer the object was stored under. |
| `objectKey` | Relative object key. |
| `sizeBytes` | Non-negative byte size for `STORED`. |
| `checksum` | Non-empty for `STORED`. |
| `contentType` | User-safe content type. |
| `adapterKey` | Adapter that produced the descriptor. |
| `status` | `STORED` / `DELETED` / `MISSING` / `FAILED`. |
| `safeError` | Sanitized error summary. |

## Status Mapping

| Source / Outcome | Object Status |
|---|---|
| Object stored successfully | `STORED` |
| Object deleted successfully | `DELETED` |
| Object not found on get/exists | `MISSING` |
| Object operation failed | `FAILED` |
| Unsafe/invalid target | rejected before write/delete, reported as skipped |

No new `FileStatus` values are allowed in this slice; storage operations only update artifact pointer fields.

## Contract Tests

Run before implementation completion:

```bash
cd backend && mvn verify
git diff --check
! rg -n "AmazonS3|software\\.amazon\\.awssdk|MinioClient|MinIO|S3Client|putObject|getObject|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/storage-adapter-requirements.md docs/02-user-stories/storage-adapter-stories.md docs/03-spec/storage-adapter-spec.md docs/04-architecture/storage-adapter-architecture.md docs/04-architecture/storage-adapter-data-flow.md docs/04-architecture/storage-adapter-data-model.md docs/05-design/storage-adapter-design.md docs/05-design/contracts/storage-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/storage-adapter-tasks.md
```

The seam scan must return no matches in non-adapter product layers. The storage adapter package may contain storage-engine names only when covered by the updated guard test, and no outbound network clients may leak into product layers.
