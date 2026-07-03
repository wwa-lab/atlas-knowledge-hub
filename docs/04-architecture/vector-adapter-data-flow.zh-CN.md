# 数据流：Vector Adapter

## 状态

草稿。`docs/04-architecture/vector-adapter-architecture.md` 的 companion 文档。

## 概览

Vector-adapter 将 Atlas source-chunk metadata 通过 vector adapter boundary 进入索引，并记录 index/query evidence；它不改变 source metadata、review status、Wiki publication state 或 graph state。

## Flow 1: Capability Listing

```text
+------------------+       +------------------------+       +--------------------+
| API request       | ----> | Vector adapter registry| ----> | Vector adapters    |
| GET capabilities  |       | sorted default first   |       | capability()       |
+------------------+       +-----------+------------+       +----------+---------+
                                        |                               |
                                        v                               v
                              +--------------------+          +-------------------+
                              | Mask config fields | <--------| Raw config stays  |
                              | endpoint/DSN safe  |          | inside adapter    |
                              +---------+----------+          +-------------------+
                                        |
                                        v
                              +--------------------+
                              | ApiEnvelope result |
                              +--------------------+
```

## Flow 2: Index Run

```text
+-------------------------+
| POST vector run         |
| action = index          |
+------------+------------+
             |
             v
+-------------------------+
| Validate space/batch/   |
| file/chunk scope        |
+------------+------------+
             |
             v
+-------------------------+
| Load source_chunk +     |
| file_item trace state   |
+------------+------------+
             |
             v
+-------------------------+      invalid scope/dimensions
| Validate review policy, |----------------------------+
| dimension, result limit |                            |
+------------+------------+                            v
             |                                 +----------------+
             v                                 | Safe 400 error |
+-------------------------+                    | no mutation    |
| Resolve vector adapter  |                    +----------------+
+------------+------------+
             |
             v
+-------------------------+
| Adapter index mock/     |
| configured vector items |
+------------+------------+
             |
             v
+-------------------------+
| Persist vector_run and  |
| vector_item_result rows |
+------------+------------+
             |
             v
+-------------------------+
| Return run report       |
+-------------------------+
```

## Flow 3: Similarity Query

```text
+--------------------------+
| POST vector query         |
| query vector/mock token   |
+-------------+------------+
              |
              v
+--------------------------+
| Validate space, limit,    |
| review inclusion policy   |
+-------------+------------+
              |
              v
+--------------------------+
| Resolve vector adapter    |
+-------------+------------+
              |
              v
+--------------------------+
| Adapter returns scored    |
| vector item matches       |
+-------------+------------+
              |
              v
+--------------------------+
| Join source_chunk/file    |
| trace and review state    |
+-------------+------------+
              |
              v
+--------------------------+
| Filter approved-only by   |
| default; sort by score    |
+-------------+------------+
              |
              v
+--------------------------+
| Return safe evidence      |
| no raw vectors/internals  |
+--------------------------+
```

## Flow 4: Deindex Run

```text
+-----------------------+      +-----------------------+
| POST vector run       | ---> | Resolve scoped vector |
| action = deindex      |      | item keys             |
+----------+------------+      +-----------+-----------+
           |                               |
           v                               v
+-----------------------+      +-----------------------+
| Validate scope and    | ---> | Adapter delete        |
| preserve source state |      | index entries only    |
+----------+------------+      +-----------+-----------+
           |                               |
           v                               v
+-----------------------+      +-----------------------+
| Persist deleted/      | <--- | Per-item outcomes     |
| failed/skipped result |      | from adapter          |
+-----------------------+      +-----------------------+
```

## Data Objects And Lifecycle

| Object | Created / Read | Updated | Terminal / Output |
|---|---|---|---|
| `source_chunk` | 为 index/query trace 读取。 | 本切片不更新。 | 仍是 chunk trace/review state 真相源。 |
| `file_item` | 为 file scope 和 source path 读取。 | vector operations 不更新。 | Status/confidence/review 不变。 |
| `vector_run` | 请求时创建。 | `REQUESTED -> RUNNING -> terminal`。 | Run report 与 audit evidence。 |
| `vector_item_result` | 每个 indexed/deindexed item 创建。 | 创建后不可变。 | 绑定 source chunk 的 query/run evidence。 |
| Adapter capability | 从 registry 读取。 | 本切片不持久化。 | 脱敏 API response。 |

## Field Mapping

| Source Field | Vector Index Field | Notes |
|---|---|---|
| `source_chunk.id` | `chunkId` | 稳定 source evidence key。 |
| `source_chunk.fileItemId` | `fileItemId` | 将结果连回 file item。 |
| `source_chunk.sourceFile` | `sourceFile` | query evidence 中返回。 |
| `source_chunk.page` | `page` | 可选 page trace。 |
| `source_chunk.section` | `section` | 可选 section trace。 |
| `source_chunk.confidence` | `confidence` | 保留，不重新计算。 |
| `source_chunk.reviewStatus` | `reviewStatus` | 保留，并用于 approved-only filtering。 |
| Adapter result key | `vectorItemKey` | 安全 adapter-facing item reference，不是 raw engine internals。 |
| Adapter score | `score` | Query-only，有限数 `0.000` 到 `1.000`。 |

## Error Cascade

| Failure | Behavior |
|---|---|
| Unknown scope | adapter execution 前拒绝；若 run 已创建，只记录 safe error。 |
| Invalid dimension | adapter execution 前拒绝。 |
| Adapter unavailable | Run terminal `FAILED`；source metadata 不变。 |
| Partial item failure | Run terminal `PARTIAL_FAILED`；持久化 per-item safe errors。 |
| Unsafe adapter output | 脱敏 safe error/message；响应中拒绝 raw internals。 |

## Verification Data Path

所有自动化验证使用 mock source chunks 与 mock/in-memory vector adapter。不需要来自机密文档的真实 vectors、真实 vector database、model provider、网络、私有 endpoint 或凭证。
