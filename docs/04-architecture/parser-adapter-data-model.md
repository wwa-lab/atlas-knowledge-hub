# Data Model: Parser Adapter

## Status

Draft. Phase 3 adapter slice. Extends metadata with parser execution evidence.

## Existing Entities Reused

| Entity | Existing Contract | Use In This Slice |
|---|---|---|
| `batch` | Owns file items and processing run context. | Parser run is scoped to a batch. |
| `file_item` | Holds `source_path`, `source_type`, `status`, `confidence`, `review_status`, `pdf_path`, `markdown_path`, `assets_path`, `error_message`. | Parser updates status, Markdown/assets/error/confidence fields through metadata boundary. |
| `source_chunk` | Stores source trace records for file items. | Parser creates source chunks from parsed Markdown sections/chunks. |
| `wiki_page` | Deferred wiki page metadata table. | Not created by this slice. |

## New Logical Entities

### `parser_run`

| Field | Type | Required | Description |
|---|---|---|---|
| `id` | String | Yes | Opaque run id. |
| `batchId` | String | Yes | Existing batch id. |
| `adapterKey` | String | Yes | Registered parser adapter key, e.g. `document-normalize`. |
| `adapterVersion` | String | No | Adapter-reported version when safe. |
| `status` | Enum | Yes | `REQUESTED`, `RUNNING`, `SUCCEEDED`, `PARTIAL_FAILED`, `FAILED`. |
| `requestedBy` | String | No | Internal actor/team. |
| `mode` | String | Yes | `mock` or `configured`. Tests use `mock`. |
| `lowConfidenceThreshold` | Decimal | Yes | Default `0.800`. |
| `startedAt` | Timestamp | Yes | Server timestamp. |
| `completedAt` | Timestamp | No | Server timestamp after terminal state. |
| `summary` | Object | Yes | Counts by outcome; no raw parser output. |
| `safeMessage` | String | No | User-safe run summary or error. |

### `parser_file_result`

| Field | Type | Required | Description |
|---|---|---|---|
| `id` | String | Yes | Opaque result id. |
| `runId` | String | Yes | Owning parser run. |
| `fileItemId` | String | Yes | Existing file item id. |
| `sourcePath` | String | Yes | Relative source path copied from file item. |
| `pdfPath` | String | No | Relative PDF path used as parser input; may be absent only for skipped/ineligible report rows. |
| `sourceType` | Enum | Yes | Existing `SourceType`; expected `pdf` after conversion. |
| `status` | Enum | Yes | Existing `FileStatus` parser result. |
| `markdownPath` | String | No | Relative generated Markdown path. |
| `assetsPath` | String | No | Relative extracted assets path. |
| `confidence` | Decimal | No | `[0,1]` when parser can provide it. |
| `adapterKey` | String | Yes | Adapter key that produced the result. |
| `chunkCount` | Integer | Yes | Number of source chunks accepted for the file. |
| `skipped` | Boolean | Yes | `true` when this is a skipped/ineligible report row and the file item was not passed to the adapter. |
| `safeError` | String | No | Sanitized error summary only. |
| `createdAt` | Timestamp | Yes | Server timestamp. |

## Enum Reuse

### `ParserRunStatus`

```text
REQUESTED | RUNNING | SUCCEEDED | PARTIAL_FAILED | FAILED
```

### File Status Reuse

Parser result mapping uses existing `FileStatus` values only:

```text
MARKDOWN_GENERATED | LOW_CONFIDENCE | OCR_REQUIRED | FAILED | UNSUPPORTED
```

`PDF_CONVERTED` remains the eligible input status.

## Relationships

```text
batch 1 ---- N parser_run
parser_run 1 ---- N parser_file_result
file_item 1 ---- N parser_file_result
file_item 1 ---- N source_chunk
file_item 1 ---- latest status/markdownPath/assetsPath/error fields updated by parser result
```

## Invariants

- `parser_file_result.sourcePath`, `pdfPath`, `markdownPath`, and `assetsPath` are relative and traversal-free when present.
- `parser_file_result.status` uses existing `FileStatus`; no file enum expansion in this slice. Skipped rows preserve the unchanged current file status and set `skipped=true`.
- `parser_run.summary` is derived from associated result rows, including skipped/ineligible report rows, not trusted independently if recomputation disagrees.
- Raw parser logs, raw local paths, secrets, hostnames, and source document content are never stored in `safeMessage` or `safeError`.
- `file_item.review_status` remains `REVIEW_REQUIRED` for generated/low-confidence/OCR-required output unless a later review slice changes it.
- `source_chunk.review_status` defaults to `REVIEW_REQUIRED`.
- `wiki_page` rows are untouched by this slice.

## API Shape Mapping

| API Field | Entity Field | Notes |
|---|---|---|
| `runId` | `parser_run.id` | Returned by create and detail endpoints. |
| `adapterKey` | `parser_run.adapterKey`, `parser_file_result.adapterKey` | Must match registered adapter. |
| `results[].fileId` | `parser_file_result.fileItemId` | Existing file item id. |
| `results[].markdownPath` | `parser_file_result.markdownPath`, `file_item.markdown_path` | Relative only. |
| `results[].assetsPath` | `parser_file_result.assetsPath`, `file_item.assets_path` | Relative only when present. |
| `results[].status` | `parser_file_result.status`, `file_item.status` | Existing `FileStatus`. |
| `results[].safeError` | `parser_file_result.safeError`, `file_item.error_message` | Sanitized only. |
| `chunks[]` | `source_chunk` rows | Created only after validation succeeds. |

## Deferred Data

- Object storage byte hashes, signed URLs, and copy/delete operations belong to `storage-adapter`.
- Wiki page publication metadata belongs to `review-publish`.
- Graph node/edge projections belong to `knowledge-graph`.
- Ask/RAG indexing metadata belongs to `ask-rag` or vector/model adapter slices.
