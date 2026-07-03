# Data Model: Vector Adapter

## Status

Draft logical data model for the Phase 3 `vector-adapter` slice.

## Overview

Vector-adapter adds run/result evidence for vector indexing and retrieval. Existing `source_chunk` and `file_item` records remain the source of truth for trace, confidence, and review status.

## Entity Relationship Diagram

```text
+------------------+       1:N       +------------------+       1:N       +----------------------+
| space            | --------------> | batch            | --------------> | file_item            |
+------------------+                 +------------------+                 +----------+-----------+
                                                                                     |
                                                                                     | 1:N
                                                                                     v
                                                                           +----------------------+
                                                                           | source_chunk         |
                                                                           +----------+-----------+
                                                                                      |
                                                                                      | 1:N
                                                                                      v
+------------------+       1:N       +----------------------+
| vector_run       | --------------> | vector_item_result   |
+------------------+                 +----------------------+
```

## Existing Entities Reused

### `source_chunk`

| Field | Use In Vector Adapter |
|---|---|
| `id` | Chunk identity and query evidence key. |
| `file_item_id` | File-level trace and scope validation. |
| `source_file` | Returned in query evidence. |
| `page` | Optional source trace. |
| `section` | Optional source trace. |
| `confidence` | Preserved and returned; not recalculated by vector adapter. |
| `review_status` | Controls approved-only default query behavior. |

### `file_item`

| Field | Use In Vector Adapter |
|---|---|
| `id` | File/chunk scope validation. |
| `batch_id` | Batch scope validation. |
| `source_path` | Source trace context. |
| `status` | Eligibility context only; not changed. |
| `confidence` | File-level context; not changed. |
| `review_status` | File-level context; not changed. |

## New Logical Entities

### `vector_run`

| Column | Type | Nullable | Description |
|---|---|---|---|
| `id` | String | No | Stable run id. |
| `space_id` | String | No | Knowledge Space scope. |
| `batch_id` | String | Yes | Optional batch scope. |
| `adapter_key` | String | No | Resolved adapter key. |
| `adapter_version` | String | Yes | Safe adapter version. |
| `operation` | Enum | No | `INDEX`, `DEINDEX`. |
| `status` | Enum | No | `REQUESTED`, `RUNNING`, `SUCCEEDED`, `PARTIAL_FAILED`, `FAILED`. |
| `mode` | String | No | `mock` or `configured`. |
| `review_policy` | Enum | No | `APPROVED_ONLY`, `INCLUDE_REVIEW_REQUIRED`. |
| `dimension` | Integer | Yes | Vector dimension used, when known. |
| `requested_by` | String | Yes | Mock/internal requester. |
| `total_count` | Integer | No | Total requested/considered items. |
| `indexed_count` | Integer | No | Items indexed. |
| `deleted_count` | Integer | No | Items deleted. |
| `skipped_count` | Integer | No | Items skipped. |
| `failed_count` | Integer | No | Items failed. |
| `started_at` | Timestamp | No | Run start. |
| `completed_at` | Timestamp | Yes | Run completion. |
| `safe_message` | String | Yes | Sanitized summary. |

### `vector_item_result`

| Column | Type | Nullable | Description |
|---|---|---|---|
| `id` | String | No | Stable result id. |
| `run_id` | String | No | Parent vector run. |
| `source_chunk_id` | String | No | Source evidence chunk. |
| `file_item_id` | String | No | File item trace. |
| `source_file` | String | No | Source file trace. |
| `page` | Integer | Yes | Page trace. |
| `section` | String | Yes | Section trace. |
| `review_status` | Enum | No | Preserved source chunk review status. |
| `confidence` | Decimal | Yes | Preserved source chunk confidence. |
| `vector_item_key` | String | Yes | Safe adapter item key. |
| `status` | Enum | No | `INDEXED`, `DELETED`, `SKIPPED`, `FAILED`. |
| `score` | Decimal | Yes | Query score, `0.000` through `1.000`. |
| `safe_error` | String | Yes | Sanitized item error. |
| `created_at` | Timestamp | No | Result creation time. |

## Enums

| Enum | Values |
|---|---|
| `VectorAdapterStatus` | `AVAILABLE`, `UNAVAILABLE`, `MISCONFIGURED` |
| `VectorRunOperation` | `INDEX`, `DEINDEX` |
| `VectorRunStatus` | `REQUESTED`, `RUNNING`, `SUCCEEDED`, `PARTIAL_FAILED`, `FAILED` |
| `VectorItemStatus` | `INDEXED`, `DELETED`, `SKIPPED`, `FAILED` |
| `VectorReviewPolicy` | `APPROVED_ONLY`, `INCLUDE_REVIEW_REQUIRED` |

## State Models

### Vector Run

```text
REQUESTED -> RUNNING -> SUCCEEDED
                     -> PARTIAL_FAILED
                     -> FAILED
```

### Vector Item

```text
candidate -> INDEXED
          -> DELETED
          -> SKIPPED
          -> FAILED
```

## Constraints And Indexes

- `vector_run.status`, `operation`, `mode`, and `review_policy` are constrained enum values.
- Counts are non-negative.
- `dimension`, when present, is positive.
- `vector_item_result.score`, when present, is between `0.000` and `1.000`.
- Query paths need indexes on `vector_run(space_id, status)` and `vector_item_result(source_chunk_id)`.
- Run report paths need an index on `vector_item_result(run_id, created_at)`.

## Non-Mutation Rules

- Vector runs do not mutate `source_chunk`.
- Vector runs do not mutate `file_item`.
- Vector runs do not create `wiki_page`, `graph_node`, or `graph_edge`.
- Vector query results are evidence, not trusted answers.
