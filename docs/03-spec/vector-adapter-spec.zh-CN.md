# 规格：Vector Adapter

## 状态

草稿。Phase 3 adapter 切片。`vector-adapter` 的行为真相源。来源于 `docs/02-user-stories/vector-adapter-stories.md`。

## Source Documents

- `docs/01-requirements/vector-adapter-requirements.md`
- `docs/02-user-stories/vector-adapter-stories.md`
- `docs/03-spec/parser-adapter-spec.md`
- `docs/03-spec/metadata-api-spec.md`
- `docs/architecture.md`
- `docs/markdown-standard.md`
- `docs/knowledge-graph-design.md`
- `docs/00-context/slice-roadmap.md`

## 范围

Atlas 必须通过产品面的 vector adapter contract 支持 vector indexing 与 vector similarity evidence retrieval。本切片包含 vector capability 行为、index/deindex runs、query evidence responses、source-chunk trace 保留、vector run records、per-chunk result records、mock-engine 验证、adapter seam guards、内部 API/adapter 指南。不实现真实 vector DB 执行、embedding/model 调用、Ask answer generation、图谱抽取、Wiki 发布、前端 UI、生产 auth/RBAC 或生产 secret 管理。

## 约束

- **仅 adapter：** 产品工作流依赖 vector interfaces 和 registry contracts，不直接依赖 pgvector、Milvus、Qdrant、vector SDK、JDBC vector extension 或 outbound vector client（REQ-VA-001）。
- **不硬编码单一实现：** pgvector/vector DB 是一个目标类别，但必须可通过 adapter configuration 与 capability metadata 替换（REQ-VA-002）。
- **mock-engine 验证：** 自动化测试使用 mock/in-memory vector engine，不要求真实 vector DB、embedding provider、网络或凭证（REQ-VA-010）。
- **secret/endpoint 安全：** capability response 和 error 不得暴露 raw endpoint、DSN、collection name、credential、token、hostname、stack trace 或私有路径（REQ-VA-003, REQ-VA-009）。
- **trace/review 保留：** index 和 query results 保留 source chunk trace、confidence、review status；vector 操作永不自动 approve 内容（REQ-VA-004, REQ-VA-011）。
- **阶段纪律：** vector runs 不生成 embeddings、不创建 graph nodes/edges、不发布 Wiki pages、不回答 Ask queries（REQ-VA-012）。

## Actors

| Actor | 角色 |
|---|---|
| Knowledge base administrator | 发起 index/deindex run 并查看 run report。 |
| Platform administrator | 查看 vector adapter 可用性和脱敏配置。 |
| Knowledge user / future Ask flow | 消费可追溯 similarity evidence。 |
| SME reviewer | 依赖 retrieved evidence 中 review status 持续可见。 |
| Codex implementation agent | SDD 接受后严格依据 spec 和 task checklist 实现。 |

## Functional Requirements

### Adapter Boundary

- **FR-VA-001:** Vector workflows 必须在任何 index、deindex 或 query 操作前通过 registry/capability contract 解析 vector adapter。（US-VA-001, US-VA-005）
- **FR-VA-002:** 非 adapter 产品层不得引用 pgvector、Milvus、Qdrant、vector SDK、JDBC vector extension 或 outbound vector client 来执行 vector work。（US-VA-005）
- **FR-VA-003:** Adapter registry 必须支持至少一个 configured default vector adapter，并安全暴露 unavailable/misconfigured 状态。（US-VA-002）

### Capability Metadata

- **FR-VA-004:** Capability metadata 必须包含 adapter key、display name、version、supported dimensions、supported operations、default marker、health/status、masked configuration summary。（US-VA-002）
- **FR-VA-005:** Capability metadata 不得暴露 raw endpoints、DSNs、collection names、credentials、tokens、hostnames 或私有路径。（US-VA-002）

### Indexing And Deindexing

