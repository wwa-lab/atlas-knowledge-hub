# Model Adapter — API / Adapter Implementation Guide

## Status

Draft. Required before Phase 3 model-adapter implementation. Slice `model-adapter`.

## Overview

This guide defines internal API and adapter contracts for model capability discovery and mock model execution. The API does not expose raw prompts, raw provider payloads, raw vectors, model credentials, private endpoints, external cloud calls, local runtime paths, or confidential source text.

## Base Conventions

- Base path: `/api`.
- Envelope: reuse `ApiEnvelope`.
- JSON only.
- Auth/RBAC: deferred; internal-only for this slice.
- Mode: tests use `mock`; configured runtime behavior remains behind the adapter.
- Secrets: masked/status-only; never return raw credentials, endpoints, provider account ids, hostnames, local paths, or provider payloads.

## Adapter Capability Contract

### `GET /api/model-adapters`

Purpose: list configured model adapters and model capabilities.

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

`status` values: `AVAILABLE`, `DISABLED`, `MISCONFIGURED`.

Validation/security:

- Do not return raw provider endpoints, local runtime paths, account ids, organization ids, credentials, hostnames, or command strings.
- Capability listing must not make a real provider call.

## Create Model Run

### `POST /api/model-runs`

Purpose: execute a model operation through the model adapter contract and record run evidence.

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

Rules:

- `adapterKey` and `modelKey` are optional only when a default exists for the operation type.
- `mode` is required and must be `mock` or `configured`; tests use `mock`.
- `safeMockInput` is bounded and must not contain real company content, raw credentials, URLs, or private paths.
- `inputReference` is a product reference, not a local path or provider payload.
- `sourceReferences` are product ids and safe labels only.

Success response:

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

Embedding output example:

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

Error cases:

| HTTP | Code | When |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Unknown adapter/model, incompatible operation, invalid mode, unsafe input reference, oversized safe input, invalid output metadata. |
| 404 | `NOT_FOUND` | Referenced metadata id is unknown when strict validation is available. |
| 409 | `CONFLICT` | A conflicting model run is already active for the same scoped purpose, if implemented. |
| 500 | `INTERNAL_ERROR` | Unexpected adapter fault; details logged safely server-side only. |

## Get Model Run

### `GET /api/model-runs/{runId}`

Purpose: return model run summary, safe outputs, source references, and usage summary.

Response shape: same `data` body as create model run.

## Internal Adapter Interface Contract

Conceptual interface:

```text
ModelAdapter
  capabilities() -> List<ModelCapability>
  execute(ModelRequest) -> ModelResult
```

Required request fields:

| Field | Description |
|---|---|
| `runId` | Server-created model run id. |
| `operationType` | `CHAT`, `EMBEDDING`, `RERANK`, `VISION`, or `SPEECH`. |
| `modelKey` | Selected model capability key. |
| `mode` | `mock` or `configured`. |
| `purpose` | Safe run purpose. |
| `inputReference` | Product input reference. |
| `safeMockInput` | Optional bounded mock input. |
| `sourceReferences[]` | Product source references. |

Required result fields:

| Field | Description |
|---|---|
| `adapterKey` | Adapter that produced the result. |
| `modelKey` | Selected model capability. |
| `safeMessage` | Sanitized run-level summary. |
| `usage` | Non-negative usage counts. |
| `outputs[]` | Safe output descriptors. |

## Contract Tests

Run before implementation completion:

```bash
cd backend && mvn verify
git diff --check
! rg -n "OpenAI|Ollama|DeepSeek|GitHub Models|Copilot|WebClient|RestTemplate|HttpClient|ProcessBuilder|Runtime\\.getRuntime\\(" backend/src/main/java/com/atlas/metadata/controller backend/src/main/java/com/atlas/metadata/service backend/src/main/java/com/atlas/metadata/repository backend/src/main/java/com/atlas/metadata/domain
! rg -n "api[_-]?[k]ey\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[p]assword\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[t]oken\\s*[:=]\\s*['\\\"]?[^$\\s{][^\\s]*|[A]KIA|BEGIN .*PRIVATE [K]EY|/[U]sers/|[C]:\\\\" backend/src docs/01-requirements/model-adapter-requirements.md docs/02-user-stories/model-adapter-stories.md docs/03-spec/model-adapter-spec.md docs/04-architecture/model-adapter-architecture.md docs/04-architecture/model-adapter-data-flow.md docs/04-architecture/model-adapter-data-model.md docs/05-design/model-adapter-design.md docs/05-design/contracts/model-adapter-API_IMPLEMENTATION_GUIDE.md docs/06-tasks/model-adapter-tasks.md
```

The seam scan must return no matches in non-adapter product layers. Adapter implementation packages may contain safe provider labels only when covered by guard tests and no outbound network clients leak into product layers.
