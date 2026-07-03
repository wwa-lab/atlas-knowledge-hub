# 任务：模型适配器

## 状态

`model-adapter` 的草稿实现清单。只有在 SDD 集被接受后，本文件才可执行。本轮 SDD 不实现产品代码。

## 来源设计

- Spec: `docs/03-spec/model-adapter-spec.md`
- Design: `docs/05-design/model-adapter-design.md`
- API guide: `docs/05-design/contracts/model-adapter-API_IMPLEMENTATION_GUIDE.md`
- Data model: `docs/04-architecture/model-adapter-data-model.md`

## 每个任务的约束

- 自动化测试使用 mock/fake model engine。
- 模型执行必须留在产品面的 adapter contract 后。
- 不得在 controller/service/repository/domain 层调用真实 model provider、SDK、本地 runtime、command runner 或 outbound HTTP client。
- 不得引入外部网络依赖或云调用。
- 保留 source trace、confidence/evidence 与 review status；生成输出默认 `REVIEW_REQUIRED`。
- 脱敏 secret、endpoint、provider account id、hostname、本地 runtime path、raw prompt、raw provider payload、stack trace 与 private path。
- 不写 vector、Ask answer、graph node/edge、Wiki page 或 frontend settings change。
- 只使用 mock/sample metadata。

## 工作流

| Workstream | Tasks |
|---|---|
| Domain and persistence | T-MODA-001, T-MODA-002 |
| Adapter contract | T-MODA-003, T-MODA-004 |
| Service behavior | T-MODA-005, T-MODA-006, T-MODA-007 |
| API contract | T-MODA-008 |
| Verification and guards | T-MODA-009, T-MODA-010 |

## 任务详情

### T-MODA-001：增加 model run domain model 与 migration

- **映射到：** REQ-MODA-006, REQ-MODA-007, REQ-MODA-008, REQ-MODA-009, REQ-MODA-014；spec sections "Model Run Lifecycle", "Operation Outputs", "State Model"；data model `model_run`, `model_run_output`, `model_run_source_reference`。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** None
- **Scope:** 增加 model type、operation type、adapter status、run status、output kind、source reference type enums；增加 model run/output/source reference entities 与 repositories；为 model run evidence 增加 Flyway migration。不得增加 vector、Ask、graph、Wiki 或 provider credential tables。
- **Constraints:** Adapter boundary；mock-only test data；review-required outputs；secret/path safety。
- **Verification:**

```bash
cd backend && mvn verify
git diff --check
```

### T-MODA-002：增加 model DTOs 与 mapping contracts

- **映射到：** REQ-MODA-003, REQ-MODA-004, REQ-MODA-007, REQ-MODA-009；spec sections "Capability Metadata", "API / Interface Surface"；API guide response shapes。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-MODA-001
- **Scope:** 增加 model capability、create-run request、run response、usage response、output response、source-reference response mapping。所有 API response 使用 `ApiEnvelope`。
- **Constraints:** Secret-masked capability summaries；无 raw provider/runtime details；无 raw prompt fields。
- **Verification:**

```bash
cd backend && mvn test
git diff --check
```

### T-MODA-003：定义 model adapter interface 与 capability model

- **映射到：** REQ-MODA-001, REQ-MODA-002, REQ-MODA-003, REQ-MODA-005；spec sections "Adapter Boundary", "Capability Metadata"；design "Adapter Contract"。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-MODA-002
- **Scope:** 增加产品面的 `ModelAdapter` interface、capability record、request record、result record、usage record、output descriptor record、source reference record。包含 adapter key、model key、model type、supported operations、status、default marker、context limit 与 masked config summary。
- **Constraints:** 只使用产品概念；interface 中不直接调用 model provider；无 network/client dependency。
- **Verification:**

```bash
cd backend && mvn -Dtest=ModelAdapterContractTest test
! rg -n "WebClient|RestTemplate|HttpClient|ProcessBuilder|Runtime\\.getRuntime\\(" backend/src/main/java/com/atlas/metadata/adapter/ModelAdapter.java
```

