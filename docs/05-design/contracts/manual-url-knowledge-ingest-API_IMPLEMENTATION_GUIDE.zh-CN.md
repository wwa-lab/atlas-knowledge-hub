# manual-url-knowledge-ingest API Implementation Guide

## Overview

Base path：`/api`。Responses 使用 `ApiEnvelope<T>`。现有 auth、safe error 与 rate limit middleware 生效。

## Endpoints

| Operation | Method | Path | Acceptance |
|---|---|---|---|
| Register URL source | POST | `/api/spaces/{spaceId}/manual-url-sources` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-001` |
| List URL sources | GET | `/api/spaces/{spaceId}/manual-url-sources` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-002` |
| Get URL source | GET | `/api/manual-url-sources/{sourceId}` | `AC-MANUAL-URL-KNOWLEDGE-INGEST-002` |

## Create Request

```json
{
  "url": "https://example.com/reference/page",
  "title": "Vendor reference page",
  "description": "Manual seed for later connector sync planning.",
  "fetchIntent": "METADATA_ONLY",
  "createdBy": "frontend-user"
}
```

## Response

```json
{
  "success": true,
  "data": {
    "id": "url-src-12345678",
    "spaceId": "space-ibm-i",
    "displayUrl": "https://example.com/reference/page",
    "host": "example.com",
    "title": "Vendor reference page",
    "description": "Manual seed for later connector sync planning.",
    "fetchIntent": "METADATA_ONLY",
    "fetchPolicy": "NO_FETCH_METADATA_ONLY",
    "ingestStatus": "REVIEW_REQUIRED",
    "reviewStatus": "REVIEW_REQUIRED",
    "eligibilityStatus": "REVIEW_REQUIRED_ONLY",
    "confidence": 0.300,
    "sourceTrace": "Manual URL metadata: https://example.com/reference/page",
    "batchId": "batch-2026-07-07-abcd1234",
    "fileItemId": "file-abcd1234",
    "createdBy": "frontend-user",
    "createdAt": "2026-07-07T00:00:00Z",
    "updatedAt": "2026-07-07T00:00:00Z"
  },
  "error": null,
  "meta": null
}
```

## Validation Errors

Unsafe inputs 返回现有 safe validation envelopes。Field errors 不得回显 raw rejected URLs。

| Field | Condition |
|---|---|
| `url` | Missing、malformed、non-HTTPS、包含 userinfo、包含 query/fragment，或 internal/private host。 |
| `title` | 超过实现长度上限。 |
| `description` | 超过实现长度上限。 |
| `fetchIntent` | Unsupported value。 |

## Persistence Side Effects

- 插入一条 `manual_url_source` row。
- 插入一条 `SourceKind.url` 的 `batch` row。
- 插入一条 `SourceType.url`、`FileStatus.REVIEW_REQUIRED`、`ReviewStatus.REVIEW_REQUIRED` 的 `file_item` row。
- 插入一条携带 safe source trace 的 `source_chunk` row。

## Test Contract

- `T-MANUAL-URL-KNOWLEDGE-INGEST-004` 验证 API create/list/get。
- `T-MANUAL-URL-KNOWLEDGE-INGEST-005` 验证 unsafe URL redaction。
- `T-MANUAL-URL-KNOWLEDGE-INGEST-006` 验证 source trace 与 review-required defaults。
