# 数据模型：secret-manager-integration

状态：已按预授权接受
最后更新：2026-07-07

## 概览

本切片不引入数据库迁移，也不创建持久化 secret table。它增加 API/domain DTO，用于表示 secret references 与 status-only configuration。Runtime model configuration 仍为 in-memory，并 fallback 到 process environment。

## DTOs

### `SecretReferenceResponse`

| Field | Type | Description |
|---|---|---|
| `provider` | String | Logical provider 或 adapter family，例如 `deepseek`、`runtime`、`storage` 或 `vector`。 |
| `scope` | String | Logical configuration scope，例如 `model`、`converter-runtime` 或 `vector-adapter`。 |
| `key` | String | Logical secret/config key，例如 `credential` 或 `command`。 |
| `displayName` | String | 安全 UI label。 |

### `SecretStatusResponse`

| Field | Type | Description |
|---|---|---|
| `reference` | SecretReferenceResponse | Logical reference，不含 secret material。 |
| `status` | String | `CONFIGURED`、`ENV_CONFIGURED`、`MISSING`、`DISABLED` 或 `NOT_REQUIRED`。 |
| `source` | String | `runtime`、`environment`、`adapter`、`mock` 或 `none`。 |
| `maskedLabel` | String | 安全 label，例如 `Configured` 或 `Missing`。 |
| `replaceable` | Boolean | UI 是否可展示 replace action。 |
| `removable` | Boolean | UI 是否可展示 remove action。 |

## 更新的 Response Shapes

- `ModelConfigurationResponse` 增加 `secretStatuses`。
- `ModelCapabilityResponse` 增加 `secretStatuses`。
- `ConverterCapabilityResponse` 增加 `secretStatuses`。
- `ParserCapabilityResponse` 增加 `secretStatuses`。
- `StorageCapabilityResponse` 增加 `secretStatuses`。
- `VectorCapabilityResponse` 增加 `secretStatuses`。

## 持久化

无 migration。无 secret storage table。未来 production secret-manager integration 可以在同一 DTO contract 后新增 resolver。

## 校验规则

- Secret input field 保持 write-only。
- Endpoint value 可用于 shape validation，但不得返回。
- Runtime command value 可用于计算 status，但不得出现在 response 中。
