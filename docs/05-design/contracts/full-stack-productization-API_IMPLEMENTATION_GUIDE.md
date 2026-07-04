# Full-Stack Productization — API Implementation Guide

## Status

Draft for implementation. This guide composes existing Atlas APIs into one P0 browser workflow.

## Base Contract

- Base path: `/api`.
- Envelope: `ApiEnvelope<T>` with `success`, `data`, `error`, and `meta`.
- Auth: production auth/RBAC out of scope. Graph endpoints use existing local headers.
- Data: mock/sample metadata only.

## Endpoint Sequence

| Step | Method | Endpoint | Purpose |
|---|---|---|---|
| 1 | GET | `/api/spaces` | List Knowledge Spaces. |
| 2 | GET | `/api/spaces/{spaceId}` | Load selected space detail. |
| 3 | GET | `/api/spaces/{spaceId}/batches` | Load existing batches. |
| 4 | POST | `/api/spaces/{spaceId}/batches` | Create metadata-only sample batch. |
| 5 | GET | `/api/batches/{batchId}/files` | Load batch files. |
| 6 | GET | `/api/files/{fileId}/chunks` | Load source trace chunks. |
| 7 | GET | `/api/spaces/{spaceId}/review-queues` | Load review queues. |
| 8 | POST | `/api/files/{fileId}/reviews` | Approve selected file. |
| 9 | POST | `/api/files/{fileId}/publish` | Publish approved file to Wiki. |
| 10 | GET | `/api/spaces/{spaceId}/wiki-pages` | Load published Wiki pages. |
| 11 | POST | `/api/spaces/{spaceId}/graph/projection-runs` | Refresh graph evidence. |
| 12 | POST | `/api/spaces/{spaceId}/vector-runs` | Index approved chunks for Ask. |
| 13 | GET | `/api/spaces/{spaceId}/graph` | Load graph view. |
| 14 | GET | `/api/spaces/{spaceId}/graph/nodes/{nodeId}` | Load graph detail evidence. |
| 15 | POST | `/api/spaces/{spaceId}/ask` | Create trusted Ask run. |
| 16 | GET | `/api/ask-runs/{runId}` | Read final Ask answer/evidence. |

## Sample Batch Request

```json
{
  "name": "P0 Productization Sample 20260703",
  "sourceKind": "folder",
  "owner": "P0 Browser E2E",
  "files": [
    {
      "sourcePath": "samples/p0/productization-sample.md",
      "sourceType": "pdf",
      "status": "MARKDOWN_GENERATED",
      "confidence": 0.93,
      "reviewStatus": "REVIEW_REQUIRED",
      "markdownPath": "generated/md/productization-sample.md",
      "chunks": [
        {
          "sourceFile": "productization-sample.md",
          "page": 1,
          "section": "P0 Browser Evidence",
          "confidence": 0.93,
          "reviewStatus": "APPROVED"
        }
      ]
    }
  ]
}
```

## Review Request

```json
{
  "action": "APPROVE",
  "reviewer": "p0-browser-e2e",
  "comment": "Approved for P0 browser loop.",
  "affectedChunks": ["chunk-id"]
}
```

## Publish Request

```json
{
  "title": "P0 Productization Wiki",
  "owner": "p0-browser-e2e"
}
```

## Graph Headers

Graph read:

```text
X-Atlas-User: frontend-demo
X-Atlas-Role: VIEWER
```

Graph projection:

```text
X-Atlas-User: frontend-demo
X-Atlas-Role: ADMIN
```

## Ask Request

```json
{
  "question": "What evidence was published for the P0 browser flow?",
  "requestedBy": "p0-browser-e2e",
  "reviewPolicy": "APPROVED_ONLY",
  "limit": 3,
  "mode": "mock",
  "filters": {
    "fileItemIds": ["file-id"],
    "sourceTypes": ["pdf"]
  }
}
```

## Error Handling

- `400 VALIDATION_ERROR`: invalid request body or unsafe fields.
- `404 NOT_FOUND`: missing space, batch, file, wiki page, graph node, or Ask run.
- `409 CONFLICT`: publish attempted before review approval or source trace readiness.
- `401/403`: graph headers absent or insufficient.
- `500 INTERNAL_ERROR`: safe server fault only; no stack traces or private details.

## Contract Tests

- Existing backend contract tests must still pass under `mvn verify`.
- New frontend E2E must use browser controls to call the endpoint sequence.
- No frontend code may call provider/vector/parser/storage/converter engines directly.
