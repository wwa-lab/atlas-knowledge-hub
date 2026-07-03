# 架构：模型适配器

## 状态

草稿。Phase 3 适配器切片。由 `docs/03-spec/model-adapter-spec.md` 派生。

## 概览

Model-adapter 为 metadata control plane 增加可替换的模型 provider 集成 seam。后端负责能力元数据、model run 生命周期、校验、运行证据、review-required 输出状态与用户安全报告。模型执行被隔离在产品面的 adapter contract 后；mock/fake adapter 是验证必需项，真实 provider 执行继续延后在同一 seam 后。

## 架构驱动

| 驱动 | 影响 |
|---|---|
| Adapter neutrality | 产品服务通过 registry/capability contract 解析模型 provider。 |
| Secret safety | Capability 与 run response 只暴露状态化配置和 safe summary，绝不暴露原始 provider secret 或 endpoint。 |
| Review-required output | 模型生成内容在后续 SME review 或确定性校验前保持不可信。 |
| Mock-only verification | 单元/集成测试使用 fake model engine，不需要外部网络、provider 账号或本地 daemon。 |
| Boundary discipline | 模型输出不实现 Ask/RAG、向量索引、图谱抽取或 Wiki 发布。 |

## 现有元数据上下文

当前后端已经使用 `ApiEnvelope` 作为成功/错误响应（`backend/src/main/java/com/atlas/metadata/dto/ApiEnvelope.java:4`）、产品面的 parser adapter contract（`backend/src/main/java/com/atlas/metadata/adapter/ParserAdapter.java:4`）、adapter registry（`backend/src/main/java/com/atlas/metadata/service/ParserAdapterRegistry.java:13`）和相对路径安全校验（`backend/src/main/java/com/atlas/metadata/validation/RelativePathValidator.java:10`）。Model-adapter 应遵循这些模式，同时把 provider-specific 逻辑留在 adapter package 内。

## 系统上下文

| 边界 | 职责 |
|---|---|
| Frontend | 本切片范围外。现有 settings UI 仅作为产品参考。 |
| Backend API / metadata control plane | 拥有模型能力端点、model run 生命周期、校验、运行报告和持久化。 |
| Model adapter seam | 封装 provider-specific 模型行为，返回 Atlas 产品概念。 |
| Model provider / worker | 位于产品工作流之外。真实 runtime call 延后在 adapter 后。 |
| PostgreSQL metadata | 存储 model run evidence、output descriptors、source references 与 safe summaries。 |

## 高层架构

```text
+------------------------------------------------------------+
| Users / agents                                             |
| Platform admin, delivery lead, SME reviewer, Codex          |
+------------------------------+-----------------------------+
                               |
                               | REST / JSON
                               v
+------------------------------------------------------------+
| Spring Boot metadata API                                   |
| Model capability endpoint, model run endpoint, reports       |
+------------------------------+-----------------------------+
                               |
                               v
+------------------------------------------------------------+
| Model application service                                  |
| Request validation, default resolution, status mapping,      |
| safe error handling, evidence persistence                   |
+------------------------------+-----------------------------+
                               |
                   product-facing adapter interface
                               v
+------------------------------+        +---------------------+
| Model adapter registry       |        | Model adapters       |
| capability + default policy  |------->| mock provider in CI  |
+------------------------------+        | configured boundary  |
                                        +----------+----------+
                                                   |
                                                   | provider/worker boundary
                                                   v
                                        +---------------------+
                                        | Model provider       |
                                        | outside product flow |
                                        +---------------------+
                               |
                               v
+------------------------------------------------------------+
| PostgreSQL metadata                                         |
| model_run, model_run_output, model_run_source_reference      |
+------------------------------------------------------------+
```

## 组件拆分

### Backend API

- **Model adapter capability API：** 列出模型能力与脱敏配置。
- **Model run API：** 创建 mock/configured model run 并返回报告。
- **Metadata/source reference reuse：** file item 与 source chunk 元数据仍然是 source reference 表面；本切片不复制原始源文本。

### Application Services

