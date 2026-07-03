# Review Publish — API Implementation Guide

## Status

Draft. Required before implementation because this slice introduces new endpoints.

## Base Contract

- Base path: `/api`
- Envelope: existing `ApiEnvelope` shape.
- Auth model: internal/trusted only for this slice; production RBAC deferred.
- Errors: user-safe `VALIDATION_ERROR`, `NOT_FOUND`, `CONFLICT`, `INTERNAL_ERROR`.
- No endpoint calls parser, converter, model, vector, storage, or search engines.

## Endpoint Summary

| Operation | Method | Endpoint | Purpose |
|---|---|---|---|
| Review queues | GET | `/api/spaces/{spaceId}/review-queues` | Return blocked queues and publish-ready candidates. |
| Append file review | POST | `/api/files/{fileId}/reviews` | Existing review history endpoint; kept in this contract. |
| List file reviews | GET | `/api/files/{fileId}/reviews` | Existing chronological history endpoint. |
| Publish file | POST | `/api/files/{fileId}/publish` | Publish eligible approved Markdown to Wiki metadata. |
| List Wiki pages | GET | `/api/spaces/{spaceId}/wiki-pages` | Return published Wiki metadata for a space. |
| Get Wiki page | GET | `/api/wiki-pages/{wikiPageId}` | Return one published Wiki metadata record. |

## `GET /api/spaces/{spaceId}/review-queues`

Response:

```json
{
  "success": true,
  "data": {
    "spaceId": "ibm-i-modernization",
    "queues": [
      {
        "type": "MISSING_SOURCE_TRACE",
        "count": 94,
        "publishBlocked": true,
        "representativeItems": [
          {
            "fileId": "file-missing-trace-001",
            "status": "MARKDOWN_GENERATED",
            "reviewStatus": "APPROVED",
            "confidence": 0.88,
            "hasSourceTrace": false
          }
        ]
      },
      {
        "type": "READY_TO_PUBLISH",
        "count": 12579,
        "publishBlocked": false,
        "representativeItems": [
          {
            "fileId": "file-ready-001",
            "status": "MARKDOWN_GENERATED",
            "reviewStatus": "APPROVED",
            "confidence": 0.96,
            "hasSourceTrace": true
          }
        ]
      }
    ]
  },
  "error": null,
  "meta": null
}
```

Representative items are bounded examples for triage. They must use safe metadata fields only and must not include raw source paths, raw document content, private absolute paths, stack traces, or secrets.

## `POST /api/files/{fileId}/publish`

Request:

```json
{
  "title": "Migration Boundary",
  "owner": "sme-team"
}
```

Validation:

- File exists.
- File review status is `APPROVED`.
- File has relative `markdownPath`.
- File has confidence.
- At least one source document id can be derived.
- At least one source trace/source chunk exists.

Response `201` or idempotent `200`:

```json
{
  "success": true,
  "data": {
    "id": "wiki-file-003",
    "spaceId": "ibm-i-modernization",
    "title": "Migration Boundary",
    "markdownPath": "generated/md/Migration_Boundary.md",
    "sourceDocumentIds": ["file-003"],
    "confidence": 0.96,
    "reviewStatus": "PUBLISHED",
    "owner": "sme-team",
    "lastUpdated": "2026-07-03T00:00:00Z"
  },
  "error": null,
  "meta": null
}
```

Errors:

- `400 VALIDATION_ERROR`: missing title/owner or invalid path.
- `404 NOT_FOUND`: file or space not found.
- `409 CONFLICT`: candidate not approved, missing trace, missing confidence, OCR required, need fix, parser failed, or unsupported.

## `GET /api/spaces/{spaceId}/wiki-pages`

Returns only Wiki pages that are published or otherwise explicitly allowed by this slice's trusted read contract.

## `GET /api/wiki-pages/{wikiPageId}`

Returns one Wiki page metadata record. Must not return raw document content or raw secrets.

## Contract Tests

- `mvn verify` must include publish API contract tests.
- Review queue tests assert category counts and representative item shape.
- Success tests assert envelope, status, persisted `PUBLISHED`, and preserved fields.
- Failure tests assert no mutation for missing trace, not approved, missing markdown path, missing confidence, failed/unsupported file, and unknown id.
- Safety tests assert no stack trace, SQL, secret, private absolute path, or raw confidential content appears in errors.
