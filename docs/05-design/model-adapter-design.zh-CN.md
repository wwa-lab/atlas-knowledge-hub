# 设计：模型适配器

## 状态

草稿。Phase 3 适配器切片。`model-adapter` 的实现设计。

## 概览

本设计在 Spring Boot metadata control plane 中增加模型 adapter seam。它复用现有 converter/parser adapter 模式，同时将 provider execution、credential 与 raw prompt 保持在产品层之外。

## 来源架构

- Spec: `docs/03-spec/model-adapter-spec.md`
- Architecture: `docs/04-architecture/model-adapter-architecture.md`
- Data flow: `docs/04-architecture/model-adapter-data-flow.md`
- Data model: `docs/04-architecture/model-adapter-data-model.md`

## 设计假设

- 后端 stack 仍为 Java + Spring Boot。
- API response 使用 `ApiEnvelope`，对齐 `backend/src/main/java/com/atlas/metadata/dto/ApiEnvelope.java:4`。
- Adapter contract 位于后端 adapter package，遵循 `backend/src/main/java/com/atlas/metadata/adapter/ParserAdapter.java:4` 的 parser adapter 模式。
- 测试只使用 mock/fake model adapter。

## 设计范围

范围内：domain enums/entities、DTOs/mappers、adapter interface、mock/configured adapter boundary、registry、service、REST controller、Flyway migration、tests、seam guard updates 与 secret/path scans。

范围外：真实 provider calls、Ask/RAG、vector writes、graph/Wiki publication、frontend changes、production auth/RBAC 与 production credential storage。

## 模块设计

### Domain And Persistence

- 增加 model-specific enums：adapter status、model type、operation type、run status、output kind、source reference type。
- 增加 `model_run`、`model_run_output`、`model_run_source_reference` entities。
- 保持 append-oriented evidence 语义：run record 以 terminal status 和 safe message 完成；output 默认 review-required。
- 存 input/output references 与 safe summaries，不存 raw prompts、raw provider payloads、raw vectors 或 confidential text。

### Adapter Contract

概念接口：

```text
ModelAdapter
  capability() -> List<ModelCapability>
  execute(ModelRequest) -> ModelResult
```

设计规则：

- Request/result types 使用 Atlas 产品概念：operation、model type、source references、usage summary、output descriptors。
- Provider SDK payload 不泄漏进 controller/service/domain/repository 层。
- Capability metadata 脱敏/状态化。
- 真实 configured execution 可在 provider topology 接受前作为安全 placeholder。

### Registry And Default Selection

- Registry 列出所有 capability，并将 default 排在前面。
- 省略 model selector 时，按请求 operation/model type 解析默认模型。
- 未知 selector 在 adapter 执行前抛出 validation error。
- Unavailable/misconfigured adapter 返回安全 status 或安全 failed run 行为。

### Service Behavior

- 校验 request fields、mode、operation/model compatibility、source references、safe input length、confidence 与 usage counts。
- 持久化 `REQUESTED`，转换为 `RUNNING`，执行 adapter，校验 outputs，然后完成 terminal status。
- 将部分 output failure 映射为 `PARTIAL_FAILED`。
- 持久化和响应前 sanitizing safe messages。

### REST API

- `GET /api/model-adapters`
- `POST /api/model-runs`
- `GET /api/model-runs/{runId}`

所有响应使用 `ApiEnvelope`；create 返回 `201`。

## 数据设计

数据模型遵循 `docs/04-architecture/model-adapter-data-model.md`。

关键不变量：

- `review_status` 默认 `REVIEW_REQUIRED`。
- Embedding 只存 dimension/count metadata。
- Source references 是产品 id 和 label，不是原始源文本。
- 任何端点都不返回 raw vectors、provider endpoints、credentials、local paths 或 raw prompts。

## 工作流 / 执行设计

1. API 接收 model run request。
2. Service 校验 operation、mode、selectors、safe input/reference 与 source references。
3. Registry 解析 adapter/model default。
4. Service 将 run 持久化为 `REQUESTED`，标记为 `RUNNING`。
5. Adapter 返回 mock result。
6. Service 校验 output descriptors 和 usage summary。
7. Service 持久化 outputs/source references 并完成 run。
8. API 返回 run report。

## 校验与错误处理

| 规则 | 失败 |
|---|---|
| Unsupported operation 或 incompatible model type | `400 VALIDATION_ERROR` |
| Unknown adapter/model key | `400 VALIDATION_ERROR` |
| Unsafe input reference 或 raw path/URL | `400 VALIDATION_ERROR` |
| Oversized safe input | `400 VALIDATION_ERROR` |
| Unavailable adapter | safe failed run 或 `400 VALIDATION_ERROR` |
| Adapter fault | 带 sanitized safe message 的 `FAILED` |
| Invalid output confidence/usage/dimension | 拒绝 output；`PARTIAL_FAILED` 或 `FAILED` |

## 测试考虑

- Adapter contract 和 output validation 单元测试。
- Registry default resolution 单元测试。
- Summary/status calculation 单元测试。
- Service tests 覆盖 success、partial failure、unavailable adapter、invalid selector、unsafe references 与 safe error handling。
- API contract tests 覆盖端点与 envelope shape。
- Seam guard tests 覆盖 adapter scope 外的直接 provider/client 引用。

## 风险 / 取舍

| ID | 风险 | 设计响应 |
|---|---|---|
| DT-MODA-001 | SDD 接受后 provider 选择可能变化。 | Provider detail 保持在 `ModelAdapter` 后；mock contract 稳定。 |
| DT-MODA-002 | Raw prompt 对调试有用但不安全。 | 只持久化 reference 和 safe summary；通过 OQ-MODA-002 请求产品决策。 |
| DT-MODA-003 | Embedding 可能诱发直接 vector write。 | 明确任务和 guard 覆盖，禁止 vector database call。 |
| DT-MODA-004 | 现有 seam guard 需要扩展 model 术语。 | 设置专门任务，有意更新 forbidden references。 |

## 待确认问题

- OQ-MODA-001：mock implementation 后首个真实 provider。
- OQ-MODA-002：原始 prompt 保留策略。
- OQ-MODA-003：Usage/cost governance 的时机。