- **Model run service：** 校验请求、解析 adapter/model default、执行 mock/configured mode、映射输出、持久化运行证据并构建报告。
- **Model summary calculator：** 派生 operation output counts、failed/skipped counts 与 usage summaries。
- **Model adapter registry：** 负责按 model type 默认选择，以及 unavailable/misconfigured 行为。
- **Safety helpers：** 复用安全响应、安全错误和相对 reference 校验模式。

### Integration Adapters

- **ModelAdapter contract：** 接收 Atlas model operation request，返回 Atlas model result。
- **MockModelAdapter：** 用于 contract/API tests 的确定性 fake implementation。
- **ConfiguredModelAdapter boundary：** 未来 provider execution 的安全占位边界。它可以知道 provider 细节，但产品层不得知道。

### Persistence

- 新增 `model_run` 存储执行证据和生命周期状态。
- 新增 `model_run_output` 存储安全 operation outputs、confidence/evidence、review status 与 output references。
- 新增 `model_run_source_reference` 存储 source chunk/file/wiki/graph/Ask references。
- 本切片不创建 vector index rows、Ask answers、graph nodes 或 Wiki pages。

## 状态策略

Model run status：

```text
REQUESTED -> RUNNING -> SUCCEEDED
                     -> PARTIAL_FAILED
                     -> FAILED
```

输出审核策略：

- 生成的 chat、vision、speech、rerank summary 默认 `REVIEW_REQUIRED`。
- Embedding output 只记录 dimension/item counts 与 references。
- 不自动把任何 model output 设为 `APPROVED` 或 `PUBLISHED`。
- 可用时保留 confidence/evidence，且仅作为辅助证据。

## API / Interface 边界

| Interface | Consumer | Purpose |
|---|---|---|
| `GET /api/model-adapters` | Admin / implementation tests | 列出脱敏模型能力。 |
| `POST /api/model-runs` | Internal workflow / future UI | 通过 model adapter contract 启动 model run。 |
| `GET /api/model-runs/{runId}` | Delivery lead / future UI | 读取 model run report、safe outputs、source references 与 usage summary。 |
| `ModelAdapter` | Model service | 在产品面 interface 后执行模型工作。 |

## 安全 / 可靠性 / 可观测性

- Capability response 只脱敏/状态化。
- Provider error 在持久化或响应前有界并脱敏。
- Input reference 是产品 reference，不是本地路径、URL 或原始 provider payload。
- 原始 prompt、provider payload、provider endpoint、credential、私有路径和 stack trace 绝不存入用户可见字段。
- 自动化验证必须使用 mock engine。
- Seam guard 扫描 non-adapter 产品层是否有直接 model provider、SDK、command runner、outbound client 引用。

## 架构评审说明

- **可扩展性：** Registry + capability model 避免 provider lock-in。
- **解耦：** 产品代码依赖 Atlas operation/result 概念，而非 provider SDK payload。
- **阶段纪律：** Ask/RAG、vector writes、graph derivation、Wiki publishing、frontend UI 均明确在本架构范围外。
- **风险：** 现有 seam guard 已包含 parser/vector/storage/provider 相关术语；实现时必须有意扩展 model provider 术语。

## 风险 / 取舍

| ID | 风险 / 取舍 | 缓解 |
|---|---|---|
| R-MODA-001 | 真实 provider topology 尚未确定。 | configured execution 保持在 adapter 后；mock contract 稳定。 |
| R-MODA-002 | 原始 prompt 或 provider payload 可能泄漏机密内容。 | 只持久化 reference 与 safe summary；错误和输出均脱敏。 |
| R-MODA-003 | Embedding output 可能绕过 vector adapter。 | 只返回 dimension/count metadata；本切片禁止 vector write。 |
| R-MODA-004 | Model output 可能过早被视为可信 Ask/Wiki 内容。 | 所有生成输出默认 `REVIEW_REQUIRED`；发布不在范围内。 |

## 待确认问题

- OQ-MODA-001：mock implementation 后首个真实 provider。
- OQ-MODA-002：原始 prompt 保留策略。
- OQ-MODA-003：Usage/cost governance 的时机。
