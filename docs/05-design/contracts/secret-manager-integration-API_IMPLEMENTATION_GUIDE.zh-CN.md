# secret-manager-integration API 实现指南

日期：2026-07-07
状态：已按预授权接受
Base path: `/api`

## 概览

本指南为现有 configuration 与 capability endpoints 增加 typed secret status fields。不新增 endpoints，也不调用真实 secret manager。

## 共享 Response Types

```json
{
  "reference": {
    "provider": "deepseek",
    "scope": "model",
    "key": "credential",
    "displayName": "Model credential"
  },
  "status": "CONFIGURED",
  "source": "runtime",
  "maskedLabel": "Configured",
  "replaceable": true,
  "removable": true
}
```

允许的 status values：`CONFIGURED`、`ENV_CONFIGURED`、`MISSING`、`DISABLED`、`NOT_REQUIRED`。

## Endpoint Updates

| Operation | Method | Path | Change |
|---|---|---|---|
| List model adapters | GET | `/model-adapters` | 增加 `secretStatuses`。 |
| Read DeepSeek config | GET | `/model-configurations/deepseek` | 增加 `secretStatuses`。 |
| Save DeepSeek config | PUT | `/model-configurations/deepseek` | 接收 write-only replacement input；response 仅 status-only。 |
| Clear DeepSeek config | DELETE | `/model-configurations/deepseek` | 返回 status-only fallback。 |
| List converter adapters | GET | `/converter-adapters` | 增加 `secretStatuses`。 |
| List parser adapters | GET | `/parser-adapters` | 增加 `secretStatuses`。 |
| List storage adapters | GET | `/storage-adapters` | 增加 `secretStatuses`。 |
| List vector adapters | GET | `/vector-adapters` | 增加 `secretStatuses`。 |

## Redaction Rules

- 绝不序列化 submitted secret material。
- 绝不序列化 runtime command values。
- 绝不序列化 raw endpoint、hostname、bucket URL、DSN、token、password、private path 或 stack trace。
- Validation responses 可以命名字段，但不得回显 submitted values。

## Contract Tests

- Configuration save/read/clear response text 不包含 replacement material 或 endpoint values。
- Capability endpoints 包含 `secretStatuses`。
- 保留现有 `maskedConfigSummary` 兼容性。
