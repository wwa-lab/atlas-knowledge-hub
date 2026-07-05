# Wiki Ingest v0 API Implementation Guide

## Status

Accepted by user. Implementation may proceed strictly against this API guide, the accepted spec, and the task list.

## Overview

This guide defines the API contract for starting and inspecting Auto Wiki ingest v0 runs. The contract is backend-owned, uses Atlas response envelopes, and keeps generated content review-required.

## Base Path

- Backend stack: Spring Boot.
- Envelope: `ApiEnvelope<T>`.
- Auth model: current project-safe local/API path; production auth/RBAC remains out of scope.

## Endpoint Summary

| Operation | Method | Endpoint | Purpose |
|---|---|---|---|
| Start ingest run | POST | `/api/spaces/{spaceId}/wiki-ingest-runs` | Generate review-required candidates from approved chunks. |
| Get ingest run | GET | `/api/spaces/{spaceId}/wiki-generation-runs/{runId}` | Read one safe run summary. |
| List ingest runs | GET | `/api/spaces/{spaceId}/wiki-generation-runs` | Read recent run metadata. |
| List Wiki pages with drafts | GET | `/api/spaces/{spaceId}/wiki-pages?includeDrafts=true` | Explicitly include generated review-required candidates. |

## Request / Response Types

### `CreateWikiIngestRunRequest`

| Field | Type | Required | Rule |
|---|---|---:|---|
| `mode` | string | No | Default `deterministic`; v0 rejects or disables `model-assisted`. |
| `sourceFileIds` | string[] | No | Optional filter; files must belong to the space. |
| `requestedBy` | string | No | Safe actor label. |
| `dryRun` | boolean | No | Default false; if true, return candidates without writing pages. |

### `WikiIngestRunResponse`

| Field | Type | Description |
|---|---|---|
| `runId` | string | Generation run id. |
| `spaceId` | string | Owning space. |
| `status` | string | Run status. |
| `mode` | string | `deterministic` for v0. |
| `createdPageIds` | string[] | Created candidate page ids. |
| `updatedPageIds` | string[] | Updated candidate page ids. |
| `issueIds` | string[] | Safe issue ids. |
| `eligibleChunkCount` | number | Approved traced chunks used. |
| `excludedChunkCount` | number | Chunks excluded by safety/review rules. |
| `safeSummary` | string | Safe human-readable summary. |
| `safeError` | string or null | Sanitized error. |
| `startedAt` | string or null | ISO timestamp. |
| `finishedAt` | string or null | ISO timestamp. |

## Start Ingest Run

`POST /api/spaces/{spaceId}/wiki-ingest-runs`

Example request:

```json
{
  "mode": "deterministic",
  "sourceFileIds": ["file-001"],
  "requestedBy": "knowledge-manager",
  "dryRun": false
}
```

Example response:

```json
{
  "success": true,
  "data": {
    "runId": "wiki-ingest-run-001",
    "spaceId": "space-ibm-i-modernization",
    "status": "SUCCEEDED",
    "mode": "deterministic",
    "createdPageIds": ["wiki-auto-modernization-scope"],
    "updatedPageIds": [],
    "issueIds": [],
    "eligibleChunkCount": 3,
    "excludedChunkCount": 1,
    "safeSummary": "Created 1 review-required Wiki candidate from approved source chunks.",
    "safeError": null,
    "startedAt": "2026-07-05T00:00:00Z",
    "finishedAt": "2026-07-05T00:00:01Z"
  },
  "error": null,
  "meta": null
}
```

Validation:

- `spaceId` must exist.
- `mode=model-assisted` is out of scope for v0 and must return a safe validation response if requested.
- `sourceFileIds`, when present, must belong to the selected space.

Error cases:

| Status | Code | When |
|---|---|---|
| 404 | `NOT_FOUND` | Space or run not found. |
| 409 | `CONFLICT` | A run cannot proceed because trusted slug collision policy blocks writes. |
| 422 | `VALIDATION_ERROR` | Invalid mode or cross-space source filter. |
| 500 | `INTERNAL_ERROR` | Unexpected failure with sanitized message only. |

## Get Ingest Run

`GET /api/spaces/{spaceId}/wiki-generation-runs/{runId}`

- Returns `WikiIngestRunResponse`.
- Must verify run belongs to `spaceId`.
- Must not expose raw source text, prompts, provider payloads, secrets, private paths, or stack traces.

## List Wiki Pages With Drafts

`GET /api/spaces/{spaceId}/wiki-pages?includeDrafts=true`

Decision:

- Existing `GET /api/spaces/{spaceId}/wiki-pages` remains published-only by default.
- `includeDrafts=true` includes generated review-required candidates.
- Draft responses must include `sourceMode=AUTO_GENERATED` and `reviewStatus=REVIEW_REQUIRED`.

## Side Effects

- Writes `wiki_generation_run`.
- Creates or updates generated `wiki_page` candidates.
- Writes generated Markdown artifacts with deterministic safe summaries.
- Appends safe `wiki_log_entry`.
- Creates `wiki_page_issue` for conflicts or missing evidence when needed.

## Testing Contracts

- Start run excludes non-approved chunks.
- Generated candidates are review-required.
- Repeated run is idempotent by slug.
- Trusted published pages are not overwritten.
- Generated Markdown artifacts contain safe summaries and source/chunk labels only.
- Safe logs contain no raw text, prompts, provider payloads, secrets, private paths, or stack traces.
