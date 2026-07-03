# 数据模型：Vector Adapter

## 状态

Phase 3 `vector-adapter` 切片的草稿 logical data model。

## 概览

Vector-adapter 为 vector indexing 和 retrieval 增加 run/result evidence。现有 `source_chunk` 与 `file_item` records 仍是 trace、confidence、review status 的真相源。

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

## 复用的现有实体

### `source_chunk`

| Field | Vector Adapter 用法 |
|---|---|
| `id` | Chunk identity 与 query evidence key。 |
| `file_item_id` | File-level trace 与 scope validation。 |
| `source_file` | 在 query evidence 中返回。 |
| `page` | 可选 source trace。 |
| `section` | 可选 source trace。 |
| `confidence` | 保留并返回；vector adapter 不重新计算。 |
| `review_status` | 控制 approved-only 默认 query 行为。 |

### `file_item`

| Field | Vector Adapter 用法 |
|---|---|
| `id` | File/chunk scope validation。 |
| `batch_id` | Batch scope validation。 |
| `source_path` | Source trace context。 |
| `status` | 仅作为 eligibility context；不改变。 |
| `confidence` | File-level context；不改变。 |
| `review_status` | File-level context；不改变。 |

## 新增 Logical Entities

### `vector_run`

| Column | Type | Nullable | Description |
|---|---|---|---|
| `id` | String | No | 稳定 run id。 |
| `space_id` | String | No | Knowledge Space scope。 |
| `batch_id` | String | Yes | 可选 batch scope。 |
| `adapter_key` | String | No | 已解析 adapter key。 |
| `adapter_version` | String | Yes | 安全 adapter version。 |
| `operation` | Enum | No | `INDEX`, `DEINDEX`。 |
| `status` | Enum | No | `REQUESTED`, `RUNNING`, `SUCCEEDED`, `PARTIAL_FAILED`, `FAILED`。 |
| `mode` | String | No | `mock` 或 `configured`。 |
| `review_policy` | Enum | No | `APPROVED_ONLY`, `INCLUDE_REVIEW_REQUIRED`。 |
| `dimension` | Integer | Yes | 使用的 vector dimension（如已知）。 |
| `requested_by` | String | Yes | Mock/internal requester。 |
| `total_count` | Integer | No | 请求/候选 item 总数。 |
| `indexed_count` | Integer | No | Indexed items。 |
| `deleted_count` | Integer | No | Deleted items。 |
| `skipped_count` | Integer | No | Skipped items。 |
| `failed_count` | Integer | No | Failed items。 |
| `started_at` | Timestamp | No | Run start。 |
| `completed_at` | Timestamp | Yes | Run completion。 |
| `safe_message` | String | Yes | 脱敏 summary。 |

### `vector_item_result`

| Column | Type | Nullable | Description |
|---|---|---|---|
| `id` | String | No | 稳定 result id。 |
| `run_id` | String | No | 父 vector run。 |
| `source_chunk_id` | String | No | Source evidence chunk。 |
| `file_item_id` | String | No | File item trace。 |
| `source_file` | String | No | Source file trace。 |
| `page` | Integer | Yes | Page trace。 |
| `section` | String | Yes | Section trace。 |
| `review_status` | Enum | No | 保留的 source chunk review status。 |
| `confidence` | Decimal | Yes | 保留的 source chunk confidence。 |
| `vector_item_key` | String | Yes | 安全 adapter item key。 |
| `status` | Enum | No | `INDEXED`, `DELETED`, `SKIPPED`, `FAILED`。 |
| `score` | Decimal | Yes | Query score，`0.000` 到 `1.000`。 |
| `safe_error` | String | Yes | 脱敏 item error。 |
| `created_at` | Timestamp | No | Result creation time。 |

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

- `vector_run.status`、`operation`、`mode`、`review_policy` 是受约束 enum values。
- Counts 非负。
- `dimension` 如存在必须为正。
- `vector_item_result.score` 如存在必须在 `0.000` 与 `1.000` 之间。
- Query path 需要 `vector_run(space_id, status)` 和 `vector_item_result(source_chunk_id)` indexes。
- Run report path 需要 `vector_item_result(run_id, created_at)` index。

## Non-Mutation Rules

- Vector runs 不修改 `source_chunk`。
- Vector runs 不修改 `file_item`。
- Vector runs 不创建 `wiki_page`、`graph_node` 或 `graph_edge`。
- Vector query results 是 evidence，不是 trusted answers。
