# Data Model: Converter Adapter

## Status

Draft. Phase 3 adapter slice. Extends the metadata model with conversion execution evidence.

## Existing Entities Reused

| Entity | Existing Contract | Use In This Slice |
|---|---|---|
| `batch` | Owns file items and processing run context. | Conversion run is scoped to a batch. |
| `file_item` | Holds `source_path`, `source_type`, `status`, `confidence`, `review_status`, `pdf_path`, `error_message`. | Converter updates status/PDF/error fields through metadata boundary. |
| `source_chunk` | Parser trace records. | Not created by converter-adapter. |

## New Logical Entities

### `conversion_run`

| Field | Type | Required | Description |
|---|---|---|---|
| `id` | String | Yes | Opaque run id. |
| `batchId` | String | Yes | Existing batch id. |
| `adapterKey` | String | Yes | Registered adapter key, e.g. `trinity-office`. |
| `adapterVersion` | String | No | Adapter-reported version when available. |
| `status` | Enum | Yes | `REQUESTED`, `RUNNING`, `SUCCEEDED`, `PARTIAL_FAILED`, `FAILED`. |
| `requestedBy` | String | No | Internal actor/team. |
| `startedAt` | Timestamp | Yes | Server timestamp. |
| `completedAt` | Timestamp | No | Server timestamp after terminal state. |
| `summary` | Object | Yes | Counts by outcome; no raw engine output. |
| `safeMessage` | String | No | User-safe run summary or error. |

### `conversion_file_result`

| Field | Type | Required | Description |
|---|---|---|---|
| `id` | String | Yes | Opaque result id. |
| `runId` | String | Yes | Owning conversion run. |
| `fileItemId` | String | Yes | Existing file item id. |
| `sourcePath` | String | Yes | Relative source path copied from file item. |
| `sourceType` | Enum | Yes | Existing `SourceType`. |
| `status` | Enum | Yes | Existing `FileStatus` result. |
| `pdfPath` | String | No | Relative generated/pass-through PDF path. |
| `confidence` | Decimal | No | `[0,1]` when adapter can provide it. |
| `adapterKey` | String | Yes | Adapter key that produced the result. |
| `safeError` | String | No | Sanitized error summary only. |
| `createdAt` | Timestamp | Yes | Server timestamp. |

## Enums

### `ConversionRunStatus`

```text
REQUESTED | RUNNING | SUCCEEDED | PARTIAL_FAILED | FAILED
```

### File Status Reuse

This slice reuses the existing file status set. It must not introduce new file lifecycle states.

```text
PDF_CONVERTED | PDF_CONVERT_FAILED | OCR_REQUIRED | FAILED | UNSUPPORTED
```

Other existing statuses remain valid in the broader model, but converter result mapping uses the subset above.

## Relationships

```text
batch 1 ---- N conversion_run
conversion_run 1 ---- N conversion_file_result
file_item 1 ---- N conversion_file_result
file_item 1 ---- latest status/pdfPath/error fields updated by conversion result
```

## Invariants

- `conversion_file_result.sourcePath` and `pdfPath` are relative and traversal-free.
- `conversion_file_result.status` uses existing `FileStatus`; no enum expansion in this slice.
- `conversion_run.summary` is derived from associated result rows, not independently trusted if recomputation disagrees.
- Raw command output, raw local paths, secrets, and source document content are never stored in `safeMessage` or `safeError`.
- `file_item.review_status` remains `REVIEW_REQUIRED` for generated/derived conversion outputs unless a later review slice changes it.
- `source_chunk` is untouched by this slice.

## API Shape Mapping

| API Field | Entity Field | Notes |
|---|---|---|
| `runId` | `conversion_run.id` | Returned by create and detail endpoints. |
| `adapterKey` | `conversion_run.adapterKey`, `conversion_file_result.adapterKey` | Must match registered adapter. |
| `results[].fileId` | `conversion_file_result.fileItemId` | Existing file item id. |
| `results[].pdfPath` | `conversion_file_result.pdfPath`, `file_item.pdf_path` | Relative only. |
| `results[].status` | `conversion_file_result.status`, `file_item.status` | Existing `FileStatus`. |
| `results[].safeError` | `conversion_file_result.safeError`, `file_item.error_message` | Sanitized only. |

## Deferred Data

- Object storage locations and byte hashes belong to `storage-adapter`.
- Parser chunks, Markdown paths, and asset paths belong to `parser-adapter`.
- Review publish timestamps belong to `review-publish`.
