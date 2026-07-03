# 详细设计：Vector Adapter

## 状态

草稿。Phase 3 adapter 切片。来源于 `docs/03-spec/vector-adapter-spec.md` 与 `docs/04-architecture/vector-adapter-architecture.md`。

## Source Architecture

Vector-adapter 是 backend/API + adapter contract 切片。它在 metadata control plane 中增加 vector capability、index/deindex 与 query evidence 行为，同时把 vector engine details 保持在 adapter implementations 内。设计沿用 converter/parser adapter 的 run/report 形态，便于 Codex 使用现有本地模式实现。

## Grounded Existing Code Context

这些 anchor 已在设计前验证：

| Existing element | Verified anchor | Vector-adapter use |
|---|---|---|
| Source chunk trace fields | `backend/src/main/java/com/atlas/metadata/domain/SourceChunk.java:21` | 复用 file item id、source file、page/section、confidence、review status 作为 vector evidence。 |
| File item trace/review fields | `backend/src/main/java/com/atlas/metadata/domain/FileItem.java:24` | 验证 batch/file scope，同时保留 status、confidence、review status。 |
| Parser run evidence pattern | `backend/src/main/java/com/atlas/metadata/domain/ParserRun.java:13` | 复用 run lifecycle 和 safe message pattern。 |
| Parser result evidence pattern | `backend/src/main/java/com/atlas/metadata/domain/ParserFileResult.java:14` | 复用 per-item result persistence style。 |
| Parser adapter contract | `backend/src/main/java/com/atlas/metadata/adapter/ParserAdapter.java:3` | 复用产品面 adapter interface 形态。 |
| Parser capability metadata | `backend/src/main/java/com/atlas/metadata/adapter/ParserCapability.java:9` | 复用 masked capability record。 |
| Parser registry | `backend/src/main/java/com/atlas/metadata/service/ParserAdapterRegistry.java:11` | 复用 explicit/default adapter resolution。 |
| API envelope | `backend/src/main/java/com/atlas/metadata/dto/ApiEnvelope.java:1` | 所有 vector responses 使用 `ApiEnvelope`。 |
| Parser controller pattern | `backend/src/main/java/com/atlas/metadata/controller/ParserController.java:18` | 遵循 capability/create/get route 形态。 |
| Adapter seam guard | `backend/src/test/java/com/atlas/metadata/integration/AdapterSeamGuardTest.java:15` | 必须更新：当前 vector-engine names 在 adapter scope 中也被禁止。 |

## Design Scope

### 范围内

- Vector adapter domain contracts、capability metadata、request/result shapes。
- Vector run persistence 与 per-item result persistence。
- Vector service 行为：target validation、adapter resolution、review policy、dimension validation、execution、summary calculation、safe error handling、query result mapping。
- Vector API guide 与 contract tests。
- 用于 deterministic tests 的 mock/in-memory vector adapter。

### 范围外

- 真实 vector runtime topology、embedding/model provider calls、Ask answer synthesis、graph extraction、Wiki publication、frontend UI、production auth/RBAC、production secret manager integration。

## Module Design

### Vector Adapter Contract

概念 contract：

```text
VectorAdapter
  capability() -> VectorCapability
  index(VectorIndexRequest) -> VectorIndexResult
  delete(VectorDeleteRequest) -> VectorDeleteResult
  query(VectorQueryRequest) -> VectorQueryResult
```

`VectorCapability` 包含：

- adapter key 与 display name
- safe version/status
- supported dimensions
- supported operations：`INDEX`、`DEINDEX`、`QUERY`
- default marker
- masked configuration summary（endpoint、DSN、collection、credentials 仅状态化）

`VectorIndexRequest` 包含：

- run id、space/batch scope、review policy、mode
- index items：source chunk id、file item id、trace metadata、review status、confidence、vector payload/reference

`VectorQueryRequest` 包含：

- space id、query vector 或 deterministic mock query token、limit、review policy

`VectorQueryResult` 包含：

- 带 score 的 item matches，含 safe vector item keys 和 source chunk references

### Vector Service

职责：

- 验证 space、可选 batch、file item、source chunk scope。
- 验证 review policy、result limit、dimensions、adapter key。
- 选择 eligible chunks：默认 approved-only；只有显式请求时包含 review-required。
- 按 explicit key 或 default marker 解析 adapter。
- 创建 vector run，标记 running，执行 adapter，持久化已验证 item results。
- 计算 summary counts 与 terminal status。
- 通过 source chunk/file metadata join 返回 query evidence。
- 持久化/响应前脱敏 safe messages 与 safe errors。

### Vector Summary Calculator

| Count | Rule |
|---|---|
| `total` | run 考虑的所有 candidate items。 |
| `indexed` | Status `INDEXED`。 |
| `deleted` | Status `DELETED`。 |
| `skipped` | Status `SKIPPED`。 |
| `failed` | Status `FAILED`。 |

Terminal run status：

- 所有可执行 item 成功时为 `SUCCEEDED`。
- 至少一项成功且至少一项 failed 或 skipped 时为 `PARTIAL_FAILED`。
- 无 item 成功或 adapter-level failure 阻止 per-item results 时为 `FAILED`。

### Persistence

新增与 data model 等价的 logical domain entities：

- `VectorRun`
- `VectorItemResult`
- `VectorRunOperation`、`VectorRunStatus`、`VectorItemStatus`、`VectorReviewPolicy`、`VectorAdapterStatus`

Repositories 只用于 vector run/result persistence 和现有 source chunk/file lookup。不得修改 source chunks、file items、Wiki pages 或 graph rows。

### DTOs And Mapping

DTOs 应匹配 API guide：

