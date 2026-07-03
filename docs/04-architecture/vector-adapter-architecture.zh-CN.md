# 架构：Vector Adapter

## 状态

草稿。Phase 3 adapter 切片。来源于 `docs/03-spec/vector-adapter-spec.md`。

## 概览

Vector-adapter 在 metadata control plane 中加入可替换的向量检索 seam。后端负责 vector run state、source-chunk eligibility、review-aware query policy、safe error handling 与 result persistence；vector index execution 被隔离在产品面的 adapter contract 后。pgvector/vector DB 是首个命名目标类别，但架构保持 Milvus、Qdrant 与未来引擎可替换。

## 架构驱动

| Driver | Impact |
|---|---|
| Adapter neutrality | 产品服务通过 registry/capability contract 解析 vector adapters。 |
| Source trace preservation | 每个 index/query result 都回指 source chunks、file items、source file、page/section、confidence、review status。 |
| Review-aware retrieval | Query 默认 approved evidence；显式包含 review-required evidence 时必须清晰标记。 |
| Mock-only verification | 测试使用 mock/in-memory vector engine，不需要真实 vector DB、embedding、网络或凭证。 |
| Secret/endpoint safety | Capability 与 error output 必须脱敏且有界。 |
| Phase discipline | Vector-adapter 只准备 retrieval evidence，不生成 embeddings、不合成 Ask answers、不发布 Wiki、不派生 graph entities。 |

## 现有 Metadata 上下文

当前 metadata control plane 已拥有 source chunks、file items、review status、confidence、graph metadata placeholders、API envelopes、parser adapter run patterns 与 adapter seam guarding。Vector-adapter 应复用这些产品概念，而不是创建 vector-owned knowledge model。实现级 grounding anchors 记录在 design 与 traceability 文档中。

## System Context

| Boundary | Responsibility |
|---|---|
| Frontend | 本切片范围外。未来 UI 可消费 vector query APIs，但本切片不做前端改动。 |
| Backend API / metadata control plane | 负责 vector capability endpoints、vector run lifecycle、validation、result persistence、query responses。 |
| Vector adapter seam | 封装 vector engine-specific index/delete/query execution，并返回 Atlas 产品概念。 |
| Vector engine / worker | 产品工作流之外。pgvector、Milvus、Qdrant 或其他引擎只位于 adapter contract 后。 |
| PostgreSQL metadata | 存储 vector run records 与 vector item result evidence；source chunks/file items 仍是 trace 与 review state 真相源。 |

## High-Level Architecture

```text
+------------------------------------------------------------+
| Users / agents                                             |
| Admin, platform admin, future Ask flow, Codex implementation|
+------------------------------+-----------------------------+
                               |
                               | REST / JSON
                               v
+------------------------------------------------------------+
| Spring Boot metadata API                                   |
| Vector capabilities, vector runs, vector query evidence     |
+------------------------------+-----------------------------+
                               |
                               v
+------------------------------------------------------------+
| Vector application service                                 |
| Scope validation, chunk eligibility, adapter resolution,    |
| review policy, safe error handling, run/result persistence  |
+------------------------------+-----------------------------+
                               |
                   product-facing adapter interface
                               v
+------------------------------+        +---------------------+
| Vector adapter registry      |        | Vector adapters      |
| capability + default policy  |------->| mock/in-memory       |
+------------------------------+        | pgvector boundary    |
                                        | Milvus/Qdrant future |
                                        +----------+----------+
                                                   |
                                                   | engine/worker boundary
                                                   v
                                        +---------------------+
                                        | Vector database      |
                                        | outside product flow |
                                        +---------------------+
                               |
                               v
+------------------------------------------------------------+
| PostgreSQL metadata                                        |
| source_chunk, file_item, vector_run, vector_item_result     |
+------------------------------------------------------------+
```

## Component Breakdown

### Backend API

- **Vector adapter capability API：** 列出 vector capabilities 与脱敏配置。
- **Vector run API：** 对 scoped source chunks 执行 index/deindex runs 并返回 run records。
- **Vector query API：** 返回有界 similarity evidence，包含 source trace 与 review status。

