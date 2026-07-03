# Data Model: Storage Adapter

## Status

Draft. Phase 3 adapter slice. Extends metadata with object-storage execution evidence and descriptors.

## Existing Entities Reused

| Entity | Existing Contract | Use In This Slice |
|---|---|---|
| `batch` | Owns file items and processing run context. | Storage operation is scoped to a batch (and its workspace). |
| `file_item` | Holds `source_path`, `source_type`, `status`, `confidence`, `review_status`, `pdf_path`, `markdown_path`, `assets_path`, `error_message`. | Artifact path fields are treated as pointers; storage write-back records the returned key without changing review status/status/confidence. |
| `wiki_page` | Deferred wiki page metadata table. | Not created by this slice; a wiki-layer object pointer may be recorded on future publish. |

## New Logical Entities

### `storage_operation`

| Field | Type | Required | Description |
|---|---|---|---|
| `id` | String | Yes | Opaque operation id. |
| `batchId` | String | Yes | Existing batch id. |
| `workspaceId` | String | No | Existing workspace id when resolvable from the batch. |
| `adapterKey` | String | Yes | Registered storage adapter key, e.g. `s3-compatible`. |
| `adapterVersion` | String | No | Adapter-reported version when safe. |
| `operationType` | Enum | Yes | `STORE`, `DELETE`, `LIST` (operation set intent). |
| `status` | Enum | Yes | `REQUESTED`, `RUNNING`, `SUCCEEDED`, `PARTIAL_FAILED`, `FAILED`. |
| `requestedBy` | String | No | Internal actor/team. |
| `mode` | String | Yes | `mock` or `configured`. Tests use `mock`. |
| `startedAt` | Timestamp | Yes | Server timestamp. |
| `completedAt` | Timestamp | No | Server timestamp after terminal state. |
| `summary` | Object | Yes | Counts by outcome and total bytes; no raw storage output. |
| `safeMessage` | String | No | User-safe operation summary or error. |

### `storage_object`

| Field | Type | Required | Description |
|---|---|---|---|
| `id` | String | Yes | Opaque descriptor id. |
| `operationId` | String | Yes | Owning storage operation. |
| `batchId` | String | Yes | Existing batch id. |
| `fileItemId` | String | No | Existing file item id when the object maps to a file artifact. |
| `layer` | Enum | Yes | `RAW`, `PDF`, `MARKDOWN`, `ASSETS`, `REPORTS`, `WIKI`. |
| `objectKey` | String | Yes | Safe relative key within the workspace/batch namespace. |
| `contentType` | String | No | User-safe content type label. |
| `sizeBytes` | Long | No | Non-negative byte size for `STORED` descriptors. |
| `checksum` | String | No | Non-empty for `STORED` descriptors (e.g. content hash). |
| `adapterKey` | String | Yes | Adapter key that produced the descriptor. |
| `status` | Enum | Yes | `STORED`, `DELETED`, `MISSING`, `FAILED`. |
| `safeError` | String | No | Sanitized error summary only. |
| `createdAt` | Timestamp | Yes | Server timestamp. |
| `updatedAt` | Timestamp | No | Server timestamp after status change. |

## New Enums

### `StorageLayer`

```text
RAW | PDF | MARKDOWN | ASSETS | REPORTS | WIKI
```

### `StorageOperationType`

```text
STORE | DELETE | LIST
```

### `StorageOperationStatus`

```text
REQUESTED | RUNNING | SUCCEEDED | PARTIAL_FAILED | FAILED
```

### `StorageObjectStatus`

```text
STORED | DELETED | MISSING | FAILED
```

### `StorageAdapterStatus`

```text
AVAILABLE | DISABLED | MISCONFIGURED
```

`StorageAdapterStatus` mirrors the existing `ConverterAdapterStatus` naming pattern; reuse a shared neutral adapter-status enum only if introduced deliberately across adapters.

## File Status Note

This slice introduces **no new `FileStatus` values**. Storage operations do not change `file_item.status`; they update artifact pointer fields only. Existing `FileStatus` values remain owned by converter/parser/review slices.

## Relationships

```text
batch 1 ---- N storage_operation
storage_operation 1 ---- N storage_object
file_item 0..1 ---- N storage_object   (via optional fileItemId)
file_item 1 ---- pdf_path/markdown_path/assets_path pointer updated by storage write-back
```

## Invariants

- `storage_object.objectKey` and any resolved layer path are relative and traversal-free within the workspace/batch namespace.
- `storage_object.layer` uses `StorageLayer`; cross-layer or cross-namespace targets are rejected.
- `storage_object.status` uses `StorageObjectStatus`; no `FileStatus` expansion in this slice.
- `storage_object.sizeBytes` is non-negative; `checksum` is non-empty for `STORED` descriptors.
- `storage_operation.summary` is derived from associated `storage_object` rows, not trusted independently if recomputation disagrees.
- Raw storage endpoints, buckets, regions, credentials, SDK output, hostnames, and private absolute paths are never stored in `safeMessage` or `safeError`.
- `file_item.review_status`, `status`, and `confidence` are unchanged by storage operations unless a workflow explicitly maps a change.
- `wiki_page` rows are untouched by this slice.

## API Shape Mapping

| API Field | Entity Field | Notes |
|---|---|---|
| `operationId` | `storage_operation.id` | Returned by create and detail endpoints. |
| `adapterKey` | `storage_operation.adapterKey`, `storage_object.adapterKey` | Must match registered adapter. |
| `objects[].layer` | `storage_object.layer` | `StorageLayer` value. |
| `objects[].objectKey` | `storage_object.objectKey`, `file_item` pointer | Relative only. |
| `objects[].sizeBytes` | `storage_object.sizeBytes` | Non-negative. |
| `objects[].checksum` | `storage_object.checksum` | Present for `STORED`. |
| `objects[].status` | `storage_object.status` | `StorageObjectStatus`. |
| `objects[].fileId` | `storage_object.fileItemId` | Optional existing file item id. |
| `objects[].safeError` | `storage_object.safeError` | Sanitized only. |
| `summary.totalBytes` | `storage_operation.summary` | Derived from object rows. |

## Deferred Data

- Real object bytes, presigned URLs, and lifecycle/retention metadata belong to a real storage-runtime slice, not this contract.
- Wiki page publication metadata belongs to `review-publish`.
- Vector index metadata belongs to `vector-adapter`.
- Model/embedding metadata belongs to `model-adapter`.
- Graph node/edge projections belong to `knowledge-graph`.
