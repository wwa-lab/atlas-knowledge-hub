# secret-manager-integration API Implementation Guide

Date: 2026-07-07
Status: Preauthorized accepted
Base path: `/api`

## Overview

This guide adds typed secret status fields to existing configuration and capability endpoints. It does not introduce new endpoints or real secret manager calls.

## Shared Response Types

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

Allowed status values: `CONFIGURED`, `ENV_CONFIGURED`, `MISSING`, `DISABLED`, `NOT_REQUIRED`.

## Endpoint Updates

| Operation | Method | Path | Change |
|---|---|---|---|
| List model adapters | GET | `/model-adapters` | Add `secretStatuses`. |
| Read DeepSeek config | GET | `/model-configurations/deepseek` | Add `secretStatuses`. |
| Save DeepSeek config | PUT | `/model-configurations/deepseek` | Accept write-only replacement input; response is status-only. |
| Clear DeepSeek config | DELETE | `/model-configurations/deepseek` | Return status-only fallback. |
| List converter adapters | GET | `/converter-adapters` | Add `secretStatuses`. |
| List parser adapters | GET | `/parser-adapters` | Add `secretStatuses`. |
| List storage adapters | GET | `/storage-adapters` | Add `secretStatuses`. |
| List vector adapters | GET | `/vector-adapters` | Add `secretStatuses`. |

## Redaction Rules

- Never serialize submitted secret material.
- Never serialize runtime command values.
- Never serialize raw endpoint, hostname, bucket URL, DSN, token, password, private path, or stack trace.
- Validation responses may name fields but must not echo submitted values.

## Contract Tests

- Configuration save/read/clear does not include replacement material or endpoint values in response text.
- Capability endpoints include `secretStatuses`.
- Existing `maskedConfigSummary` remains for compatibility.
