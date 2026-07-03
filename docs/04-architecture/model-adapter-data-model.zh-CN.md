# 数据模型：模型适配器

## 状态

草稿。Phase 3 适配器切片。模型运行证据的逻辑持久化契约。物理 SQL 仅在实现阶段通过 Flyway 创建。

## 概览

Model adapter 存储 model run evidence 与安全 output descriptor。它不存储原始 prompt、原始 provider payload、原始 vector、credential、私有 endpoint 或机密源文本。

## 枚举

```text
model_type          : CHAT | EMBEDDING | RERANK | VISION | SPEECH
model_operation     : CHAT | EMBEDDING | RERANK | VISION | SPEECH
model_adapter_status: AVAILABLE | DISABLED | MISCONFIGURED
model_run_status    : REQUESTED | RUNNING | SUCCEEDED | PARTIAL_FAILED | FAILED
model_output_kind   : TEXT_SUMMARY | EMBEDDING_METADATA | RERANK_SCORES |
                      VISION_SUMMARY | SPEECH_SUMMARY | ERROR
review_status       : REVIEW_REQUIRED | APPROVED | NEED_FIX | OCR_REQUIRED | PUBLISHED
source_ref_type     : FILE_ITEM | SOURCE_CHUNK | WIKI_PAGE | GRAPH_NODE | ASK_CONTEXT
```

生成输出默认 `REVIEW_REQUIRED`。`APPROVED` 与 `PUBLISHED` 不由本切片设置。

## 实体关系图

```text
+-----------------+       1:N       +--------------------+
| model_run        |---------------->| model_run_output   |
+-----------------+                 +--------------------+
        |
        | 1:N
        v
+----------------------------+
| model_run_source_reference |
+----------------------------+
```

## 表

### `model_run`

| Column | Type | 说明 |
|---|---|---|
| `id` | text PK | 服务端创建的 run id。 |
| `adapter_key` | text NOT NULL | 执行或尝试执行的 adapter。 |
| `model_key` | text NOT NULL | 选中的模型能力。 |
| `model_type` | model_type NOT NULL | 能力类型。 |
| `operation` | model_operation NOT NULL | 请求的操作。 |
| `status` | model_run_status NOT NULL | 生命周期状态。 |
| `mode` | text NOT NULL | `mock` 或 `configured`；测试用 `mock`。 |
| `purpose` | text NOT NULL | 安全业务目的标签。 |
| `requested_by` | text | mock/internal 请求者身份。 |
| `input_reference` | text | 产品 reference，不是原始 prompt。 |
| `safe_input_summary` | text | 可选有界 mock-safe summary。 |
| `safe_message` | text | 用户安全运行 summary 或 error。 |
| `prompt_units` | integer | 可用时记录非负 usage count。 |
| `completion_units` | integer | 可用时记录非负 usage count。 |
| `started_at` | timestamp | 服务端设置。 |
| `completed_at` | timestamp | 终态时设置。 |

### `model_run_output`

| Column | Type | 说明 |
|---|---|---|
| `id` | text PK | 稳定 output id。 |
| `run_id` | text FK -> `model_run.id` | 所属 run。 |
| `kind` | model_output_kind NOT NULL | Output descriptor 类型。 |
| `output_reference` | text | 生成 artifact 或 mock descriptor 的相对产品 reference。 |
| `safe_summary` | text | 有界 safe summary；不含原始机密文本。 |
| `ranked_item_ids` | text array | 适用于 rerank output ids。 |
| `embedding_dimension` | integer | 仅 embedding dimension metadata。 |
| `embedding_item_count` | integer | Embedded item 数量；不存 vector。 |
| `confidence` | decimal | 可选 `[0,1]`。 |
| `review_status` | review_status NOT NULL | 默认 `REVIEW_REQUIRED`。 |
| `safe_error` | text | 此 output 失败时的脱敏错误。 |

### `model_run_source_reference`

| Column | Type | 说明 |
|---|---|---|
| `id` | text PK | Source reference id。 |
| `run_id` | text FK -> `model_run.id` | 所属 run。 |
| `ref_type` | source_ref_type NOT NULL | Source reference 类型。 |
| `ref_id` | text NOT NULL | 产品 id，不是路径或 URL。 |
| `label` | text | 安全展示标签。 |
| `confidence` | decimal | 可选 source/evidence confidence。 |
| `review_status` | review_status | 已知 source review status。 |

## Capability Metadata Shape

Capabilities 可由配置的 adapter 计算，不一定持久化：

| Field | 说明 |
|---|---|
| `adapterKey` | 稳定 adapter id，例如 `mock-model`。 |
| `modelKey` | 稳定 model capability id。 |
| `displayName` | 用户安全展示名。 |
| `providerFamily` | 安全 family label，不含 endpoint。 |
| `modelType` | `CHAT`、`EMBEDDING`、`RERANK`、`VISION` 或 `SPEECH`。 |
| `supportedOperations` | 该 capability 支持的 operation 集合。 |
| `defaultModel` | 按 type 的 default marker。 |
| `status` | `AVAILABLE`、`DISABLED`、`MISCONFIGURED`。 |
| `contextLimit` | 已知时的安全数值限制。 |
| `maskedConfigSummary` | 状态化字段，例如 `credential: configured`。 |

## 不变量

- 不持久化原始 secret、endpoint、hostname、organization id、私有路径、原始 prompt、原始 provider payload、原始 vector 或机密文档文本。
- Model output 默认 `REVIEW_REQUIRED`。
- Embedding output 只存 metadata；vector persistence 属于 `vector-adapter`。
- Source reference 存产品 id 与安全 label，不存原始源文本。
- Usage counts 非负。
- Confidence values 可为空或在 `[0,1]`。
- 终态 run 不能回到 `RUNNING`。

## 延后数据

- Provider credential storage 与 rotation。
- Cost/quota/account policy tables。
- Ask answer records。
- Vector index records。
- Graph extraction records。
- Wiki publication records。