### Application Services

- **Vector service：** 验证 space/batch/file/chunk targets、review policy、dimensions、result limits；解析 adapter；执行 mock/configured mode；映射 results；持久化 run evidence；构建 query responses。
- **Vector summary calculator：** 从 per-item results 派生 total/indexed/deleted/skipped/failed counts。
- **Vector adapter registry：** 负责 default adapter resolution 与 unavailable/misconfigured behavior。
- **Safety helpers：** 复用 safe relative-path/error-masking 实践，并新增针对 endpoints、DSNs、collection names、raw SDK output 的 vector-specific masking。

### Integration Adapters

- **VectorAdapter contract：** 接收 Atlas vector requests，返回 Atlas descriptors/results。
- **MockVectorAdapter：** 用于 CI 和集成测试的 deterministic in-memory implementation。
- **PgVectorAdapter：** 真实 adapter boundary placeholder 或 configured implementation。它可以知道 engine details，但 product layers 不得知道。

### Persistence

- 复用 `source_chunk` 与 `file_item` 作为 trace/review 真相源。
- 新增 `vector_run` 与 `vector_item_result` logical entities，记录 execution evidence 与 index/query result metadata。
- 本切片不创建 `wiki_page`、`graph_node` 或 `graph_edge` rows。

## State And Status Strategy

Vector run status：

```text
REQUESTED -> RUNNING -> SUCCEEDED
                     -> PARTIAL_FAILED
                     -> FAILED
```

Vector item status mapping：

- index success -> `INDEXED`
- deindex success -> `DELETED`
- policy 或 eligibility exclusion -> `SKIPPED`
- item failure -> `FAILED`
- invalid target -> adapter execution 前 rejected

Vector operations 不改变 `file_item.review_status`、`source_chunk.review_status`、confidence、Wiki state 或 graph state。

## API / Interface Boundaries

| Interface | Consumer | Purpose |
|---|---|---|
| `GET /api/vector-adapters` | Admin / implementation tests | 列出脱敏 vector capabilities。 |
| `POST /api/spaces/{spaceId}/vector-runs` | Internal workflow / future UI | 对 source chunks 执行 index/deindex runs。 |
| `GET /api/vector-runs/{runId}` | Delivery lead / future UI | 读取 run record 与 per-item outcomes。 |
| `POST /api/spaces/{spaceId}/vector-query` | Future Ask/search flow | 返回有界 similarity evidence。 |
| `VectorAdapter` | Vector service | 在产品面 interface 后执行 vector work。 |

## Security / Reliability / Observability

- Capability responses 仅返回脱敏/状态化信息。
- Adapter errors 在持久化或响应前必须有界并脱敏。
- Query responses 不返回 raw vector values 或 engine diagnostics。
- Run/result evidence 保留可审计 trace（space、batch、file item、source chunk、review status、score）。
- 自动化验证必须使用 mock/in-memory engine。
- Adapter seam guard 必须只允许 vector-engine 名称出现在 vector adapter package 内，并保持其在非 adapter 产品层中被禁止。

## Risks / Tradeoffs

| ID | Risk / Tradeoff | Mitigation |
|---|---|---|
| R-VA-001 | 真实 vector engine topology 尚未确定。 | 将 execution 保持在 adapter 后；mock contract 保持稳定。 |
| R-VA-002 | Embedding generation 属于 model/worker scope，不属于本切片。 | 接受 precomputed/mock vectors，并推迟 model-provider calls。 |
| R-VA-003 | Review-required chunks 可能被误认为 trusted evidence。 | Query 默认 approved-only，并保留可见 review status。 |
| R-VA-004 | 现有 seam guard 即使在 adapter scope 也禁止 vector engine names。 | 有意更新 guard，使 vector names 只允许在 adapter package。 |

## Open Questions

- OQ-VA-001：首个真实 vector engine。
- OQ-VA-002：未来 embedding 来源。
- OQ-VA-003：未来 Ask 的 review-required query inclusion policy。
