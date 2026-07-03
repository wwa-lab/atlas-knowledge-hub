# 数据流：模型适配器

## 状态

草稿。Phase 3 适配器切片。`docs/04-architecture/model-adapter-architecture.md` 的配套文档。

## 范围

本文描述模型能力列表与 mock model run 的运行时数据流。覆盖产品元数据、adapter 调用、安全输出校验和运行证据持久化。真实 provider 执行、vector write、Ask/RAG、图谱抽取、Wiki 发布、前端变更与生产 secret 管理均在范围外。

## Flow 1：能力列表

```text
Admin / test
  -> GET /api/model-adapters
  -> ModelController
  -> ModelService.listCapabilities
  -> ModelAdapterRegistry.capabilities
  -> ModelAdapter.capability
  -> masked ModelCapabilityResponse
  -> ApiEnvelope.ok(data)
```

规则：

- Capability response 只暴露脱敏/状态字段。
- Capability listing 不得调用真实模型 provider 来发现 live state。
- Default model marker 是产品策略，不是 provider 硬编码。

## Flow 2：创建 Model Run

```text
Internal workflow / future UI
  -> POST /api/model-runs
  -> validate operation, mode, selector, input reference, source references
  -> resolve adapter/model default
  -> persist model_run as REQUESTED
  -> mark RUNNING
  -> ModelAdapter.execute(request)
  -> validate output descriptors and usage summary
  -> persist outputs + source references
  -> complete run as SUCCEEDED / PARTIAL_FAILED / FAILED
  -> return ModelRunResponse
```

失败分支：

- 无效请求：创建 run 前以 `VALIDATION_ERROR` 拒绝。
- 未知 adapter/model：adapter 执行前拒绝。
- Adapter unavailable/misconfigured：安全失败，不暴露原始 provider 细节。
- Adapter fault：只持久化/返回有界 safe message。

## Flow 3：保留 Source Reference

```text
file_item/source_chunk/wiki/graph/ask reference ids
  -> request sourceReferences[]
  -> service validates known scope where available
  -> adapter receives references, not raw document text
  -> output records keep sourceReferences[]
  -> reviewStatus remains REVIEW_REQUIRED
```

规则：

- Source references 是 id 与安全标签，不是原始文档内容。
- Model output 必须保持 review-required，直到另一个已接受切片改变它。
- Confidence/evidence values 只有在 `[0,1]` 有效时复制。

## Flow 4：Embedding 边界

```text
embedding model run
  -> mock adapter returns dimension + itemCount + outputReference
  -> service validates dimension/itemCount
  -> persist metadata only
  -> no vector database write
```

规则：

- Vector 不在 JSON 中返回。
- Vector indexing 属于 `vector-adapter`，不属于本切片。

## 状态转换

```text
REQUESTED
   |
   v
RUNNING
   |---- all outputs valid ------------------> SUCCEEDED
   |---- some outputs valid, some failed ----> PARTIAL_FAILED
   |---- no outputs or adapter fault --------> FAILED
```

## 错误级联

| 阶段 | 错误 | 结果 |
|---|---|---|
| Request validation | Invalid operation, mode, selector, source reference, or safe input | `400 VALIDATION_ERROR`；不调用 adapter |
| Adapter resolution | Unknown key or no default | `400 VALIDATION_ERROR`；不泄漏 provider |
| Capability status | Unavailable/misconfigured adapter | safe failed run 或 validation error |
| Adapter execution | Unexpected fault | `FAILED`；只返回 safe message |
| Output validation | Invalid confidence, usage, output kind, or unsafe summary | `PARTIAL_FAILED` 或 `FAILED`；拒绝问题 output |

## 验证钩子

- Contract tests 校验 mock model adapter request/result shape。
- API contract tests 校验 envelope、status mapping、review status 与 safe output fields。
- Seam guard 扫描 non-adapter 产品层中的 provider/client/runtime 引用。
- Secret scans 覆盖 backend model-adapter code 与所有 `model-adapter` SDD docs。