- **FR-VA-006:** Index run 必须接受 space/batch scope、可选 file/chunk filters、adapter key、mode、review inclusion policy，以及有界 chunk vector payloads 或 deterministic mock vectors。（US-VA-001）
- **FR-VA-007:** Index input 必须引用现有 source chunks，并保留 chunk id、file item id、source file、page/section、confidence、review status。（US-VA-001）
- **FR-VA-008:** 成功 index result 必须记录 vector item key、adapter key、source chunk trace、review status、status、timestamps。（US-VA-001）
- **FR-VA-009:** Deindex run 必须能按 space、batch、file item 或 chunk id 移除 vector entries，且不得删除 source chunks 或改变 file/chunk review state。（US-VA-004）
- **FR-VA-010:** Vector run summary 必须包含 total、indexed、deleted、skipped、failed counts、adapter key、mode、terminal status、timestamps、safe message。（US-VA-001, US-VA-004）

### Similarity Query

- **FR-VA-011:** Similarity query 必须接受 space scope、query vector 或 deterministic mock query token、result limit、review inclusion flag。（US-VA-003）
- **FR-VA-012:** Similarity query results 必须有界，并按 score 降序排序，score 相同时按 chunk id 稳定升序排序。（US-VA-003）
- **FR-VA-013:** Query results 必须包含 chunk id、file item id、source file、page/section、score、confidence、review status、safe metadata。（US-VA-003）
- **FR-VA-014:** Query results 默认只返回 approved evidence；review-required evidence 只有显式请求时返回，并保持清晰 review-required 标记。（US-VA-003）
- **FR-VA-015:** Query responses 不得返回 raw vector values、raw collection internals、SDK payloads 或数据库特定 diagnostics。（US-VA-003）

### Validation And Failure Behavior

- **FR-VA-016:** 未知 adapter、space、batch、file item 或 source chunk reference 必须在 index/deindex mutation 前安全失败。（US-VA-001, US-VA-004）
- **FR-VA-017:** 当提供 dimensions 时，vector dimensions 必须匹配 adapter capability；无效 dimensions 在 adapter 执行前失败。（US-VA-001）
- **FR-VA-018:** Adapter fault 必须产生脱敏 safe message，并保持 source metadata 不变。（US-VA-004, US-VA-005）
- **FR-VA-019:** Seam guard verification 必须只允许 vector-engine 名称出现在 adapter scope，并禁止其出现在 controller/service/repository/domain 层。（US-VA-005）

## Non-Functional Requirements

| 类别 | 要求 |
|---|---|
| Security | 响应或持久化 safe message 中不得出现 raw endpoint、DSN、collection name、credential、token、hostname、private path、stack trace 或 raw SDK output。 |
| Reliability | Mock/in-memory vector 测试覆盖 capability listing、index、deindex、query、review policy、invalid dimension、unavailable adapter、safe error masking、seam guard behavior。 |
| Extensibility | Vector adapter interface 支持未来 pgvector、Milvus、Qdrant 或其他 vector 实现，而无需改变产品工作流调用方。 |
| Auditability | Vector run 和 result records 可追溯到 space、batch、file item、source chunk、adapter identity、review status、timestamps。 |
| Data safety | 仅使用 mock/sample metadata；测试中无真实公司文档、由机密数据生成的 raw vectors、私有路径、外部网络调用或生产凭证。 |

## Workflow

```text
+------------------------------+
| Source chunks + review state  |
+---------------+--------------+
                |
                v
+------------------------------+        unavailable/misconfigured
| Resolve vector adapter        |--------------------------------+
+---------------+--------------+                                |
                | available                                     v
                v                                      +------------------+
+------------------------------+                       | Safe run failure |
| Validate scope, policy,       |                       | no unsafe leak   |
| dimensions, chunk references  |                       +------------------+
+---------------+--------------+
                |
                v
+------------------------------+
| Execute vector adapter        |
| mock/in-memory in CI          |
+---------------+--------------+
                |
                v
+------------------------------+
| Persist run/result evidence   |
| preserve trace/review state   |
+------------------------------+
```

## State Model

### Vector Run Status

`REQUESTED -> RUNNING -> SUCCEEDED | PARTIAL_FAILED | FAILED`

- `REQUESTED -> RUNNING`：adapter 已解析且 validation 通过。
- `RUNNING -> SUCCEEDED`：所有请求的 index/deindex evidence 操作安全完成。
- `RUNNING -> PARTIAL_FAILED`：至少一项成功且至少一项失败或 skipped。
- `RUNNING -> FAILED`：无 item 成功，或 adapter-level failure 阻止逐项结果。

