# 任务：Vector Adapter

## 状态

`vector-adapter` 的草稿实现清单。只有 SDD set 被接受后才可执行。本轮 SDD pass 不实现产品代码。

## Source Design

- Spec：`docs/03-spec/vector-adapter-spec.md`
- Design：`docs/05-design/vector-adapter-design.md`
- API guide：`docs/05-design/contracts/vector-adapter-API_IMPLEMENTATION_GUIDE.md`
- Data model：`docs/04-architecture/vector-adapter-data-model.md`

## 每个任务的约束

- 自动化测试使用 mock/in-memory vector engine。
- Vector execution 必须位于产品面 adapter contracts 后。
- Controller/service/repository/domain 层不得调用 pgvector、Milvus、Qdrant、vector SDK、JDBC vector extension 或 outbound vector client。
- 不引入外部网络依赖、云调用、真实 vector database、真实 embedding/model call 或凭证。
- 保留 source trace、confidence、review status；vector operations 不改变 source chunks、file items、Wiki pages 或 graph rows。
- 脱敏 secrets、endpoints、DSNs、collection names、credentials、tokens、raw SDK output、hostnames、stack traces、private paths。
- 只使用 mock/sample metadata；不得持久化来自机密内容的 raw confidential vectors 或 document content。

## Workstreams

| Workstream | Tasks |
|---|---|
| Domain and persistence | T-VA-001, T-VA-002 |
| Adapter contract | T-VA-003, T-VA-004 |
| Service behavior | T-VA-005, T-VA-006, T-VA-007 |
| API contract | T-VA-008 |
| Verification and guards | T-VA-009, T-VA-010 |

## Task Details

### T-VA-001: Add vector run domain model and migration

- **Maps to:** REQ-VA-004, REQ-VA-005, REQ-VA-006, REQ-VA-011, REQ-VA-012；spec sections "Indexing And Deindexing", "State Model"；data model `vector_run`, `vector_item_result`。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** None
- **Scope:** 添加 `VectorRunOperation`、`VectorRunStatus`、`VectorItemStatus`、`VectorReviewPolicy`、`VectorAdapterStatus` enums；`VectorRun` 与 `VectorItemResult` entities；repositories；以及 vector execution evidence 的 Flyway migration。不得新增 `FileStatus`，不得修改 graph/wiki tables。
- **Constraints:** Adapter boundary；mock-only test data；trace/review preservation；secret/endpoint safety。
- **Verification:**

```bash
cd backend && mvn verify
git diff --check
```

### T-VA-002: Add vector DTOs and mapping contracts

- **Maps to:** REQ-VA-003, REQ-VA-006, REQ-VA-007；spec sections "Capability Metadata", "Similarity Query", "API / Interface Surface"；API guide response shapes。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-VA-001
- **Scope:** 添加 vector capability、create-run request、run response、summary response、item result response、query request、query response、query match response mapping。所有 controller responses 使用 `ApiEnvelope`。
- **Constraints:** Secret-masked capability summaries；不返回 raw runtime details 或 raw vectors。
- **Verification:**

```bash
cd backend && mvn test
git diff --check
```

### T-VA-003: Define vector adapter interface and capability model

- **Maps to:** REQ-VA-001, REQ-VA-002, REQ-VA-003；spec sections "Adapter Boundary", "Capability Metadata"；design "Vector Adapter Contract"。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-VA-002
- **Scope:** 添加产品面 `VectorAdapter` interface（`capability`, `index`, `delete`, `query`）、capability record、index/delete/query request records、result records、match descriptors。包含 adapter key、safe status、supported dimensions、supported operations、default marker、masked config summary。
- **Constraints:** 只暴露产品概念；interface 中无 direct vector-engine call；无 network/client dependency。
- **Verification:**

```bash
cd backend && mvn -Dtest=VectorAdapterContractTest test
! rg -n "WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/adapter/VectorAdapter.java
```

