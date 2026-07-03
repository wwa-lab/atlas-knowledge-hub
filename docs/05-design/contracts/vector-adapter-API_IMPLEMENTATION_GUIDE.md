# Vector Adapter — API Implementation Guide

## Status

Draft contract for Phase 3 `vector-adapter`. Backend/API guide is included because this adapter slice defines internal API and adapter contracts.

## Overview

The vector API exposes masked adapter capabilities, index/deindex run creation, run reports, and traceable similarity evidence. The API does not expose raw vectors, raw engine diagnostics, real credentials, private endpoints, or Ask/RAG answers.

## Base Path And Envelope

- **Base path:** `/api`
- **Backend stack:** Java + Spring Boot metadata control plane.
- **Auth model:** Internal/mock only for this slice; production auth/RBAC is out of scope.
- **Envelope:** all responses use `ApiEnvelope<T>` with `success`, `data`, `error`, and optional `meta`.

## Error Response Format

Errors use the existing safe API envelope shape.

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

Error messages must not include raw endpoint, DSN, collection name, credential, token, hostname, private path, stack trace, SDK output, or raw vector content.

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

Returns configured vector adapter capabilities with masked configuration.

Example response:

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

Validation and safety:

- No raw endpoint, DSN, collection name, token, credential, hostname, or private path.
- Default adapter appears first.

### Create Vector Run

`POST /api/spaces/{spaceId}/vector-runs`

Creates and executes an index or deindex run for scoped source chunks.

Example request:

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

Notes:

- `items[].vector` is allowed only as mock/sample vector payload for contract tests. Production embedding generation is out of scope.
- In mock mode, implementations may also use deterministic mock vectors when `items` is absent.

Example response:

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

Validation rules:

- `operation` must be `INDEX` or `DEINDEX`.
- `mode` must be `mock` or `configured`.
- `reviewPolicy` defaults to `APPROVED_ONLY`.
- `dimension`, when provided, must be positive and match adapter capability.
- `spaceId`, `batchId`, `fileItemIds`, and `sourceChunkIds` must be consistent.
- Source metadata is never mutated by vector runs.

### Get Vector Run

`GET /api/vector-runs/{runId}`

Returns vector run summary and per-item outcomes.

Errors:

- `404 NOT_FOUND` when the run id does not exist.
- Safe response only; no raw adapter diagnostics.

### Query Vector Evidence

`POST /api/spaces/{spaceId}/vector-query`

Runs a bounded similarity query over indexed vector evidence.

Example request:

```json
{
  "adapterKey": "mock-vector",
  "queryVector": [0.12, 0.34, 0.56],
  "mockQuery": null,
  "limit": 5,
  "reviewPolicy": "APPROVED_ONLY"
}
```

Example response:

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

Validation rules:

- `limit` must be positive and bounded.
- Either `queryVector` or `mockQuery` must be present.
- `queryVector` dimension must match selected adapter capability when used.
- Default review policy returns approved evidence only.
- Raw vectors are not returned.

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

Adapter implementations return Atlas product concepts only: capability, vector item results, query matches, safe messages, and safe errors.

## Contract Tests

Implementation must add API and adapter contract tests that verify:

- Masked capability response.
- Mock index run.
- Mock deindex run.
- Mock query with approved-only default.
- Explicit inclusion of review-required evidence.
- Invalid dimensions rejected before adapter execution.
- Safe error masking.
- No raw vector engine references outside adapter scope.