- `VectorCapabilityResponse`
- `CreateVectorRunRequest`
- `VectorRunResponse`
- `VectorRunSummaryResponse`
- `VectorItemResultResponse`
- `VectorQueryRequest`
- `VectorQueryResponse`
- `VectorQueryMatchResponse`

所有 API responses 使用 `ApiEnvelope`。

## API / Interface Design

API implementation guide 是 payload 权威：

- `GET /api/vector-adapters`
- `POST /api/spaces/{spaceId}/vector-runs`
- `GET /api/vector-runs/{runId}`
- `POST /api/spaces/{spaceId}/vector-query`

Authentication 在本切片中保持 deferred/internal-only。

## Data Design

Data model 位于 `docs/04-architecture/vector-adapter-data-model.md`。

重要不变量：

- 不新增 `FileStatus` values。
- Source chunk 和 file item 的 review/confidence/status 永不被 vector operations 改变。
- `wiki_page`、`graph_node`、`graph_edge` rows 不被触碰。
- Query results 不暴露 raw vectors 或 engine internals。
- Review-required evidence 继续标记为 review-required。

## Workflow / Execution Design

### Successful Index

1. API 接收 operation `index` 的 vector run request。
2. Service 验证 space、可选 batch/file/chunk scope、review policy、dimensions。
3. Service 加载 eligible source chunks 与 file items。
4. Registry 解析 vector adapter。
5. Service 创建 vector run 并标记 `RUNNING`。
6. Adapter 索引每个 item 并返回 per-item outcomes。
7. Service 验证 adapter results 并持久化 `vector_item_result` rows。
8. Service 计算 summary 与 terminal status。
9. API 返回 vector run response。

### Successful Query

1. API 接收 vector query request。
2. Service 验证 space、limit、review policy、query vector/mock token。
3. Registry 解析 vector adapter。
4. Adapter 返回 scored matches。
5. Service 将 matches join 回 source chunk/file metadata。
6. Service 默认应用 approved-only filtering，并稳定按 score 排序。
7. API 返回 safe evidence matches。

### Adapter Unavailable

- 对 index/deindex，vector run 变为 `FAILED`。
- Query 返回 safe adapter unavailable error。
- Source metadata 保持不变。

### Unsafe Output

- Raw vectors、endpoints、DSNs、collection names、SDK output、stack traces、private paths 从 safe messages/errors 中移除。

## Validation And Error Handling

| Case | Expected handling |
|---|---|
| Unknown space | 404 safe not found。 |
| Unknown batch/file/chunk in requested scope | 404 safe not found 或 400 validation error，不泄露无关 membership。 |
| Unknown adapter key | 400 validation error。 |
| Adapter unavailable/misconfigured | Safe failed run 或 safe query error。 |
| Invalid dimension | adapter execution 前 400 validation error。 |
| Invalid result limit | 400 validation error。 |
| Raw vector in response candidate | 删除 raw vector fields，只持久化 safe metadata。 |
| Adapter exception | Safe `FAILED` status/message；source metadata 不变。 |

## Edge Case Trace

### Review Policy

规则：query 默认 `APPROVED_ONLY`。

| Input | Result |
|---|---|
| Approved chunk, default policy | score 符合时返回。 |
| Review-required chunk, default policy | 被过滤。 |
| Review-required chunk, `INCLUDE_REVIEW_REQUIRED` | 返回并标记 `REVIEW_REQUIRED`。 |

### Score Sorting

规则：score 降序，tie 时 chunk id 升序。

| Input | Result |
|---|---|
| `chunk-b=0.91`, `chunk-a=0.87` | `chunk-b` 然后 `chunk-a`。 |
| `chunk-b=0.91`, `chunk-a=0.91` | `chunk-a` 然后 `chunk-b`。 |
| score missing 或 non-finite | 作为 invalid adapter output 拒绝。 |

### Dimension Validation

规则：提供的 vector dimension 必须等于 adapter capability dimension。

| Input | Result |
|---|---|
| capability `1536`, vector length `1536` | 接受。 |
| capability `1536`, vector length `768` | adapter execution 前拒绝。 |
| mock token with no vector length | 只在 mock mode 接受。 |

## Seam Guard Update (Required)

`AdapterSeamGuardTest` 当前禁止 `pgvector`、`Milvus`、`Qdrant` 出现在所有 adapter source 中。vector adapter 合理引用这些 vector-engine concepts，因此实现必须：

- 放宽 adapter-scope assertion，使 vector adapter package 可以包含 vector-engine names。
- 将 vector-engine names 与 SDK/client types 加入 non-adapter forbidden list，确保它们仍被禁止出现在 adapter scope 外。
- 保持 `WebClient`、`RestTemplate`、`HttpClient` 在 adapter scope 外被禁止。

在 tasks 和 traceability 中记录此变更，确保 guard update 是有意的。

## Testing Considerations

必需实现验证：

```bash
cd backend && mvn verify
git diff --check
! rg -n "pgvector|Milvus|Qdrant|EmbeddingClient|VectorStore|VectorDb|JDBC vector|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/vector-adapter-requirements.md docs/02-user-stories/vector-adapter-stories.md docs/03-spec/vector-adapter-spec.md docs/04-architecture/vector-adapter-architecture.md docs/04-architecture/vector-adapter-data-flow.md docs/04-architecture/vector-adapter-data-model.md docs/05-design/vector-adapter-design.md docs/05-design/contracts/vector-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/vector-adapter-tasks.md
```

测试覆盖必须包含：

- 使用 mock vector engine 的 adapter contract tests。
- Summary calculator unit tests。
- Service validation、review policy、dimensions、query sorting tests。
- API contract integration tests。
- Vector engine references 的 seam guard test update。
