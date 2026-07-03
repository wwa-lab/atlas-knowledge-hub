# Data Flow: Vector Adapter

## Status

Draft. Companion to `docs/04-architecture/vector-adapter-architecture.md`.

## Overview

Vector-adapter moves Atlas source-chunk metadata through a vector adapter boundary and records index/query evidence without changing source metadata, review status, Wiki publication state, or graph state.

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
| `source_chunk` | Read for index/query trace. | Not updated by this slice. | Remains source of truth for chunk trace/review state. |
| `file_item` | Read for file scope and source path. | Not updated by vector operations. | Status/confidence/review unchanged. |
| `vector_run` | Created at request. | `REQUESTED -> RUNNING -> terminal`. | Run report and audit evidence. |
| `vector_item_result` | Created per indexed/deindexed item. | Immutable after creation. | Query/run evidence tied to source chunk. |
| Adapter capability | Read from registry. | Not persisted by this slice. | Masked API response. |

## Field Mapping

| Source Field | Vector Index Field | Notes |
|---|---|---|
| `source_chunk.id` | `chunkId` | Stable source evidence key. |
| `source_chunk.fileItemId` | `fileItemId` | Connects result back to file item. |
| `source_chunk.sourceFile` | `sourceFile` | Returned in query evidence. |
| `source_chunk.page` | `page` | Optional page trace. |
| `source_chunk.section` | `section` | Optional section trace. |
| `source_chunk.confidence` | `confidence` | Preserved, not recalculated. |
| `source_chunk.reviewStatus` | `reviewStatus` | Preserved and used for approved-only filtering. |
| Adapter result key | `vectorItemKey` | Safe adapter-facing item reference, not raw engine internals. |
| Adapter score | `score` | Query-only, finite `0.000` through `1.000`. |

## Error Cascade

| Failure | Behavior |
|---|---|
| Unknown scope | Reject before adapter execution; no vector run mutation beyond safe error if run was created. |
| Invalid dimension | Reject before adapter execution. |
| Adapter unavailable | Run terminal `FAILED`; source metadata unchanged. |
| Partial item failure | Run terminal `PARTIAL_FAILED`; per-item safe errors persisted. |
| Unsafe adapter output | Sanitize safe error/message; reject raw internals from response. |

## Verification Data Path

All automated verification uses mock source chunks and a mock/in-memory vector adapter. No real vectors from confidential documents, real vector database, model provider, network, private endpoint, or credential is required.