### Vector Item Status

| Outcome | Item Status | Notes |
|---|---|---|
| Chunk indexed successfully | `INDEXED` | 需要 source chunk trace 和 vector item key。 |
| Chunk deindexed successfully | `DELETED` | 只移除 vector entry。 |
| Chunk skipped | `SKIPPED` | policy 或 eligibility 在 adapter 执行前排除。 |
| Item failed | `FAILED` | 需要 user-safe error summary。 |
| Invalid target | rejected | index/deindex 前 validation error；无状态变化。 |

## Validation Rules

- Space、batch、file item、source chunk ids 必须引用现有 metadata records。
- Index input 必须携带 source chunk id 与 file item id；source trace 必须可从现有 metadata 恢复。
- Review policy values 为 `approved_only` 与 `include_review_required`；默认 `approved_only`。
- Result limit 必须有界且为正数。
- 当提供 dimensions 时，必须等于 adapter capability dimension。
- Similarity scores 必须为 `0.000` 到 `1.000` 闭区间内的有限数。
- Safe message 与 safe error 必须有界，并移除 raw SDK output、stack trace、private path、endpoint、DSN、collection name、hostname、token、credential。
- Adapter key 必须解析到 registered adapter，否则以 safe unavailable/misconfigured status 失败。

## API / Interface Surface

完整契约见 `docs/05-design/contracts/vector-adapter-API_IMPLEMENTATION_GUIDE.md`。

| Interface | Behavior |
|---|---|
| `GET /api/vector-adapters` | 列出脱敏后的 vector adapter capabilities。 |
| `POST /api/spaces/{spaceId}/vector-runs` | 对 scoped source chunks 执行 index 或 deindex run。 |
| `GET /api/vector-runs/{runId}` | 返回 vector run summary 与 per-chunk outcomes。 |
| `POST /api/spaces/{spaceId}/vector-query` | 对 indexed evidence 执行有界 similarity query。 |
| Internal vector adapter interface | 在产品面 contract 后执行 index/delete/query；测试必须有 mock/in-memory 实现。 |

## Acceptance Matrix

| Check | Requirement | Observable Result |
|---|---|---|
| AC-VA-01 | REQ-VA-001, REQ-VA-013 | direct vector-engine references 出现在 adapter implementation 外时 static guard tests 失败。 |
| AC-VA-02 | REQ-VA-002, REQ-VA-003 | Capability contract 返回脱敏 adapter metadata 和可替换 default marker。 |
| AC-VA-03 | REQ-VA-004, REQ-VA-006 | Mock index run 为每个 indexed item 记录 source chunk trace、confidence、review status。 |
| AC-VA-04 | REQ-VA-007, REQ-VA-011 | Query 返回有界、带 score 的结果，包含 source trace 与 review status；默认 approved-only。 |
| AC-VA-05 | REQ-VA-008, REQ-VA-012 | Deindex 移除 vector entries，但不删除 source metadata 或 graph/wiki records。 |
| AC-VA-06 | REQ-VA-009 | Error responses/log assertions 不包含 raw secrets、endpoints、collection names、private paths 或 SDK output。 |
| AC-VA-07 | REQ-VA-010 | `cd backend && mvn verify` 仅使用 mock/in-memory vector engine 通过。 |
| AC-VA-08 | REQ-VA-014 | Tasks 包含 REQ/spec 映射与确切命令。 |

## Out Of Scope

- 真实 pgvector/Milvus/Qdrant 执行、embedding generation、model-provider calls、RAG answer generation、Ask UI、reranking、graph derivation、Wiki publication、frontend screens、production auth/RBAC、production secret-manager integration。

## Open Questions

| ID | Question | Impact |
|---|---|---|
| OQ-VA-001 | pgvector vs standalone vector DB 作为首个真实引擎。 | 影响部署拓扑，不影响当前 mock contract。 |
| OQ-VA-002 | 未来 embedding 来源：model-adapter vs offline worker。 | 本 SDD 接受 precomputed/mock vectors，不调用模型供应商。 |
| OQ-VA-003 | 未来 Ask 的 review-required query inclusion 默认值。 | 当前契约默认 approved-only，并允许显式 inclusion。 |
