# manual-url-knowledge-ingest API Implementation Guide

## Overview

Base path: `/api`. Responses use `ApiEnvelope<T>`. Existing auth, safe error, and rate limit middleware apply.

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

Unsafe inputs return existing safe validation envelopes. Field errors must not echo raw rejected URLs.

| Field | Condition |
|---|---|
| `url` | Missing, malformed, non-HTTPS, has userinfo, has query/fragment, or has internal/private host. |
| `title` | Longer than implementation limit. |
| `description` | Longer than implementation limit. |
| `fetchIntent` | Unsupported value. |

## Persistence Side Effects

- Insert one `manual_url_source` row.
- Insert one `batch` row with `SourceKind.url`.
- Insert one `file_item` row with `SourceType.url`, `FileStatus.REVIEW_REQUIRED`, `ReviewStatus.REVIEW_REQUIRED`.
- Insert one `source_chunk` row carrying safe source trace.

## Test Contract

- `T-MANUAL-URL-KNOWLEDGE-INGEST-004` validates API create/list/get.
- `T-MANUAL-URL-KNOWLEDGE-INGEST-005` validates unsafe URL redaction.
- `T-MANUAL-URL-KNOWLEDGE-INGEST-006` validates source trace and review-required defaults.