### T-VA-004: Add mock and configured vector adapter implementations

- **Maps to:** REQ-VA-002, REQ-VA-010；spec sections "Adapter Boundary", "Indexing And Deindexing", "Similarity Query"；design "Vector Adapter Contract"。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-VA-003
- **Scope:** 添加 deterministic mock/in-memory vector adapter 用于 CI，以及一个 configured pgvector/vector DB adapter boundary，后者只报告 safe capability metadata。真实 vector execution 保留在 adapter 后，并可在 runtime topology 决定前作为 safe placeholder。
- **Constraints:** Mock-only verification；无外部网络；capability response 不含 raw endpoint/DSN/collection/credential/token/private path。
- **Verification:**

```bash
cd backend && mvn -Dtest=VectorAdapterContractTest test
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src/main/java/com/atlas/metadata/adapter
```

### T-VA-005: Implement vector adapter registry and capability listing

- **Maps to:** REQ-VA-001, REQ-VA-003；spec sections "Adapter Boundary", "Capability Metadata"；API guide `GET /api/vector-adapters`。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-VA-003, T-VA-004
- **Scope:** 添加 explicit/default vector adapter key 的 registry resolution，以及 unavailable/misconfigured statuses。添加带 masked config 的 capability listing service behavior。
- **Constraints:** Adapter boundary；secret masking；不得把单一实现硬编码为唯一未来选项。
- **Verification:**

```bash
cd backend && mvn -Dtest=VectorAdapterRegistryTest test
git diff --check
```

### T-VA-006: Implement vector run service and source-chunk validation

- **Maps to:** REQ-VA-004, REQ-VA-005, REQ-VA-006, REQ-VA-010, REQ-VA-011, REQ-VA-012；spec sections "Indexing And Deindexing", "Validation And Failure Behavior"；design "Vector Service"。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-VA-001, T-VA-003, T-VA-005
- **Scope:** 实现 index/deindex run creation、source-chunk/file/batch/space validation、review policy filtering、dimension validation、adapter execution、result validation、summary calculation、terminal status mapping、安全 adapter-fault handling。
- **Constraints:** 测试中使用 mock-engine execution；invalid scope/dimensions 在 adapter execution 前拒绝；不修改 source metadata。
- **Verification:**

```bash
cd backend && mvn -Dtest=VectorServiceTest,VectorSummaryCalculatorTest test
git diff --check
```

### T-VA-007: Implement review-aware query evidence mapping

- **Maps to:** REQ-VA-007, REQ-VA-009, REQ-VA-011；spec sections "Similarity Query", "Validation And Failure Behavior"；design "Successful Query"。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-VA-006
- **Scope:** 实现 vector query service behavior：验证 query vector/mock token 与 bounded limit、解析 adapter、将 adapter matches 映射回 source chunk/file metadata、默认 approved-only filtering、支持显式包含 review-required、按 score 降序和 chunk id 升序排序、脱敏 safe metadata。
- **Constraints:** responses 中无 raw vectors 或 engine internals；review-required evidence 继续清晰标记。
- **Verification:**

```bash
cd backend && mvn -Dtest=VectorQueryServiceTest,VectorServiceTest test
git diff --check
```

### T-VA-008: Add vector REST API endpoints

- **Maps to:** REQ-VA-003, REQ-VA-005, REQ-VA-007, REQ-VA-014；spec section "API / Interface Surface"；API guide all endpoints。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-VA-005, T-VA-006, T-VA-007
- **Scope:** 添加 vector controller endpoints：`GET /api/vector-adapters`、`POST /api/spaces/{spaceId}/vector-runs`、`GET /api/vector-runs/{runId}`、`POST /api/spaces/{spaceId}/vector-query`。Responses 必须使用 `ApiEnvelope` 并匹配 API guide。
- **Constraints:** Internal-only；不实现 auth/RBAC；无 raw vector output；无 Ask/RAG answer synthesis。
- **Verification:**

