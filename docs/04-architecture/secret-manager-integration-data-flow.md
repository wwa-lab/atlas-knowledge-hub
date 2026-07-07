# Data Flow: secret-manager-integration

Status: Preauthorized accepted
Last updated: 2026-07-07

## Read Flow

```text
Admin/UI
  -> GET capability/config endpoint
  -> Controller
  -> Service or adapter registry
  -> Capability/config mapper
  -> SecretStatusResponse list
  -> ApiEnvelope response without raw values
```

## Write Flow

```text
Admin/UI
  -> PUT /api/model-configurations/deepseek with write-only replacement
  -> ModelRuntimeConfigurationService validates provider/model/endpoint shape
  -> Runtime in-memory state updated
  -> readChatConfiguration recomputes status
  -> response contains secret reference/status only
```

## Clear Flow

```text
Admin/UI
  -> DELETE /api/model-configurations/deepseek
  -> Runtime state cleared
  -> process environment fallback evaluated
  -> response reports ENV_CONFIGURED or MISSING without values
```

## Data Classification

| Data | Flow Rule |
|---|---|
| Replacement secret material | Request-only, write-only, never returned. |
| Runtime command value | Adapter-internal only, reported as configured/missing/disabled. |
| Provider endpoint value | Adapter/service-internal only, reported as configured/missing. |
| Secret reference | Safe logical metadata, no material. |
| Masked status | Safe for API and UI. |

## Failure Flow

Validation errors identify fields such as `provider`, `endpoint`, or `credential` but do not include raw submitted values. Adapter failures continue using sanitized safe messages.
