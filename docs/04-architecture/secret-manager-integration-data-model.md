# Data Model: secret-manager-integration

Status: Preauthorized accepted
Last updated: 2026-07-07

## Overview

This slice introduces no database migration and no persistent secret table. It adds API/domain DTOs that model secret references and status-only configuration. Runtime model configuration remains in memory with process environment fallback.

## DTOs

### `SecretReferenceResponse`

| Field | Type | Description |
|---|---|---|
| `provider` | String | Logical provider or adapter family, such as `deepseek`, `runtime`, `storage`, or `vector`. |
| `scope` | String | Logical configuration scope, such as `model`, `converter-runtime`, or `vector-adapter`. |
| `key` | String | Logical secret/config key, such as `credential` or `command`. |
| `displayName` | String | Safe UI label. |

### `SecretStatusResponse`

| Field | Type | Description |
|---|---|---|
| `reference` | SecretReferenceResponse | Logical reference, no secret material. |
| `status` | String | `CONFIGURED`, `ENV_CONFIGURED`, `MISSING`, `DISABLED`, or `NOT_REQUIRED`. |
| `source` | String | `runtime`, `environment`, `adapter`, `mock`, or `none`. |
| `maskedLabel` | String | Safe label such as `Configured` or `Missing`. |
| `replaceable` | Boolean | Whether UI may show replace action. |
| `removable` | Boolean | Whether UI may show remove action. |

## Updated Response Shapes

- `ModelConfigurationResponse` adds `secretStatuses`.
- `ModelCapabilityResponse` adds `secretStatuses`.
- `ConverterCapabilityResponse` adds `secretStatuses`.
- `ParserCapabilityResponse` adds `secretStatuses`.
- `StorageCapabilityResponse` adds `secretStatuses`.
- `VectorCapabilityResponse` adds `secretStatuses`.

## Persistence

No migration. No secret storage table. Future production secret-manager integration can add a resolver behind the same DTO contract.

## Validation Rules

- Secret input field remains write-only.
- Endpoint values may be validated for shape but not returned.
- Runtime command values may determine status but not appear in responses.
