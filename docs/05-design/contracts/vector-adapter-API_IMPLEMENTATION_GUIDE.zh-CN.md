# Vector Adapter — API 实现指南

## 状态

Phase 3 `vector-adapter` 草稿契约。此 adapter 切片定义内部 API 与 adapter contract，因此包含 backend/API guide。

## 概览

Vector API 暴露脱敏 adapter capabilities、index/deindex run creation、run reports 与可追溯 similarity evidence。API 不暴露 raw vectors、raw engine diagnostics、真实凭证、私有 endpoint 或 Ask/RAG answers。

## Base Path And Envelope

- **Base path：** `/api`
- **Backend stack：** Java + Spring Boot metadata control plane。
- **Auth model：** 本切片仅 internal/mock；production auth/RBAC 范围外。
- **Envelope：** 所有 responses 使用 `ApiEnvelope<T>`，包含 `success`、`data`、`error`、可选 `meta`。

## Error Response Format

Errors 使用现有 safe API envelope 形态。

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request validation failed.",
    "details": {
      "dimension": "must match the selected adapter capability"
    }
  },
  "meta": null
}
```

Error message 不得包含 raw endpoint、DSN、collection name、credential、token、hostname、private path、stack trace、SDK output 或 raw vector content。

## API Endpoints Summary

| Operation | Method | Endpoint | Auth |
|---|---|---|---|
| List vector adapters | `GET` | `/api/vector-adapters` | Internal/mock |
| Create vector run | `POST` | `/api/spaces/{spaceId}/vector-runs` | Internal/mock |
| Get vector run | `GET` | `/api/vector-runs/{runId}` | Internal/mock |
| Query vector evidence | `POST` | `/api/spaces/{spaceId}/vector-query` | Internal/mock |

## Endpoint Reference

### List Vector Adapters

`GET /api/vector-adapters`

返回已配置 vector adapter capabilities 与脱敏配置。

Example response：

```json
{
  "success": true,
  "data": [
    {
      "adapterKey": "mock-vector",
      "displayName": "Mock Vector Adapter",
      "version": "mock-1",
      "defaultAdapter": true,
      "status": "AVAILABLE",
      "supportedDimensions": [384, 768, 1536],
      "supportedOperations": ["INDEX", "DEINDEX", "QUERY"],
      "maskedConfigSummary": {
        "engine": "mock",
        "endpoint": "not_configured",
        "credentials": "not_configured"
      }
    }
  ],
  "error": null,
  "meta": null
}
```

Validation and safety：

- 不返回 raw endpoint、DSN、collection name、token、credential、hostname 或 private path。
- Default adapter 排在第一位。

### Create Vector Run

`POST /api/spaces/{spaceId}/vector-runs`

为 scoped source chunks 创建并执行 index 或 deindex run。

Example request：

```json
{
  "operation": "INDEX",
  "adapterKey": "mock-vector",
  "batchId": "batch-2026-001",
  "fileItemIds": ["file-001"],
  "sourceChunkIds": ["chunk-001"],
  "mode": "mock",
  "reviewPolicy": "APPROVED_ONLY",
  "dimension": 384,
  "requestedBy": "sme-team",
  "items": [
    {
      "sourceChunkId": "chunk-001",
      "vector": [0.12, 0.34, 0.56]
    }
  ]
}
```

说明：

- `items[].vector` 仅作为 contract tests 的 mock/sample vector payload。生产 embedding generation 范围外。
- 在 mock mode 中，当 `items` 不存在时实现可使用 deterministic mock vectors。

Example response：

```json
{
  "success": true,
  "data": {
    "id": "vector-run-20260703-0001",
    "spaceId": "space-001",
    "batchId": "batch-2026-001",
    "operation": "INDEX",
    "adapterKey": "mock-vector",
    "adapterVersion": "mock-1",
    "status": "SUCCEEDED",
    "mode": "mock",
    "reviewPolicy": "APPROVED_ONLY",
    "dimension": 384,
    "summary": {
      "total": 1,
      "indexed": 1,
      "deleted": 0,
      "skipped": 0,
      "failed": 0
    },
    "results": [
      {
        "sourceChunkId": "chunk-001",
        "fileItemId": "file-001",
        "sourceFile": "modernization-overview.pptx",
        "page": 12,
        "section": "Application Inventory",
        "reviewStatus": "APPROVED",
        "confidence": 0.91,
        "vectorItemKey": "space-001/chunk-001",
        "status": "INDEXED",
        "score": null,
        "safeError": null
      }
    ],
    "startedAt": "2026-07-03T00:00:00Z",
    "completedAt": "2026-07-03T00:00:01Z",
    "safeMessage": "Vector run completed."
  },
  "error": null,
  "meta": null
}
```

Validation rules：

- `operation` 必须是 `INDEX` 或 `DEINDEX`。
- `mode` 必须是 `mock` 或 `configured`。
- `reviewPolicy` 默认 `APPROVED_ONLY`。
- `dimension` 如提供，必须为正且匹配 adapter capability。
- `spaceId`、`batchId`、`fileItemIds`、`sourceChunkIds` 必须一致。
- Source metadata 永不被 vector runs 修改。

### Get Vector Run

`GET /api/vector-runs/{runId}`

返回 vector run summary 与 per-item outcomes。

Errors：

- run id 不存在时返回 `404 NOT_FOUND`。
- 只返回 safe response；不包含 raw adapter diagnostics。

### Query Vector Evidence

`POST /api/spaces/{spaceId}/vector-query`

对 indexed vector evidence 执行有界 similarity query。

Example request：

```json
{
  "adapterKey": "mock-vector",
  "queryVector": [0.12, 0.34, 0.56],
  "mockQuery": null,
  "limit": 5,
  "reviewPolicy": "APPROVED_ONLY"
}
```

Example response：

```json
{
  "success": true,
  "data": {
    "spaceId": "space-001",
    "adapterKey": "mock-vector",
    "reviewPolicy": "APPROVED_ONLY",
    "matches": [
      {
        "sourceChunkId": "chunk-001",
        "fileItemId": "file-001",
        "sourceFile": "modernization-overview.pptx",
        "page": 12,
        "section": "Application Inventory",
        "score": 0.92,
        "confidence": 0.91,
        "reviewStatus": "APPROVED",
        "safeMetadata": {
          "vectorItemKey": "space-001/chunk-001"
        }
      }
    ],
    "safeMessage": "1 match returned."
  },
  "error": null,
  "meta": null
}
```

Validation rules：

- `limit` 必须为正且有界。
- `queryVector` 或 `mockQuery` 必须至少有一个。
- 使用 `queryVector` 时，其 dimension 必须匹配 selected adapter capability。
- 默认 review policy 只返回 approved evidence。
- 不返回 raw vectors。

## State Reference

```text
VectorRun: REQUESTED -> RUNNING -> SUCCEEDED | PARTIAL_FAILED | FAILED
VectorItem: candidate -> INDEXED | DELETED | SKIPPED | FAILED
```

## Adapter Interface Contract

```text
VectorAdapter
  capability()
  index(VectorIndexRequest)
  delete(VectorDeleteRequest)
  query(VectorQueryRequest)
```

Adapter implementations 只返回 Atlas 产品概念：capability、vector item results、query matches、safe messages、safe errors。

## Contract Tests

实现必须增加 API 与 adapter contract tests，验证：

- 脱敏 capability response。
- Mock index run。
- Mock deindex run。
- 默认 approved-only 的 mock query。
- 显式包含 review-required evidence。
- Invalid dimensions 在 adapter execution 前被拒绝。
- Safe error masking。
- Adapter scope 外没有 raw vector engine references。
