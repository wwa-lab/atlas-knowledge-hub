# Model Adapter — API / Adapter 实现指南

## 状态

草稿。Phase 3 model-adapter 实现前必需。切片 `model-adapter`。

## 概览

本指南定义模型能力发现与 mock 模型执行的内部 API 和 adapter contract。API 不暴露原始 prompt、原始 provider payload、原始 vector、模型 credential、私有 endpoint、外部云调用、本地 runtime path 或机密源文本。

## 基础约定

- Base path: `/api`。
- Envelope: 复用 `ApiEnvelope`。
- 仅 JSON。
- Auth/RBAC：本切片延后；内部使用。
- Mode：测试使用 `mock`；configured runtime behavior 继续留在 adapter 后。
- Secrets：仅脱敏/状态化；绝不返回原始 credential、endpoint、provider account id、hostname、本地路径或 provider payload。

## Adapter Capability Contract

### `GET /api/model-adapters`

目的：列出已配置 model adapters 与 model capabilities。

Response:

```json
{
  "success": true,
  "data": [
    {
      "adapterKey": "mock-model",
      "modelKey": "deepseek-flash",
      "displayName": "DeepSeek Flash",
      "providerFamily": "built-in mock",
      "modelType": "CHAT",
      "supportedOperations": ["CHAT"],
      "defaultModel": true,
      "status": "AVAILABLE",
      "contextLimit": 8192,
      "maskedConfigSummary": {
        "credential": "mock",
        "endpoint": "not_configured",
        "externalNetwork": "disabled"
      }
    }
  ],
  "error": null,
  "meta": null
}
```

`status` values: `AVAILABLE`, `DISABLED`, `MISCONFIGURED`。

校验/安全：

- 不返回原始 provider endpoint、本地 runtime path、account id、organization id、credential、hostname 或 command string。
- Capability listing 不得进行真实 provider call。

## Create Model Run

### `POST /api/model-runs`

目的：通过 model adapter contract 执行 model operation 并记录 run evidence。

Request:

```json
{
  "adapterKey": "mock-model",
  "modelKey": "deepseek-flash",
  "operationType": "CHAT",
  "purpose": "review-assist",
  "requestedBy": "delivery-lead",
  "mode": "mock",
  "inputReference": "source-chunk:chunk-file-001-001",
  "safeMockInput": "Summarize the referenced chunk for review.",
  "sourceReferences": [
    {
      "refType": "SOURCE_CHUNK",
      "refId": "chunk-file-001-001",
      "label": "BRD page 3 / Business Rules"
    }
  ]
}
```

规则：

- `adapterKey` 与 `modelKey` 只有在该 operation type 存在 default 时才可省略。
- `mode` 必填，且必须是 `mock` 或 `configured`；测试用 `mock`。
- `safeMockInput` 有界，且不得包含真实公司内容、原始 credential、URL 或私有路径。
- `inputReference` 是产品 reference，不是本地路径或 provider payload。
- `sourceReferences` 只包含产品 id 和安全 label。

成功响应：

```json
{
  "success": true,
  "data": {
    "runId": "model-run-2026-07-03-001",
    "adapterKey": "mock-model",
    "modelKey": "deepseek-flash",
    "modelType": "CHAT",
    "operationType": "CHAT",
    "status": "SUCCEEDED",
    "mode": "mock",
    "purpose": "review-assist",
    "requestedBy": "delivery-lead",
    "safeMessage": "Mock model run completed with review-required output.",
    "usage": {
      "promptUnits": 32,
      "completionUnits": 48,
      "outputCount": 1,
      "failedOutputCount": 0
    },
    "outputs": [
      {
        "outputId": "model-output-001",
        "kind": "TEXT_SUMMARY",
        "outputReference": "generated/model/model-output-001.json",
        "safeSummary": "Mock summary for the referenced source chunk.",
        "embeddingDimension": null,
        "embeddingItemCount": null,
        "rankedItemIds": [],
        "confidence": 0.82,
        "reviewStatus": "REVIEW_REQUIRED",
        "safeError": null
      }
    ],
    "sourceReferences": [
      {
        "refType": "SOURCE_CHUNK",
        "refId": "chunk-file-001-001",
        "label": "BRD page 3 / Business Rules",
        "confidence": null,
        "reviewStatus": "REVIEW_REQUIRED"
      }
    ],
    "startedAt": "2026-07-03T00:00:00Z",
    "completedAt": "2026-07-03T00:00:02Z"
  },
  "error": null,
  "meta": null
}
```

Embedding output 示例：

```json
{
  "outputId": "model-output-embedding-001",
  "kind": "EMBEDDING_METADATA",
  "outputReference": "generated/model/embedding-metadata-001.json",
  "safeSummary": "Mock embedding metadata only.",
  "embeddingDimension": 1024,
  "embeddingItemCount": 3,
  "rankedItemIds": [],
  "confidence": null,
  "reviewStatus": "REVIEW_REQUIRED",
  "safeError": null
}
```

错误：

| HTTP | Code | When |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Unknown adapter/model, incompatible operation, invalid mode, unsafe input reference, oversized safe input, invalid output metadata. |
| 404 | `NOT_FOUND` | 严格校验可用时，引用的 metadata id 不存在。 |
| 409 | `CONFLICT` | 若实现了同一 scoped purpose 的 active run 冲突。 |
| 500 | `INTERNAL_ERROR` | 未预期 adapter fault；细节只在服务端安全记录。 |

## Get Model Run

### `GET /api/model-runs/{runId}`

目的：返回 model run summary、safe outputs、source references 与 usage summary。

Response shape：与 create model run 的 `data` body 相同。

## Internal Adapter Interface Contract

概念接口：

```text
ModelAdapter
  capabilities() -> List<ModelCapability>
  execute(ModelRequest) -> ModelResult
```

必要 request fields:

| Field | Description |
|---|---|
| `runId` | 服务端创建的 model run id。 |
| `operationType` | `CHAT`、`EMBEDDING`、`RERANK`、`VISION` 或 `SPEECH`。 |
| `modelKey` | 选中的 model capability key。 |
| `mode` | `mock` 或 `configured`。 |
| `purpose` | 安全 run purpose。 |
| `inputReference` | 产品 input reference。 |
| `safeMockInput` | 可选有界 mock input。 |
| `sourceReferences[]` | 产品 source references。 |

必要 result fields:

| Field | Description |
|---|---|
| `adapterKey` | 产出结果的 adapter。 |
| `modelKey` | 选中的 model capability。 |
| `safeMessage` | 脱敏 run-level summary。 |
| `usage` | 非负 usage counts。 |
| `outputs[]` | 安全 output descriptors。 |

## 契约测试

实现完成前运行：

```bash
cd backend && mvn verify
git diff --check
! rg -n "OpenAI|Ollama|DeepSeek|GitHub Models|Copilot|WebClient|RestTemplate|HttpClient|ProcessBuilder|Runtime\\.getRuntime\\(" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/model-adapter-requirements.md docs/02-user-stories/model-adapter-stories.md docs/03-spec/model-adapter-spec.md docs/04-architecture/model-adapter-architecture.md docs/04-architecture/model-adapter-data-flow.md docs/04-architecture/model-adapter-data-model.md docs/05-design/model-adapter-design.md docs/05-design/contracts/model-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/model-adapter-tasks.md
```

Seam scan 必须在 non-adapter 产品层无匹配。Adapter implementation package 只有在 guard tests 覆盖且无 outbound network client 泄漏到产品层时，才可包含安全 provider label。