```bash
cd backend && mvn -Dit.test=VectorApiContractIT verify
git diff --check
```

### T-VA-009: Update adapter seam and safety guards

- **Maps to:** REQ-VA-001, REQ-VA-009, REQ-VA-010, REQ-VA-013；spec sections "Adapter Boundary", "Validation And Failure Behavior"；design "Seam Guard Update (Required)"。
- **Owner type:** QA/backend
- **Priority:** Must
- **Dependencies:** T-VA-003, T-VA-004, T-VA-008
- **Scope:** 更新 `AdapterSeamGuardTest`，让 adapter-scope assertion 允许 vector-engine names 出现在 vector adapter package 内，同时非 adapter 产品层继续禁止 `pgvector`、`Milvus`、`Qdrant`、vector SDK/client names、JDBC vector extension calls、`WebClient`、`RestTemplate`、`HttpClient`。为 vector implementation 和 docs 添加 secret/private-path scans。
- **Constraints:** Adapter 外禁止 direct vector calls；仅 secret-masked output。必须在 traceability 中记录此 guard change。
- **Verification:**

```bash
cd backend && mvn -Dtest=AdapterSeamGuardTest test
! rg -n "pgvector|Milvus|Qdrant|EmbeddingClient|VectorStore|VectorDb|JDBC vector|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/vector-adapter-requirements.md docs/02-user-stories/vector-adapter-stories.md docs/03-spec/vector-adapter-spec.md docs/04-architecture/vector-adapter-architecture.md docs/04-architecture/vector-adapter-data-flow.md docs/04-architecture/vector-adapter-data-model.md docs/05-design/vector-adapter-design.md docs/05-design/contracts/vector-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/vector-adapter-tasks.md
```

### T-VA-010: Run final vector-adapter verification

- **Maps to:** REQ-VA-010, REQ-VA-011, REQ-VA-013, REQ-VA-014；spec acceptance matrix AC-VA-01 through AC-VA-08。
- **Owner type:** QA/backend
- **Priority:** Must
- **Dependencies:** T-VA-001 through T-VA-009
- **Scope:** 运行完整 verification set，review diff；若后续实现更改 docs/status，则更新 traceability evidence。
- **Constraints:** Mock-only；adapter only；无外部网络/云/model calls；secret-masked；trace/review preserved；不修改 graph/wiki/source metadata。
- **Verification:**

```bash
cd backend && mvn verify
git diff --check
! rg -n "pgvector|Milvus|Qdrant|EmbeddingClient|VectorStore|VectorDb|JDBC vector|WebClient|RestTemplate|HttpClient" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/vector-adapter-requirements.md docs/02-user-stories/vector-adapter-stories.md docs/03-spec/vector-adapter-spec.md docs/04-architecture/vector-adapter-architecture.md docs/04-architecture/vector-adapter-data-flow.md docs/04-architecture/vector-adapter-data-model.md docs/05-design/vector-adapter-design.md docs/05-design/contracts/vector-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/vector-adapter-tasks.md
```

## Dependency Plan

- Critical path：T-VA-001 -> T-VA-002 -> T-VA-003 -> T-VA-004 -> T-VA-005 -> T-VA-006 -> T-VA-007 -> T-VA-008 -> T-VA-009 -> T-VA-010
- Parallel opportunities after T-VA-003：adapter contract tests 与 DTO mapper tests 可与 service tests 并行构建。

## Open Questions / Risks

- OQ-VA-001：首个真实 vector engine 延后；mock contract 不依赖它。
- OQ-VA-002：真实 embedding generation 延后到 model-adapter 或 worker scope。
- OQ-VA-003：query 默认 approved-only；Ask hardening 时产品可重新评估 review-required inclusion。
- R-VA-004：现有 seam guard 在 adapter scope 中也禁止 vector engine names；T-VA-009 必须有意更新，而非偶然修改。