### T-MODA-004：增加 mock 与 configured model adapter implementations

- **映射到：** REQ-MODA-002, REQ-MODA-005, REQ-MODA-011, REQ-MODA-013；spec sections "Adapter Boundary", "Operation Outputs"；design "Adapter Contract"。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-MODA-003
- **Scope:** 增加覆盖 chat、embedding、rerank、vision、speech 契约测试的确定性 mock model adapter。增加 configured model adapter boundary，只报告安全 capability metadata；真实执行延后到 provider topology 被接受后。
- **Constraints:** Mock-only verification；无外部网络；capability 或 output 中不出现 raw endpoint/credential/path/prompt/provider payload。
- **Verification:**

```bash
cd backend && mvn -Dtest=ModelAdapterContractTest test
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src/main/java/com/atlas/metadata/adapter
```

### T-MODA-005：实现 model adapter registry 与 capability listing

- **映射到：** REQ-MODA-001, REQ-MODA-002, REQ-MODA-003, REQ-MODA-004；spec sections "Adapter Boundary", "Capability Metadata"；API guide `GET /api/model-adapters`。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-MODA-003, T-MODA-004
- **Scope:** 增加显式/默认 adapter/model 选择的 registry resolution，包括 unavailable 与 misconfigured 状态。增加带 masked config 的 capability listing service behavior。
- **Constraints:** Adapter boundary；secret masking；不把单一实现硬编码为唯一未来选项。
- **Verification:**

```bash
cd backend && mvn -Dtest=ModelAdapterRegistryTest test
git diff --check
```

### T-MODA-006：实现 model run service 与 status mapping

- **映射到：** REQ-MODA-006, REQ-MODA-007, REQ-MODA-008, REQ-MODA-014；spec sections "Model Run Lifecycle", "Operation Outputs", "State Model"；design "Service Behavior"。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-MODA-001, T-MODA-003, T-MODA-005
- **Scope:** 实现 model run creation、request validation、default resolution、adapter execution、output validation、usage summary calculation、`SUCCEEDED` / `PARTIAL_FAILED` / `FAILED` mapping 与 safe adapter-fault handling。
- **Constraints:** 测试中使用 mock-engine execution；生成输出保持 `REVIEW_REQUIRED`；无 Ask/RAG/vector/graph/Wiki side effects。
- **Verification:**

```bash
cd backend && mvn -Dtest=ModelServiceTest,ModelSummaryCalculatorTest test
git diff --check
```

`ModelServiceTest` 必须直接断言 `SUCCEEDED`、`PARTIAL_FAILED`、`FAILED`，以及 unavailable/misconfigured adapter 的状态映射。

### T-MODA-007：实现 source reference 与 output persistence

- **映射到：** REQ-MODA-007, REQ-MODA-008, REQ-MODA-009, REQ-MODA-010, REQ-MODA-011, REQ-MODA-012；spec sections "Operation Outputs", "Validation And Failure Behavior"；design "Data Design"。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-MODA-006
- **Scope:** 持久化已校验的 source references 和 output descriptors。拒绝 unsafe references、invalid confidence、negative usage counts、invalid embedding dimensions、raw vector payloads、oversized summaries，以及不是 `REVIEW_REQUIRED` 的 output status。
- **Constraints:** 保留 source trace 与 review status；secret/prompt/provider masking；不存 raw vector，不写 vector database。
- **Verification:**

```bash
cd backend && mvn -Dtest=ModelDomainInvariantTest,ModelServiceTest test
git diff --check
```

### T-MODA-008：增加 model REST API endpoints

- **映射到：** REQ-MODA-003, REQ-MODA-006, REQ-MODA-007, REQ-MODA-014；spec section "API / Interface Surface"；API guide all endpoints。
- **Owner type:** backend
- **Priority:** Must
- **Dependencies:** T-MODA-005, T-MODA-006, T-MODA-007
- **Scope:** 增加 controller endpoints `GET /api/model-adapters`、`POST /api/model-runs`、`GET /api/model-runs/{runId}`。响应必须使用 `ApiEnvelope` 并匹配 API guide。
- **Constraints:** 仅内部；不实现 auth/RBAC；无 raw prompt、provider payload、vector 或 credential。
- **Verification:**

