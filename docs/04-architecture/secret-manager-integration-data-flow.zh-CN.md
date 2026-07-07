# 数据流：secret-manager-integration

状态：已按预授权接受
最后更新：2026-07-07

## 读取流程

```text
Admin/UI
  -> GET capability/config endpoint
  -> Controller
  -> Service or adapter registry
  -> Capability/config mapper
  -> SecretStatusResponse list
  -> ApiEnvelope response without raw values
```

## 写入流程

```text
Admin/UI
  -> PUT /api/model-configurations/deepseek with write-only replacement
  -> ModelRuntimeConfigurationService validates provider/model/endpoint shape
  -> Runtime in-memory state updated
  -> readChatConfiguration recomputes status
  -> response contains secret reference/status only
```

## 清除流程

```text
Admin/UI
  -> DELETE /api/model-configurations/deepseek
  -> Runtime state cleared
  -> process environment fallback evaluated
  -> response reports ENV_CONFIGURED or MISSING without values
```

## 数据分类

| Data | Flow Rule |
|---|---|
| Replacement secret material | 只在 request 中出现，write-only，绝不返回。 |
| Runtime command value | 仅 adapter-internal，报告为 configured/missing/disabled。 |
| Provider endpoint value | 仅 adapter/service-internal，报告为 configured/missing。 |
| Secret reference | 安全 logical metadata，不含 material。 |
| Masked status | 可安全用于 API 与 UI。 |

## 失败流程

Validation errors 只标识 `provider`、`endpoint` 或 `credential` 等字段，不包含 raw submitted values。Adapter failures 继续使用 sanitized safe messages。