```bash
cd backend && mvn -Dit.test=ModelApiContractIT verify
git diff --check
```

### T-MODA-009：扩展 adapter seam 与安全 guard

- **映射到：** REQ-MODA-001, REQ-MODA-004, REQ-MODA-012, REQ-MODA-013, REQ-MODA-015；spec sections "Adapter Boundary", "Validation And Failure Behavior"；design "Testing Considerations"。
- **Owner type:** QA/backend
- **Priority:** Must
- **Dependencies:** T-MODA-003, T-MODA-004, T-MODA-008
- **Scope:** 扩展 guard tests，使 model provider names、本地 runtime markers、command runners、outbound network clients 与 vector database write paths 在 non-adapter 产品层被禁止。为 model implementation 和 docs 增加 secret/private-path/raw-prompt scans。
- **Constraints:** Adapter 外不得直接 model provider call；不得 vector write；output 只脱敏。
- **Verification:**

```bash
cd backend && mvn -Dtest=AdapterSeamGuardTest test
! rg -n "OpenAI|Ollama|DeepSeek|GitHub Models|Copilot|WebClient|RestTemplate|HttpClient|ProcessBuilder|Runtime\\.getRuntime\\(|pgvector|Milvus|Qdrant" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/model-adapter-requirements.md docs/02-user-stories/model-adapter-stories.md docs/03-spec/model-adapter-spec.md docs/04-architecture/model-adapter-architecture.md docs/04-architecture/model-adapter-data-flow.md docs/04-architecture/model-adapter-data-model.md docs/05-design/model-adapter-design.md docs/05-design/contracts/model-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/model-adapter-tasks.md
```

### T-MODA-010：运行最终 model-adapter 验证

- **映射到：** REQ-MODA-013, REQ-MODA-014, REQ-MODA-015；spec acceptance matrix AC-MODA-01 through AC-MODA-09。
- **Owner type:** QA/backend
- **Priority:** Must
- **Dependencies:** T-MODA-001 through T-MODA-009
- **Scope:** 运行完整验证集，review diff，并在后续实现改变 docs/status 时更新 traceability evidence。
- **Constraints:** Mock-only；adapter only；产品代码无外部网络/云调用；secret-masked；review-required output；无 vector/Ask/graph/Wiki/frontend side effect。
- **Verification:**

```bash
cd backend && mvn verify
git diff --check
! rg -n "OpenAI|Ollama|DeepSeek|GitHub Models|Copilot|WebClient|RestTemplate|HttpClient|ProcessBuilder|Runtime\\.getRuntime\\(|pgvector|Milvus|Qdrant" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/model-adapter-requirements.md docs/02-user-stories/model-adapter-stories.md docs/03-spec/model-adapter-spec.md docs/04-architecture/model-adapter-architecture.md docs/04-architecture/model-adapter-data-flow.md docs/04-architecture/model-adapter-data-model.md docs/05-design/model-adapter-design.md docs/05-design/contracts/model-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/model-adapter-tasks.md
```

## 依赖计划

- Critical path: T-MODA-001 -> T-MODA-002 -> T-MODA-003 -> T-MODA-004 -> T-MODA-005 -> T-MODA-006 -> T-MODA-007 -> T-MODA-008 -> T-MODA-009 -> T-MODA-010
- T-MODA-003 后的并行机会：adapter contract tests、DTO mapper tests、summary calculator tests 可与 service tests 并行构建。

## 待确认问题 / 风险

- OQ-MODA-001：首个真实 provider 延后；mock contract 不能依赖它。
- OQ-MODA-002：本任务集只存 reference 和 safe summary；原始 prompt 保留需要已接受的 SDD 变更。
- OQ-MODA-003：Usage/cost governance 延后；范围内只记录 counts。
- R-MODA-003：Embedding output 不得绕过 `vector-adapter`；T-MODA-009 必须 guard non-adapter vector writes。
